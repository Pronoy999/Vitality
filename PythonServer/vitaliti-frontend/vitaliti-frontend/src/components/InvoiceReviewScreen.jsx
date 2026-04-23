import React, { useState } from 'react'
import { saveInvoice } from '../services/api'

const EMPTY_ITEM = {
  itemDescription: '',
  receivedQuantity: null,
  damagedQuantity: null,
  freeQuantity: null,
  itemPrice: null,
  hsnCode: '',
  expiryDate: '',
  manufacturedDate: '',
  batchNumber: '',
  taxPercentage: null,
  itemTotalPrice: null,
  mrp: null,
}

const s = {
  container: { maxWidth: 1100, margin: '0 auto', padding: '48px 24px 120px' },
  reviewHeader: { display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 32, flexWrap: 'wrap', gap: 12 },
  reviewTitle: { fontFamily: 'var(--serif)', fontSize: '2rem', animation: 'fadeUp 0.3s ease' },
  reviewHint: { fontSize: '0.78rem', fontFamily: 'var(--mono)', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em' },
  section: {
    background: 'var(--white)',
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    marginBottom: 20,
    overflow: 'hidden',
    boxShadow: 'var(--shadow)',
    animation: 'fadeUp 0.35s ease',
  },
  sectionHead: {
    padding: '12px 20px',
    background: 'var(--cream)',
    borderBottom: '1.5px solid var(--rule)',
    fontFamily: 'var(--mono)',
    fontSize: '0.72rem',
    textTransform: 'uppercase',
    letterSpacing: '0.1em',
    color: 'var(--muted)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  fieldsGrid: { display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))' },
  field: { padding: '14px 20px', borderRight: '1px solid var(--rule)', borderBottom: '1px solid var(--rule)' },
  fieldLabel: { fontFamily: 'var(--mono)', fontSize: '0.68rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--muted)', marginBottom: 5 },
  input: {
    width: '100%',
    border: 'none',
    background: 'transparent',
    fontFamily: 'var(--sans)',
    fontSize: '0.92rem',
    color: 'var(--ink)',
    outline: 'none',
    padding: '4px 0',
    borderBottom: '1.5px solid transparent',
  },
  checkboxLabel: { display: 'flex', alignItems: 'center', gap: 10, fontSize: '0.92rem', minHeight: 31 },
  checkbox: { width: 16, height: 16, accentColor: 'var(--accent2)' },
  tableWrap: { overflowX: 'auto' },
  table: { width: '100%', minWidth: 1280, borderCollapse: 'collapse' },
  th: { padding: '10px 12px', textAlign: 'left', fontFamily: 'var(--mono)', fontSize: '0.66rem', textTransform: 'uppercase', letterSpacing: '0.06em', color: 'var(--muted)', borderBottom: '1.5px solid var(--rule)', background: 'var(--cream)', whiteSpace: 'nowrap' },
  td: { padding: '10px 12px', borderBottom: '1px solid var(--rule)', verticalAlign: 'middle' },
  tdInput: { width: '100%', minWidth: 70, border: 'none', background: 'transparent', fontFamily: 'var(--sans)', fontSize: '0.86rem', color: 'var(--ink)', outline: 'none', padding: '3px 0', borderBottom: '1.5px solid transparent' },
  addRow: { padding: '12px 16px', borderTop: '1.5px solid var(--rule)' },
  btnAdd: { background: 'transparent', border: '1.5px dashed var(--rule)', color: 'var(--muted)', padding: '7px 16px', borderRadius: 'var(--radius)', fontSize: '0.82rem', fontFamily: 'var(--sans)', cursor: 'pointer', width: '100%', textAlign: 'center' },
  btnDanger: { background: 'transparent', color: 'var(--accent)', border: '1.5px solid var(--accent)', borderRadius: 'var(--radius)', padding: '5px 12px', fontSize: '0.78rem', cursor: 'pointer', fontFamily: 'var(--sans)' },
  confirmBar: { position: 'fixed', bottom: 0, left: 0, right: 0, background: 'var(--paper)', borderTop: '2px solid var(--ink)', padding: '16px 40px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, zIndex: 200 },
  confirmNote: { fontSize: '0.78rem', color: 'var(--muted)', fontFamily: 'var(--mono)' },
  btnRow: { display: 'flex', gap: 12, flexWrap: 'wrap' },
  btnSecondary: { display: 'inline-flex', alignItems: 'center', padding: '12px 24px', borderRadius: 'var(--radius)', border: '2px solid var(--ink)', fontFamily: 'var(--sans)', fontSize: '0.9rem', fontWeight: 500, cursor: 'pointer', background: 'transparent', color: 'var(--ink)' },
  btnConfirm: (disabled) => ({ display: 'inline-flex', alignItems: 'center', padding: '13px 32px', borderRadius: 'var(--radius)', border: '2px solid var(--accent2)', fontFamily: 'var(--sans)', fontSize: '0.95rem', fontWeight: 500, cursor: disabled ? 'not-allowed' : 'pointer', background: 'var(--accent2)', color: 'var(--white)', opacity: disabled ? 0.5 : 1 }),
}

function Field({ label, children }) {
  return (
    <div style={s.field}>
      <div style={s.fieldLabel}>{label}</div>
      {children}
    </div>
  )
}

function EditInput({ value, onChange, type = 'text', placeholder = '-' }) {
  const [focused, setFocused] = useState(false)
  return (
    <input
      type={type}
      style={{ ...s.input, borderBottomColor: focused ? 'var(--ink)' : 'transparent' }}
      value={value ?? ''}
      placeholder={placeholder}
      onChange={e => onChange(e.target.value)}
      onFocus={() => setFocused(true)}
      onBlur={() => setFocused(false)}
    />
  )
}

function toNumber(value) {
  return value !== '' && value != null ? Number(value) : null
}

function toInteger(value) {
  return value !== '' && value != null ? parseInt(value, 10) : null
}

function toDate(value) {
  return value || null
}

export default function InvoiceReviewScreen({ initialData, onConfirmed, onReset }) {
  const [data, setData] = useState(() => ({ invoiceItems: [], ...initialData }))
  const [saving, setSaving] = useState(false)

  function setField(key, val) {
    setData(d => ({ ...d, [key]: val }))
  }

  function setItem(i, key, val) {
    setData(d => ({
      ...d,
      invoiceItems: (d.invoiceItems || []).map((item, idx) => idx === i ? { ...item, [key]: val } : item),
    }))
  }

  function addItem() {
    setData(d => ({ ...d, invoiceItems: [...(d.invoiceItems || []), { ...EMPTY_ITEM }] }))
  }

  function removeItem(i) {
    setData(d => ({ ...d, invoiceItems: (d.invoiceItems || []).filter((_, idx) => idx !== i) }))
  }

  function buildPayload() {
    return {
      ...data,
      purchaseOrderId: toInteger(data.purchaseOrderId),
      supplierId: toInteger(data.supplierId),
      invoiceDate: toDate(data.invoiceDate),
      receivedDate: toDate(data.receivedDate),
      areItemsDelivered: Boolean(data.areItemsDelivered),
      itemTotalPrice: toNumber(data.itemTotalPrice),
      discountAmount: toNumber(data.discountAmount),
      logisticsAmount: toNumber(data.logisticsAmount),
      insuranceAmount: toNumber(data.insuranceAmount),
      roundOffAmount: toNumber(data.roundOffAmount),
      taxAmount: toNumber(data.taxAmount),
      totalPrice: toNumber(data.totalPrice),
      invoiceItems: (data.invoiceItems || []).map(item => ({
        ...item,
        receivedQuantity: toInteger(item.receivedQuantity),
        damagedQuantity: toInteger(item.damagedQuantity),
        freeQuantity: toInteger(item.freeQuantity),
        itemPrice: toNumber(item.itemPrice),
        expiryDate: toDate(item.expiryDate),
        manufacturedDate: toDate(item.manufacturedDate),
        taxPercentage: toNumber(item.taxPercentage),
        itemTotalPrice: toNumber(item.itemTotalPrice),
        mrp: toNumber(item.mrp),
      })),
    }
  }

  async function confirm() {
    setSaving(true)
    try {
      const result = await saveInvoice(buildPayload())
      onConfirmed(result.invoiceId)
    } catch (e) {
      alert('Error: ' + e.message)
      setSaving(false)
    }
  }

  const items = data.invoiceItems || []

  return (
    <div style={s.container}>
      <div style={s.reviewHeader}>
        <h2 style={s.reviewTitle}>Review Invoice</h2>
        <span style={s.reviewHint}>Click any field to edit</span>
      </div>

      <div style={s.section}>
        <div style={s.sectionHead}>Invoice Details</div>
        <div style={s.fieldsGrid}>
          <Field label="Invoice Number">
            <EditInput value={data.invoiceNumber} onChange={v => setField('invoiceNumber', v)} />
          </Field>
          <Field label="Purchase Order ID">
            <EditInput type="number" value={data.purchaseOrderId} onChange={v => setField('purchaseOrderId', v)} />
          </Field>
          <Field label="Invoice Date">
            <EditInput type="date" value={data.invoiceDate} onChange={v => setField('invoiceDate', v)} />
          </Field>
          <Field label="Supplier Name">
            <EditInput value={data.supplierName} onChange={v => setField('supplierName', v)} />
          </Field>
          <Field label="Supplier ID">
            <EditInput type="number" value={data.supplierId} onChange={v => setField('supplierId', v)} />
          </Field>
          <Field label="Received Date">
            <EditInput type="date" value={data.receivedDate} onChange={v => setField('receivedDate', v)} />
          </Field>
          <Field label="Items Delivered">
            <label style={s.checkboxLabel}>
              <input style={s.checkbox} type="checkbox" checked={Boolean(data.areItemsDelivered)} onChange={e => setField('areItemsDelivered', e.target.checked)} />
              Delivered or received
            </label>
          </Field>
        </div>
      </div>

      <div style={s.section}>
        <div style={s.sectionHead}>Amounts</div>
        <div style={s.fieldsGrid}>
          <Field label="Item Total">
            <EditInput type="number" value={data.itemTotalPrice} onChange={v => setField('itemTotalPrice', v)} />
          </Field>
          <Field label="Discount">
            <EditInput type="number" value={data.discountAmount} onChange={v => setField('discountAmount', v)} />
          </Field>
          <Field label="Tax">
            <EditInput type="number" value={data.taxAmount} onChange={v => setField('taxAmount', v)} />
          </Field>
          <Field label="Logistics">
            <EditInput type="number" value={data.logisticsAmount} onChange={v => setField('logisticsAmount', v)} />
          </Field>
          <Field label="Insurance">
            <EditInput type="number" value={data.insuranceAmount} onChange={v => setField('insuranceAmount', v)} />
          </Field>
          <Field label="Round Off">
            <EditInput type="number" value={data.roundOffAmount} onChange={v => setField('roundOffAmount', v)} />
          </Field>
          <Field label="Total Price">
            <EditInput type="number" value={data.totalPrice} onChange={v => setField('totalPrice', v)} />
          </Field>
        </div>
      </div>

      <div style={s.section}>
        <div style={s.sectionHead}>Invoice Items</div>
        <div style={s.tableWrap}>
          <table style={s.table}>
            <thead>
              <tr>
                <th style={s.th}>Description</th>
                <th style={s.th}>Received</th>
                <th style={s.th}>Damaged</th>
                <th style={s.th}>Free</th>
                <th style={s.th}>Price</th>
                <th style={s.th}>HSN</th>
                <th style={s.th}>Expiry</th>
                <th style={s.th}>Mfg Date</th>
                <th style={s.th}>Batch</th>
                <th style={s.th}>Tax %</th>
                <th style={s.th}>Line Total</th>
                <th style={s.th}>MRP</th>
                <th style={{ ...s.th, width: 70 }}></th>
              </tr>
            </thead>
            <tbody>
              {items.map((item, i) => (
                <tr key={i}>
                  <td style={s.td}><input style={{ ...s.tdInput, minWidth: 180 }} value={item.itemDescription || ''} onChange={e => setItem(i, 'itemDescription', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} type="number" value={item.receivedQuantity ?? ''} onChange={e => setItem(i, 'receivedQuantity', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} type="number" value={item.damagedQuantity ?? ''} onChange={e => setItem(i, 'damagedQuantity', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} type="number" value={item.freeQuantity ?? ''} onChange={e => setItem(i, 'freeQuantity', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} type="number" value={item.itemPrice ?? ''} onChange={e => setItem(i, 'itemPrice', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} value={item.hsnCode || ''} onChange={e => setItem(i, 'hsnCode', e.target.value)} /></td>
                  <td style={s.td}><input style={{ ...s.tdInput, minWidth: 120 }} type="date" value={item.expiryDate || ''} onChange={e => setItem(i, 'expiryDate', e.target.value)} /></td>
                  <td style={s.td}><input style={{ ...s.tdInput, minWidth: 120 }} type="date" value={item.manufacturedDate || ''} onChange={e => setItem(i, 'manufacturedDate', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} value={item.batchNumber || ''} onChange={e => setItem(i, 'batchNumber', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} type="number" value={item.taxPercentage ?? ''} onChange={e => setItem(i, 'taxPercentage', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} type="number" value={item.itemTotalPrice ?? ''} onChange={e => setItem(i, 'itemTotalPrice', e.target.value)} /></td>
                  <td style={s.td}><input style={s.tdInput} type="number" value={item.mrp ?? ''} onChange={e => setItem(i, 'mrp', e.target.value)} /></td>
                  <td style={s.td}><button style={s.btnDanger} onClick={() => removeItem(i)}>x</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div style={s.addRow}>
          <button style={s.btnAdd} onClick={addItem}>+ Add invoice item</button>
        </div>
      </div>

      <div style={s.confirmBar}>
        <span style={s.confirmNote}>All edits are local until you save the invoice.</span>
        <div style={s.btnRow}>
          <button style={s.btnSecondary} onClick={onReset}>Start over</button>
          <button style={s.btnConfirm(saving)} disabled={saving} onClick={confirm}>
            {saving ? 'Saving...' : 'Save Invoice ->'}
          </button>
        </div>
      </div>
    </div>
  )
}
