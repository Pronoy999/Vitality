import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { createOrder, downloadOrderInvoice, getInventory } from '../services/api'

const s = {
  container: { maxWidth: 1480, margin: '0 auto', padding: '48px 24px 120px' },
  topBar: {
    display: 'flex',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    marginBottom: 24,
    gap: 12,
    flexWrap: 'wrap',
  },
  title: { fontFamily: 'var(--serif)', fontSize: '2rem', animation: 'fadeUp 0.3s ease' },
  hint: {
    fontSize: '0.78rem',
    fontFamily: 'var(--mono)',
    color: 'var(--muted)',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
  },
  statusBanner: (tone) => ({
    marginBottom: 20,
    padding: '14px 18px',
    borderRadius: 'var(--radius)',
    border: `1.5px solid ${tone === 'error' ? 'var(--accent)' : 'var(--accent2)'}`,
    background: tone === 'error' ? '#faece9' : '#edf7f1',
    color: tone === 'error' ? 'var(--accent)' : 'var(--accent2)',
    boxShadow: 'var(--shadow)',
    fontSize: '0.9rem',
  }),
  statRow: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(170px, 1fr))',
    gap: 16,
    marginBottom: 20,
    animation: 'fadeUp 0.35s ease 0.05s both',
  },
  statCard: {
    background: 'var(--white)',
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    padding: '16px 18px',
    boxShadow: 'var(--shadow)',
  },
  statLabel: {
    fontFamily: 'var(--mono)',
    fontSize: '0.68rem',
    textTransform: 'uppercase',
    letterSpacing: '0.08em',
    color: 'var(--muted)',
    marginBottom: 6,
  },
  statValue: { fontFamily: 'var(--serif)', fontSize: '2rem', lineHeight: 1.1 },
  statSub: { fontSize: '0.78rem', color: 'var(--muted)', marginTop: 4 },
  detailLayout: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1.2fr) minmax(320px, 0.8fr)',
    gap: 20,
    alignItems: 'start',
    marginTop: 20,
  },
  section: {
    background: 'var(--white)',
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    overflow: 'hidden',
    boxShadow: 'var(--shadow)',
    animation: 'fadeUp 0.35s ease 0.1s both',
  },
  sectionHead: {
    padding: '12px 18px',
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
    gap: 12,
    flexWrap: 'wrap',
  },
  toolbar: {
    padding: '16px 18px',
    borderBottom: '1px solid var(--rule)',
    display: 'flex',
    gap: 12,
    flexWrap: 'wrap',
    alignItems: 'center',
  },
  search: {
    flex: '1 1 260px',
    minWidth: 0,
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    background: 'var(--paper)',
    padding: '11px 14px',
    fontFamily: 'var(--sans)',
    fontSize: '0.92rem',
    outline: 'none',
  },
  tableWrap: { overflowX: 'auto' },
  table: { width: '100%', borderCollapse: 'collapse', minWidth: 1120 },
  th: {
    padding: '10px 14px',
    textAlign: 'left',
    fontFamily: 'var(--mono)',
    fontSize: '0.68rem',
    textTransform: 'uppercase',
    letterSpacing: '0.08em',
    color: 'var(--muted)',
    borderBottom: '1.5px solid var(--rule)',
    background: 'var(--cream)',
  },
  td: {
    padding: '12px 14px',
    borderBottom: '1px solid var(--rule)',
    verticalAlign: 'middle',
    fontSize: '0.9rem',
  },
  itemTitle: { fontWeight: 500, marginBottom: 4 },
  itemMeta: { fontSize: '0.76rem', color: 'var(--muted)', lineHeight: 1.45 },
  qtyControl: {
    display: 'inline-flex',
    alignItems: 'center',
    border: '1.5px solid var(--rule)',
    borderRadius: '999px',
    overflow: 'hidden',
    background: 'var(--paper)',
  },
  qtyButton: {
    width: 32,
    height: 32,
    border: 'none',
    background: 'transparent',
    cursor: 'pointer',
    fontSize: '1rem',
    color: 'var(--ink)',
  },
  qtyValue: {
    minWidth: 42,
    textAlign: 'center',
    fontFamily: 'var(--mono)',
    fontSize: '0.88rem',
    padding: '0 8px',
  },
  markupInput: {
    width: 88,
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    padding: '8px 10px',
    fontFamily: 'var(--sans)',
    fontSize: '0.88rem',
    background: 'var(--paper)',
    outline: 'none',
  },
  muted: { color: 'var(--muted)', fontSize: '0.8rem' },
  empty: {
    padding: '56px 24px',
    textAlign: 'center',
    color: 'var(--muted)',
    fontFamily: 'var(--mono)',
    fontSize: '0.84rem',
  },
  sideBody: { padding: 18, display: 'grid', gap: 18 },
  fieldsGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 },
  fieldLabel: {
    fontFamily: 'var(--mono)',
    fontSize: '0.68rem',
    textTransform: 'uppercase',
    letterSpacing: '0.08em',
    color: 'var(--muted)',
    marginBottom: 6,
  },
  input: {
    width: '100%',
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    background: 'var(--paper)',
    padding: '11px 12px',
    fontFamily: 'var(--sans)',
    fontSize: '0.92rem',
    outline: 'none',
  },
  summaryList: { display: 'grid', gap: 12 },
  summaryRow: {
    display: 'flex',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    gap: 16,
    fontSize: '0.92rem',
  },
  summaryLabel: { color: 'var(--muted)' },
  summaryValue: { fontFamily: 'var(--mono)' },
  lineItems: {
    display: 'grid',
    gap: 10,
    maxHeight: 240,
    overflowY: 'auto',
    paddingRight: 4,
  },
  lineItem: {
    border: '1px solid var(--rule)',
    borderRadius: 'var(--radius)',
    padding: '10px 12px',
    background: '#fbf8f2',
  },
  lineHeader: {
    display: 'flex',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    gap: 12,
    marginBottom: 4,
  },
  lineName: { fontWeight: 500, fontSize: '0.9rem' },
  lineMeta: { fontSize: '0.76rem', color: 'var(--muted)' },
  createdOrders: {
    display: 'grid',
    gap: 12,
  },
  createdOrderCard: {
    border: '1px solid var(--rule)',
    borderRadius: 'var(--radius)',
    padding: '14px 16px',
    background: '#fbf8f2',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 16,
    flexWrap: 'wrap',
  },
  createdOrderMeta: {
    display: 'grid',
    gap: 4,
  },
  createdOrderId: {
    fontFamily: 'var(--mono)',
    fontSize: '0.95rem',
    color: 'var(--ink)',
  },
  createdOrderSub: {
    fontSize: '0.78rem',
    color: 'var(--muted)',
  },
  btnRow: { display: 'flex', gap: 12, flexWrap: 'wrap' },
  btnSecondary: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '11px 18px',
    borderRadius: 'var(--radius)',
    border: '2px solid var(--ink)',
    fontFamily: 'var(--sans)',
    fontSize: '0.9rem',
    cursor: 'pointer',
    background: 'transparent',
    color: 'var(--ink)',
  },
  btnPrimary: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '12px 22px',
    borderRadius: 'var(--radius)',
    border: '2px solid var(--accent2)',
    fontFamily: 'var(--sans)',
    fontSize: '0.92rem',
    fontWeight: 500,
    cursor: 'pointer',
    background: 'var(--accent2)',
    color: 'var(--white)',
  },
  spinnerWrap: {
    textAlign: 'center',
    padding: '60px 0',
    fontFamily: 'var(--mono)',
    fontSize: '0.85rem',
    color: 'var(--muted)',
  },
}

