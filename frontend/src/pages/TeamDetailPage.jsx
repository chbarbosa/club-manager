import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { getAllAdmins } from '../api/admins.js'
import { getAllPlayers } from '../api/players.js'
import { getAllTrainers } from '../api/trainers.js'
import {
  assignPlayerToTeam,
  deactivateTeam,
  getTeam,
  getTeamRoster,
  reactivateTeam,
  removePlayerFromTeam,
  updateTeam,
} from '../api/teams.js'
import TeamForm from '../components/teams/TeamForm.jsx'

export default function TeamDetailPage() {
  const { uuid } = useParams()
  const location = useLocation()
  const [team, setTeam] = useState(null)
  const [trainers, setTrainers] = useState([])
  const [admins, setAdmins] = useState([])
  const [players, setPlayers] = useState([])
  const [roster, setRoster] = useState([])
  const [selectedPlayerUuid, setSelectedPlayerUuid] = useState('')
  const [editing, setEditing] = useState(new URLSearchParams(location.search).get('edit') === '1')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadTeam()
    loadTrainers()
    loadAdmins()
    loadPlayers()
    loadRoster()
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

  async function loadAdmins() {
    try {
      setAdmins(await getAllAdmins())
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load admins for teams.')
    }
  }

  async function loadPlayers() {
    try {
      const response = await getAllPlayers({ page: 0, size: 200 })
      setPlayers(response.content ?? [])
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load players for roster.')
    }
  }

  async function loadRoster() {
    try {
      setRoster(await getTeamRoster(uuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load team roster.')
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

  async function assignPlayer(event) {
    event.preventDefault()
    if (!selectedPlayerUuid) {
      return
    }
    setError('')
    setMessage('')
    try {
      await assignPlayerToTeam(uuid, selectedPlayerUuid)
      setSelectedPlayerUuid('')
      await loadRoster()
      setMessage('Player assigned to team.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to assign player to team.')
    }
  }

  async function removePlayer(assignment) {
    if (!window.confirm(`Remove ${assignment.playerName} from this team?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      await removePlayerFromTeam(uuid, assignment.uuid)
      await loadRoster()
      setMessage('Player removed from team.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to remove player from team.')
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
                <TeamForm admins={admins} initialTeam={team} onCancel={() => setEditing(false)} onSubmit={submitTeam} trainers={trainers} />
              </div>
            </div>
          ) : (
            <>
              <dl className="row">
                <dt className="col-sm-3">Identification</dt>
                <dd className="col-sm-9">{team.identification ?? team.ageGroup}</dd>
                <dt className="col-sm-3">Age category</dt>
                <dd className="col-sm-9">{formatAgeCategory(team.ageCategory)}</dd>
                <dt className="col-sm-3">Team category</dt>
                <dd className="col-sm-9">{formatTeamCategory(team.teamCategory)}</dd>
                <dt className="col-sm-3">Trainer</dt>
                <dd className="col-sm-9">{team.trainerName}</dd>
                <dt className="col-sm-3">Sub trainer / assistant</dt>
                <dd className="col-sm-9">{team.subTrainerName ?? '-'}</dd>
                <dt className="col-sm-3">Administrative assistant</dt>
                <dd className="col-sm-9">{team.assistantAdminName ?? '-'}</dd>
              </dl>

              <section className="mt-5">
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <div>
                    <h2 className="h3">Roster</h2>
                    <p className="mb-1"><strong>{roster.length}</strong> active player{roster.length === 1 ? '' : 's'}</p>
                    <p className="text-muted mb-0">Assign active players with the same team category.</p>
                  </div>
                </div>

                <form className="row g-2 align-items-end mb-4" onSubmit={assignPlayer}>
                  <div className="col-md-8">
                    <label className="form-label" htmlFor="roster-player">Player</label>
                    <select
                      className="form-select"
                      id="roster-player"
                      onChange={(event) => setSelectedPlayerUuid(event.target.value)}
                      value={selectedPlayerUuid}
                    >
                      <option value="">Select a player</option>
                      {availablePlayers(team, players, roster).map((player) => (
                        <option key={player.uuid} value={player.uuid}>{player.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="col-md-4">
                    <button className="btn btn-primary" disabled={!selectedPlayerUuid} type="submit">
                      Assign player
                    </button>
                  </div>
                </form>

                <table className="table table-striped align-middle">
                  <thead>
                    <tr>
                      <th>Player</th>
                      <th>Team category</th>
                      <th>Positions</th>
                      <th>Assigned date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((assignment) => (
                      <tr key={assignment.uuid}>
                        <td>{assignment.playerName}</td>
                        <td>{formatTeamCategory(assignment.playerTeamCategory)}</td>
                        <td>{formatPositions(assignment.playerPositions)}</td>
                        <td>{formatDate(assignment.assignedDate)}</td>
                        <td>
                          <button className="btn btn-sm btn-outline-danger" onClick={() => removePlayer(assignment)} type="button">
                            Remove
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {roster.length === 0 && <p className="text-muted">No players assigned to this team.</p>}
              </section>
            </>
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
  return `${team.identification ?? team.ageGroup} ${formatTeamCategory(team.teamCategory)}`
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

function availablePlayers(team, players, roster) {
  const assignedPlayerUuids = new Set(roster.map((assignment) => assignment.playerUuid))
  return players.filter((player) => (
    player.active
    && player.teamCategory === team.teamCategory
    && !assignedPlayerUuids.has(player.uuid)
  ))
}

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}

function formatPositions(values = []) {
  if (!values.length) {
    return '-'
  }
  return values.map((value) => {
    if (value === 'GOALKEEPER') return 'Goalkeeper'
    if (value === 'DEFENSE') return 'Defense'
    if (value === 'MIDFIELD') return 'Midfield'
    if (value === 'ATTACK') return 'Attack'
    return value
  }).join(', ')
}
