import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { createTrainer, deactivateTrainer, getAllTrainers, reactivateTrainer } from '../api/trainers.js'
import TrainerForm from '../components/trainers/TrainerForm.jsx'
import { useAuth } from '../context/AuthContext.jsx'

const PAGE_SIZE = 20

export default function TrainersPage() {
  const { role } = useAuth()
  const [trainers, setTrainers] = useState([])
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0 })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [showInactive, setShowInactive] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const activeSearch = useDebouncedValue(search, 300)

  useEffect(() => {
    loadTrainers()
  }, [page, activeSearch, showInactive])

  async function loadTrainers() {
    setError('')
    try {
      const response = await getAllTrainers({
        page,
        size: PAGE_SIZE,
        name: activeSearch || undefined,
        active: showInactive ? undefined : true,
      })
      setTrainers(response.content ?? [])
      setPageInfo({ number: response.number ?? 0, totalPages: response.totalPages ?? 0 })
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load trainers.')
    }
  }

  async function submitTrainer(data) {
    setError('')
    setMessage('')
    try {
      await createTrainer(data)
      setShowForm(false)
      setMessage('Trainer created.')
      setPage(0)
      await loadTrainers()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save trainer.')
    }
  }

  async function toggleStatus(trainer) {
    const action = trainer.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} trainer ${trainer.name}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      if (trainer.active) {
        await deactivateTrainer(trainer.uuid)
        setMessage('Trainer deactivated.')
      } else {
        await reactivateTrainer(trainer.uuid)
        setMessage('Trainer reactivated.')
      }
      await loadTrainers()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} trainer.`)
    }
  }

  function changeSearch(event) {
    setSearch(event.target.value)
    setPage(0)
  }

  function changeShowInactive(event) {
    setShowInactive(event.target.checked)
    setPage(0)
  }

  const canGoPrevious = pageInfo.number > 0
  const canGoNext = pageInfo.totalPages > 0 && pageInfo.number < pageInfo.totalPages - 1
  const canManage = role === 'ADMIN'

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h1>Trainers</h1>
          <p className="text-muted mb-0">Register and manage trainers for this club.</p>
        </div>
        {canManage && (
          <button className="btn btn-primary" onClick={() => setShowForm(true)} type="button">
            Add Trainer
          </button>
        )}
      </div>

      {!canManage && <p className="alert alert-info">Support access is read-only.</p>}

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      {canManage && showForm && (
        <div aria-modal="true" className="card mb-4" role="dialog">
          <div className="card-body">
            <h2 className="h4">Add Trainer</h2>
            <TrainerForm onCancel={() => setShowForm(false)} onSubmit={submitTrainer} />
          </div>
        </div>
      )}

      <div className="row g-3 align-items-end mb-3">
        <div className="col-md-8">
          <label className="form-label" htmlFor="trainer-search">Search trainers</label>
          <input
            className="form-control"
            id="trainer-search"
            onChange={changeSearch}
            placeholder="Search by name"
            value={search}
          />
        </div>
        <div className="col-md-4">
          <div className="form-check">
            <input
              checked={showInactive}
              className="form-check-input"
              id="show-inactive-trainers"
              onChange={changeShowInactive}
              type="checkbox"
            />
            <label className="form-check-label" htmlFor="show-inactive-trainers">
              Show inactive trainers too
            </label>
          </div>
        </div>
      </div>

      <table className="table table-striped align-middle">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Member Since</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {trainers.map((trainer) => (
            <tr className={trainer.active ? '' : 'text-muted'} key={trainer.uuid}>
              <td>{trainer.name}</td>
              <td>{trainer.email || '-'}</td>
              <td>{formatDate(trainer.memberSince)}</td>
              <td>
                <span className={`badge ${trainer.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                  {trainer.active ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td>
                <div className="d-flex gap-2">
                  <Link className="btn btn-sm btn-outline-primary" to={`/trainers/${trainer.uuid}`}>View</Link>
                  {canManage && (
                    <>
                      <Link className="btn btn-sm btn-outline-secondary" to={`/trainers/${trainer.uuid}?edit=1`}>Edit</Link>
                      <button
                        className={`btn btn-sm ${trainer.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                        onClick={() => toggleStatus(trainer)}
                        type="button"
                      >
                        {trainer.active ? 'Deactivate' : 'Reactivate'}
                      </button>
                    </>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {trainers.length === 0 && <p className="text-muted">No trainers found.</p>}

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

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}

function useDebouncedValue(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value)

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setDebouncedValue(value), delay)
    return () => window.clearTimeout(timeoutId)
  }, [value, delay])

  return debouncedValue
}
