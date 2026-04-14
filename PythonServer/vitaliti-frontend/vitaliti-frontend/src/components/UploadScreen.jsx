import React, { useRef, useState } from 'react'
import { uploadPrescription } from '../services/api'

const SUPPORTED = ['image/jpeg', 'image/png', 'image/webp', 'image/heic']

const EMPTY_PRESCRIPTION = {
  patient_name:   null,
  patient_age:    null,
  doctor_name:    null,
  date:           null,
  patient_issue:  null,
  diagnosis:      null,
  health_metrics: {},
  medicines:      [],
}

const s = {
  container: { maxWidth: 860, margin: '0 auto', padding: '48px 24px 80px' },
  title: { fontFamily: 'var(--serif)', fontSize: '2.4rem', lineHeight: 1.15, marginBottom: 8, animation: 'fadeUp 0.4s ease' },
  sub: { color: 'var(--muted)', fontSize: '0.9rem', marginBottom: 36, fontWeight: 300, animation: 'fadeUp 0.4s ease 0.05s both' },
  dropZone: (dragging) => ({
    border: `2px dashed ${dragging ? 'var(--ink)' : 'var(--rule)'}`,
    borderRadius: 'var(--radius)',
    padding: '60px 32px',
    textAlign: 'center',
    cursor: 'pointer',
    background: dragging ? 'rgba(255,255,255,0.6)' : 'var(--cream)',
    transition: 'border-color 0.2s, background 0.2s',
    position: 'relative',
    animation: 'fadeUp 0.4s ease 0.1s both',
  }),
  dropIcon: { fontSize: '2.8rem', marginBottom: 12, display: 'block', lineHeight: 1 },
  dropLabel: { fontSize: '1rem', fontWeight: 500, marginBottom: 6 },
  dropHint: { fontSize: '0.8rem', color: 'var(--muted)', fontFamily: 'var(--mono)' },
  previewGrid: { display: 'flex', flexWrap: 'wrap', gap: 12, marginTop: 20 },
  thumb: { width: 80, height: 80, objectFit: 'cover', borderRadius: 'var(--radius)', border: '1.5px solid var(--rule)' },
  btnRow: { display: 'flex', alignItems: 'center', gap: 16, marginTop: 24, flexWrap: 'wrap' },
  btn: (disabled) => ({
    display: 'inline-flex',
    alignItems: 'center',
    gap: 8,
    padding: '12px 28px',
    borderRadius: 'var(--radius)',
    border: '2px solid var(--ink)',
    fontFamily: 'var(--sans)',
    fontSize: '0.9rem',
    fontWeight: 500,
    cursor: disabled ? 'not-allowed' : 'pointer',
    background: 'var(--ink)',
    color: 'var(--white)',
    opacity: disabled ? 0.4 : 1,
    transition: 'background 0.15s, transform 0.1s',
  }),
  divider: {
    fontFamily: 'var(--mono)',
    fontSize: '0.75rem',
    color: 'var(--muted)',
    textTransform: 'uppercase',
    letterSpacing: '0.06em',
  },
  btnManual: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 8,
    padding: '12px 24px',
    borderRadius: 'var(--radius)',
    border: '2px solid var(--rule)',
    fontFamily: 'var(--sans)',
    fontSize: '0.9rem',
    fontWeight: 400,
    cursor: 'pointer',
    background: 'transparent',
    color: 'var(--muted)',
    transition: 'border-color 0.15s, color 0.15s',
  },
}

export default function UploadScreen({ onUpload, onManual }) {
  const [files, setFiles]     = useState([])
  const [dragging, setDragging] = useState(false)
  const [loading, setLoading] = useState(false)
  const inputRef = useRef()

  function handleFiles(incoming) {
    const valid = incoming.filter(f => SUPPORTED.includes(f.type) || f.name.match(/\.(heic|jpg|jpeg|png|webp)$/i))
    setFiles(valid)
  }

  function onDrop(e) {
    e.preventDefault()
    setDragging(false)
    handleFiles(Array.from(e.dataTransfer.files))
  }

  async function submit() {
    if (!files.length || loading) return
    setLoading(true)
    try {
      const result = await uploadPrescription(files)
      onUpload(result.job_id)
    } catch (e) {
      alert('Upload failed: ' + e.message)
      setLoading(false)
    }
  }

  return (
    <div style={s.container}>
      <h1 style={s.title}>Drop a prescription.<br /><em>We'll handle the rest.</em></h1>
      <p style={s.sub}>Upload one or more images — Gemini will parse and extract all fields for your review.</p>

      <div
        style={s.dropZone(dragging)}
        onClick={() => inputRef.current.click()}
        onDragOver={e => { e.preventDefault(); setDragging(true) }}
        onDragLeave={() => setDragging(false)}
        onDrop={onDrop}
      >
        <span style={s.dropIcon}>⌗</span>
        <p style={s.dropLabel}>Drop images here, or click to browse</p>
        <p style={s.dropHint}>JPG · PNG · WEBP · HEIC — multiple pages supported</p>
        <input
          ref={inputRef}
          type="file"
          multiple
          accept="image/*"
          style={{ display: 'none' }}
          onChange={e => handleFiles(Array.from(e.target.files))}
        />
      </div>

      {files.length > 0 && (
        <div style={s.previewGrid}>
          {files.map((f, i) => (
            <img key={i} src={URL.createObjectURL(f)} style={s.thumb} alt={f.name} />
          ))}
        </div>
      )}

      <div style={s.btnRow}>
        <button
          style={s.btn(!files.length || loading)}
          disabled={!files.length || loading}
          onClick={submit}
        >
          {loading ? 'Uploading…' : 'Parse Prescription →'}
        </button>

        <span style={s.divider}>or</span>

        <button style={s.btnManual} onClick={() => onManual(EMPTY_PRESCRIPTION)}>
          Enter manually
        </button>
      </div>
    </div>
  )
}
