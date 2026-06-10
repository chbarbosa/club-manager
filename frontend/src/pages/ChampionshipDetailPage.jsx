import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  assignChampionshipRosterPlayer,
  deactivateChampionship,
  getChampionship,
  getChampionshipRoster,
  reactivateChampionship,
  removeChampionshipRosterPlayer,
  updateChampionship,
} from '../api/championships.js'
import { getAllTrainers } from '../api/trainers.js'
import { getTeamRoster } from '../api/teams.js'

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

export default function ChampionshipDetailPage() {
  const { uuid } = useParams()
  const [championship, setChampionship] = useState(null)
  const [teamRoster, setTeamRoster] = useState([])
  const [championshipRoster, setChampionshipRoster] = useState([])
  const [trainers, setTrainers] = useState([])
  const [selectedPlayerUuid, setSelectedPlayerUuid] = useState('')
  const [selectedTrainerUuid, setSelectedTrainerUuid] = useState('')
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadChampionship()
    loadTrainers()
    loadRoster()
  }, [uuid])

  async function loadChampionship() {
    setError('')
    try {
      const response = await getChampionship(uuid)
      setChampionship(response)
      setForm({
        name: response.name,
        description: response.description ?? '',
        startMonth: String(response.startMonth),
        startYear: String(response.startYear),
        endMonth: String(response.endMonth),
        endYear: String(response.endYear),
      })
      await loadTeamRoster(response.teamUuid)
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load championship.')
    }
  }

  async function loadTeamRoster(teamUuid) {
    try {
      setTeamRoster(await getTeamRoster(teamUuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load team roster.')
    }
  }

  async function loadTrainers() {
    try {
      const response = await getAllTrainers({ page: 0, size: 100, active: true })
      const activeTrainers = response.content ?? []
      setTrainers(activeTrainers)
      setSelectedTrainerUuid((current) => current || activeTrainers[0]?.uuid || '')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load trainers.')
    }
  }

  async function loadRoster() {
    try {
      setChampionshipRoster(await getChampionshipRoster(uuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load championship roster.')
    }
  }

  function updateForm(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submitChampionship(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const updated = await updateChampionship(uuid, {
        name: form.name.trim(),
        description: form.description.trim() || null,
        startMonth: Number(form.startMonth),
        startYear: Number(form.startYear),
        endMonth: Number(form.endMonth),
        endYear: Number(form.endYear),
      })
      setChampionship(updated)
      setEditing(false)
      setMessage('Championship updated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save championship.')
    }
  }

  async function toggleStatus() {
    const action = championship.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} ${championship.name}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      const updated = championship.active ? await deactivateChampionship(uuid) : await reactivateChampionship(uuid)
      setChampionship(updated)
      setMessage(championship.active ? 'Championship deactivated.' : 'Championship reactivated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} championship.`)
    }
  }

  async function assignPlayer(event) {
    event.preventDefault()
    if (!selectedPlayerUuid || !selectedTrainerUuid) {
      return
    }
    setError('')
    setMessage('')
    try {
      await assignChampionshipRosterPlayer(uuid, selectedPlayerUuid, selectedTrainerUuid)
      setSelectedPlayerUuid('')
      await loadRoster()
      setMessage('Player assigned to championship.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to assign player to championship.')
    }
  }

  async function removePlayer(assignment) {
    if (!window.confirm(`Remove ${assignment.playerName} from this championship?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      await removeChampionshipRosterPlayer(uuid, assignment.uuid)
      await loadRoster()
      setMessage('Player removed from championship.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to remove player from championship.')
    }
  }

  const availablePlayers = eligiblePlayers(teamRoster, championshipRoster)

  return (
    <main className="container py-5">
      <Link to="/championships">&larr; Back to championships</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {message && <p className="alert alert-success mt-3">{message}</p>}
      {!championship && !error && <p className="mt-3">Loading championship...</p>}

      {championship && (
        <>
          <div className="d-flex justify-content-between align-items-center mt-3 mb-4">
            <div>
              <h1>{championship.name}</h1>
              <p className="text-muted mb-2">{teamLabel(championship)} · {formatPeriod(championship)}</p>
              <span className={`badge ${championship.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                {championship.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <div className="d-flex gap-2">
              <button className="btn btn-outline-primary" disabled={!championship.active} onClick={() => setEditing(true)} type="button">Edit</button>
              <button
                className={`btn ${championship.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                onClick={toggleStatus}
                type="button"
              >
                {championship.active ? 'Deactivate' : 'Reactivate'}
              </button>
            </div>
          </div>

          {editing && form ? (
            <section className="card mb-4">
              <div className="card-body">
                <h2 className="h4">Edit championship</h2>
                <form className="row g-3" onSubmit={submitChampionship}>
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="championship-edit-name">Name</label>
                    <input className="form-control" id="championship-edit-name" name="name" onChange={updateForm} required value={form.name} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="championship-edit-description">Description</label>
                    <input className="form-control" id="championship-edit-description" name="description" onChange={updateForm} value={form.description} />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label" htmlFor="championship-edit-start-month">Start month</label>
                    <select className="form-select" id="championship-edit-start-month" name="startMonth" onChange={updateForm} value={form.startMonth}>
                      {MONTHS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                    </select>
                  </div>
                  <div className="col-md-3">
                    <label className="form-label" htmlFor="championship-edit-start-year">Start year</label>
                    <input className="form-control" id="championship-edit-start-year" min="2000" name="startYear" onChange={updateForm} required type="number" value={form.startYear} />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label" htmlFor="championship-edit-end-month">End month</label>
                    <select className="form-select" id="championship-edit-end-month" name="endMonth" onChange={updateForm} value={form.endMonth}>
                      {MONTHS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                    </select>
                  </div>
                  <div className="col-md-3">
                    <label className="form-label" htmlFor="championship-edit-end-year">End year</label>
                    <input className="form-control" id="championship-edit-end-year" min="2000" name="endYear" onChange={updateForm} required type="number" value={form.endYear} />
                  </div>
                  <div className="col-12 d-flex gap-2">
                    <button className="btn btn-primary" type="submit">Save changes</button>
                    <button className="btn btn-outline-secondary" onClick={() => setEditing(false)} type="button">Cancel</button>
                  </div>
                </form>
              </div>
            </section>
          ) : (
            <dl className="row">
              <dt className="col-sm-3">Description</dt>
              <dd className="col-sm-9">{championship.description || '-'}</dd>
              <dt className="col-sm-3">Team trainer</dt>
              <dd className="col-sm-9">{championship.trainerName}</dd>
            </dl>
          )}

          <section className="mt-5">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h2 className="h3">Championship roster</h2>
                <p className="mb-1"><strong>{championshipRoster.length}</strong> active player{championshipRoster.length === 1 ? '' : 's'}</p>
                <p className="text-muted mb-0">Only active players already assigned to this team can be added.</p>
              </div>
            </div>

            <form className="row g-2 align-items-end mb-4" onSubmit={assignPlayer}>
              <div className="col-md-5">
                <label className="form-label" htmlFor="championship-roster-player">Player</label>
                <select
                  className="form-select"
                  disabled={!championship.active}
                  id="championship-roster-player"
                  onChange={(event) => setSelectedPlayerUuid(event.target.value)}
                  value={selectedPlayerUuid}
                >
                  <option value="">Select a player</option>
                  {availablePlayers.map((assignment) => (
                    <option key={assignment.playerUuid} value={assignment.playerUuid}>{assignment.playerName}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-5">
                <label className="form-label" htmlFor="championship-roster-trainer">Responsible trainer</label>
                <select
                  className="form-select"
                  disabled={!championship.active}
                  id="championship-roster-trainer"
                  onChange={(event) => setSelectedTrainerUuid(event.target.value)}
                  value={selectedTrainerUuid}
                >
                  <option value="">Select a trainer</option>
                  {trainers.map((trainer) => (
                    <option key={trainer.uuid} value={trainer.uuid}>{trainer.name}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-2">
                <button className="btn btn-primary w-100" disabled={!championship.active || !selectedPlayerUuid || !selectedTrainerUuid} type="submit">
                  Assign
                </button>
              </div>
            </form>

            <table className="table table-striped align-middle">
              <thead>
                <tr>
                  <th>Player</th>
                  <th>Age</th>
                  <th>Positions</th>
                  <th>Trainer</th>
                  <th>Assigned date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {championshipRoster.map((assignment) => (
                  <tr key={assignment.uuid}>
                    <td><Link to={`/players/${assignment.playerUuid}`}>{assignment.playerName}</Link></td>
                    <td>{assignment.playerAge}</td>
                    <td>{formatPositions(assignment.playerPositions)}</td>
                    <td>{assignment.trainerName}</td>
                    <td>{formatDate(assignment.assignedDate)}</td>
                    <td>
                      <button className="btn btn-sm btn-outline-danger" disabled={!championship.active} onClick={() => removePlayer(assignment)} type="button">
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {championshipRoster.length === 0 && <p className="text-muted">No players assigned to this championship.</p>}
          </section>
        </>
      )}
    </main>
  )
}

function eligiblePlayers(teamRoster, championshipRoster) {
  const assignedPlayerUuids = new Set(championshipRoster.map((assignment) => assignment.playerUuid))
  return teamRoster.filter((assignment) => !assignedPlayerUuids.has(assignment.playerUuid))
}

function teamLabel(championship) {
  return `${championship.teamIdentification} ${formatTeamCategory(championship.teamCategory)}`
}

function formatTeamCategory(value) {
  return value === 'FEMININE' ? 'Feminine' : 'Masculine'
}

function formatPeriod(championship) {
  return `${monthName(championship.startMonth)} ${championship.startYear} - ${monthName(championship.endMonth)} ${championship.endYear}`
}

function monthName(month) {
  return MONTHS.find(([value]) => Number(value) === Number(month))?.[1] ?? month
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
