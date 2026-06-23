import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getCurrentTrainerProfile } from '../api/trainers.js'

export default function TrainerProfilePage() {
  const [profile, setProfile] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    loadProfile()
  }, [])

  async function loadProfile() {
    setError('')
    try {
      setProfile(await getCurrentTrainerProfile())
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load trainer profile.')
    }
  }

  const trainer = profile?.trainer
  const teams = profile?.teams ?? []

  return (
    <main className="container py-5">
      <Link to="/dashboard">&larr; Back to dashboard</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {!profile && !error && <p className="mt-3">Loading trainer profile...</p>}

      {trainer && (
        <>
          <div className="mt-3 mb-4">
            <h1>My trainer profile</h1>
            <p className="alert alert-info">This profile is read-only. Ask a club admin if something needs to change.</p>
          </div>

          <section className="card mb-4">
            <div className="card-body">
              <h2 className="h4">{trainer.name}</h2>
              <dl className="row mb-0">
                <dt className="col-sm-3">Email</dt>
                <dd className="col-sm-9">{trainer.email ?? '-'}</dd>
                <dt className="col-sm-3">Phone</dt>
                <dd className="col-sm-9">{trainer.phone ?? '-'}</dd>
                <dt className="col-sm-3">Member since</dt>
                <dd className="col-sm-9">{formatDate(trainer.memberSince)}</dd>
                <dt className="col-sm-3">Status</dt>
                <dd className="col-sm-9">
                  <span className={`badge ${trainer.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                    {trainer.active ? 'Active' : 'Inactive'}
                  </span>
                </dd>
              </dl>
            </div>
          </section>

          <section>
            <h2 className="h3">Team history</h2>
            <table className="table table-striped align-middle">
              <thead>
                <tr>
                  <th>Team</th>
                  <th>Age</th>
                  <th>Category</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {teams.map((team) => (
                  <tr key={team.teamUuid}>
                    <td>{team.teamIdentification}</td>
                    <td>{formatAgeCategory(team.ageCategory)}</td>
                    <td>{formatTeamCategory(team.teamCategory)}</td>
                    <td>{team.role}</td>
                    <td>
                      <span className={`badge ${team.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                        {team.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td><Link className="btn btn-sm btn-outline-primary" to={`/teams/${team.teamUuid}`}>View team</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
            {teams.length === 0 && <p className="text-muted">No teams assigned yet.</p>}
          </section>
        </>
      )}
    </main>
  )
}

function formatTeamCategory(value) {
  return value === 'FEMININE' ? 'Feminine' : 'Masculine'
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

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}
