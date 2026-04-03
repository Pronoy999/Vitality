"""
store_prescriptions.py
----------------------
Accepts one or more prescription image paths (via CLI glob or explicit files),
sends them all to Gemini in a single call, parses the response, and stores
the result as one prescription row in PostgreSQL.

Layers:
  Models        — Prescription, Medicine (domain objects)
  GeminiClient  — calls Gemini generateContent API with retry
  Parser        — sends images to Gemini, deserialises response into models
  Repository    — all DB interaction
  Pipeline      — orchestrates parse → store
"""

from __future__ import annotations

import base64
import glob
import json
import os
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

import psycopg2
from psycopg2.extras import RealDictCursor
from dotenv import load_dotenv

load_dotenv()

# ==============================================================
# Constants
# ==============================================================

SUPPORTED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp", ".heic"}

MIME_TYPES = {
    ".jpg":  "image/jpeg",
    ".jpeg": "image/jpeg",
    ".png":  "image/png",
    ".webp": "image/webp",
    ".heic": "image/heic",
}

GEMINI_MODEL = "gemini-2.5-flash"
GEMINI_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/{}:generateContent"
GEMINI_RETRY_MAX_ATTEMPTS = 5
GEMINI_RETRY_BASE_DELAY_SEC = 2.0
GEMINI_RETRY_STATUSES = {429} | set(range(500, 600))

SYSTEM_PROMPT = """You are a medical document parser specialising in handwritten and printed prescription images.

Extract structured information from the attached prescription image(s) and return a valid JSON object.

Rules:
- Extract only what is explicitly visible. Do not infer or hallucinate values.
- If a field is absent, illegible, or uncertain, set its value to null.
- If a word is partially legible, make a best-effort transcription and append "?" to that value (e.g., "Amoxicill?").
- Normalise medicine names to their canonical form if clearly identifiable; otherwise transcribe exactly as visible.
- Dosage should be a single string capturing quantity, frequency, and duration. Preserve shorthand like "1-0-1", "BD", "TDS", "OD", "SOS" as-is.
- Quantity is the total number of units (tablets, capsules, etc.) to be dispensed, as an integer. First, try to **infer what is the total number of units needed**. If you fail to infer, just set to 1.
- Health metrics should be key-value pairs with standard abbreviations as keys (e.g., "BP", "SpO2", "BMI", "HR", "Temp", "Weight", "Height", "Blood Glucose").
- Dates should be in ISO 8601 format (YYYY-MM-DD) if determinable; otherwise transcribe as-is.
- If multiple images are provided, treat them as pages of the same prescription.
- Return only the JSON — no explanation, no markdown fences, no preamble.

Output schema:
{
  "patient_name": string | null,
  "patient_age": string | null,
  "doctor_name": string | null,
  "date": string | null,
  "patient_issue": string | null,
  "diagnosis": string | null,
  "health_metrics": { "<metric_key>": string } | null,
  "medicines": [
    {
      "name": string | null,
      "dosage": string | null,
      "quantity": integer | null
    }
  ] | null
}"""

USER_PROMPT = (
    "The attached image(s) contain a medical prescription. "
    "Parse all visible information and return a JSON object matching the schema."
)


# ==============================================================
# Models
# ==============================================================

@dataclass
class Medicine:
    name:     Optional[str]
    dosage:   Optional[str]
    quantity: Optional[int]

    @classmethod
    def from_dict(cls, data: dict) -> Medicine:
        raw_qty = data.get("quantity")
        return cls(
            name=data.get("name"),
            dosage=data.get("dosage"),
            quantity=int(raw_qty) if raw_qty is not None else None,
        )


@dataclass
class Prescription:
    patient_name:   Optional[str]
    patient_age:    Optional[str]
    doctor_name:    Optional[str]
    date:           Optional[str]
    patient_issue:  Optional[str]
    diagnosis:      Optional[str]
    health_metrics: Optional[dict]
    medicines:      list[Medicine] = field(default_factory=list)
    status:         str = "pending"

    @classmethod
    def from_dict(cls, data: dict) -> Prescription:
        return cls(
            patient_name=data.get("patient_name"),
            patient_age=data.get("patient_age"),
            doctor_name=data.get("doctor_name"),
            date=data.get("date"),
            patient_issue=data.get("patient_issue"),
            diagnosis=data.get("diagnosis"),
            health_metrics=data.get("health_metrics"),
            medicines=[Medicine.from_dict(m) for m in (data.get("medicines") or [])],
        )


# ==============================================================
# Gemini Client
# ==============================================================

