import React, { useCallback, useEffect, useState } from 'react'

// ── Styles (same design tokens as ReviewScreen) ───────────────
const s = {
  container:   { maxWidth: 860, margin: '0 auto', padding: '48px 24px 120px' },
  topBar: {
    display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
    marginBottom: 32, flexWrap: 'wrap', gap: 12,
  },
  title:       { fontFamily: 'var(--serif)', fontSize: '2rem', animation: 'fadeUp 0.3s ease' },
  statRow: {
    display: 'flex', gap: 20, marginBottom: 32, flexWrap: 'wrap',
    animation: 'fadeUp 0.35s ease 0.05s both',
  },
  statCard: {
    background: 'var(--white)', border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)', padding: '16px 24px',
    boxShadow: 'var(--shadow)', minWidth: 140,
  },
  statLabel:   { fontFamily: 'var(--mono)', fontSize: '0.68rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--muted)', marginBottom: 4 },
  statValue:   { fontFamily: 'var(--serif)', fontSize: '2rem', color: 'var(--ink)', lineHeight: 1.1 },
  statSub:     { fontSize: '0.75rem', color: 'var(--muted)', marginTop: 2 },
  emptyState: {
    textAlign: 'center', padding: '80px 0',
    fontFamily: 'var(--mono)', fontSize: '0.85rem', color: 'var(--muted)',
    animation: 'fadeUp 0.3s ease',
  },
  emptyIcon:   { fontSize: '2.5rem', display: 'block', marginBottom: 12 },
  section: {
    background: 'var(--white)', border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)', marginBottom: 20,
    overflow: 'hidden', boxShadow: 'var(--shadow)',
    animation: 'fadeUp 0.35s ease 0.1s both',
  },
  sectionHead: {
    padding: '12px 20px', background: 'var(--cream)',
    borderBottom: '1.5px solid var(--rule)', fontFamily: 'var(--mono)',
    fontSize: '0.72rem', textTransform: 'uppercase', letterSpacing: '0.1em',
    color: 'var(--muted)', display: 'flex', alignItems: 'center', justifyContent: 'space-between',
  },
  table:       { width: '100%', borderCollapse: 'collapse' },
  th:          { padding: '10px 16px', textAlign: 'left', fontFamily: 'var(--mono)', fontSize: '0.68rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--muted)', borderBottom: '1.5px solid var(--rule)', background: 'var(--cream)' },
  td:          { padding: '10px 16px', borderBottom: '1px solid var(--rule)', verticalAlign: 'middle', fontSize: '0.9rem' },
  tdLast:      { padding: '10px 16px', verticalAlign: 'middle', fontSize: '0.9rem' },
  tdInput:     { width: '100%', border: 'none', background: 'transparent', fontFamily: 'var(--sans)', fontSize: '0.88rem', color: 'var(--ink)', outline: 'none', padding: '3px 0', borderBottom: '1.5px solid transparent', transition: 'border-color 0.2s' },
  dosages:     { fontFamily: 'var(--mono)', fontSize: '0.72rem', color: 'var(--muted)' },
  btnDanger:   { background: 'transparent', color: 'var(--accent)', border: '1.5px solid var(--accent)', borderRadius: 'var(--radius)', padding: '5px 12px', fontSize: '0.78rem', cursor: 'pointer', fontFamily: 'var(--sans)', transition: 'background 0.15s, color 0.15s' },
  addRow:      { padding: '12px 16px', borderTop: '1.5px solid var(--rule)' },
  btnAdd:      { background: 'transparent', border: '1.5px dashed var(--rule)', color: 'var(--muted)', padding: '7px 16px', borderRadius: 'var(--radius)', fontSize: '0.82rem', fontFamily: 'var(--sans)', cursor: 'pointer', width: '100%', textAlign: 'center', transition: 'border-color 0.2s, color 0.2s' },
  confirmBar: {
    position: 'fixed', bottom: 0, left: 0, right: 0,
    background: 'var(--paper)', borderTop: '2px solid var(--ink)',
    padding: '16px 40px', display: 'flex', alignItems: 'center',
    justifyContent: 'space-between', gap: 16, zIndex: 200,
  },
  confirmNote: { fontSize: '0.78rem', color: 'var(--muted)', fontFamily: 'var(--mono)' },
  btnRow:      { display: 'flex', gap: 12, flexWrap: 'wrap' },
  btnRefresh:  { display: 'inline-flex', alignItems: 'center', padding: '11px 22px', borderRadius: 'var(--radius)', border: '2px solid var(--ink)', fontFamily: 'var(--sans)', fontSize: '0.88rem', fontWeight: 500, cursor: 'pointer', background: 'transparent', color: 'var(--ink)', transition: 'background 0.15s' },
  btnGenerate: { display: 'inline-flex', alignItems: 'center', padding: '12px 32px', borderRadius: 'var(--radius)', border: '2px solid var(--accent2)', fontFamily: 'var(--sans)', fontSize: '0.95rem', fontWeight: 500, cursor: 'pointer', background: 'var(--accent2)', color: 'var(--white)', transition: 'background 0.15s' },

  spinnerWrap: { textAlign: 'center', padding: '60px 0', fontFamily: 'var(--mono)', fontSize: '0.85rem', color: 'var(--muted)' },
}

// ── Spinner ───────────────────────────────────────────────────
function Spinner({ label }) {
  return <div style={s.spinnerWrap}>{label || 'Loading…'}</div>
}

