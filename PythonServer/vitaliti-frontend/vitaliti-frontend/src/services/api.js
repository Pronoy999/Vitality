const BASE_URL = import.meta.env.VITE_BACKEND_API_BASE_URL || ''

const API_PATHS = {
  login: '/api/v1/vitality/user',
  uploadPrescription: '/api/v1/vitality/prescription/upload',
  prescriptionStatus: (jobId) => `/api/v1/vitality/prescription/status/${jobId}`,
  confirmPrescription: (jobId) => `/api/v1/vitality/prescription/confirm/${jobId}`,
  manualPrescription: '/api/v1/vitality/prescription/manual',
  pendingPurchaseOrder: '/api/po/pending',
  generatePurchaseOrder: '/api/po/generate',
}

function getStoredJWT() {
  return localStorage.getItem('jwt')
}

function buildHeaders(headers = {}, body) {
  const token = getStoredJWT()
  const isFormData = body instanceof FormData
  return {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
    ...headers,
    ...(token ? { token } : {}),
  }
}

function buildUrl(path) {
  return `${BASE_URL}${path}`
}

async function parseError(res) {
  try {
    const body = await res.json()
    return body.message || body.detail || `API error: ${res.status}`
  } catch {
    return `API error: ${res.status}`
  }
}

function getFilenameFromResponse(res, fallback) {
  const disposition = res.headers.get('content-disposition') || ''
  const match = disposition.match(/filename="?([^"]+)"?/)
  return match ? match[1] : fallback
}

export async function apiFetch(path, options = {}) {
  const { responseType = 'json', ...fetchOptions } = options
  const res = await fetch(buildUrl(path), {
    ...fetchOptions,
    headers: buildHeaders(fetchOptions.headers, fetchOptions.body),
  })

  if (!res.ok) {
    throw new Error(await parseError(res))
  }

  if (responseType === 'blob') {
    return {
      blob: await res.blob(),
      filename: getFilenameFromResponse(res, 'download'),
    }
  }

  if (responseType === 'response') {
    return res
  }

  return res.json()
}

export function loginWithGoogleToken(idToken) {
  return apiFetch(API_PATHS.login, {
    method: 'POST',
    body: JSON.stringify({ googleToken: idToken }),
  })
}

export function uploadPrescription(files) {
  const formData = new FormData()
  files.forEach(file => formData.append('files', file))
  return apiFetch(API_PATHS.uploadPrescription, {
    method: 'POST',
    body: formData,
  })
}

export function getPrescriptionStatus(jobId) {
  return apiFetch(API_PATHS.prescriptionStatus(jobId))
}

export function saveReviewedPrescription({ jobId, data }) {
  const path = jobId
    ? API_PATHS.confirmPrescription(jobId)
    : API_PATHS.manualPrescription

  return apiFetch(path, {
    method: 'POST',
    body: JSON.stringify({ data }),
  })
}

export function getPendingPurchaseOrder() {
  return apiFetch(API_PATHS.pendingPurchaseOrder)
}

export async function generatePurchaseOrder(medicines) {
  return apiFetch(API_PATHS.generatePurchaseOrder, {
    method: 'POST',
    body: JSON.stringify({ medicines }),
    responseType: 'blob',
  }).then(({ blob, filename }) => ({
    blob,
    filename: filename === 'download' ? 'purchase_order.pdf' : filename,
  }))
}