class GeminiClient:
    """Calls Gemini generateContent API with image input and retry on 429/5xx."""

    def __init__(
        self,
        api_key: str,
        model: str = GEMINI_MODEL,
        max_attempts: int = GEMINI_RETRY_MAX_ATTEMPTS,
        base_delay_sec: float = GEMINI_RETRY_BASE_DELAY_SEC,
        request_timeout_sec: int = 120,
    ) -> None:
        self._api_key = api_key
        self._model = model
        self._max_attempts = max_attempts
        self._base_delay = base_delay_sec
        self._request_timeout = request_timeout_sec

    def generate_content(
        self,
        system_prompt: str,
        user_prompt: str,
        image_paths: list[Path],
    ) -> str:
        """Send one or more prescription images to Gemini, return raw text response."""
        if not image_paths:
            raise ValueError("At least one image path is required.")

        url = GEMINI_URL_TEMPLATE.format(self._model)
        payload = self._build_payload(system_prompt, user_prompt, image_paths)
        data = json.dumps(payload).encode("utf-8")

        last_error: Exception | None = None
        for attempt in range(self._max_attempts):
            try:
                return self._do_request(url, data)
            except urllib.error.HTTPError as e:
                last_error = e
                if e.code not in GEMINI_RETRY_STATUSES:
                    raise
                if attempt == self._max_attempts - 1:
                    raise
                time.sleep(self._base_delay * (2 ** attempt))
            except (OSError, json.JSONDecodeError) as e:
                last_error = e
                if attempt == self._max_attempts - 1:
                    raise
                time.sleep(self._base_delay * (2 ** attempt))

        raise last_error or RuntimeError("Gemini request failed after retries")

    def _build_payload(self, system_prompt: str, user_prompt: str, image_paths: list[Path]) -> dict:
        image_parts = []
        for path in image_paths:
            mime_type = MIME_TYPES.get(path.suffix.lower(), "image/jpeg")
            with open(path, "rb") as f:
                b64 = base64.b64encode(f.read()).decode("utf-8")
            image_parts.append({"inlineData": {"mimeType": mime_type, "data": b64}})

        return {
            "systemInstruction": {"parts": [{"text": system_prompt}]},
            "contents": [
                {"parts": image_parts + [{"text": user_prompt}]}
            ],
        }

    def _do_request(self, url: str, data: bytes) -> str:
        req = urllib.request.Request(
            url,
            data=data,
            headers={
                "x-goog-api-key": self._api_key,
                "Content-Type": "application/json",
            },
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=self._request_timeout) as resp:
            body = json.loads(resp.read().decode("utf-8"))

        candidates = body.get("candidates") or []
        if not candidates:
            raise RuntimeError(f"Gemini returned no candidates: {body}")
        parts = candidates[0].get("content", {}).get("parts") or []
        if not parts:
            raise RuntimeError(f"Gemini returned no parts: {body}")
        return parts[0].get("text", "")


# ==============================================================
# Parser
# ==============================================================

class PrescriptionParser:
    """Sends prescription images to Gemini and deserialises the response."""

    def __init__(self, gemini: GeminiClient):
        self.gemini = gemini

    def parse(self, image_paths: list[Path]) -> Prescription:
        raw = self.gemini.generate_content(
            system_prompt=SYSTEM_PROMPT,
            user_prompt=USER_PROMPT,
            image_paths=image_paths,
        )
        # Strip accidental markdown fences if Gemini adds them
        cleaned = (
            raw.strip()
            .removeprefix("```json")
            .removeprefix("```")
            .removesuffix("```")
            .strip()
        )
        return Prescription.from_dict(json.loads(cleaned))


# ==============================================================
# Repository
# ==============================================================

