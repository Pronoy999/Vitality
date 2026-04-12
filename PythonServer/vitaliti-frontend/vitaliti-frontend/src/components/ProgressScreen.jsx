import React, { useEffect, useState } from 'react'

const STEPS = [
  { key: 'uploading', label: 'Uploading images' },
  { key: 'parsing',   label: 'Parsing with Gemini' },
  { key: 'ready',     label: 'Preparing review' },
]

const STATUS_TO_STEP = {
  uploading: 'uploading',
  parsing:   'parsing',
  ready:     'ready',
  error:     'parsing',
}

const s = {
  container: { maxWidth: 860, margin: '0 auto', padding: '48px 24px 80px' },
  wrap: { padding: '60px 0', textAlign: 'center' },
  title: { fontFamily: 'var(--serif)', fontSize: '1.8rem', marginBottom: 40, animation: 'fadeUp 0.4s ease' },
  steps: { display: 'inline-flex', flexDirection: 'column', gap: 0, textAlign: 'left', minWidth: 280 },
  step: (state) => ({
    display: 'flex',
    alignItems: 'center',
    gap: 16,
    padding: '14px 0',
    borderBottom: '1px solid var(--rule)',
    fontSize: '0.9rem',
    color: state === 'done' ? 'var(--accent2)' : state === 'active' ? 'var(--ink)' : state === 'error' ? 'var(--accent)' : 'var(--muted)',
    fontWeight: state === 'active' ? 500 : 400,
    transition: 'color 0.3s',
  }),
  dot: (state) => ({
    width: 10,
    height: 10,
    borderRadius: '50%',
    border: '2px solid currentColor',
    flexShrink: 0,
    background: state === 'done' ? 'var(--accent2)' : state === 'active' ? 'var(--ink)' : state === 'error' ? 'var(--accent)' : 'transparent',
    animation: state === 'active' ? 'pulse 1.2s infinite' : 'none',
    transition: 'background 0.3s',
  }),
  errorBox: {
    marginTop: 24,
    padding: '16px 20px',
    background: '#fdf0ee',
    borderLeft: '3px solid var(--accent)',
    borderRadius: 'var(--radius)',
    fontFamily: 'var(--mono)',
    fontSize: '0.82rem',
    color: 'var(--accent)',
    textAlign: 'left',
    display: 'inline-block',
    maxWidth: 500,
    wordBreak: 'break-word',
  },
}

function stepState(stepKey, currentStatus, errorOccurred) {
  const order = ['uploading', 'parsing', 'ready']
  const currentIdx = order.indexOf(STATUS_TO_STEP[currentStatus] || 'uploading')
  const stepIdx    = order.indexOf(stepKey)

  if (errorOccurred && stepKey === STATUS_TO_STEP[currentStatus]) return 'error'
  if (stepIdx < currentIdx) return 'done'
  if (stepIdx === currentIdx && !errorOccurred) return 'active'
  return 'pending'
}

export default function ProgressScreen({ jobId, onReady }) {
  const [status, setStatus]   = useState('uploading')
  const [errorMsg, setError]  = useState(null)

  useEffect(() => {
    if (!jobId) return
    const timer = setInterval(async () => {
      try {
        const res  = await fetch(`/api/status/${jobId}`)
        const json = await res.json()
        setStatus(json.status)

        if (json.status === 'ready') {
          clearInterval(timer)
          setTimeout(() => onReady(json.data), 600)
        } else if (json.status === 'error') {
          clearInterval(timer)
          setError(json.error || 'An unknown error occurred.')
        }
      } catch (e) {
        clearInterval(timer)
        setError(e.message)
      }
    }, 1500)
    return () => clearInterval(timer)
  }, [jobId])

  const isError = status === 'error'

  return (
    <div style={s.container}>
      <div style={s.wrap}>
        <h2 style={s.title}>Reading your prescription…</h2>
        <div style={s.steps}>
          {STEPS.map(({ key, label }, i) => {
            const state = stepState(key, status, isError)
            return (
              <div key={key} style={{ ...s.step(state), borderBottom: i === STEPS.length - 1 ? 'none' : '1px solid var(--rule)' }}>
                <span style={s.dot(state)} />
                {label}
              </div>
            )
          })}
        </div>
        {isError && <div style={s.errorBox}>{errorMsg}</div>}
      </div>
    </div>
  )
}