import React from 'react'
import { GoogleLogin } from '@react-oauth/google'

const s = {
  wrapper: {
    height: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'var(--paper)',
  },
  card: {
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    padding: '32px 40px',
    textAlign: 'center',
    minWidth: 320,
  },
  title: {
    fontFamily: 'var(--serif)',
    fontSize: '1.6rem',
    marginBottom: 12,
  },
  subtitle: {
    fontFamily: 'var(--mono)',
    fontSize: '0.75rem',
    color: 'var(--muted)',
    marginBottom: 24,
    textTransform: 'uppercase',
    letterSpacing: '0.06em',
  },
}

export default function LoginScreen({ onLogin }) {
  return (
    <div style={s.wrapper}>
      <div style={s.card}>
        <div style={s.title}>
          Vitali<span style={{ color: 'var(--accent)' }}>ty</span>
        </div>

        <GoogleLogin
          onSuccess={onLogin}
          onError={() => console.log('Login Failed')}
        />
      </div>
    </div>
  )
}