class PrescriptionRepository:
    """Handles all database operations for prescriptions and medicines."""

    _INSERT_PRESCRIPTION = """
        INSERT INTO prescriptions
            (patient_name, patient_age, doctor_name, date,
             patient_issue, diagnosis, health_metrics, status)
        VALUES
            (%(patient_name)s, %(patient_age)s, %(doctor_name)s, %(date)s,
             %(patient_issue)s, %(diagnosis)s, %(health_metrics)s, %(status)s)
        RETURNING id;
    """

    _INSERT_MEDICINE = """
        INSERT INTO prescription_medicines (prescription_id, name, dosage, quantity)
        VALUES (%s, %s, %s, %s);
    """

    def __init__(self, cursor):
        self.cursor = cursor

    def save(self, prescription: Prescription) -> int:
        """Inserts a prescription and its medicines. Returns the new prescription id."""
        self.cursor.execute(self._INSERT_PRESCRIPTION, {
            "patient_name":   prescription.patient_name,
            "patient_age":    prescription.patient_age,
            "doctor_name":    prescription.doctor_name,
            "date":           prescription.date,
            "patient_issue":  prescription.patient_issue,
            "diagnosis":      prescription.diagnosis,
            "health_metrics": json.dumps(prescription.health_metrics)
                              if prescription.health_metrics else None,
            "status":         prescription.status,
        })
        prescription_id = self.cursor.fetchone()["id"]
        self._save_medicines(prescription_id, prescription.medicines)
        return prescription_id

    def _save_medicines(self, prescription_id: int, medicines: list[Medicine]):
        for med in medicines:
            self.cursor.execute(self._INSERT_MEDICINE, (
                prescription_id,
                med.name,
                med.dosage,
                med.quantity,
            ))

    def get_pending(self) -> list[dict]:
        """Fetches all pending prescriptions joined with their medicines."""
        self.cursor.execute("""
            SELECT
                p.*,
                json_agg(
                    json_build_object('name', m.name, 'dosage', m.dosage, 'quantity', m.quantity)
                ) AS medicines
            FROM prescriptions p
            LEFT JOIN prescription_medicines m ON m.prescription_id = p.id
            WHERE p.status = 'pending'
            GROUP BY p.id;
        """)
        return self.cursor.fetchall()

    def mark_ordered(self, prescription_ids: list[int]):
        """Marks a list of prescriptions as ordered once a purchase order is generated."""
        self.cursor.execute("""
            UPDATE prescriptions SET status = 'ordered'
            WHERE id = ANY(%s);
        """, (prescription_ids,))


# ==============================================================
# Pipeline
# ==============================================================

class IngestionPipeline:
    """Parses a set of prescription images via Gemini and stores as one DB row."""

    def __init__(self, image_paths: list[Path], parser: PrescriptionParser, conn):
        self.image_paths = image_paths
        self.parser = parser
        self.conn = conn

    def run(self):
        print(f"Sending {len(self.image_paths)} image(s) to Gemini...")
        for p in self.image_paths:
            print(f"  {p}")

        prescription = self.parser.parse(self.image_paths)

        with self.conn:
            with self.conn.cursor(cursor_factory=RealDictCursor) as cur:
                repo = PrescriptionRepository(cur)
                pid = repo.save(prescription)

        print(f"\nStored as prescription id={pid}")
        print(f"  Patient  : {prescription.patient_name or '—'}")
        print(f"  Doctor   : {prescription.doctor_name or '—'}")
        print(f"  Date     : {prescription.date or '—'}")
        print(f"  Medicines: {len(prescription.medicines)}")
        for med in prescription.medicines:
            print(f"    • {med.name or '—'}  |  {med.dosage or '—'}  |  qty: {med.quantity}")
        print(f"  Status   : {prescription.status}")


# ==============================================================
# Entrypoint
# ==============================================================

def resolve_image_paths(args: list[str]) -> list[Path]:
    """
    Accepts one or more CLI arguments, each of which may be:
      - An explicit file path      e.g.  scan.jpg
      - A glob pattern             e.g.  "scans/*.png"
    Returns a sorted, deduplicated list of valid image paths.
    """
    paths: list[Path] = []
    for arg in args:
        matched = [Path(p) for p in glob.glob(arg)]
        if not matched:
            # Try treating it as a literal path in case glob found nothing
            literal = Path(arg)
            if literal.is_file():
                matched = [literal]
            else:
                print(f"Warning: no files matched '{arg}', skipping.")
        paths.extend(matched)

    # Deduplicate, filter to supported types, sort
    seen: set[Path] = set()
    result: list[Path] = []
    for p in sorted(paths):
        resolved = p.resolve()
        if resolved in seen:
            continue
        seen.add(resolved)
        if p.suffix.lower() not in SUPPORTED_IMAGE_EXTENSIONS:
            print(f"Warning: unsupported file type '{p}', skipping.")
            continue
        result.append(p)

    return result


def main():
    if len(sys.argv) < 2:
        print("Usage: python store_prescriptions.py <image1> [image2 ...] [\"glob/pattern/*.jpg\"]")
        print("Examples:")
        print("  python store_prescriptions.py scan.jpg")
        print("  python store_prescriptions.py page1.jpg page2.jpg")
        print("  python store_prescriptions.py \"scans/rx_001/*.png\"")
        sys.exit(1)

    image_paths = resolve_image_paths(sys.argv[1:])
    if not image_paths:
        print("Error: no valid image files found.")
        sys.exit(1)

    gemini = GeminiClient(api_key=os.environ["GEMINI_API_KEY"])
    parser = PrescriptionParser(gemini)
    conn   = psycopg2.connect(os.environ["DATABASE_URL"])

    try:
        IngestionPipeline(image_paths, parser, conn).run()
    finally:
        conn.close()


if __name__ == "__main__":
    main()