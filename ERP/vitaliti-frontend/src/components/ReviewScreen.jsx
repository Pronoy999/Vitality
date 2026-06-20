import React, { useState } from 'react'
import { saveReviewedPrescription } from '../services/api'

const DEFAULT_COUNTRY_CODE = '+91'
const TODAY = new Date().toISOString().slice(0, 10)

const s = {
  container: { maxWidth: 860, margin: '0 auto', padding: '48px 24px 120px' },
  reviewHeader: {
    display: 'flex',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    marginBottom: 32,
    flexWrap: 'wrap',
    gap: 12,
  },
  reviewTitle: { fontFamily: 'var(--serif)', fontSize: '2rem', animation: 'fadeUp 0.3s ease' },
  reviewHint: {
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
  fieldsGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr' },
  field: (full) => ({
    padding: '14px 20px',
    borderRight: full ? 'none' : '1px solid var(--rule)',
    borderBottom: '1px solid var(--rule)',
    gridColumn: full ? '1 / -1' : undefined,
  }),
  fieldLabel: {
    fontFamily: 'var(--mono)',
    fontSize: '0.68rem',
    textTransform: 'uppercase',
    letterSpacing: '0.08em',
    color: 'var(--muted)',
    marginBottom: 5,
  },
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
    transition: 'border-color 0.2s',
  },
  textarea: {
    width: '100%',
    border: 'none',
    background: 'transparent',
    fontFamily: 'var(--sans)',
    fontSize: '0.92rem',
    color: 'var(--ink)',
    outline: 'none',
    padding: '4px 0',
    resize: 'none',
    borderBottom: '1.5px solid transparent',
    transition: 'border-color 0.2s',
  },
  checkboxRow: {
    padding: '14px 20px',
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    fontSize: '0.9rem',
  },
  checkbox: {
    width: 16,
    height: 16,
    accentColor: 'var(--ink)',
  },
  nestedSection: {
    borderTop: '1px solid var(--rule)',
  },
  phoneInputRow: {
    display: 'grid',
    gridTemplateColumns: '84px 1fr',
    gap: 10,
    alignItems: 'center',
  },
  phoneCode: {
    width: '100%',
    border: '1.5px solid var(--rule)',
    borderRadius: 'var(--radius)',
    background: 'var(--paper)',
    padding: '8px 10px',
    fontFamily: 'var(--mono)',
    fontSize: '0.84rem',
    color: 'var(--ink)',
    outline: 'none',
    textAlign: 'center',
  },
  phoneNumberWrap: {
    borderBottom: '1.5px solid transparent',
    transition: 'border-color 0.2s',
  },
  metricsList: { padding: '16px 20px', display: 'flex', flexWrap: 'wrap', gap: 10 },
  metricChip: {
    display: 'flex',
    alignItems: 'center',
    border: '1.5px solid var(--rule)',
    borderRadius: 20,
    overflow: 'hidden',
    fontSize: '0.82rem',
  },
  metricKey: {
    padding: '5px 10px',
    background: 'var(--cream)',
    fontFamily: 'var(--mono)',
    fontSize: '0.72rem',
    color: 'var(--muted)',
    borderRight: '1.5px solid var(--rule)',
    border: 'none',
    outline: 'none',
    width: 70,
  },
  metricVal: {
    padding: '5px 10px',
    border: 'none',
    background: 'transparent',
    fontFamily: 'var(--mono)',
    fontSize: '0.82rem',
    color: 'var(--ink)',
    width: 80,
    outline: 'none',
  },
  metricDel: {
    border: 'none',
    background: 'none',
    cursor: 'pointer',
    padding: '0 8px',
    color: 'var(--muted)',
    fontSize: '0.9rem',
    lineHeight: 1,
  },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: {
    padding: '10px 16px',
    textAlign: 'left',
    fontFamily: 'var(--mono)',
    fontSize: '0.68rem',
    textTransform: 'uppercase',
    letterSpacing: '0.08em',
    color: 'var(--muted)',
    borderBottom: '1.5px solid var(--rule)',
    background: 'var(--cream)',
  },
  td: { padding: '10px 16px', borderBottom: '1px solid var(--rule)', verticalAlign: 'middle' },
  tdInput: {
    width: '100%',
    border: 'none',
    background: 'transparent',
    fontFamily: 'var(--sans)',
    fontSize: '0.88rem',
    color: 'var(--ink)',
    outline: 'none',
    padding: '3px 0',
    borderBottom: '1.5px solid transparent',
    transition: 'border-color 0.2s',
  },
  addRow: { padding: '12px 16px', borderTop: '1.5px solid var(--rule)' },
  btnAdd: {
    background: 'transparent',
    border: '1.5px dashed var(--rule)',
    color: 'var(--muted)',
    padding: '7px 16px',
    borderRadius: 'var(--radius)',
    fontSize: '0.82rem',
    fontFamily: 'var(--sans)',
    cursor: 'pointer',
    width: '100%',
    textAlign: 'center',
    transition: 'border-color 0.2s, color 0.2s',
  },
  btnSmallAdd: {
    background: 'transparent',
    border: '1.5px dashed var(--rule)',
    color: 'var(--muted)',
    padding: '4px 14px',
    borderRadius: 'var(--radius)',
    fontSize: '0.72rem',
    fontFamily: 'var(--sans)',
    cursor: 'pointer',
    transition: 'border-color 0.2s, color 0.2s',
  },
  btnDanger: {
    background: 'transparent',
    color: 'var(--accent)',
    border: '1.5px solid var(--accent)',
    borderRadius: 'var(--radius)',
    padding: '5px 12px',
    fontSize: '0.78rem',
    cursor: 'pointer',
    fontFamily: 'var(--sans)',
    transition: 'background 0.15s, color 0.15s',
  },
  confirmBar: {
    position: 'fixed',
    bottom: 0,
    left: 0,
    right: 0,
    background: 'var(--paper)',
    borderTop: '2px solid var(--ink)',
    padding: '16px 40px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 16,
    zIndex: 200,
  },
  confirmNote: { fontSize: '0.78rem', color: 'var(--muted)', fontFamily: 'var(--mono)' },
  btnRow: { display: 'flex', gap: 12, flexWrap: 'wrap' },
  btnSecondary: {
    display: 'inline-flex',
    alignItems: 'center',
    padding: '12px 24px',
    borderRadius: 'var(--radius)',
    border: '2px solid var(--ink)',
    fontFamily: 'var(--sans)',
    fontSize: '0.9rem',
    fontWeight: 500,
    cursor: 'pointer',
    background: 'transparent',
    color: 'var(--ink)',
    transition: 'background 0.15s',
  },
  btnConfirm: {
    display: 'inline-flex',
    alignItems: 'center',
    padding: '13px 32px',
    borderRadius: 'var(--radius)',
    border: '2px solid var(--accent2)',
    fontFamily: 'var(--sans)',
    fontSize: '0.95rem',
    fontWeight: 500,
    cursor: 'pointer',
    background: 'var(--accent2)',
    color: 'var(--white)',
    transition: 'background 0.15s',
  },
}

