import React, { useState } from 'react'

// ── Styles ────────────────────────────────────────────────────
const s = {
  container:  { maxWidth: 860, margin: '0 auto', padding: '48px 24px 120px' },
  reviewHeader: {
    display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
    marginBottom: 32, flexWrap: 'wrap', gap: 12,
  },
  reviewTitle: { fontFamily: 'var(--serif)', fontSize: '2rem', animation: 'fadeUp 0.3s ease' },
  reviewHint:  { fontSize: '0.78rem', fontFamily: 'var(--mono)', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em' },
  section: {
    background: 'var(--white)', border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)', marginBottom: 20,
    overflow: 'hidden', boxShadow: 'var(--shadow)',
    animation: 'fadeUp 0.35s ease',
  },
  sectionHead: {
    padding: '12px 20px', background: 'var(--cream)',
    borderBottom: '1.5px solid var(--rule)',
    fontFamily: 'var(--mono)', fontSize: '0.72rem',
    textTransform: 'uppercase', letterSpacing: '0.1em', color: 'var(--muted)',
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
  },
  fieldsGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr' },
  field: (full) => ({
    padding: '14px 20px',
    borderRight: full ? 'none' : '1px solid var(--rule)',
    borderBottom: '1px solid var(--rule)',
    gridColumn: full ? '1 / -1' : undefined,
  }),
  fieldLabel: { fontFamily: 'var(--mono)', fontSize: '0.68rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--muted)', marginBottom: 5 },
  input: {
    width: '100%', border: 'none', background: 'transparent',
    fontFamily: 'var(--sans)', fontSize: '0.92rem', color: 'var(--ink)',
    outline: 'none', padding: '4px 0',
    borderBottom: '1.5px solid transparent',
    transition: 'border-color 0.2s',
  },
  textarea: {
    width: '100%', border: 'none', background: 'transparent',
    fontFamily: 'var(--sans)', fontSize: '0.92rem', color: 'var(--ink)',
    outline: 'none', padding: '4px 0', resize: 'none',
    borderBottom: '1.5px solid transparent',
    transition: 'border-color 0.2s',
  },
  metricsList:  { padding: '16px 20px', display: 'flex', flexWrap: 'wrap', gap: 10 },
  metricChip:   { display: 'flex', alignItems: 'center', border: '1.5px solid var(--rule)', borderRadius: 20, overflow: 'hidden', fontSize: '0.82rem' },
  metricKey:    { padding: '5px 10px', background: 'var(--cream)', fontFamily: 'var(--mono)', fontSize: '0.72rem', color: 'var(--muted)', borderRight: '1.5px solid var(--rule)', border: 'none', outline: 'none', width: 70 },
  metricVal:    { padding: '5px 10px', border: 'none', background: 'transparent', fontFamily: 'var(--mono)', fontSize: '0.82rem', color: 'var(--ink)', width: 80, outline: 'none' },
  metricDel:    { border: 'none', background: 'none', cursor: 'pointer', padding: '0 8px', color: 'var(--muted)', fontSize: '0.9rem', lineHeight: 1 },
  table:        { width: '100%', borderCollapse: 'collapse' },
  th:           { padding: '10px 16px', textAlign: 'left', fontFamily: 'var(--mono)', fontSize: '0.68rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--muted)', borderBottom: '1.5px solid var(--rule)', background: 'var(--cream)' },
  td:           { padding: '10px 16px', borderBottom: '1px solid var(--rule)', verticalAlign: 'middle' },
  tdInput:      { width: '100%', border: 'none', background: 'transparent', fontFamily: 'var(--sans)', fontSize: '0.88rem', color: 'var(--ink)', outline: 'none', padding: '3px 0', borderBottom: '1.5px solid transparent', transition: 'border-color 0.2s' },
  addRow:       { padding: '12px 16px', borderTop: '1.5px solid var(--rule)' },
  btnAdd:       { background: 'transparent', border: '1.5px dashed var(--rule)', color: 'var(--muted)', padding: '7px 16px', borderRadius: 'var(--radius)', fontSize: '0.82rem', fontFamily: 'var(--sans)', cursor: 'pointer', width: '100%', textAlign: 'center', transition: 'border-color 0.2s, color 0.2s' },
  btnSmallAdd:  { background: 'transparent', border: '1.5px dashed var(--rule)', color: 'var(--muted)', padding: '4px 14px', borderRadius: 'var(--radius)', fontSize: '0.72rem', fontFamily: 'var(--sans)', cursor: 'pointer', transition: 'border-color 0.2s, color 0.2s' },
  btnDanger:    { background: 'transparent', color: 'var(--accent)', border: '1.5px solid var(--accent)', borderRadius: 'var(--radius)', padding: '5px 12px', fontSize: '0.78rem', cursor: 'pointer', fontFamily: 'var(--sans)', transition: 'background 0.15s, color 0.15s' },
  confirmBar: {
    position: 'fixed', bottom: 0, left: 0, right: 0,
    background: 'var(--paper)', borderTop: '2px solid var(--ink)',
    padding: '16px 40px', display: 'flex', alignItems: 'center',
    justifyContent: 'space-between', gap: 16, zIndex: 200,
  },
  confirmNote: { fontSize: '0.78rem', color: 'var(--muted)', fontFamily: 'var(--mono)' },
  btnRow:      { display: 'flex', gap: 12, flexWrap: 'wrap' },
  btnSecondary: { display: 'inline-flex', alignItems: 'center', padding: '12px 24px', borderRadius: 'var(--radius)', border: '2px solid var(--ink)', fontFamily: 'var(--sans)', fontSize: '0.9rem', fontWeight: 500, cursor: 'pointer', background: 'transparent', color: 'var(--ink)', transition: 'background 0.15s' },
  btnConfirm:   { display: 'inline-flex', alignItems: 'center', padding: '13px 32px', borderRadius: 'var(--radius)', border: '2px solid var(--accent2)', fontFamily: 'var(--sans)', fontSize: '0.95rem', fontWeight: 500, cursor: 'pointer', background: 'var(--accent2)', color: 'var(--white)', transition: 'background 0.15s' },
}

