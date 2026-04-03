import base64
import json
import time
import urllib.error
import urllib.request
from pathlib import Path

MIME_TYPES = {
    ".jpg":  "image/jpeg",
    ".jpeg": "image/jpeg",
    ".png":  "image/png",
    ".webp": "image/webp",
    ".heic": "image/heic",
}

GEMINI_MODEL = "gemini-2.5-flash"
GEMINI_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/{}:generateContent"

# Retry for Gemini: 429 (rate limit) and 5xx (server errors)
GEMINI_RETRY_MAX_ATTEMPTS = 5
GEMINI_RETRY_BASE_DELAY_SEC = 2.0
GEMINI_RETRY_STATUSES = {429} | set(range(500, 600))

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