function sanitizePhoneDigits(value) {
  return (value || '').replace(/\D/g, '')
}

function normalizeCountryCode(value) {
  const trimmed = (value || '').trim()
  if (!trimmed) {
    return DEFAULT_COUNTRY_CODE
  }
  const digits = trimmed.replace(/\D/g, '')
  return digits ? `+${digits}` : DEFAULT_COUNTRY_CODE
}

function splitPhoneValue(value) {
  const trimmed = (value || '').trim()
  if (!trimmed) {
    return {
      countryCode: DEFAULT_COUNTRY_CODE,
      number: '',
    }
  }
  const match = trimmed.match(/^(\+\d{1,4})(\d+)$/)
  if (match) {
    return {
      countryCode: match[1],
      number: match[2],
    }
  }
  return {
    countryCode: DEFAULT_COUNTRY_CODE,
    number: sanitizePhoneDigits(trimmed),
  }
}

function buildPhoneValue(countryCode, number) {
  const digits = sanitizePhoneDigits(number)
  if (!digits) {
    return ''
  }
  return `${normalizeCountryCode(countryCode)}${digits}`
}

function normalizeInitialData(initialData) {
  const patientPhone = splitPhoneValue(initialData?.patientPhoneNumber || initialData?.patient_phone_number)
  const customerPhone = splitPhoneValue(initialData?.customerPhoneNumber || initialData?.customer_phone_number)
  const patientNameParts = splitPatientName(initialData?.patient_name)
  const customerFirstName = initialData?.customerFirstName ?? initialData?.customer_first_name ?? ''
  const customerLastName = initialData?.customerLastName ?? initialData?.customer_last_name ?? ''

  return {
    ...initialData,
    patient_name: initialData?.patient_name ?? [patientNameParts.firstName, patientNameParts.lastName].filter(Boolean).join(' '),
    patient_age: sanitizePhoneDigits(initialData?.patient_age),
    date: initialData?.date || TODAY,
    patient_phone_country_code: patientPhone.countryCode,
    patient_phone_number: patientPhone.number,
    your_name: [customerFirstName, customerLastName].filter(Boolean).join(' '),
    customer_first_name: customerFirstName,
    customer_last_name: customerLastName,
    customer_phone_country_code: customerPhone.countryCode,
    customer_phone_number: customerPhone.number,
    medicines: (initialData?.medicines || []).map(medicine => ({
      ...medicine,
      startDate: medicine?.startDate || initialData?.date || TODAY,
      endDate: medicine?.endDate || '',
    })),
  }
}

