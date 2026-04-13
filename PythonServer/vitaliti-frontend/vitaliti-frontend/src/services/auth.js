export async function exchangeGoogleToken(idToken) {
  const res = await fetch('http://122.166.244.91:8080/api/v1/vitality/user', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ googleToken: idToken }),
  })

  const data = await res.json()

  localStorage.setItem('jwt', data.jwt)
  return data
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