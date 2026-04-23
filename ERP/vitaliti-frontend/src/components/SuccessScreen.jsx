import React from 'react'

const s = {
  container: { maxWidth: 860, margin: '0 auto', padding: '48px 24px 80px' },
  wrap: { padding: '80px 0', textAlign: 'center' },
  icon: { fontSize: '3.5rem', display: 'block', marginBottom: 20, animation: 'popIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)' },
  title: { fontFamily: 'var(--serif)', fontSize: '2rem', marginBottom: 8, animation: 'fadeUp 0.4s ease 0.1s both' },
  id: { fontFamily: 'var(--mono)', fontSize: '0.85rem', color: 'var(--muted)', marginBottom: 32, animation: 'fadeUp 0.4s ease 0.15s both' },
  btn: {
    display: 'inline-flex', alignItems: 'center', gap: 8,
    padding: '12px 28px', borderRadius: 'var(--radius)',
    border: '2px solid var(--ink)', fontFamily: 'var(--sans)',
    fontSize: '0.9rem', fontWeight: 500, cursor: 'pointer',
    background: 'var(--ink)', color: 'var(--white)',
    transition: 'background 0.15s',
    animation: 'fadeUp 0.4s ease 0.2s both',
  },
}

export default function SuccessScreen({ prescriptionId, onReset }) {
  return (
    <div style={s.container}>
      <div style={s.wrap}>
        <span style={s.icon}>✦</span>
        <h2 style={s.title}>Prescription saved.</h2>
        <p style={s.id}>Prescription ID: {prescriptionId} · Status: pending</p>
        <button style={s.btn} onClick={onReset}>Add another →</button>
      </div>
    </div>
  )
}