function splitPatientName(fullName) {
  const trimmed = (fullName || '').trim()
  if (!trimmed) {
    return { firstName: '', lastName: '' }
  }
  const parts = trimmed.split(/\s+/)
  return {
    firstName: parts.slice(0, -1).join(' '),
    lastName: parts.slice(-1).join(' '),
  }
}

function hasValue(value) {
  if (value == null) {
    return false
  }
  if (typeof value === 'string') {
    return value.trim() !== ''
  }
  return true
}

function setIfPresent(target, key, value) {
  if (hasValue(value)) {
    target[key] = value
  }
}

function buildHealthParameters(metrics) {
  return Object.entries(metrics || {})
    .map(([key, value]) => [key?.trim(), value?.trim()])
    .filter(([key, value]) => key && value)
    .map(([key, value]) => `${key}: ${value}`)
    .join(', ')
}

function buildPrescriptionDiagnoses(medicines, diagnosis) {
  return (medicines || [])
    .map(medicine => {
      const item = {}
      setIfPresent(item, 'diagnosis', diagnosis)
      setIfPresent(item, 'medicineName', medicine?.name)
      setIfPresent(item, 'dosage', medicine?.dosage)
      setIfPresent(item, 'startDate', medicine?.startDate)
      setIfPresent(item, 'endDate', medicine?.endDate)

      if (medicine?.quantity !== '' && medicine?.quantity != null) {
        const unit = parseInt(medicine.quantity, 10)
        if (!Number.isNaN(unit)) {
          item.unit = unit
        }
      }

      return item
    })
    .filter(item => Object.keys(item).length > 0)
}

function buildReviewedPrescriptionPayload(data, isFillingForSomeoneElse) {
  const patientPhoneNumber = buildPhoneValue(data.patient_phone_country_code, data.patient_phone_number)
  const patientName = (data.patient_name || '').trim()
  const { firstName, lastName } = splitPatientName(patientName)
  const yourName = (data.your_name || '').trim()
  const { firstName: customerFirstName, lastName: customerLastName } = splitPatientName(yourName)
  const customerPhoneNumber = isFillingForSomeoneElse
    ? buildPhoneValue(data.customer_phone_country_code, data.customer_phone_number)
    : ''
  const diagnosis = (data.diagnosis || '').trim()
  const prescriptionDate = (data.date || '').trim()
  const healthParameters = buildHealthParameters(data.health_metrics)
  const prescriptionDiagnoses = buildPrescriptionDiagnoses(
    (data.medicines || []).map(medicine => ({
      ...medicine,
      startDate: medicine?.startDate || prescriptionDate,
    })),
    diagnosis,
  )
  const payload = {}

  setIfPresent(payload, 'firstName', firstName)
  setIfPresent(payload, 'lastName', lastName)
  setIfPresent(payload, 'patientPhoneNumber', patientPhoneNumber)
  setIfPresent(payload, 'age', data.patient_age !== '' && data.patient_age != null ? parseInt(data.patient_age, 10) : null)
  setIfPresent(payload, 'prescriptionDate', prescriptionDate)
  setIfPresent(payload, 'referredByDoctor', data.doctor_name)
  setIfPresent(payload, 'diagnosis', diagnosis)
  setIfPresent(payload, 'healthParameters', healthParameters)

  if (isFillingForSomeoneElse) {
    setIfPresent(payload, 'customerFirstName', customerFirstName)
    setIfPresent(payload, 'customerLastName', customerLastName)
    setIfPresent(payload, 'customerPhoneNumber', customerPhoneNumber)
  }

  if (prescriptionDiagnoses.length > 0) {
    payload.prescriptionDiagnoses = prescriptionDiagnoses
  }

  return {
    payload,
    patientPhoneNumber,
    customerPhoneNumber,
    patientFirstName: firstName,
    patientLastName: lastName,
    customerFirstName,
    customerLastName,
  }
}