function Spinner({ label }) {
  return <div style={s.spinnerWrap}>{label || 'Loading...'}</div>
}

function formatCurrency(value) {
  const amount = Number(value || 0)
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(amount)
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename
  anchor.click()
  URL.revokeObjectURL(url)
}

function parseDecimal(value) {
  if (value === '' || value == null) {
    return 0
  }
  const parsed = Number.parseFloat(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function parseMarkup(value) {
  if (value === '' || value == null) {
    return 0
  }
  const parsed = Number.parseFloat(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function getAvailableQuantity(item) {
  const available = Number(item.quantityAvailable || 0)
  const reserved = Number(item.quantityReserved || 0)
  return Math.max(available - reserved, 0)
}

export default function OrderScreen() {
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [downloadingOrderId, setDownloadingOrderId] = useState(null)
  const [inventory, setInventory] = useState([])
  const [search, setSearch] = useState('')
  const [quantities, setQuantities] = useState({})
  const [markups, setMarkups] = useState({})
  const [createdOrders, setCreatedOrders] = useState([])
  const [patient, setPatient] = useState({
    patientFirstName: '',
    patientLastName: '',
    patientPhoneNumber: '',
    patientEmail: '',
    deliveryFee: '0',
    platformFee: '0',
  })
  const [message, setMessage] = useState(null)

  const loadInventory = useCallback(async (options = {}) => {
    const { preserveMessage = false } = options
    setLoading(true)
    if (!preserveMessage) {
      setMessage(null)
    }
    try {
      const data = await getInventory()
      setInventory(Array.isArray(data) ? data : [])
    } catch (error) {
      setMessage({ tone: 'error', text: error.message })
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadInventory()
  }, [loadInventory])

  const filteredInventory = useMemo(() => {
    const query = search.trim().toLowerCase()
    if (!query) {
      return inventory
    }
    return inventory.filter(item => {
      const haystack = [
        item.itemDescription,
        item.batchNumber,
        item.supplierName,
      ].filter(Boolean).join(' ').toLowerCase()
      return haystack.includes(query)
    })
  }, [inventory, search])

  const selectedItems = useMemo(() => inventory
    .map(item => {
      const quantity = Number(quantities[item.inventoryId] || 0)
      if (quantity <= 0) {
        return null
      }
      return {
        ...item,
        quantity,
        markupPercentage: parseMarkup(markups[item.inventoryId]),
      }
    })
    .filter(Boolean), [inventory, markups, quantities])

  const selectedUnits = selectedItems.reduce((total, item) => total + item.quantity, 0)
  const subtotal = selectedItems.reduce(
    (total, item) => total + (Number(item.mrp || 0) * item.quantity),
    0,
  )
  const estimatedTotal = subtotal + parseDecimal(patient.deliveryFee) + parseDecimal(patient.platformFee)

  function setPatientField(key, value) {
    setPatient(current => ({ ...current, [key]: value }))
  }

  function adjustQuantity(item, delta) {
    const limit = getAvailableQuantity(item)
    setQuantities(current => {
      const nextValue = Math.max(0, Math.min(limit, Number(current[item.inventoryId] || 0) + delta))
      return { ...current, [item.inventoryId]: nextValue }
    })
  }

  function setMarkup(itemId, value) {
    setMarkups(current => ({ ...current, [itemId]: value }))
  }

  function clearSelection() {
    setQuantities({})
    setMarkups({})
  }

  async function handleInvoiceDownload(orderId) {
    setDownloadingOrderId(orderId)
    setMessage(null)
    try {
      const { blob, filename } = await downloadOrderInvoice(orderId)
      downloadBlob(blob, filename)
      setMessage({ tone: 'success', text: `Invoice downloaded for order ${orderId}.` })
    } catch (error) {
      setMessage({ tone: 'error', text: error.message })
    } finally {
      setDownloadingOrderId(null)
    }
  }

  async function placeOrder() {
    if (selectedItems.length === 0) {
      setMessage({ tone: 'error', text: 'Select at least one inventory item before placing the order.' })
      return
    }
    if (!patient.patientFirstName.trim() || !patient.patientLastName.trim()) {
      setMessage({ tone: 'error', text: 'Patient first name and last name are required.' })
      return
    }

    setSubmitting(true)
    setMessage(null)
    try {
      const payload = {
        orderRequestItems: selectedItems.map(item => ({
          inventoryId: item.inventoryId,
          quantity: item.quantity,
          markupPercentage: item.markupPercentage,
        })),
        patientFirstName: patient.patientFirstName.trim(),
        patientLastName: patient.patientLastName.trim(),
        patientPhoneNumber: patient.patientPhoneNumber.trim(),
        patientEmail: patient.patientEmail.trim(),
        deliveryFee: parseDecimal(patient.deliveryFee),
        platformFee: parseDecimal(patient.platformFee),
      }

      const { orderId } = await createOrder(payload)
      setCreatedOrders(current => [
        {
          orderId,
          patientName: `${patient.patientFirstName.trim()} ${patient.patientLastName.trim()}`.trim(),
          itemCount: selectedItems.length,
          units: selectedUnits,
          total: estimatedTotal,
        },
        ...current,
      ])
      clearSelection()
      setMessage({ tone: 'success', text: `Order ${orderId} created. Use the created orders section to download its invoice.` })
      await loadInventory({ preserveMessage: true })
    } catch (error) {
      setMessage({ tone: 'error', text: error.message })
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <div style={s.container}><Spinner label="Fetching inventory..." /></div>
  }

  return (
    <div style={s.container}>
      <div style={s.topBar}>
        <h2 style={s.title}>Orders</h2>
        <span style={s.hint}>Select inventory, set quantities, place order, download invoice when needed</span>
      </div>

      {message && (
        <div style={s.statusBanner(message.tone)}>
          {message.text}
        </div>
      )}

      <div style={s.statRow}>
        <div style={s.statCard}>
          <div style={s.statLabel}>Inventory Lines</div>
          <div style={s.statValue}>{inventory.length}</div>
          <div style={s.statSub}>items returned by inventory API</div>
        </div>
        <div style={s.statCard}>
          <div style={s.statLabel}>Selected Lines</div>
          <div style={s.statValue}>{selectedItems.length}</div>
          <div style={s.statSub}>included in this order</div>
        </div>
        <div style={s.statCard}>
          <div style={s.statLabel}>Selected Units</div>
          <div style={s.statValue}>{selectedUnits}</div>
          <div style={s.statSub}>total quantity across items</div>
        </div>
        <div style={s.statCard}>
          <div style={s.statLabel}>Estimated Total</div>
          <div style={{ ...s.statValue, fontSize: '1.5rem' }}>{formatCurrency(estimatedTotal)}</div>
          <div style={s.statSub}>MRP estimate plus fees</div>
        </div>
      </div>

      <div style={s.section}>
        <div style={s.sectionHead}>
          <span>Inventory</span>
          <span>{filteredInventory.length} visible</span>
        </div>
        <div style={s.toolbar}>
          <input
            style={s.search}
            value={search}
            placeholder="Search by item, batch, or supplier"
            onChange={event => setSearch(event.target.value)}
          />
          <button style={s.btnSecondary} onClick={loadInventory}>Refresh</button>
        </div>
        {filteredInventory.length === 0 ? (
          <div style={s.empty}>No inventory items matched the current filter.</div>
        ) : (
          <div style={s.tableWrap}>
            <table style={s.table}>
              <thead>
                <tr>
                  <th style={s.th}>Item</th>
                  <th style={s.th}>Stock</th>
                  <th style={s.th}>MRP</th>
                  <th style={s.th}>Purchase</th>
                  <th style={s.th}>Markup %</th>
                  <th style={s.th}>Quantity</th>
                </tr>
              </thead>
              <tbody>
                {filteredInventory.map(item => {
                  const available = getAvailableQuantity(item)
                  const quantity = Number(quantities[item.inventoryId] || 0)
                  return (
                    <tr key={item.inventoryId}>
                      <td style={s.td}>
                        <div style={s.itemTitle}>{item.itemDescription}</div>
                        <div style={s.itemMeta}>
                          Batch {item.batchNumber || '-'} | Supplier {item.supplierName || '-'} | Exp {item.expiryDate || '-'}
                        </div>
                      </td>
                      <td style={s.td}>
                        <div>{available}</div>
                        <div style={s.muted}>reserved {Number(item.quantityReserved || 0)}</div>
                      </td>
                      <td style={s.td}>{formatCurrency(item.mrp)}</td>
                      <td style={s.td}>{formatCurrency(item.purchasePrice)}</td>
                      <td style={s.td}>
                        <input
                          style={s.markupInput}
                          type="number"
                          min="0"
                          step="0.01"
                          value={markups[item.inventoryId] ?? '0'}
                          onChange={event => setMarkup(item.inventoryId, event.target.value)}
                        />
                      </td>
                      <td style={s.td}>
                        <div style={s.qtyControl}>
                          <button
                            style={s.qtyButton}
                            onClick={() => adjustQuantity(item, -1)}
                            disabled={quantity === 0}
                          >
                            -
                          </button>
                          <span style={s.qtyValue}>{quantity}</span>
                          <button
                            style={s.qtyButton}
                            onClick={() => adjustQuantity(item, 1)}
                            disabled={quantity >= available}
                          >
                            +
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div style={s.detailLayout}>
        <div style={s.section}>
          <div style={s.sectionHead}>Order Summary</div>
          <div style={s.sideBody}>
            <div>
              <div style={s.fieldLabel}>Patient Details</div>
              <div style={s.fieldsGrid}>
                <div>
                  <div style={s.fieldLabel}>First Name</div>
                  <input
                    style={s.input}
                    value={patient.patientFirstName}
                    onChange={event => setPatientField('patientFirstName', event.target.value)}
                  />
                </div>
                <div>
                  <div style={s.fieldLabel}>Last Name</div>
                  <input
                    style={s.input}
                    value={patient.patientLastName}
                    onChange={event => setPatientField('patientLastName', event.target.value)}
                  />
                </div>
                <div>
                  <div style={s.fieldLabel}>Phone Number</div>
                  <input
                    style={s.input}
                    value={patient.patientPhoneNumber}
                    onChange={event => setPatientField('patientPhoneNumber', event.target.value)}
                  />
                </div>
                <div>
                  <div style={s.fieldLabel}>Email</div>
                  <input
                    style={s.input}
                    type="email"
                    value={patient.patientEmail}
                    onChange={event => setPatientField('patientEmail', event.target.value)}
                  />
                </div>
                <div>
                  <div style={s.fieldLabel}>Delivery Fee</div>
                  <input
                    style={s.input}
                    type="number"
                    min="0"
                    step="0.01"
                    value={patient.deliveryFee}
                    onChange={event => setPatientField('deliveryFee', event.target.value)}
                  />
                </div>
                <div>
                  <div style={s.fieldLabel}>Platform Fee</div>
                  <input
                    style={s.input}
                    type="number"
                    min="0"
                    step="0.01"
                    value={patient.platformFee}
                    onChange={event => setPatientField('platformFee', event.target.value)}
                  />
                </div>
              </div>
            </div>

            <div>
              <div style={s.fieldLabel}>Selected Items</div>
              {selectedItems.length === 0 ? (
                <div style={s.empty}>No items selected yet.</div>
              ) : (
                <div style={s.lineItems}>
                  {selectedItems.map(item => (
                    <div key={item.inventoryId} style={s.lineItem}>
                      <div style={s.lineHeader}>
                        <span style={s.lineName}>{item.itemDescription}</span>
                        <span style={s.lineMeta}>{item.quantity} unit(s)</span>
                      </div>
                      <div style={s.lineMeta}>
                        Markup {item.markupPercentage}% | Batch {item.batchNumber || '-'}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div style={s.summaryList}>
              <div style={s.summaryRow}>
                <span style={s.summaryLabel}>Items subtotal</span>
                <span style={s.summaryValue}>{formatCurrency(subtotal)}</span>
              </div>
              <div style={s.summaryRow}>
                <span style={s.summaryLabel}>Delivery fee</span>
                <span style={s.summaryValue}>{formatCurrency(patient.deliveryFee)}</span>
              </div>
              <div style={s.summaryRow}>
                <span style={s.summaryLabel}>Platform fee</span>
                <span style={s.summaryValue}>{formatCurrency(patient.platformFee)}</span>
              </div>
              <div style={s.summaryRow}>
                <span style={s.summaryLabel}>Estimated payable</span>
                <span style={s.summaryValue}>{formatCurrency(estimatedTotal)}</span>
              </div>
            </div>

            <div style={s.btnRow}>
              <button style={s.btnSecondary} onClick={clearSelection}>Clear Selection</button>
              <button
                style={{ ...s.btnPrimary, opacity: submitting ? 0.5 : 1, cursor: submitting ? 'not-allowed' : 'pointer' }}
                disabled={submitting}
                onClick={placeOrder}
              >
                {submitting ? 'Placing Order...' : 'Place Order'}
              </button>
            </div>
          </div>
        </div>

        <div style={s.section}>
          <div style={s.sectionHead}>Created Orders</div>
          <div style={s.sideBody}>
            {createdOrders.length === 0 ? (
              <div style={s.empty}>Orders created from this screen will appear here.</div>
            ) : (
              <div style={s.createdOrders}>
                {createdOrders.map(order => (
                  <div key={order.orderId} style={s.createdOrderCard}>
                    <div style={s.createdOrderMeta}>
                      <div style={s.createdOrderId}>Order #{order.orderId}</div>
                      <div style={s.createdOrderSub}>
                        {order.patientName || 'Patient not provided'} | {order.itemCount} line(s) | {order.units} unit(s) | {formatCurrency(order.total)}
                      </div>
                    </div>
                    <button
                      style={{
                        ...s.btnPrimary,
                        padding: '10px 16px',
                        opacity: downloadingOrderId === order.orderId ? 0.5 : 1,
                        cursor: downloadingOrderId === order.orderId ? 'not-allowed' : 'pointer',
                      }}
                      disabled={downloadingOrderId === order.orderId}
                      onClick={() => handleInvoiceDownload(order.orderId)}
                    >
                      {downloadingOrderId === order.orderId ? 'Downloading...' : 'Download Bill'}
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
