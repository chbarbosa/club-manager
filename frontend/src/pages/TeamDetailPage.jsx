import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { getAllTrainers } from '../api/trainers.js'
import { deactivateTeam, getTeam, reactivateTeam, updateTeam } from '../api/teams.js'
import TeamForm from '../components/teams/TeamForm.jsx'

export default function TeamDetailPage() {
  const { uuid } = useParams()
  const location = useLocation()
  const [team, setTeam] = useState(null)
  const [trainers, setTrainers] = useState([])
  const [editing, setEditing] = useState(new URLSearchParams(location.search).get('edit') === '1')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadTeam()
    loadTrainers()
  }, [uuid])

  async function loadTeam() {
    setError('')
    try {
      setTeam(await getTeam(uuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load team.')
    }
  }

  async function loadTrainers() {
    try {
      const response = await getAllTrainers({ page: 0, size: 100 })
      setTrainers(response.content ?? [])
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load trainers for teams.')
    }
  }

  async function submitTeam(data) {
    setError('')
    setMessage('')
    try {
      setTeam(await updateTeam(uuid, data))
      setEditing(false)
      setMessage('Team updated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save team.')
    }
  }

  async function toggleStatus() {
    const action = team.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} team ${teamLabel(team)}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      const updated = team.active ? await deactivateTeam(uuid) : await reactivateTeam(uuid)
      setTeam(updated)
      setMessage(team.active ? 'Team deactivated.' : 'Team reactivated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} team.`)
    }
  }

  return (
    <main className="container py-5">
      <Link to="/teams">&larr; Back to teams</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {message && <p className="alert alert-success mt-3">{message}</p>}

      {!team && !error && <p className="mt-3">Loading team...</p>}

      {team && (
        <>
          <div className="d-flex justify-content-between align-items-center mt-3 mb-4">
            <div>
              <h1>{teamLabel(team)}</h1>
              <span className={`badge ${team.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                {team.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <div className="d-flex gap-2">
              <button className="btn btn-outline-primary" onClick={() => setEditing(true)} type="button">Edit</button>
              <button
                className={`btn ${team.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                onClick={toggleStatus}
                type="button"
              >
                {team.active ? 'Deactivate' : 'Reactivate'}
              </button>
            </div>
          </div>

          {editing ? (
            <div className="card">
              <div className="card-body">
                <h2 className="h4">Edit team</h2>
                <TeamForm initialTeam={team} onCancel={() => setEditing(false)} onSubmit={submitTeam} trainers={trainers} />
              </div>
            </div>
          ) : (
            <dl className="row">
              <dt className="col-sm-3">Age group</dt>
              <dd className="col-sm-9">{team.ageGroup}</dd>
              <dt className="col-sm-3">Team category</dt>
              <dd className="col-sm-9">{formatTeamCategory(team.teamCategory)}</dd>
              <dt className="col-sm-3">Trainer</dt>
              <dd className="col-sm-9">{team.trainerName}</dd>
            </dl>
          )}
        </>
      )}
    </main>
  )
}

function formatTeamCategory(value) {
  return value === 'FEMININE' ? 'Feminine' : 'Masculine'
}

function teamLabel(team) {
  return `${team.ageGroup} ${formatTeamCategory(team.teamCategory)}`
}