function Field({ label, full, children }) {
  return (
    <div style={s.field(full)}>
      <div style={s.fieldLabel}>{label}</div>
      {children}
    </div>
  )
}

function EditInput({ value, onChange, type = 'text', placeholder = '-', rows, inputMode, pattern }) {
  const [focused, setFocused] = useState(false)
  const focusStyle = { borderBottomColor: focused ? 'var(--ink)' : 'transparent' }

  if (rows) {
    return (
      <textarea
        rows={rows}
        style={{ ...s.textarea, ...focusStyle }}
        value={value || ''}
        placeholder={placeholder}
        onChange={event => onChange(event.target.value)}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
      />
    )
  }

  return (
    <input
      type={type}
      inputMode={inputMode}
      pattern={pattern}
      style={{ ...s.input, ...focusStyle }}
      value={value ?? ''}
      placeholder={placeholder}
      onChange={event => onChange(event.target.value)}
      onFocus={() => setFocused(true)}
      onBlur={() => setFocused(false)}
    />
  )
}

function PhoneInput({ countryCode, number, onCountryCodeChange, onNumberChange, placeholder }) {
  const [focused, setFocused] = useState(false)

  return (
    <div style={s.phoneInputRow}>
      <input
        type="text"
        inputMode="numeric"
        style={s.phoneCode}
        value={countryCode}
        onChange={event => onCountryCodeChange(normalizeCountryCode(event.target.value))}
        onBlur={event => onCountryCodeChange(normalizeCountryCode(event.target.value))}
      />
      <div style={{ ...s.phoneNumberWrap, borderBottomColor: focused ? 'var(--ink)' : 'transparent' }}>
        <input
          type="text"
          inputMode="numeric"
          pattern="[0-9]*"
          style={s.input}
          value={number}
          placeholder={placeholder}
          onChange={event => onNumberChange(sanitizePhoneDigits(event.target.value))}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
        />
      </div>
    </div>
  )
}

