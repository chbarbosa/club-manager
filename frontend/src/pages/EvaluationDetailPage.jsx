import { useEffect, useMemo, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import {
  assignEvaluationPlayer,
  cancelEvaluationEvent,
  completeEvaluationEvent,
  createEvaluationEvent,
  finalizeEvaluation,
  getEventAttendance,
  getEvaluation,
  getEvaluationEvents,
  getEvaluationPlayers,
  removeEvaluationPlayer,
  startEvaluation,
  updateEvaluation,
  updateEventAttendance,
} from '../api/evaluations.js'
import { getAllPlayers } from '../api/players.js'
import EvaluationForm from '../components/evaluations/EvaluationForm.jsx'

const EMPTY_EVENT = {
  place: '',
  eventDate: '',
  startTime: '18:00',
  durationMinutes: '60',
}

export default function EvaluationDetailPage() {
  const { uuid } = useParams()
  const location = useLocation()
  const [evaluation, setEvaluation] = useState(null)
  const [evaluationPlayers, setEvaluationPlayers] = useState([])
  const [allPlayers, setAllPlayers] = useState([])
  const [selectedPlayerUuid, setSelectedPlayerUuid] = useState('')
  const [events, setEvents] = useState([])
  const [attendanceByEvent, setAttendanceByEvent] = useState({})
  const [eventForm, setEventForm] = useState(EMPTY_EVENT)
  const [editing, setEditing] = useState(new URLSearchParams(location.search).get('edit') === '1')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadAll()
  }, [uuid])

  const assignedPlayerUuids = useMemo(
    () => new Set(evaluationPlayers.map((assignment) => assignment.playerUuid)),
    [evaluationPlayers],
  )

  const availablePlayers = allPlayers.filter((player) => (
    player.active
    && player.teamCategory === evaluation?.teamCategory
    && !assignedPlayerUuids.has(player.uuid)
  ))

  async function loadAll() {
    setError('')
    try {
      const [evaluationResponse, playersResponse, allPlayersResponse, eventResponse] = await Promise.all([
        getEvaluation(uuid),
        getEvaluationPlayers(uuid),
        getAllPlayers({ page: 0, size: 500 }),
        getEvaluationEvents(uuid),
      ])
      setEvaluation(evaluationResponse)
      setEvaluationPlayers(playersResponse)
      setAllPlayers(allPlayersResponse.content ?? [])
      setEvents(eventResponse)
      setSelectedPlayerUuid('')
      await loadAttendance(eventResponse)
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load evaluation.')
    }
  }

  async function loadAttendance(eventList = events) {
    const entries = await Promise.all(eventList.map(async (event) => [event.uuid, await getEventAttendance(event.uuid)]))
    setAttendanceByEvent(Object.fromEntries(entries))
  }

  async function submitEvaluation(data) {
    setError('')
    setMessage('')
    try {
      setEvaluation(await updateEvaluation(uuid, data))
      setEditing(false)
      setMessage('Evaluation updated.')
      await loadAll()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save evaluation.')
    }
  }

  async function changeStatus(action) {
    setError('')
    setMessage('')
    try {
      const updated = action === 'start' ? await startEvaluation(uuid) : await finalizeEvaluation(uuid)
      setEvaluation(updated)
      setMessage(action === 'start' ? 'Evaluation started.' : 'Evaluation finalized.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} evaluation.`)
    }
  }

  async function assignPlayer(event) {
    event.preventDefault()
    if (!selectedPlayerUuid) {
      return
    }
    await runAction('Player assigned.', async () => {
      await assignEvaluationPlayer(uuid, selectedPlayerUuid)
      await loadAll()
    })
  }

  async function removePlayer(assignmentUuid) {
    await runAction('Player removed.', async () => {
      await removeEvaluationPlayer(uuid, assignmentUuid)
      await loadAll()
    })
  }

  async function submitEvent(event) {
    event.preventDefault()
    await runAction('Event created.', async () => {
      await createEvaluationEvent(uuid, {
        ...eventForm,
        durationMinutes: Number(eventForm.durationMinutes),
      })
      setEventForm(EMPTY_EVENT)
      await loadAll()
    })
  }

  async function saveAttendance(eventUuid, playerUuid, values) {
    await runAction('Attendance saved.', async () => {
      await updateEventAttendance(eventUuid, playerUuid, values)
      const attendance = await getEventAttendance(eventUuid)
      setAttendanceByEvent({ ...attendanceByEvent, [eventUuid]: attendance })
    })
  }

  async function completeEvent(eventUuid) {
    await runAction('Event completed.', async () => {
      await completeEvaluationEvent(eventUuid)
      await loadAll()
    })
  }

  async function cancelEvent(eventUuid) {
    await runAction('Event canceled.', async () => {
      await cancelEvaluationEvent(eventUuid)
      await loadAll()
    })
  }

  async function runAction(successMessage, action) {
    setError('')
    setMessage('')
    try {
      await action()
      setMessage(successMessage)
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Action failed.')
    }
  }

  const evaluationLocked = evaluation?.status === 'FINALIZED'

  return (
    <main className="container py-5">
      <Link to="/evaluations">&larr; Back to evaluations</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {message && <p className="alert alert-success mt-3">{message}</p>}
      {!evaluation && !error && <p className="mt-3">Loading evaluation...</p>}

      {evaluation && (
        <>
          <div className="d-flex justify-content-between align-items-center mt-3 mb-4">
            <div>
              <h1>{evaluation.title}</h1>
              <p className="text-muted mb-1">{groupLabel(evaluation)}</p>
              <span className={`badge ${statusClass(evaluation.status)}`}>{formatStatus(evaluation.status)}</span>
            </div>
            <div className="d-flex gap-2">
              {evaluation.status !== 'FINALIZED' && (
                <button className="btn btn-outline-primary" onClick={() => setEditing(true)} type="button">Edit</button>
              )}
              {evaluation.status === 'OPEN' && (
                <button className="btn btn-outline-success" onClick={() => changeStatus('start')} type="button">Start</button>
              )}
              {evaluation.status !== 'FINALIZED' && (
                <button className="btn btn-outline-danger" onClick={() => changeStatus('finalize')} type="button">Finalize</button>
              )}
            </div>
          </div>

          {editing ? (
            <div className="card mb-4">
              <div className="card-body">
                <h2 className="h4">Edit evaluation</h2>
                <EvaluationForm initialEvaluation={evaluation} onCancel={() => setEditing(false)} onSubmit={submitEvaluation} />
              </div>
            </div>
          ) : (
            <dl className="row">
              <dt className="col-sm-3">Group</dt>
              <dd className="col-sm-9">{groupLabel(evaluation)}</dd>
              <dt className="col-sm-3">Created date</dt>
              <dd className="col-sm-9">{formatDate(evaluation.createdDate)}</dd>
            </dl>
          )}

          <section className="card mb-4">
            <div className="card-body">
              <h2 className="h4">Players</h2>
              {evaluationLocked ? (
                <p className="text-muted">This evaluation is finalized. Player assignments are locked.</p>
              ) : (
                <form className="row g-2 align-items-end mb-3" onSubmit={assignPlayer}>
                  <div className="col-md-8">
                    <label className="form-label" htmlFor="evaluation-player">Assign player</label>
                    <select className="form-select" id="evaluation-player" onChange={(event) => setSelectedPlayerUuid(event.target.value)} value={selectedPlayerUuid}>
                      <option value="">Select a matching player</option>
                      {availablePlayers.map((player) => (
                        <option key={player.uuid} value={player.uuid}>{player.name}</option>
                      ))}
                    </select>
                  </div>
                  <div className="col-md-4">
                    <button className="btn btn-primary" disabled={!selectedPlayerUuid} type="submit">Assign player</button>
                  </div>
                </form>
              )}
              <table className="table table-sm">
                <thead>
                  <tr><th>Name</th><th>Assigned</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {evaluationPlayers.map((assignment) => (
                    <tr key={assignment.uuid}>
                      <td>{assignment.playerName}</td>
                      <td>{formatDate(assignment.assignedDate)}</td>
                      <td>
                        {evaluationLocked ? (
                          <span className="text-muted">Locked</span>
                        ) : (
                          <button className="btn btn-sm btn-outline-danger" onClick={() => removePlayer(assignment.uuid)} type="button">Remove</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {evaluationPlayers.length === 0 && <p className="text-muted">No players assigned yet.</p>}
            </div>
          </section>

          <section className="card">
            <div className="card-body">
              <h2 className="h4">Events</h2>
              {evaluationLocked ? (
                <p className="text-muted">This evaluation is finalized. Events are locked.</p>
              ) : (
                <form className="row g-2 align-items-end mb-4" onSubmit={submitEvent}>
                  <div className="col-md-3">
                    <label className="form-label" htmlFor="event-place">Place</label>
                    <input className="form-control" id="event-place" onChange={(event) => setEventForm({ ...eventForm, place: event.target.value })} required value={eventForm.place} />
                  </div>
                  <div className="col-md-3">
                    <label className="form-label" htmlFor="event-date">Date</label>
                    <input className="form-control" id="event-date" onChange={(event) => setEventForm({ ...eventForm, eventDate: event.target.value })} required type="date" value={eventForm.eventDate} />
                  </div>
                  <div className="col-md-2">
                    <label className="form-label" htmlFor="event-time">Start time</label>
                    <input className="form-control" id="event-time" onChange={(event) => setEventForm({ ...eventForm, startTime: event.target.value })} required type="time" value={eventForm.startTime} />
                  </div>
                  <div className="col-md-2">
                    <label className="form-label" htmlFor="event-duration">Duration</label>
                    <select className="form-select" id="event-duration" onChange={(event) => setEventForm({ ...eventForm, durationMinutes: event.target.value })} value={eventForm.durationMinutes}>
                      <option value="60">1h</option>
                      <option value="90">1.5h</option>
                      <option value="120">2h</option>
                    </select>
                  </div>
                  <div className="col-md-2">
                    <button className="btn btn-primary" type="submit">Add event</button>
                  </div>
                </form>
              )}

              {events.map((event) => (
                <EvaluationEventCard
                  attendance={attendanceByEvent[event.uuid] ?? []}
                  event={event}
                  evaluationLocked={evaluationLocked}
                  key={event.uuid}
                  onCancel={() => cancelEvent(event.uuid)}
                  onComplete={() => completeEvent(event.uuid)}
                  onSaveAttendance={saveAttendance}
                  players={evaluationPlayers}
                />
              ))}
              {events.length === 0 && <p className="text-muted">No events scheduled yet.</p>}
            </div>
          </section>
        </>
      )}
    </main>
  )
}

function EvaluationEventCard({ attendance, evaluationLocked, event, onCancel, onComplete, onSaveAttendance, players }) {
  const attendanceByPlayer = new Map(attendance.map((entry) => [entry.playerUuid, entry]))

  return (
    <div className="border rounded p-3 mb-3">
      <div className="d-flex justify-content-between align-items-center">
        <div>
          <strong>{event.place}</strong>
          <span className="ms-2 text-muted">{formatDate(event.eventDate)} {event.startTime} ({formatDuration(event.durationMinutes)})</span>
          <span className={`badge ms-2 ${event.status === 'COMPLETED' ? 'text-bg-success' : event.status === 'CANCELED' ? 'text-bg-secondary' : 'text-bg-warning'}`}>
            {event.status}
          </span>
        </div>
        {!evaluationLocked && event.status === 'SCHEDULED' && (
          <div className="d-flex gap-2">
            <button className="btn btn-sm btn-outline-success" onClick={onComplete} type="button">Complete event</button>
            <button className="btn btn-sm btn-outline-secondary" onClick={onCancel} type="button">Cancel</button>
          </div>
        )}
      </div>
      <table className="table table-sm mt-3">
        <thead>
          <tr><th>Player</th><th>Participated</th><th>Skill level</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {players.map((assignment) => {
            const current = attendanceByPlayer.get(assignment.playerUuid)
            return (
              <AttendanceRow
                assignment={assignment}
                current={current}
                disabled={evaluationLocked || event.status !== 'SCHEDULED'}
                eventUuid={event.uuid}
                key={assignment.playerUuid}
                onSave={onSaveAttendance}
              />
            )
          })}
        </tbody>
      </table>
    </div>
  )
}

function AttendanceRow({ assignment, current, disabled, eventUuid, onSave }) {
  const [status, setStatus] = useState(current?.status ?? 'PRESENT')
  const [skillLevel, setSkillLevel] = useState(current?.skillLevel ?? 'DEBUTANT')

  useEffect(() => {
    setStatus(current?.status ?? 'PRESENT')
    setSkillLevel(current?.skillLevel ?? 'DEBUTANT')
  }, [current])

  return (
    <tr>
      <td>{assignment.playerName}</td>
      <td>
        <select className="form-select form-select-sm" disabled={disabled} onChange={(event) => setStatus(event.target.value)} value={status}>
          <option value="PRESENT">Present</option>
          <option value="ABSENT">Absent</option>
        </select>
      </td>
      <td>
        <select className="form-select form-select-sm" disabled={disabled} onChange={(event) => setSkillLevel(event.target.value)} value={skillLevel}>
          <option value="DEBUTANT">Debutant</option>
          <option value="ADVANCED">Advanced</option>
          <option value="SKILLED">Skilled</option>
        </select>
      </td>
      <td>
        <button className="btn btn-sm btn-outline-primary" disabled={disabled} onClick={() => onSave(eventUuid, assignment.playerUuid, { status, skillLevel })} type="button">
          Save
        </button>
      </td>
    </tr>
  )
}

function groupLabel(value) {
  return `${value.ageGroup} ${value.teamCategory === 'FEMININE' ? 'Feminine' : 'Masculine'}`
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

function formatDuration(value) {
  if (value === 60) {
    return '1h'
  }
  if (value === 90) {
    return '1.5h'
  }
  return '2h'
}
