import React, { useRef, useState } from 'react'
import { uploadInvoice } from '../services/api'

const SUPPORTED = ['image/jpeg', 'image/png', 'image/webp', 'image/heic']

export const EMPTY_INVOICE = {
  purchaseOrderId: null,
  invoiceNumber: '',
  supplierName: '',
  supplierId: null,
  invoiceDate: '',
  receivedDate: '',
  areItemsDelivered: false,
  itemTotalPrice: null,
  discountAmount: null,
  logisticsAmount: null,
  insuranceAmount: null,
  roundOffAmount: null,
  taxAmount: null,
  totalPrice: null,
  invoiceItems: [],
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
  }),
  divider: { fontFamily: 'var(--mono)', fontSize: '0.75rem', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.06em' },
  btnManual: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 8,
    padding: '12px 24px',
    borderRadius: 'var(--radius)',
    border: '2px solid var(--rule)',
    fontFamily: 'var(--sans)',
    fontSize: '0.9rem',
    cursor: 'pointer',
    background: 'transparent',
    color: 'var(--muted)',
  },
}

export default function InvoiceUploadScreen({ onUpload, onManual }) {
  const [files, setFiles] = useState([])
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
      const result = await uploadInvoice(files)
      onUpload(result.job_id)
    } catch (e) {
      alert('Upload failed: ' + e.message)
      setLoading(false)
    }
  }

  return (
    <div style={s.container}>
      <h1 style={s.title}>Upload an invoice.<br /><em>Review before saving.</em></h1>
      <p style={s.sub}>Upload one or more invoice images. Gemini will extract supplier, totals, and line items for review.</p>

      <div
        style={s.dropZone(dragging)}
        onClick={() => inputRef.current.click()}
        onDragOver={e => { e.preventDefault(); setDragging(true) }}
        onDragLeave={() => setDragging(false)}
        onDrop={onDrop}
      >
        <span style={s.dropIcon}>+</span>
        <p style={s.dropLabel}>Drop invoice images here, or click to browse</p>
        <p style={s.dropHint}>JPG / PNG / WEBP / HEIC - multiple pages supported</p>
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
        <button style={s.btn(!files.length || loading)} disabled={!files.length || loading} onClick={submit}>
          {loading ? 'Uploading...' : 'Parse Invoice ->'}
        </button>
        <span style={s.divider}>or</span>
        <button style={s.btnManual} onClick={() => onManual(EMPTY_INVOICE)}>
          Enter manually
        </button>
      </div>
    </div>
  )
}
