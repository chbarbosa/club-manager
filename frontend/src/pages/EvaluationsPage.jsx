import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { createEvaluation, getAllEvaluations } from '../api/evaluations.js'
import EvaluationForm from '../components/evaluations/EvaluationForm.jsx'

const PAGE_SIZE = 20

export default function EvaluationsPage() {
  const [evaluations, setEvaluations] = useState([])
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0 })
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState('')
  const [ageGroup, setAgeGroup] = useState('')
  const [teamCategory, setTeamCategory] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadEvaluations()
  }, [page, status, ageGroup, teamCategory])

  async function loadEvaluations() {
    setError('')
    try {
      const response = await getAllEvaluations({
        page,
        size: PAGE_SIZE,
        status: status || undefined,
        ageGroup: ageGroup || undefined,
        teamCategory: teamCategory || undefined,
      })
      setEvaluations(response.content ?? [])
      setPageInfo({ number: response.number ?? 0, totalPages: response.totalPages ?? 0 })
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load evaluations.')
    }
  }

  async function submitEvaluation(data) {
    setError('')
    setMessage('')
    try {
      await createEvaluation(data)
      setShowForm(false)
      setMessage('Evaluation created.')
      setPage(0)
      await loadEvaluations()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save evaluation.')
    }
  }

  function changeStatus(event) {
    setStatus(event.target.value)
    setPage(0)
  }

  function changeAgeGroup(event) {
    setAgeGroup(event.target.value)
    setPage(0)
  }

  function changeTeamCategory(event) {
    setTeamCategory(event.target.value)
    setPage(0)
  }

  const canGoPrevious = pageInfo.number > 0
  const canGoNext = pageInfo.totalPages > 0 && pageInfo.number < pageInfo.totalPages - 1

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h1>Evaluations</h1>
          <p className="text-muted mb-0">Create evaluation groups by age and team category.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm(true)} type="button">
          Add Evaluation
        </button>
      </div>

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      {showForm && (
        <div aria-modal="true" className="card mb-4" role="dialog">
          <div className="card-body">
            <h2 className="h4">Add Evaluation</h2>
            <EvaluationForm onCancel={() => setShowForm(false)} onSubmit={submitEvaluation} />
          </div>
        </div>
      )}

      <div className="row mb-3">
        <div className="col-md-4">
          <label className="form-label" htmlFor="evaluation-status-filter">Status</label>
          <select className="form-select" id="evaluation-status-filter" onChange={changeStatus} value={status}>
            <option value="">All statuses</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="FINALIZED">Finalized</option>
          </select>
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="evaluation-age-filter">Age group</label>
          <input className="form-control" id="evaluation-age-filter" onChange={changeAgeGroup} placeholder="Under 15" value={ageGroup} />
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="evaluation-category-filter">Team category</label>
          <select className="form-select" id="evaluation-category-filter" onChange={changeTeamCategory} value={teamCategory}>
            <option value="">All categories</option>
            <option value="MASCULINE">Masculine</option>
            <option value="FEMININE">Feminine</option>
          </select>
        </div>
      </div>

      <table className="table table-striped align-middle">
        <thead>
          <tr>
            <th>Title</th>
            <th>Group</th>
            <th>Status</th>
            <th>Created</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {evaluations.map((evaluation) => (
            <tr key={evaluation.uuid}>
              <td>{evaluation.title}</td>
              <td>{groupLabel(evaluation)}</td>
              <td><span className={`badge ${statusClass(evaluation.status)}`}>{formatStatus(evaluation.status)}</span></td>
              <td>{formatDate(evaluation.createdDate)}</td>
              <td>
                <div className="d-flex gap-2">
                  <Link className="btn btn-sm btn-outline-primary" to={`/evaluations/${evaluation.uuid}`}>View</Link>
                  <Link className="btn btn-sm btn-outline-secondary" to={`/evaluations/${evaluation.uuid}?edit=1`}>Edit</Link>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {evaluations.length === 0 && <p className="text-muted">No evaluations found.</p>}

      <div className="d-flex align-items-center gap-2">
        <button className="btn btn-outline-secondary" disabled={!canGoPrevious} onClick={() => setPage(page - 1)} type="button">
          Previous
        </button>
        <span>Page {pageInfo.totalPages === 0 ? 0 : pageInfo.number + 1} of {pageInfo.totalPages}</span>
        <button className="btn btn-outline-secondary" disabled={!canGoNext} onClick={() => setPage(page + 1)} type="button">
          Next
        </button>
      </div>
    </main>
  )
}

function groupLabel(value) {
  return `${value.ageGroup} ${formatTeamCategory(value.teamCategory)}`
}

function formatTeamCategory(value) {
  return value === 'FEMININE' ? 'Feminine' : 'Masculine'
}

function formatStatus(value) {
  return value === 'IN_PROGRESS' ? 'In progress' : value[0] + value.slice(1).toLowerCase()
}

function statusClass(value) {
  if (value === 'FINALIZED') {
    return 'text-bg-secondary'
  }
  if (value === 'IN_PROGRESS') {
    return 'text-bg-warning'
  }
  return 'text-bg-success'
}

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}