// ── Field component ───────────────────────────────────────────
function Field({ label, full, children }) {
  return (
    <div style={s.field(full)}>
      <div style={s.fieldLabel}>{label}</div>
      {children}
    </div>
  )
}

function EditInput({ value, onChange, type = 'text', placeholder = '—', rows }) {
  const [focused, setFocused] = useState(false)
  const focusStyle = { borderBottomColor: focused ? 'var(--ink)' : 'transparent' }
  if (rows) return (
    <textarea rows={rows} style={{ ...s.textarea, ...focusStyle }} value={value || ''} placeholder={placeholder}
      onChange={e => onChange(e.target.value)} onFocus={() => setFocused(true)} onBlur={() => setFocused(false)} />
  )
  return (
    <input type={type} style={{ ...s.input, ...focusStyle }} value={value ?? ''} placeholder={placeholder}
      onChange={e => onChange(e.target.value)} onFocus={() => setFocused(true)} onBlur={() => setFocused(false)} />
  )
}

// ── Main component ────────────────────────────────────────────
export default function ReviewScreen({ jobId, initialData, onConfirmed, onReset }) {
  const [data, setData]     = useState(initialData)
  const [saving, setSaving] = useState(false)

  function setField(key, val) {
    setData(d => ({ ...d, [key]: val }))
  }

  // ── Metrics ──
  function setMetricKey(i, val) {
    const m = [...Object.entries(data.health_metrics || {})]
    m[i] = [val, m[i][1]]
    setData(d => ({ ...d, health_metrics: Object.fromEntries(m) }))
  }
  function setMetricVal(i, val) {
    const m = [...Object.entries(data.health_metrics || {})]
    m[i] = [m[i][0], val]
    setData(d => ({ ...d, health_metrics: Object.fromEntries(m) }))
  }
  function removeMetric(i) {
    const m = [...Object.entries(data.health_metrics || {})]
    m.splice(i, 1)
    setData(d => ({ ...d, health_metrics: Object.fromEntries(m) }))
  }
  function addMetric() {
    setData(d => ({ ...d, health_metrics: { ...(d.health_metrics || {}), '': '' } }))
  }

  // ── Medicines ──
  function setMed(i, key, val) {
    const meds = data.medicines.map((m, idx) => idx === i ? { ...m, [key]: val } : m)
    setData(d => ({ ...d, medicines: meds }))
  }
  function removeMed(i) {
    setData(d => ({ ...d, medicines: d.medicines.filter((_, idx) => idx !== i) }))
  }
  function addMed() {
    setData(d => ({ ...d, medicines: [...(d.medicines || []), { name: '', dosage: '', quantity: null }] }))
  }

  // ── Confirm ──
  async function confirm() {
    setSaving(true)
    try {
      const payload = {
        ...data,
        patient_age: data.patient_age !== '' && data.patient_age != null ? parseInt(data.patient_age) : null,
        medicines: (data.medicines || []).map(m => ({
          ...m,
          quantity: m.quantity !== '' && m.quantity != null ? parseInt(m.quantity) : null,
        })),
      }

      // jobId is null for manual entries — use the direct save endpoint
      const url = jobId ? `/api/confirm/${jobId}` : '/api/prescriptions'
      const res  = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ data: payload }),
      })
      const json = await res.json()
      if (res.ok) onConfirmed(json.prescription_id)
      else { alert('Error: ' + (json.detail || 'Unknown error')); setSaving(false) }
    } catch (e) {
      alert('Error: ' + e.message)
      setSaving(false)
    }
  }

  const metrics  = Object.entries(data.health_metrics || {})
  const medicines = data.medicines || []

  return (
    <div style={s.container}>
      <div style={s.reviewHeader}>
        <h2 style={s.reviewTitle}>Review &amp; Confirm</h2>
        <span style={s.reviewHint}>Click any field to edit</span>
      </div>

      {/* Patient & Doctor */}
      <div style={s.section}>
        <div style={s.sectionHead}>Patient &amp; Doctor</div>
        <div style={s.fieldsGrid}>
          <Field label="Patient Name">
            <EditInput value={data.patient_name} onChange={v => setField('patient_name', v)} />
          </Field>
          <Field label="Patient Age">
            <EditInput value={data.patient_age} onChange={v => setField('patient_age', v)} type="number" />
          </Field>
          <Field label="Doctor Name">
            <EditInput value={data.doctor_name} onChange={v => setField('doctor_name', v)} />
          </Field>
          <Field label="Date">
            <EditInput value={data.date} onChange={v => setField('date', v)} />
          </Field>
        </div>
      </div>

      {/* Clinical */}
      <div style={s.section}>
        <div style={s.sectionHead}>Clinical Details</div>
        <div style={s.fieldsGrid}>
          <Field label="Patient Issue / Complaint" full>
            <EditInput value={data.patient_issue} onChange={v => setField('patient_issue', v)} rows={2} />
          </Field>
          <Field label="Diagnosis" full>
            <EditInput value={data.diagnosis} onChange={v => setField('diagnosis', v)} rows={2} />
          </Field>
        </div>
      </div>

      {/* Health Metrics */}
      <div style={s.section}>
        <div style={s.sectionHead}>
          Health Metrics
          <button style={s.btnSmallAdd} onClick={addMetric}>+ Add</button>
        </div>
        <div style={s.metricsList}>
          {metrics.map(([k, v], i) => (
            <div key={i} style={s.metricChip}>
              <input style={s.metricKey} value={k} placeholder="Key"
                onChange={e => setMetricKey(i, e.target.value)} />
              <input style={s.metricVal} value={v} placeholder="Value"
                onChange={e => setMetricVal(i, e.target.value)} />
              <button style={s.metricDel} onClick={() => removeMetric(i)}>×</button>
            </div>
          ))}
          {metrics.length === 0 && (
            <span style={{ fontSize: '0.82rem', color: 'var(--muted)', fontFamily: 'var(--mono)' }}>No metrics recorded</span>
          )}
        </div>
      </div>

      {/* Medicines */}
      <div style={s.section}>
        <div style={s.sectionHead}>Medicines</div>
        <table style={s.table}>
          <thead>
            <tr>
              <th style={s.th}>Name</th>
              <th style={s.th}>Dosage</th>
              <th style={{ ...s.th, width: 80 }}>Qty</th>
              <th style={{ ...s.th, width: 48 }}></th>
            </tr>
          </thead>
          <tbody>
            {medicines.map((med, i) => (
              <tr key={i}>
                <td style={s.td}>
                  <input style={s.tdInput} value={med.name || ''} placeholder="Medicine name"
                    onChange={e => setMed(i, 'name', e.target.value)}
                    onFocus={e => e.target.style.borderBottomColor = 'var(--ink)'}
                    onBlur={e => e.target.style.borderBottomColor = 'transparent'} />
                </td>
                <td style={s.td}>
                  <input style={s.tdInput} value={med.dosage || ''} placeholder="Dosage / frequency"
                    onChange={e => setMed(i, 'dosage', e.target.value)}
                    onFocus={e => e.target.style.borderBottomColor = 'var(--ink)'}
                    onBlur={e => e.target.style.borderBottomColor = 'transparent'} />
                </td>
                <td style={s.td}>
                  <input style={{ ...s.tdInput, width: 60 }} type="number" value={med.quantity ?? ''} placeholder="—" min="0"
                    onChange={e => setMed(i, 'quantity', e.target.value)}
                    onFocus={e => e.target.style.borderBottomColor = 'var(--ink)'}
                    onBlur={e => e.target.style.borderBottomColor = 'transparent'} />
                </td>
                <td style={s.td}>
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

      {/* Confirm bar */}
      <div style={s.confirmBar}>
        <span style={s.confirmNote}>All edits are local until you confirm.</span>
        <div style={s.btnRow}>
          <button style={s.btnSecondary} onClick={onReset}>← Start over</button>
          <button style={s.btnConfirm} disabled={saving} onClick={confirm}>
            {saving ? 'Saving…' : 'Confirm & Save →'}
          </button>
        </div>
      </div>
    </div>
  )
}