export default function ReviewScreen({ jobId, initialData, onConfirmed, onReset }) {
  const normalizedInitialData = normalizeInitialData(initialData)
  const [data, setData] = useState(normalizedInitialData)
  const [isFillingForSomeoneElse, setIsFillingForSomeoneElse] = useState(() => (
    !!(
      normalizedInitialData.customer_first_name ||
      normalizedInitialData.customer_last_name ||
      normalizedInitialData.customer_phone_number
    )
  ))
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)

  function setField(key, value) {
    setMessage(null)
    setData(current => ({ ...current, [key]: value }))
  }

  function setMetricKey(index, value) {
    const metrics = [...Object.entries(data.health_metrics || {})]
    metrics[index] = [value, metrics[index][1]]
    setData(current => ({ ...current, health_metrics: Object.fromEntries(metrics) }))
  }

  function setMetricVal(index, value) {
    const metrics = [...Object.entries(data.health_metrics || {})]
    metrics[index] = [metrics[index][0], value]
    setData(current => ({ ...current, health_metrics: Object.fromEntries(metrics) }))
  }

  function removeMetric(index) {
    const metrics = [...Object.entries(data.health_metrics || {})]
    metrics.splice(index, 1)
    setData(current => ({ ...current, health_metrics: Object.fromEntries(metrics) }))
  }

  function addMetric() {
    setData(current => ({ ...current, health_metrics: { ...(current.health_metrics || {}), '': '' } }))
  }

  function setMed(index, key, value) {
    const medicines = (data.medicines || []).map((medicine, medicineIndex) => (
      medicineIndex === index ? { ...medicine, [key]: value } : medicine
    ))
    setData(current => ({ ...current, medicines }))
  }

  function removeMed(index) {
    setData(current => ({
      ...current,
      medicines: (current.medicines || []).filter((_, medicineIndex) => medicineIndex !== index),
    }))
  }

  function addMed() {
    setData(current => ({
      ...current,
      medicines: [...(current.medicines || []), { name: '', dosage: '', quantity: null, startDate: TODAY, endDate: '' }],
    }))
  }

  function handleFillingForSomeoneElseChange(checked) {
    setMessage(null)
    setIsFillingForSomeoneElse(checked)
    if (checked) {
      return
    }
    setData(current => ({
      ...current,
      your_name: '',
      customer_first_name: '',
      customer_last_name: '',
      customer_phone_country_code: DEFAULT_COUNTRY_CODE,
      customer_phone_number: '',
      customerFirstName: '',
      customerLastName: '',
      customerPhoneNumber: '',
      customer_phone_number_full: '',
    }))
  }

  async function confirm() {
    const {
      payload,
      patientPhoneNumber,
      customerPhoneNumber,
      patientFirstName,
      patientLastName,
      customerFirstName,
      customerLastName,
    } = buildReviewedPrescriptionPayload(data, isFillingForSomeoneElse)

    if (!patientPhoneNumber && !customerPhoneNumber) {
      setMessage({ tone: 'error', text: 'Add at least one phone number: patient or customer.' })
      return
    }
    if (isFillingForSomeoneElse && !customerPhoneNumber) {
      setMessage({ tone: 'error', text: 'Your phone number is required when filling for someone else.' })
      return
    }
    if (isFillingForSomeoneElse && !customerFirstName && !customerLastName) {
      setMessage({ tone: 'error', text: 'Your first name or last name is required when filling for someone else.' })
      return
    }
    if (!patientFirstName || !patientLastName) {
      setMessage({ tone: 'error', text: 'Patient first name and last name are required.' })
      return
    }

    setSaving(true)
    setMessage(null)

    try {
      const result = await saveReviewedPrescription({ data: payload })
      onConfirmed(result.prescription_id)
    } catch (error) {
      alert('Error: ' + error.message)
      setSaving(false)
    }
  }

  const metrics = Object.entries(data.health_metrics || {})
  const medicines = data.medicines || []

  return (
    <div style={s.container}>
      <div style={s.reviewHeader}>
        <h2 style={s.reviewTitle}>Review &amp; Confirm</h2>
        <span style={s.reviewHint}>Click any field to edit</span>
      </div>

      {message && (
        <div style={s.statusBanner(message.tone)}>
          {message.text}
        </div>
      )}

      <div style={s.section}>
        <div style={s.sectionHead}>Patient &amp; Doctor</div>
        <div style={s.fieldsGrid}>
          <Field label="Patient Name">
            <EditInput placeholder={"Indranil Bhattacharjee"} value={data.patient_name} onChange={value => setField('patient_name', value)} />
          </Field>
          <Field label="Patient Age">
            <EditInput
              placeholder={"30"}
              value={data.patient_age}
              onChange={value => setField('patient_age', sanitizePhoneDigits(value))}
              type="text"
              inputMode="numeric"
              pattern="[0-9]*"
            />
          </Field>
          <Field label="Patient Phone Number">
            <PhoneInput
              placeholder={"8420745668"}
              countryCode={data.patient_phone_country_code}
              number={data.patient_phone_number}
              onCountryCodeChange={value => setField('patient_phone_country_code', value)}
              onNumberChange={value => setField('patient_phone_number', value)}
            />
          </Field>
          <Field label="Doctor Name">
            <EditInput placeholder={"Iman Malakar"} value={data.doctor_name} onChange={value => setField('doctor_name', value)} />
          </Field>
          <Field label="Date">
            <EditInput value={data.date} onChange={value => setField('date', value)} />
          </Field>
        </div>
        <div style={s.nestedSection}>
          <div style={s.checkboxRow}>
            <input
              id="filling-for-someone-else"
              type="checkbox"
              checked={isFillingForSomeoneElse}
              onChange={event => handleFillingForSomeoneElseChange(event.target.checked)}
              style={s.checkbox}
            />
            <label htmlFor="filling-for-someone-else">Are you filling for someone else?</label>
          </div>
          {isFillingForSomeoneElse && (
            <div style={s.fieldsGrid}>
              <Field label="Your Name">
                <EditInput placeholder={"Pronoy Mukherjee"} value={data.your_name} onChange={value => setField('your_name', value)} />
              </Field>
              <Field label="Your Phone Number">
                <PhoneInput
                  placeholder={"9874045815"}
                  countryCode={data.customer_phone_country_code}
                  number={data.customer_phone_number}
                  onCountryCodeChange={value => setField('customer_phone_country_code', value)}
                  onNumberChange={value => setField('customer_phone_number', value)}
                />
              </Field>
            </div>
          )}
        </div>
      </div>

      <div style={s.section}>
        <div style={s.sectionHead}>Clinical Details</div>
        <div style={s.fieldsGrid}>
          <Field label="Patient Issue / Complaint" full>
            <EditInput value={data.patient_issue} onChange={value => setField('patient_issue', value)} rows={2} />
          </Field>
          <Field label="Diagnosis" full>
            <EditInput value={data.diagnosis} onChange={value => setField('diagnosis', value)} rows={2} />
          </Field>
        </div>
      </div>

      <div style={s.section}>
        <div style={s.sectionHead}>
          Health Metrics
          <button style={s.btnSmallAdd} onClick={addMetric}>+ Add</button>
        </div>
        <div style={s.metricsList}>
          {metrics.map(([key, value], index) => (
            <div key={index} style={s.metricChip}>
              <input
                style={s.metricKey}
                value={key}
                placeholder="Metric"
                onChange={event => setMetricKey(index, event.target.value)}
              />
              <input
                style={s.metricVal}
                value={value}
                placeholder="Value"
                onChange={event => setMetricVal(index, event.target.value)}
              />
              <button style={s.metricDel} onClick={() => removeMetric(index)}>x</button>
            </div>
          ))}
          {metrics.length === 0 && (
            <span style={{ fontSize: '0.82rem', color: 'var(--muted)', fontFamily: 'var(--mono)' }}>
              No metrics recorded
            </span>
          )}
        </div>
      </div>

      <div style={s.section}>
        <div style={s.sectionHead}>Medicines</div>
        <table style={s.table}>
          <thead>
            <tr>
              <th style={s.th}>Name</th>
              <th style={s.th}>Dosage</th>
              <th style={{ ...s.th, width: 80 }}>Qty</th>
              <th style={s.th}>Start Date</th>
              <th style={s.th}>End Date</th>
              <th style={{ ...s.th, width: 48 }}></th>
            </tr>
          </thead>
          <tbody>
            {medicines.map((medicine, index) => (
              <tr key={index}>
                <td style={s.td}>
                  <input
                    style={s.tdInput}
                    value={medicine.name || ''}
                    placeholder="Medicine name"
                    onChange={event => setMed(index, 'name', event.target.value)}
                    onFocus={event => { event.target.style.borderBottomColor = 'var(--ink)' }}
                    onBlur={event => { event.target.style.borderBottomColor = 'transparent' }}
                  />
                </td>
                <td style={s.td}>
                  <input
                    style={s.tdInput}
                    value={medicine.dosage || ''}
                    placeholder="Dosage / frequency"
                    onChange={event => setMed(index, 'dosage', event.target.value)}
                    onFocus={event => { event.target.style.borderBottomColor = 'var(--ink)' }}
                    onBlur={event => { event.target.style.borderBottomColor = 'transparent' }}
                  />
                </td>
                <td style={s.td}>
                  <input
                    style={{ ...s.tdInput, width: 60 }}
                    type="number"
                    value={medicine.quantity ?? ''}
                    placeholder="-"
                    min="0"
                    onChange={event => setMed(index, 'quantity', event.target.value)}
                    onFocus={event => { event.target.style.borderBottomColor = 'var(--ink)' }}
                    onBlur={event => { event.target.style.borderBottomColor = 'transparent' }}
                  />
                </td>
                <td style={s.td}>
                  <input
                    style={s.tdInput}
                    type="date"
                    value={medicine.startDate || ''}
                    onChange={event => setMed(index, 'startDate', event.target.value)}
                    onFocus={event => { event.target.style.borderBottomColor = 'var(--ink)' }}
                    onBlur={event => { event.target.style.borderBottomColor = 'transparent' }}
                  />
                </td>
                <td style={s.td}>
                  <input
                    style={s.tdInput}
                    type="date"
                    value={medicine.endDate || ''}
                    onChange={event => setMed(index, 'endDate', event.target.value)}
                    onFocus={event => { event.target.style.borderBottomColor = 'var(--ink)' }}
                    onBlur={event => { event.target.style.borderBottomColor = 'transparent' }}
                  />
                </td>
                <td style={s.td}>
                  <button style={s.btnDanger} onClick={() => removeMed(index)}>X</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div style={s.addRow}>
          <button style={s.btnAdd} onClick={addMed}>+ Add medicine</button>
        </div>
      </div>

      <div style={s.confirmBar}>
        <span style={s.confirmNote}>All edits are local until you confirm.</span>
        <div style={s.btnRow}>
          <button style={s.btnSecondary} onClick={onReset}>Start over</button>
          <button style={s.btnConfirm} disabled={saving} onClick={confirm}>
            {saving ? 'Saving...' : 'Confirm & Save'}
          </button>
        </div>
      </div>
    </div>
  )
}
