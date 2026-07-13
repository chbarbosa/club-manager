import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { getAllAdmins } from '../api/admins.js'
import { getAllChampionships } from '../api/championships.js'
import { createTeamMatch, getTeamMatches } from '../api/matches.js'
import { getAllPlayers } from '../api/players.js'
import { exportTeamRosterCsv } from '../api/reports.js'
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
import { useAuth } from '../context/AuthContext.jsx'

const EMPTY_MATCH_FORM = {
  championshipUuid: '',
  opponent: '',
  place: '',
  matchDate: '',
  matchTime: '18:00',
  teamScore: '',
  opponentScore: '',
  notes: '',
}

const MONTHS = [
  ['1', 'January'],
  ['2', 'February'],
  ['3', 'March'],
  ['4', 'April'],
  ['5', 'May'],
  ['6', 'June'],
  ['7', 'July'],
  ['8', 'August'],
  ['9', 'September'],
  ['10', 'October'],
  ['11', 'November'],
  ['12', 'December'],
]

export default function TeamDetailPage() {
  const { uuid } = useParams()
  const location = useLocation()
  const { role } = useAuth()
  const canManage = role === 'ADMIN'
  const canOperateTeam = ['ADMIN', 'TRAINER'].includes(role)
  const canExport = role !== 'TRAINER'
  const [team, setTeam] = useState(null)
  const [trainers, setTrainers] = useState([])
  const [admins, setAdmins] = useState([])
  const [players, setPlayers] = useState([])
  const [roster, setRoster] = useState([])
  const [matches, setMatches] = useState([])
  const [championships, setChampionships] = useState([])
  const [matchForm, setMatchForm] = useState(EMPTY_MATCH_FORM)
  const [selectedPlayerUuid, setSelectedPlayerUuid] = useState('')
  const [jerseyNumber, setJerseyNumber] = useState('')
  const [editing, setEditing] = useState(canManage && new URLSearchParams(location.search).get('edit') === '1')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadTeam()
    if (canManage) {
      loadTrainers()
      loadAdmins()
    }
    if (canOperateTeam) {
      loadPlayers()
    }
    loadRoster()
    loadMatches()
  }, [uuid, canManage, canOperateTeam])

  async function loadTeam() {
    setError('')
    try {
      const loadedTeam = await getTeam(uuid)
      setTeam(loadedTeam)
      await loadChampionships(loadedTeam.uuid)
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load team.')
    }
  }

  async function loadChampionships(teamUuid) {
    try {
      const response = await getAllChampionships({ page: 0, size: 100, teamUuid })
      setChampionships((response.content ?? []).filter((championship) => championship.active))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load championships for matches.')
    }
  }

  async function loadTrainers() {
    try {
      const response = await getAllTrainers({ page: 0, size: 100, active: true })
      setTrainers(response.content ?? [])
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load trainers for teams.')
    }
  }

  async function loadAdmins() {
    try {
      setAdmins(await getAllAdmins({ active: true }))
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

  async function loadMatches() {
    try {
      setMatches(await getTeamMatches(uuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load matches.')
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

  function updateMatchForm(event) {
    setMatchForm({ ...matchForm, [event.target.name]: event.target.value })
  }

  async function submitMatch(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      await createTeamMatch(uuid, {
        championshipUuid: matchForm.championshipUuid || null,
        opponent: matchForm.opponent.trim(),
        place: matchForm.place.trim(),
        matchDateTime: `${matchForm.matchDate}T${matchForm.matchTime}`,
        teamScore: matchForm.teamScore === '' ? null : Number(matchForm.teamScore),
        opponentScore: matchForm.opponentScore === '' ? null : Number(matchForm.opponentScore),
        notes: matchForm.notes.trim() || null,
      })
      setMatchForm(EMPTY_MATCH_FORM)
      await loadMatches()
      setMessage('Match created.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save match.')
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
    if (!selectedPlayerUuid || !jerseyNumber) {
      return
    }
    setError('')
    setMessage('')
    try {
      await assignPlayerToTeam(uuid, selectedPlayerUuid, Number(jerseyNumber))
      setSelectedPlayerUuid('')
      setJerseyNumber('')
      await Promise.all([loadTeam(), loadRoster()])
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
      await Promise.all([loadTeam(), loadRoster()])
      setMessage('Player removed from team.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to remove player from team.')
    }
  }

  async function exportRoster() {
    setError('')
    setMessage('')
    try {
      await exportTeamRosterCsv(uuid)
      setMessage('Team roster CSV export started.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to export team roster.')
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
            {canManage && (
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
            )}
          </div>

          {!canManage && !canOperateTeam && <p className="alert alert-info">This workspace is read-only for your role.</p>}
          {!canManage && canOperateTeam && (
            <p className="alert alert-info">You can manage this team's roster and matches. Team settings remain admin-only.</p>
          )}

          <CurrentChampionshipSummary canManage={canManage} championships={championships} teamUuid={team.uuid} />

          <TeamAdviceSummary advice={team.advice} />

          {canManage && editing ? (
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
                    <p className="text-muted mb-0">Assign active players with the same team category and age limit.</p>
                  </div>
                  {canExport && (
                    <button className="btn btn-outline-secondary" onClick={exportRoster} type="button">
                      Export roster CSV
                    </button>
                  )}
                </div>

                {canOperateTeam && (
                <form className="row g-2 align-items-end mb-4" onSubmit={assignPlayer}>
                  <div className="col-md-7">
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
                  <div className="col-md-2">
                    <label className="form-label" htmlFor="roster-jersey-number">Number</label>
                    <input
                      className="form-control"
                      id="roster-jersey-number"
                      max="99"
                      min="1"
                      onChange={(event) => setJerseyNumber(event.target.value)}
                      required
                      type="number"
                      value={jerseyNumber}
                    />
                  </div>
                  <div className="col-md-3">
                    <button className="btn btn-primary" disabled={!selectedPlayerUuid || !jerseyNumber} type="submit">
                      Assign player
                    </button>
                  </div>
                </form>
                )}

                <table className="table table-striped align-middle">
                  <thead>
                    <tr>
                      <th>Number</th>
                      <th>Player</th>
                      <th>Age</th>
                      <th>Team category</th>
                      <th>Positions</th>
                      <th>Assigned date</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((assignment) => (
                      <tr key={assignment.uuid}>
                        <td>{assignment.jerseyNumber ?? '-'}</td>
                        <td>{assignment.playerName}</td>
                        <td>{assignment.playerAge}</td>
                        <td>{formatTeamCategory(assignment.playerTeamCategory)}</td>
                        <td>{formatPositions(assignment.playerPositions)}</td>
                        <td>{formatDate(assignment.assignedDate)}</td>
                        <td>
                          {canOperateTeam ? (
                            <button className="btn btn-sm btn-outline-danger" onClick={() => removePlayer(assignment)} type="button">
                              Remove
                            </button>
                          ) : '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {roster.length === 0 && <p className="text-muted">No players assigned to this team.</p>}
              </section>

              <section className="mt-5">
                <h2 className="h3">Matches</h2>
                <p className="text-muted">Create standalone matches and register trainer analysis for players.</p>

                {canOperateTeam && (
                <form className="card mb-4" onSubmit={submitMatch}>
                  <div className="card-body">
                    <div className="row g-3">
                      <div className="col-md-4">
                        <label className="form-label" htmlFor="match-opponent">Opponent</label>
                        <input className="form-control" id="match-opponent" name="opponent" onChange={updateMatchForm} required value={matchForm.opponent} />
                      </div>
                      <div className="col-md-4">
                        <label className="form-label" htmlFor="match-place">Place</label>
                        <input className="form-control" id="match-place" name="place" onChange={updateMatchForm} required value={matchForm.place} />
                      </div>
                      <div className="col-md-4">
                        <label className="form-label" htmlFor="match-championship">Championship</label>
                        <select className="form-select" id="match-championship" name="championshipUuid" onChange={updateMatchForm} value={matchForm.championshipUuid}>
                          <option value="">No championship</option>
                          {championships.map((championship) => (
                            <option key={championship.uuid} value={championship.uuid}>{championship.name}</option>
                          ))}
                        </select>
                      </div>
                      <div className="col-md-3">
                        <label className="form-label" htmlFor="match-date">Date</label>
                        <input className="form-control" id="match-date" name="matchDate" onChange={updateMatchForm} required type="date" value={matchForm.matchDate} />
                      </div>
                      <div className="col-md-3">
                        <label className="form-label" htmlFor="match-time">Time</label>
                        <input className="form-control" id="match-time" name="matchTime" onChange={updateMatchForm} required type="time" value={matchForm.matchTime} />
                      </div>
                      <div className="col-md-3">
                        <label className="form-label" htmlFor="match-team-score">Team score</label>
                        <input className="form-control" id="match-team-score" min="0" name="teamScore" onChange={updateMatchForm} type="number" value={matchForm.teamScore} />
                      </div>
                      <div className="col-md-3">
                        <label className="form-label" htmlFor="match-opponent-score">Opponent score</label>
                        <input className="form-control" id="match-opponent-score" min="0" name="opponentScore" onChange={updateMatchForm} type="number" value={matchForm.opponentScore} />
                      </div>
                      <div className="col-12">
                        <label className="form-label" htmlFor="match-notes">Notes</label>
                        <input className="form-control" id="match-notes" name="notes" onChange={updateMatchForm} value={matchForm.notes} />
                      </div>
                      <div className="col-12">
                        <button className="btn btn-primary" type="submit">Save match</button>
                      </div>
                    </div>
                  </div>
                </form>
                )}

                <table className="table table-striped align-middle">
                  <thead>
                    <tr>
                      <th>Opponent</th>
                      <th>Date and time</th>
                      <th>Score</th>
                      <th>Championship</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {matches.map((match) => (
                      <tr key={match.uuid}>
                        <td>{match.opponent}</td>
                        <td>{formatDateTime(match.matchDateTime)}</td>
                        <td>{formatScore(match)}</td>
                        <td>{match.championshipName ?? '-'}</td>
                        <td>
                          <Link className="btn btn-sm btn-outline-primary" to={`/teams/${uuid}/matches/${match.uuid}`}>
                            {canOperateTeam ? 'Analyze' : 'View analysis'}
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {matches.length === 0 && <p className="text-muted">No matches registered for this team.</p>}
              </section>
            </>
          )}
        </>
      )}
    </main>
  )
}

function CurrentChampionshipSummary({ canManage, championships, teamUuid }) {
  if (championships.length === 0) {
    return (
      <section className="alert alert-danger d-flex flex-wrap justify-content-between align-items-center gap-3">
        <div>
          <strong>No active championship associated.</strong>
          <div>This team is not currently linked to an active championship.</div>
        </div>
        {canManage && (
          <Link className="btn btn-danger" to={`/championships?teamUuid=${teamUuid}`}>
            Add championship
          </Link>
        )}
      </section>
    )
  }

  return (
    <section className="card border-success mb-4">
      <div className="card-body">
        <div className="d-flex flex-wrap justify-content-between align-items-start gap-3">
          <div>
            <h2 className="h5 mb-2">Current championship</h2>
            {championships.map((championship) => (
              <div className="mb-2" key={championship.uuid}>
                <Link className="fw-semibold" to={`/championships/${championship.uuid}`}>
                  {championship.name}
                </Link>
                <span className="text-muted ms-2">{formatChampionshipPeriod(championship)}</span>
              </div>
            ))}
          </div>
          <span className="badge text-bg-success">
            {championships.length} active championship{championships.length === 1 ? '' : 's'}
          </span>
        </div>
      </div>
    </section>
  )
}

function TeamAdviceSummary({ advice }) {
  if (!advice) {
    return null
  }

  const positionCounts = [
    ['Goalkeepers', advice.goalkeepers],
    ['Defenders', advice.defenders],
    ['Midfielders', advice.midfielders],
    ['Attackers', advice.attackers],
  ]

  return (
    <section className="card mb-4">
      <div className="card-body">
        <div className="d-flex flex-wrap align-items-center justify-content-between gap-3">
          <div>
            <h2 className="h5 mb-1">Team composition</h2>
            <p className="text-muted mb-0">
              Advice starts at {advice.minimumPlayersForAdvice} active players.
            </p>
          </div>
          <span className="badge text-bg-secondary">
            {advice.totalPlayers} active player{advice.totalPlayers === 1 ? '' : 's'}
          </span>
        </div>

        <div className="table-responsive mt-3">
          <table className="table table-sm align-middle mb-0">
            <thead>
              <tr>
                {positionCounts.map(([label]) => (
                  <th key={label}>{label}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              <tr>
                {positionCounts.map(([label, count]) => (
                  <td key={label}>
                    <span className={`badge ${positionBadgeClass(label, count, advice)}`}>{count}</span>
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
        </div>

        {advice.items.length > 0 && (
          <div className="d-flex flex-wrap gap-2 mt-3">
            {advice.items.map((item) => (
              <span className="badge text-bg-warning" key={item.code}>{item.message}</span>
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

function positionBadgeClass(label, count, advice) {
  if (advice.totalPlayers < advice.minimumPlayersForAdvice) {
    return 'text-bg-secondary'
  }
  if (label === 'Goalkeepers') {
    return count < 2 ? 'text-bg-warning' : 'text-bg-success'
  }
  return count < 3 ? 'text-bg-warning' : 'text-bg-success'
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
  const maxAge = maxAgeForTeam(team.ageCategory)
  return players.filter((player) => (
    player.active
    && player.teamCategory === team.teamCategory
    && (maxAge === null || player.age <= maxAge)
    && !assignedPlayerUuids.has(player.uuid)
  ))
}

function maxAgeForTeam(ageCategory) {
  if (ageCategory === 'U17_18') {
    return 18
  }
  if (ageCategory === 'U19_PLUS') {
    return null
  }
  return Number(ageCategory?.replace('U', '')) || null
}

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}

function formatDateTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}

function formatChampionshipPeriod(championship) {
  return `${monthName(championship.startMonth)} ${championship.startYear} - ${monthName(championship.endMonth)} ${championship.endYear}`
}

function monthName(month) {
  return MONTHS.find(([value]) => Number(value) === Number(month))?.[1] ?? month
}

function formatScore(match) {
  if (match.teamScore === null || match.teamScore === undefined || match.opponentScore === null || match.opponentScore === undefined) {
    return '-'
  }
  return `${match.teamScore} - ${match.opponentScore}`
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
