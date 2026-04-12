import React, { useCallback, useEffect, useState } from 'react'
import Header from './components/Header'
import UploadScreen from './components/UploadScreen'
import ProgressScreen from './components/ProgressScreen'
import ReviewScreen from './components/ReviewScreen'
import SuccessScreen from './components/SuccessScreen'
import POScreen from './components/POScreen'

const s = {
  poBanner: {
    maxWidth: 860,
    margin: '0 auto',
    padding: '20px 24px 0',
  },
  banner: {
    background: '#edf7f1',
    border: '1.5px solid var(--accent2)',
    borderRadius: 'var(--radius)',
    padding: '14px 20px',
    fontFamily: 'var(--mono)',
    fontSize: '0.82rem',
    color: 'var(--accent2)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    animation: 'fadeUp 0.3s ease',
  },
  bannerClose: {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    color: 'var(--accent2)',
    fontSize: '1rem',
    lineHeight: 1,
    padding: '0 4px',
  },
}

export default function App() {
  const [tab, setTab]                       = useState('rx')
  const [screen, setScreen]                 = useState('upload')
  const [jobId, setJobId]                   = useState(null)
  const [parsedData, setParsedData]         = useState(null)
  const [prescriptionId, setPrescriptionId] = useState(null)
  const [pendingCount, setPendingCount]     = useState(0)
  const [poBanner, setPOBanner]             = useState(false)

  const refreshPending = useCallback(async () => {
    try {
      const res  = await fetch('/api/po/pending')
      const json = await res.json()
      setPendingCount(json.count || 0)
    } catch {}
  }, [])

  useEffect(() => { refreshPending() }, [refreshPending])

  function handleUpload(id)    { setJobId(id); setScreen('progress') }
  function handleManual(data)  { setJobId(null); setParsedData(data); setScreen('review') }
  function handleReady(data)   { setParsedData(data); setScreen('review') }
  function handleConfirmed(id) { setPrescriptionId(id); setScreen('success'); refreshPending() }
  function reset()             { setScreen('upload'); setJobId(null); setParsedData(null); setPrescriptionId(null); setPOBanner(false) }

  function handleGenerated() {
    refreshPending()
    setTab('rx')
    setPOBanner(true)
    setScreen('upload')
  }

  return (
    <>
      <Header activeTab={tab} onTab={(t) => { setTab(t); setPOBanner(false) }} pendingCount={pendingCount} />

      {/* PO success flash banner shown on rx tab after generation */}
      {tab === 'rx' && poBanner && (
        <div style={s.poBanner}>
          <div style={s.banner}>
            ✦ Purchase order generated &amp; downloaded. Prescriptions marked as ordered.
            <button style={s.bannerClose} onClick={() => setPOBanner(false)}>×</button>
          </div>
        </div>
      )}

      {tab === 'rx' && (
        <>
          {screen === 'upload'   && <UploadScreen onUpload={handleUpload} onManual={handleManual} />}
          {screen === 'progress' && <ProgressScreen jobId={jobId} onReady={handleReady} />}
          {screen === 'review'   && <ReviewScreen jobId={jobId} initialData={parsedData} onConfirmed={handleConfirmed} onReset={reset} />}
          {screen === 'success'  && <SuccessScreen prescriptionId={prescriptionId} onReset={reset} />}
        </>
      )}

      {tab === 'po' && <POScreen onGenerated={handleGenerated} />}
    </>
  )
}