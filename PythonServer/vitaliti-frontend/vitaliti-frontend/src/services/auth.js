import { loginWithGoogleToken } from './api'

export async function exchangeGoogleToken(idToken) {
  const res = await loginWithGoogleToken(idToken)

  localStorage.setItem('jwt', res.jwtToken)
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
