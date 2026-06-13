import { useEffect, useState } from 'react'
import { getFields } from '../api/fields.js'
import { exportSchedulesCsv } from '../api/reports.js'
import { createSchedule, cancelSchedule, getAllSchedules } from '../api/schedules.js'
import { getAllTeams } from '../api/teams.js'

const PAGE_SIZE = 20

const EMPTY_FORM = {
  teamUuid: '',
  fieldUuid: '',
  date: '',
  time: '18:00',
  durationMinutes: '90',
  type: 'TRAINING',
  notes: '',
}

export default function SchedulesPage() {
  const [schedules, setSchedules] = useState([])
  const [teams, setTeams] = useState([])
  const [fields, setFields] = useState([])
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0 })
  const [page, setPage] = useState(0)
  const [teamFilter, setTeamFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('SCHEDULED')
  const [form, setForm] = useState(EMPTY_FORM)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    loadReferenceData()
  }, [])

  useEffect(() => {
    loadSchedules()
  }, [page, teamFilter, statusFilter])

  async function loadReferenceData() {
    setError('')
    try {
      const [teamResponse, fieldResponse] = await Promise.all([
        getAllTeams({ page: 0, size: 100 }),
        getFields(),
      ])
      const activeTeams = (teamResponse.content ?? []).filter((team) => team.active)
      setTeams(activeTeams)
      setFields(fieldResponse)
      setForm((currentForm) => ({
        ...currentForm,
        teamUuid: currentForm.teamUuid || activeTeams[0]?.uuid || '',
        fieldUuid: currentForm.fieldUuid || fieldResponse[0]?.uuid || '',
      }))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load schedule setup data.')
    }
  }

  async function loadSchedules() {
    setError('')
    try {
      const response = await getAllSchedules({
        page,
        size: PAGE_SIZE,
        teamUuid: teamFilter || undefined,
        status: statusFilter || undefined,
      })
      setSchedules(response.content ?? [])
      setPageInfo({ number: response.number ?? 0, totalPages: response.totalPages ?? 0 })
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load schedules.')
    }
  }

  function updateForm(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submitSchedule(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    setSubmitting(true)
    try {
      await createSchedule({
        teamUuid: form.teamUuid,
        fieldUuid: form.fieldUuid,
        dateTime: `${form.date}T${form.time}`,
        durationMinutes: Number(form.durationMinutes),
        type: form.type,
        notes: form.notes.trim() || null,
      })
      setMessage('Schedule created.')
      setForm((currentForm) => ({ ...EMPTY_FORM, teamUuid: currentForm.teamUuid, fieldUuid: currentForm.fieldUuid }))
      setPage(0)
      await loadSchedules()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save schedule.')
    } finally {
      setSubmitting(false)
    }
  }

  async function cancelEntry(schedule) {
    if (!window.confirm(`Cancel ${formatType(schedule.type).toLowerCase()} for ${schedule.teamIdentification}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      await cancelSchedule(schedule.uuid, 'Canceled by admin')
      setMessage('Schedule canceled.')
      await loadSchedules()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to cancel schedule.')
    }
  }

  async function exportCsv() {
    setError('')
    setMessage('')
    try {
      await exportSchedulesCsv()
      setMessage('Schedules CSV export started.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to export schedules.')
    }
  }

  function changeTeamFilter(event) {
    setTeamFilter(event.target.value)
    setPage(0)
  }

  function changeStatusFilter(event) {
    setStatusFilter(event.target.value)
    setPage(0)
  }

  const canGoPrevious = pageInfo.number > 0
  const canGoNext = pageInfo.totalPages > 0 && pageInfo.number < pageInfo.totalPages - 1

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1>Schedules</h1>
          <p className="text-muted mb-0">Plan club sessions for teams and fields.</p>
        </div>
        <button className="btn btn-outline-primary" onClick={exportCsv} type="button">
          Export CSV
        </button>
      </div>

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      <section className="card mb-4">
        <div className="card-body">
          <h2 className="h4">Add schedule</h2>
          {teams.length === 0 && <p className="alert alert-warning">Create an active team before scheduling.</p>}
          {fields.length === 0 && <p className="alert alert-warning">No active fields are available.</p>}
          <form className="row g-3" onSubmit={submitSchedule}>
            <div className="col-md-4">
              <label className="form-label" htmlFor="schedule-team">Team</label>
              <select className="form-select" id="schedule-team" name="teamUuid" onChange={updateForm} required value={form.teamUuid}>
                <option value="" disabled>Select a team</option>
                {teams.map((team) => (
                  <option key={team.uuid} value={team.uuid}>{teamLabel(team)}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label" htmlFor="schedule-field">Field</label>
              <select className="form-select" id="schedule-field" name="fieldUuid" onChange={updateForm} required value={form.fieldUuid}>
                <option value="" disabled>Select a field</option>
                {fields.map((field) => (
                  <option key={field.uuid} value={field.uuid}>{field.name}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <label className="form-label" htmlFor="schedule-date">Date</label>
              <input className="form-control" id="schedule-date" name="date" onChange={updateForm} required type="date" value={form.date} />
            </div>
            <div className="col-md-2">
              <label className="form-label" htmlFor="schedule-time">Time</label>
              <input className="form-control" id="schedule-time" name="time" onChange={updateForm} required type="time" value={form.time} />
            </div>
            <div className="col-md-3">
              <label className="form-label" htmlFor="schedule-duration">Duration</label>
              <select className="form-select" id="schedule-duration" name="durationMinutes" onChange={updateForm} value={form.durationMinutes}>
                <option value="60">1h</option>
                <option value="90">1.5h</option>
                <option value="120">2h</option>
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label" htmlFor="schedule-type">Type</label>
              <select className="form-select" id="schedule-type" name="type" onChange={updateForm} value={form.type}>
                <option value="TRAINING">Training</option>
                <option value="MATCH">Match</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="schedule-notes">Notes</label>
              <input className="form-control" id="schedule-notes" name="notes" onChange={updateForm} value={form.notes} />
            </div>
            <div className="col-12">
              <button className="btn btn-primary" disabled={submitting || teams.length === 0 || fields.length === 0} type="submit">
                {submitting ? 'Saving...' : 'Save schedule'}
              </button>
            </div>
          </form>
        </div>
      </section>

      <div className="row mb-3">
        <div className="col-md-6">
          <label className="form-label" htmlFor="schedule-team-filter">Filter by team</label>
          <select className="form-select" id="schedule-team-filter" onChange={changeTeamFilter} value={teamFilter}>
            <option value="">All teams</option>
            {teams.map((team) => (
              <option key={team.uuid} value={team.uuid}>{teamLabel(team)}</option>
            ))}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="schedule-status-filter">Filter by status</label>
          <select className="form-select" id="schedule-status-filter" onChange={changeStatusFilter} value={statusFilter}>
            <option value="">All statuses</option>
            <option value="SCHEDULED">Scheduled</option>
            <option value="CANCELED">Canceled</option>
          </select>
        </div>
      </div>

      <table className="table table-striped align-middle">
        <thead>
          <tr>
            <th>Team</th>
            <th>Field</th>
            <th>Date and time</th>
            <th>Duration</th>
            <th>Type</th>
            <th>Status</th>
            <th>Notes</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {schedules.map((schedule) => (
            <tr className={schedule.status === 'CANCELED' ? 'text-muted' : ''} key={schedule.uuid}>
              <td>{schedule.teamIdentification}</td>
              <td>{schedule.fieldName}</td>
              <td>{formatDateTime(schedule.dateTime)}</td>
              <td>{formatDuration(schedule.durationMinutes)}</td>
              <td>{formatType(schedule.type)}</td>
              <td><span className={`badge ${schedule.status === 'CANCELED' ? 'text-bg-secondary' : 'text-bg-success'}`}>{formatStatus(schedule.status)}</span></td>
              <td>{schedule.cancelReason ? `Canceled: ${schedule.cancelReason}` : schedule.notes ?? '-'}</td>
              <td>
                {schedule.status === 'SCHEDULED' ? (
                  <button className="btn btn-sm btn-outline-danger" onClick={() => cancelEntry(schedule)} type="button">
                    Cancel
                  </button>
                ) : '-'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {schedules.length === 0 && <p className="text-muted">No schedules found.</p>}

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

function teamLabel(team) {
  return `${team.identification ?? team.ageGroup} ${team.teamCategory === 'FEMININE' ? 'Feminine' : 'Masculine'}`
}

function formatDateTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}

function formatDuration(value) {
  if (value === 90) return '1.5h'
  return `${value / 60}h`
}

function formatType(value) {
  if (value === 'TRAINING') return 'Training'
  if (value === 'MATCH') return 'Match'
  return 'Other'
}

function formatStatus(value) {
  return value === 'CANCELED' ? 'Canceled' : 'Scheduled'
}
