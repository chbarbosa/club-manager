import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { inviteTrainerAccess } from '../api/auth.js'
import { deactivateTrainer, getTrainer, getTrainerTeams, reactivateTrainer, updateTrainer } from '../api/trainers.js'
import TrainerForm from '../components/trainers/TrainerForm.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function TrainerDetailPage() {
  const { uuid } = useParams()
  const location = useLocation()
  const { role } = useAuth()
  const canManage = role === 'ADMIN'
  const [trainer, setTrainer] = useState(null)
  const [teams, setTeams] = useState([])
  const [editing, setEditing] = useState(canManage && new URLSearchParams(location.search).get('edit') === '1')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadTrainer()
  }, [uuid])

  async function loadTrainer() {
    setError('')
    try {
      const [trainerResponse, teamResponse] = await Promise.all([
        getTrainer(uuid),
        getTrainerTeams(uuid),
      ])
      setTrainer(trainerResponse)
      setTeams(teamResponse)
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

  async function sendAccessInvite() {
    if (!trainer.email) {
      setError('Trainer email is required for access.')
      return
    }
    if (!trainer.active) {
      setError('Trainer must be active to receive access.')
      return
    }
    setError('')
    setMessage('')
    try {
      await inviteTrainerAccess(trainer.uuid)
      setMessage('Trainer access email sent.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to send trainer access.')
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
            {canManage && (
              <div className="d-flex gap-2">
                <button className="btn btn-outline-secondary" onClick={sendAccessInvite} type="button">Send access</button>
                <button className="btn btn-outline-primary" onClick={() => setEditing(true)} type="button">Edit</button>
                <button
                  className={`btn ${trainer.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                  onClick={toggleStatus}
                  type="button"
                >
                  {trainer.active ? 'Deactivate' : 'Reactivate'}
                </button>
              </div>
            )}
          </div>

          {!canManage && <p className="alert alert-info">This workspace is read-only for your role.</p>}

          {canManage && editing ? (
            <div className="card">
              <div className="card-body">
                <h2 className="h4">Edit trainer</h2>
                <TrainerForm initialTrainer={trainer} onCancel={() => setEditing(false)} onSubmit={submitTrainer} />
              </div>
            </div>
          ) : (
            <>
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

              <section className="mt-5">
                <h2 className="h3">Team history</h2>
                <table className="table table-striped align-middle">
                  <thead>
                    <tr>
                      <th>Team</th>
                      <th>Age category</th>
                      <th>Team category</th>
                      <th>Role</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {teams.map((team) => (
                      <tr className={team.active ? undefined : 'text-muted'} key={`${team.teamUuid}-${team.role}`}>
                        <td><Link to={`/teams/${team.teamUuid}`}>{team.teamIdentification}</Link></td>
                        <td>{formatAgeCategory(team.ageCategory)}</td>
                        <td>{formatTeamCategory(team.teamCategory)}</td>
                        <td>{team.role}</td>
                        <td>
                          <span className={`badge ${team.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                            {team.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {teams.length === 0 && <p className="text-muted">No teams associated with this trainer.</p>}
              </section>
            </>
          )}
        </>
      )}
    </main>
  )
}

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}

function formatAgeCategory(value) {
  if (value === 'U17_18') {
    return '17-18'
  }
  if (value === 'U19_PLUS') {
    return '19+'
  }
  return value?.replace('U', '') ?? '-'
}

function formatTeamCategory(value) {
  return value === 'FEMININE' ? 'Feminine' : 'Masculine'
}
