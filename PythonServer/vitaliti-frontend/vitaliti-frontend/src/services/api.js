import { getJWT } from './auth'

const BASE_URL = import.meta.env.BACKEND_API_BASE_URL || ''

export async function apiFetch(path, options = {}) {
  const token = getJWT()

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  })

  if (!res.ok) {
    // optional: handle 401 globally later
    throw new Error(`API error: ${res.status}`)
  }

  return res.json()
}