import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { deactivateTrainer, getTrainer, reactivateTrainer, updateTrainer } from '../api/trainers.js'
import TrainerForm from '../components/trainers/TrainerForm.jsx'

export default function TrainerDetailPage() {
  const { uuid } = useParams()
  const location = useLocation()
  const [trainer, setTrainer] = useState(null)
  const [editing, setEditing] = useState(new URLSearchParams(location.search).get('edit') === '1')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadTrainer()
  }, [uuid])

  async function loadTrainer() {
    setError('')
    try {
      setTrainer(await getTrainer(uuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load trainer.')
    }
  }

  async function submitTrainer(data) {
    setError('')
    setMessage('')
    try {
      setTrainer(await updateTrainer(uuid, data))
      setEditing(false)
      setMessage('Trainer updated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save trainer.')
    }
  }

  async function toggleStatus() {
    const action = trainer.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} trainer ${trainer.name}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      const updated = trainer.active ? await deactivateTrainer(uuid) : await reactivateTrainer(uuid)
      setTrainer(updated)
      setMessage(trainer.active ? 'Trainer deactivated.' : 'Trainer reactivated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} trainer.`)
    }
  }

  return (
    <main className="container py-5">
      <Link to="/trainers">&larr; Back to trainers</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {message && <p className="alert alert-success mt-3">{message}</p>}

      {!trainer && !error && <p className="mt-3">Loading trainer...</p>}

      {trainer && (
        <>
          <div className="d-flex justify-content-between align-items-center mt-3 mb-4">
            <div>
              <h1>{trainer.name}</h1>
              <span className={`badge ${trainer.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                {trainer.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <div className="d-flex gap-2">
              <button className="btn btn-outline-primary" onClick={() => setEditing(true)} type="button">Edit</button>
              <button
                className={`btn ${trainer.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                onClick={toggleStatus}
                type="button"
              >
                {trainer.active ? 'Deactivate' : 'Reactivate'}
              </button>
            </div>
          </div>

          {editing ? (
            <div className="card">
              <div className="card-body">
                <h2 className="h4">Edit trainer</h2>
                <TrainerForm initialTrainer={trainer} onCancel={() => setEditing(false)} onSubmit={submitTrainer} />
              </div>
            </div>
          ) : (
            <dl className="row">
              <dt className="col-sm-3">Email</dt>
              <dd className="col-sm-9">{trainer.email || '-'}</dd>
              <dt className="col-sm-3">Phone</dt>
              <dd className="col-sm-9">{trainer.phone || '-'}</dd>
              <dt className="col-sm-3">Birth country</dt>
              <dd className="col-sm-9">{trainer.birthCountry || '-'}</dd>
              <dt className="col-sm-3">Living country</dt>
              <dd className="col-sm-9">{trainer.livingCountry || '-'}</dd>
              <dt className="col-sm-3">Birthdate</dt>
              <dd className="col-sm-9">{formatDate(trainer.birthdate)}</dd>
              {trainer.age !== null && trainer.age !== undefined && (
                <>
                  <dt className="col-sm-3">Age</dt>
                  <dd className="col-sm-9">{trainer.age}</dd>
                </>
              )}
              <dt className="col-sm-3">Register date</dt>
              <dd className="col-sm-9">{formatDate(trainer.registerDate)}</dd>
              <dt className="col-sm-3">Member since</dt>
              <dd className="col-sm-9">{formatDate(trainer.memberSince)}</dd>
            </dl>
          )}
        </>
      )}
    </main>
  )
}

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}
