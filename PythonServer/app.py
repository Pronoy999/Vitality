"""
app.py
------
FastAPI backend for Vitaliti — prescription ingestion and purchase order generation.

Endpoints:
  POST /upload     — accepts image files, kicks off BG Gemini parsing, returns job_id
  GET  /status/{job_id}  — returns job status + parsed data when ready
  POST /confirm/{job_id} — persists confirmed (possibly edited) prescription to DB
"""

from __future__ import annotations

import os
import tempfile
import threading
import uuid
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Optional

import psycopg2
from dotenv import load_dotenv
from fastapi import BackgroundTasks, FastAPI, File, HTTPException, UploadFile
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
from psycopg2.extras import RealDictCursor
from pydantic import BaseModel
from starlette.responses import FileResponse

load_dotenv()

# ── import domain code from store_prescriptions ──────────────────────────────
from store_prescription_data import (
    GeminiClient,
    Medicine,
    Prescription,
    PrescriptionParser,
    PrescriptionRepository,
)
from generate_purchase_order import (
    MedicineAggregator,
    PurchaseOrder,
    PurchaseOrderPDFGenerator,
    PurchaseOrderRepository,
)

# ==============================================================
# Job store (in-memory; swap for Redis/DB in production)
# ==============================================================

@dataclass
class Job:
    id:         str
    status:     str = "uploading"   # uploading | parsing | ready | error
    step:       str = "Uploading images…"
    data:       Optional[dict] = None
    error:      Optional[str] = None
    image_paths: list[str] = field(default_factory=list)

_jobs: dict[str, Job] = {}
_jobs_lock = threading.Lock()

def get_job(job_id: str) -> Job:
    with _jobs_lock:
        job = _jobs.get(job_id)
    if not job:
        raise HTTPException(status_code=404, detail="Job not found")
    return job


# ==============================================================
# App
# ==============================================================

UPLOAD_DIR = Path(tempfile.gettempdir()) / "rx_uploads"
UPLOAD_DIR.mkdir(exist_ok=True)

app = FastAPI()
gemini = GeminiClient(api_key=os.environ["GEMINI_API_KEY"])
parser = PrescriptionParser(gemini)


# ==============================================================
# Background task
# ==============================================================

def _parse_prescription(job_id: str):
    job = _jobs.get(job_id)
    if not job:
        return
    try:
        job.status = "parsing"
        job.step   = "Parsing prescription with Gemini…"

        paths = [Path(p) for p in job.image_paths]
        prescription = parser.parse(paths)

        job.data   = _prescription_to_dict(prescription)
        job.status = "ready"
        job.step   = "Done"
    except Exception as e:
        job.status = "error"
        job.error  = str(e)
        job.step   = "Error"


def _prescription_to_dict(p: Prescription) -> dict:
    return {
        "patient_name":   p.patient_name,
        "patient_age":    p.patient_age,
        "doctor_name":    p.doctor_name,
        "date":           p.date,
        "patient_issue":  p.patient_issue,
        "diagnosis":      p.diagnosis,
        "health_metrics": p.health_metrics or {},
        "medicines": [
            {"name": m.name, "dosage": m.dosage, "quantity": m.quantity}
            for m in p.medicines
        ],
    }


# ==============================================================
# Routes
# ==============================================================

class ConfirmPayload(BaseModel):
    data: dict[str, Any]


@app.post("/api/upload")
async def upload(files: list[UploadFile] = File(...), background_tasks: BackgroundTasks = None):
    job_id  = str(uuid.uuid4())
    job_dir = UPLOAD_DIR / job_id
    job_dir.mkdir()

    saved_paths = []
    for f in files:
        dest = job_dir / f.filename
        dest.write_bytes(await f.read())
        saved_paths.append(str(dest))

    job = Job(id=job_id, status="uploading", step="Uploading images…", image_paths=saved_paths)
    with _jobs_lock:
        _jobs[job_id] = job

    thread = threading.Thread(target=_parse_prescription, args=(job_id,), daemon=True)
    thread.start()

    return {"job_id": job_id}


@app.get("/api/status/{job_id}")
def status(job_id: str):
    job = get_job(job_id)
    return {
        "status": job.status,
        "step":   job.step,
        "data":   job.data,
        "error":  job.error,
    }


