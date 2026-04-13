import React from 'react'
import { GoogleLogin } from '@react-oauth/google'

const s = {
  header: {
    borderBottom: '2px solid var(--ink)',
    padding: '0 40px',
    display: 'flex',
    alignItems: 'stretch',
    background: 'var(--paper)',
    position: 'sticky',
    top: 0,
    zIndex: 100,
  },
  brand: {
    display: 'flex',
    alignItems: 'baseline',
    gap: 14,
    paddingRight: 32,
    borderRight: '1.5px solid var(--rule)',
    paddingTop: 18,
    paddingBottom: 18,
  },
  logo: {
    fontFamily: 'var(--serif)',
    fontSize: '1.6rem',
    letterSpacing: '-0.5px',
    color: 'var(--ink)',
  },
  accent: { color: 'var(--accent)' },
  tagline: {
    fontSize: '0.75rem',
    fontFamily: 'var(--mono)',
    color: 'var(--muted)',
    letterSpacing: '0.04em',
    textTransform: 'uppercase',
  },
  nav: {
    display: 'flex',
    alignItems: 'stretch',
    paddingLeft: 8,
  },
  tab: (active) => ({
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    padding: '0 24px',
    fontFamily: 'var(--mono)',
    fontSize: '0.75rem',
    textTransform: 'uppercase',
    letterSpacing: '0.06em',
    cursor: 'pointer',
    color: active ? 'var(--ink)' : 'var(--muted)',
    background: 'none',
    border: 'none',
    borderBottom: active ? '2.5px solid var(--ink)' : '2.5px solid transparent',
    marginBottom: '-2px',
    transition: 'color 0.15s',
    fontWeight: active ? 500 : 400,
  }),
  badge: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'var(--accent)',
    color: 'var(--white)',
    borderRadius: 10,
    fontSize: '0.65rem',
    fontWeight: 700,
    minWidth: 18,
    height: 18,
    padding: '0 5px',
    fontFamily: 'var(--mono)',
  },
}

export default function Header({ activeTab, onTab, pendingCount, onLogin, isAuthenticated }) {
  return (
    <header style={s.header}>
      <div style={s.brand}>
        <span style={s.logo}>Vitali<span style={s.accent}>ty</span></span>
      </div>
      <nav style={s.nav}>
        <button style={s.tab(activeTab === 'rx')} onClick={() => onTab('rx')}>
          Prescriptions
        </button>
        <button style={s.tab(activeTab === 'po')} onClick={() => onTab('po')}>
          Purchase Orders
          {pendingCount > 0 && <span style={s.badge}>{pendingCount}</span>}
        </button>
      </nav>
      <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center' }}>
        {!isAuthenticated ? (
          <GoogleLogin
            onSuccess={onLogin}
            onError={() => console.log('Login Failed')}
          />
        ) : (
          <span style={{ fontSize: '0.75rem', fontFamily: 'var(--mono)' }}>
            Logged in
          </span>
        )}
      </div>
    </header>
  )
}