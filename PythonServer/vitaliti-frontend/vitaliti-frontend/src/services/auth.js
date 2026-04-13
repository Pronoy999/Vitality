import { apiFetch } from './api'

export async function exchangeGoogleToken(idToken) {
  const res = await apiFetch('/api/v1/vitality/user', {
    method: 'POST',
    body: JSON.stringify({ token: idToken }),
  })

  localStorage.setItem('jwt', res.jwt)
  return res
}

export function getJWT() {
  return localStorage.getItem('jwt')
}

export function isLoggedIn() {
  return !!localStorage.getItem('jwt')
}

export function logout() {
  localStorage.removeItem('jwt')
}