@app.post("/api/confirm/{job_id}")
def confirm(job_id: str, payload: ConfirmPayload):
    get_job(job_id)  # validate job exists

    d = payload.data
    medicines = [
        Medicine(
            name=m.get("name"),
            dosage=m.get("dosage"),
            quantity=int(m["quantity"]) if m.get("quantity") is not None else None,
        )
        for m in (d.get("medicines") or [])
    ]

    raw_age = d.get("patient_age")
    prescription = Prescription(
        patient_name=d.get("patient_name"),
        patient_age=int(raw_age) if raw_age is not None else None,
        doctor_name=d.get("doctor_name"),
        date=d.get("date"),
        patient_issue=d.get("patient_issue"),
        diagnosis=d.get("diagnosis"),
        health_metrics=d.get("health_metrics") or None,
        medicines=medicines,
    )

    conn = psycopg2.connect(os.environ["DATABASE_URL"])
    try:
        with conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                repo = PrescriptionRepository(cur)
                pid  = repo.save(prescription)
        return {"prescription_id": pid}
    finally:
        conn.close()


@app.post("/api/prescriptions")
def save_prescription(payload: ConfirmPayload):
    """Directly saves a prescription — used for manual entry (no job/image involved)."""
    d = payload.data
    medicines = [
        Medicine(
            name=m.get("name"),
            dosage=m.get("dosage"),
            quantity=int(m["quantity"]) if m.get("quantity") is not None else None,
        )
        for m in (d.get("medicines") or [])
    ]
    raw_age = d.get("patient_age")
    prescription = Prescription(
        patient_name=d.get("patient_name"),
        patient_age=int(raw_age) if raw_age is not None else None,
        doctor_name=d.get("doctor_name"),
        date=d.get("date"),
        patient_issue=d.get("patient_issue"),
        diagnosis=d.get("diagnosis"),
        health_metrics=d.get("health_metrics") or None,
        medicines=medicines,
    )
    conn = psycopg2.connect(os.environ["DATABASE_URL"])
    try:
        with conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                repo = PrescriptionRepository(cur)
                pid  = repo.save(prescription)
        return {"prescription_id": pid}
    finally:
        conn.close()


# ==============================================================
# Purchase Order Routes
# ==============================================================

@app.get("/api/po/pending")
def po_pending():
    """Returns pending prescription count and aggregated medicine list for preview."""
    conn = psycopg2.connect(os.environ["DATABASE_URL"])
    try:
        with conn.cursor(cursor_factory=RealDictCursor) as cur:
            repo = PurchaseOrderRepository(cur)
            rows = repo.get_pending_rows()
            if not rows:
                return {"count": 0, "medicines": []}
            medicines, prescription_ids = MedicineAggregator().aggregate(rows)
            return {
                "count": len(prescription_ids),
                "medicines": [
                    {
                        "name":           m.name,
                        "total_quantity": m.total_quantity,
                        "raw_dosages":    list(set(m.raw_dosages)),
                        "patient_count":  m.patient_count,
                    }
                    for m in medicines
                ],
            }
    finally:
        conn.close()


class POConfirmPayload(BaseModel):
    medicines: list[dict[str, Any]]


@app.post("/api/po/generate")
def po_generate(payload: POConfirmPayload):
    """Generates PDF from confirmed medicine list, marks prescriptions ordered, returns file."""
    import tempfile as _tmp
    from generate_purchase_order import AggregatedMedicine

    conn = psycopg2.connect(os.environ["DATABASE_URL"])
    try:
        with conn:
            with conn.cursor(cursor_factory=RealDictCursor) as cur:
                repo = PurchaseOrderRepository(cur)
                rows = repo.get_pending_rows()
                if not rows:
                    raise HTTPException(status_code=400, detail="No pending prescriptions.")

                _, prescription_ids = MedicineAggregator().aggregate(rows)

                # Build medicines from confirmed (possibly edited) payload
                medicines = [
                    AggregatedMedicine(
                        name=m.get("name") or "Unknown",
                        total_quantity=int(m["total_quantity"]) if m.get("total_quantity") is not None else None,
                        raw_dosages=m.get("raw_dosages") or [],
                        patient_count=m.get("patient_count") or 0,
                    )
                    for m in payload.medicines
                ]

                po = PurchaseOrder.create(medicines, prescription_ids)

                pdf_path = Path(_tmp.gettempdir()) / f"{po.order_number}.pdf"
                PurchaseOrderPDFGenerator(str(pdf_path)).generate(po)
                repo.mark_ordered(prescription_ids)

        return FileResponse(
            pdf_path,
            media_type="application/pdf",
            filename=f"{po.order_number}.pdf",
        )
    finally:
        conn.close()


# Serve built React app
STATIC_DIR = Path("static")
if STATIC_DIR.exists():
    app.mount("/assets", StaticFiles(directory=STATIC_DIR / "assets"), name="assets")

@app.get("/", response_class=HTMLResponse)
@app.get("/{full_path:path}", response_class=HTMLResponse)
def index(full_path: str = ""):
    index_file = STATIC_DIR / "index.html"
    if index_file.exists():
        return FileResponse(index_file)
    return HTMLResponse("<h2>Run <code>npm run build</code> inside vitaliti-frontend/ first.</h2>", status_code=503)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)