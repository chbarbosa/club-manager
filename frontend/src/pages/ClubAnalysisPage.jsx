import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getClubAnalysisHistory, getCurrentClubAnalysis } from '../api/clubAnalysis.js'

export default function ClubAnalysisPage() {
  const [analysis, setAnalysis] = useState(null)
  const [history, setHistory] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    loadAnalysis()
  }, [])

  async function loadAnalysis() {
    setError('')
    try {
      const [currentResponse, historyResponse] = await Promise.all([
        getCurrentClubAnalysis(),
        getClubAnalysisHistory({ page: 0, size: 10 }),
      ])
      setAnalysis(currentResponse)
      setHistory(historyResponse.content ?? [])
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load club analysis.')
    }
  }

  return (
    <main className="container py-5">
      <Link to="/dashboard">&larr; Back to dashboard</Link>
      <div className="d-flex flex-wrap justify-content-between align-items-start gap-3 mt-3 mb-4">
        <div>
          <h1>Club Analysis</h1>
          <p className="text-muted mb-0">Daily health snapshot generated from current club data.</p>
        </div>
        {analysis && (
          <Link className="btn btn-outline-secondary" to={`/club-analysis/${analysis.uuid}`}>
            Open persisted detail
          </Link>
        )}
      </div>

      {error && <p className="alert alert-danger">{error}</p>}
      {!analysis && !error && <p>Loading club analysis...</p>}

      {analysis && (
        <>
          <AnalysisSummary analysis={analysis} />
          <AnalysisItems items={analysis.items} />

          <section className="card mt-4">
            <div className="card-body">
              <h2 className="h4">History</h2>
              <table className="table table-striped align-middle">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Generated</th>
                    <th>Items</th>
                    <th>Critical</th>
                    <th>Warnings</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((entry) => (
                    <tr key={entry.uuid}>
                      <td>{formatDate(entry.analysisDate)}</td>
                      <td>{formatDateTime(entry.generatedAt)}</td>
                      <td>{entry.totalItems}</td>
                      <td><span className="badge text-bg-danger">{entry.criticalCount}</span></td>
                      <td><span className="badge text-bg-warning">{entry.warningCount}</span></td>
                      <td><Link className="btn btn-sm btn-outline-primary" to={`/club-analysis/${entry.uuid}`}>View</Link></td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {history.length === 0 && <p className="text-muted mb-0">No analysis history yet.</p>}
            </div>
          </section>
        </>
      )}
    </main>
  )
}

export function AnalysisSummary({ analysis }) {
  return (
    <section className="card mb-4">
      <div className="card-body">
        <div className="d-flex flex-wrap justify-content-between align-items-start gap-3">
          <div>
            <h2 className="h4">Snapshot</h2>
            <p className="text-muted mb-0">
              Analysis date {formatDate(analysis.analysisDate)} · generated {formatDateTime(analysis.generatedAt)}
            </p>
          </div>
          <div className="d-flex flex-wrap gap-2">
            <span className="badge text-bg-secondary">{analysis.totalItems} findings</span>
            <span className="badge text-bg-info">{analysis.infoCount} info</span>
            <span className="badge text-bg-warning">{analysis.warningCount} warnings</span>
            <span className="badge text-bg-danger">{analysis.criticalCount} critical</span>
          </div>
        </div>
      </div>
    </section>
  )
}

export function AnalysisItems({ items }) {
  if (items.length === 0) {
    return <p className="alert alert-success">No findings for this analysis.</p>
  }

  return (
    <section className="d-flex flex-column gap-3">
      {items.map((item) => (
        <article className="card" key={item.uuid}>
          <div className="card-body">
            <div className="d-flex flex-wrap justify-content-between align-items-start gap-3">
              <div>
                <h2 className="h5 mb-1">{item.title}</h2>
                <p className="mb-0">{item.message}</p>
              </div>
              <span className={`badge ${severityClass(item.severity)}`}>{formatSeverity(item.severity)}</span>
            </div>

            <div className="mt-3">
              <span className="text-muted">{item.affectedRecords.length} affected record{item.affectedRecords.length === 1 ? '' : 's'}</span>
              {item.affectedRecords.length > 0 && (
                <div className="d-flex flex-wrap gap-2 mt-2">
                  {item.affectedRecords.map((record) => (
                    <AffectedRecordLink key={`${record.entityType}-${record.uuid}`} record={record} />
                  ))}
                </div>
              )}
            </div>
          </div>
        </article>
      ))}
    </section>
  )
}

function AffectedRecordLink({ record }) {
  const path = entityPath(record)
  if (!path) {
    return <span className="badge text-bg-light border">{record.label}</span>
  }
  return <Link className="badge text-bg-light border text-decoration-none" to={path}>{record.label}</Link>
}

function entityPath(record) {
  if (record.entityType === 'PLAYER') return `/players/${record.uuid}`
  if (record.entityType === 'TEAM') return `/teams/${record.uuid}`
  if (record.entityType === 'CHAMPIONSHIP') return `/championships/${record.uuid}`
  if (record.entityType === 'EVALUATION') return `/evaluations/${record.uuid}`
  return null
}

export function severityClass(severity) {
  if (severity === 'CRITICAL') return 'text-bg-danger'
  if (severity === 'WARNING') return 'text-bg-warning'
  return 'text-bg-info'
}

export function formatSeverity(severity) {
  if (severity === 'CRITICAL') return 'Critical'
  if (severity === 'WARNING') return 'Warning'
  return 'Info'
}

export function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}

export function formatDateTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}