// ── Main component ────────────────────────────────────────────
export default function POScreen({ onGenerated }) {
  const [loading, setLoading]       = useState(true)
  const [count, setCount]           = useState(0)
  const [medicines, setMedicines]   = useState([])
  const [generating, setGenerating] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res  = await fetch('/api/po/pending')
      const json = await res.json()
      setCount(json.count)
      setMedicines(json.medicines || [])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  function setMed(i, key, val) {
    setMedicines(ms => ms.map((m, idx) => idx === i ? { ...m, [key]: val } : m))
  }

  function removeMed(i) {
    setMedicines(ms => ms.filter((_, idx) => idx !== i))
  }

  function addMed() {
    setMedicines(ms => [...ms, { name: '', total_quantity: null, raw_dosages: [], patient_count: 0 }])
  }

  async function generate() {
    setGenerating(true)
    try {
      const payload = {
        medicines: medicines.map(m => ({
          ...m,
          total_quantity: m.total_quantity !== '' && m.total_quantity != null
            ? parseInt(m.total_quantity) : null,
        })),
      }
      const res = await fetch('/api/po/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.detail || 'Generation failed')
      }
      // Extract filename from Content-Disposition header
      const disposition = res.headers.get('content-disposition') || ''
      const match = disposition.match(/filename="?([^"]+)"?/)
      const filename = match ? match[1] : 'purchase_order.pdf'
      // Trigger download
      const blob = await res.blob()
      const url  = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = filename
      a.click()
      URL.revokeObjectURL(url)

      // Go back to dashboard
      onGenerated()
    } catch (e) {
      alert('Error: ' + e.message)
    } finally {
      setGenerating(false)
    }
  }

  const totalItems    = medicines.length
  const totalPatients = medicines.reduce((acc, m) => acc + (m.patient_count || 0), 0)

  if (loading) return <div style={s.container}><Spinner label="Fetching pending prescriptions…" /></div>

  return (
    <div style={s.container}>
      <div style={s.topBar}>
        <h2 style={s.title}>Purchase Orders</h2>
      </div>

      {/* Stat cards */}
      <div style={s.statRow}>
        <div style={s.statCard}>
          <div style={s.statLabel}>Pending Prescriptions</div>
          <div style={s.statValue}>{count}</div>
          <div style={s.statSub}>awaiting purchase order</div>
        </div>
        <div style={s.statCard}>
          <div style={s.statLabel}>Medicine Lines</div>
          <div style={s.statValue}>{totalItems}</div>
          <div style={s.statSub}>unique medicines</div>
        </div>
        <div style={s.statCard}>
          <div style={s.statLabel}>Patients</div>
          <div style={s.statValue}>{totalPatients}</div>
          <div style={s.statSub}>across all prescriptions</div>
        </div>
      </div>

      {count === 0 ? (
        <div style={s.emptyState}>
          <span style={s.emptyIcon}>◎</span>
          No pending prescriptions. All caught up.
        </div>
      ) : (
        <>
          {/* Medicines table */}
          <div style={s.section}>
            <div style={s.sectionHead}>
              Medicines to Order
              <span style={{ fontFamily: 'var(--mono)', fontSize: '0.7rem' }}>Click any field to edit</span>
            </div>
            <table style={s.table}>
              <thead>
                <tr>
                  <th style={s.th}>Name</th>
                  <th style={{ ...s.th, width: 90 }}>Total Qty</th>
                  <th style={s.th}>Dosages on Record</th>
                  <th style={{ ...s.th, width: 80 }}>Patients</th>
                  <th style={{ ...s.th, width: 48 }}></th>
                </tr>
              </thead>
              <tbody>
                {medicines.map((med, i) => (
                  <tr key={i}>
                    <td style={s.td}>
                      <input
                        style={s.tdInput}
                        value={med.name || ''}
                        placeholder="Medicine name"
                        onChange={e => setMed(i, 'name', e.target.value)}
                        onFocus={e => e.target.style.borderBottomColor = 'var(--ink)'}
                        onBlur={e => e.target.style.borderBottomColor = 'transparent'}
                      />
                    </td>
                    <td style={s.td}>
                      <input
                        style={{ ...s.tdInput, width: 70 }}
                        type="number"
                        value={med.total_quantity ?? ''}
                        placeholder="—"
                        min="0"
                        onChange={e => setMed(i, 'total_quantity', e.target.value)}
                        onFocus={e => e.target.style.borderBottomColor = 'var(--ink)'}
                        onBlur={e => e.target.style.borderBottomColor = 'transparent'}
                      />
                    </td>
                    <td style={s.td}>
                      <span style={s.dosages}>
                        {med.raw_dosages && med.raw_dosages.length > 0
                          ? [...new Set(med.raw_dosages)].join(', ')
                          : '—'}
                      </span>
                    </td>
                    <td style={s.td}>{med.patient_count || 0}</td>
                    <td style={s.tdLast}>
                      <button style={s.btnDanger} onClick={() => removeMed(i)}>✕</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div style={s.addRow}>
              <button style={s.btnAdd} onClick={addMed}>+ Add medicine</button>
            </div>
          </div>
        </>
      )}

      {/* Confirm bar */}
      <div style={s.confirmBar}>
        <span style={s.confirmNote}>
          {count > 0
            ? `${count} prescription${count > 1 ? 's' : ''} will be marked as ordered.`
            : 'No pending prescriptions.'}
        </span>
        <div style={s.btnRow}>
          <button style={s.btnRefresh} onClick={load}>↻ Refresh</button>
          <button
            style={{ ...s.btnGenerate, opacity: (count === 0 || generating) ? 0.4 : 1, cursor: (count === 0 || generating) ? 'not-allowed' : 'pointer' }}
            disabled={count === 0 || generating}
            onClick={generate}
          >
            {generating ? 'Generating…' : 'Generate & Download →'}
          </button>
        </div>
      </div>
    </div>
  )
}