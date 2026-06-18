import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { createChampionship, deactivateChampionship, getAllChampionships, reactivateChampionship } from '../api/championships.js'
import { exportChampionshipsCsv } from '../api/reports.js'
import { getAllTeams } from '../api/teams.js'

const PAGE_SIZE = 20
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

const CURRENT_YEAR = new Date().getFullYear()
const EMPTY_FORM = {
  name: '',
  description: '',
  teamUuid: '',
  startMonth: '1',
  startYear: String(CURRENT_YEAR),
  endMonth: '12',
  endYear: String(CURRENT_YEAR),
  expectedMatches: '0',
}

export default function ChampionshipsPage() {
  const location = useLocation()
  const initialTeamFilter = new URLSearchParams(location.search).get('teamUuid') ?? ''
  const [championships, setChampionships] = useState([])
  const [teams, setTeams] = useState([])
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0 })
  const [page, setPage] = useState(0)
  const [nameFilter, setNameFilter] = useState('')
  const [teamFilter, setTeamFilter] = useState(initialTeamFilter)
  const [form, setForm] = useState(EMPTY_FORM)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    loadTeams()
  }, [])

  useEffect(() => {
    loadChampionships()
  }, [page, teamFilter])

  async function loadTeams() {
    setError('')
    try {
      const response = await getAllTeams({ page: 0, size: 200 })
      const activeTeams = (response.content ?? []).filter((team) => team.active)
      setTeams(activeTeams)
      setForm((currentForm) => ({
        ...currentForm,
        teamUuid: currentForm.teamUuid || activeTeams[0]?.uuid || '',
      }))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load teams for championships.')
    }
  }

  async function loadChampionships(activePage = page, activeName = nameFilter) {
    setError('')
    try {
      const response = await getAllChampionships({
        page: activePage,
        size: PAGE_SIZE,
        name: activeName || undefined,
        teamUuid: teamFilter || undefined,
      })
      setChampionships(response.content ?? [])
      setPageInfo({ number: response.number ?? 0, totalPages: response.totalPages ?? 0 })
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load championships.')
    }
  }

  function updateForm(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submitChampionship(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    setSubmitting(true)
    try {
      await createChampionship({
        name: form.name.trim(),
        description: form.description.trim() || null,
        teamUuid: form.teamUuid,
        startMonth: Number(form.startMonth),
        startYear: Number(form.startYear),
        endMonth: Number(form.endMonth),
        endYear: Number(form.endYear),
        expectedMatches: Number(form.expectedMatches),
      })
      setMessage('Championship created.')
      setForm((currentForm) => ({ ...EMPTY_FORM, teamUuid: currentForm.teamUuid }))
      setPage(0)
      await loadChampionships(0)
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save championship.')
    } finally {
      setSubmitting(false)
    }
  }

  async function toggleStatus(championship) {
    const action = championship.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} ${championship.name}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      championship.active ? await deactivateChampionship(championship.uuid) : await reactivateChampionship(championship.uuid)
      setMessage(championship.active ? 'Championship deactivated.' : 'Championship reactivated.')
      await loadChampionships()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} championship.`)
    }
  }

  async function exportCsv() {
    setError('')
    setMessage('')
    try {
      await exportChampionshipsCsv()
      setMessage('Championships CSV export started.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to export championships.')
    }
  }

  function submitSearch(event) {
    event.preventDefault()
    setPage(0)
    loadChampionships(0)
  }

  function changeTeamFilter(event) {
    setTeamFilter(event.target.value)
    setPage(0)
  }

  const canGoPrevious = pageInfo.number > 0
  const canGoNext = pageInfo.totalPages > 0 && pageInfo.number < pageInfo.totalPages - 1

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-start gap-3 mb-4">
        <div>
          <h1>Championships</h1>
          <p className="text-muted mb-0">Create championships and manage the competition roster for each team.</p>
        </div>
        <button className="btn btn-outline-primary" onClick={exportCsv} type="button">
          Export CSV
        </button>
      </div>

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      <section className="card mb-4">
        <div className="card-body">
          <h2 className="h4">Add championship</h2>
          {teams.length === 0 && <p className="alert alert-warning">Create an active team before adding a championship.</p>}
          <form className="row g-3" onSubmit={submitChampionship}>
            <div className="col-md-4">
              <label className="form-label" htmlFor="championship-name">Name</label>
              <input className="form-control" id="championship-name" name="name" onChange={updateForm} required value={form.name} />
            </div>
            <div className="col-md-4">
              <label className="form-label" htmlFor="championship-team">Team</label>
              <select className="form-select" id="championship-team" name="teamUuid" onChange={updateForm} required value={form.teamUuid}>
                <option value="" disabled>Select a team</option>
                {teams.map((team) => (
                  <option key={team.uuid} value={team.uuid}>{teamLabel(team)}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label" htmlFor="championship-description">Description</label>
              <input className="form-control" id="championship-description" name="description" onChange={updateForm} value={form.description} />
            </div>
            <div className="col-md-3">
              <label className="form-label" htmlFor="championship-start-month">Start month</label>
              <select className="form-select" id="championship-start-month" name="startMonth" onChange={updateForm} value={form.startMonth}>
                {MONTHS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label" htmlFor="championship-start-year">Start year</label>
              <input className="form-control" id="championship-start-year" min="2000" name="startYear" onChange={updateForm} required type="number" value={form.startYear} />
            </div>
            <div className="col-md-3">
              <label className="form-label" htmlFor="championship-end-month">End month</label>
              <select className="form-select" id="championship-end-month" name="endMonth" onChange={updateForm} value={form.endMonth}>
                {MONTHS.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label" htmlFor="championship-end-year">End year</label>
              <input className="form-control" id="championship-end-year" min="2000" name="endYear" onChange={updateForm} required type="number" value={form.endYear} />
            </div>
            <div className="col-md-3">
              <label className="form-label" htmlFor="championship-expected-matches">Expected matches</label>
              <input className="form-control" id="championship-expected-matches" min="0" name="expectedMatches" onChange={updateForm} required type="number" value={form.expectedMatches} />
            </div>
            <div className="col-12">
              <button className="btn btn-primary" disabled={submitting || teams.length === 0} type="submit">
                {submitting ? 'Saving...' : 'Save championship'}
              </button>
            </div>
          </form>
        </div>
      </section>

      <form className="row g-3 align-items-end mb-3" onSubmit={submitSearch}>
        <div className="col-md-5">
          <label className="form-label" htmlFor="championship-search">Search championships</label>
          <input className="form-control" id="championship-search" onChange={(event) => setNameFilter(event.target.value)} value={nameFilter} />
        </div>
        <div className="col-md-5">
          <label className="form-label" htmlFor="championship-team-filter">Filter by team</label>
          <select className="form-select" id="championship-team-filter" onChange={changeTeamFilter} value={teamFilter}>
            <option value="">All teams</option>
            {teams.map((team) => (
              <option key={team.uuid} value={team.uuid}>{teamLabel(team)}</option>
            ))}
          </select>
        </div>
        <div className="col-md-2">
          <button className="btn btn-outline-primary w-100" type="submit">Search</button>
        </div>
      </form>

      <table className="table table-striped align-middle">
        <thead>
          <tr>
            <th>Name</th>
            <th>Team</th>
            <th>Period</th>
            <th>Expected matches</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {championships.map((championship) => (
            <tr key={championship.uuid}>
              <td>{championship.name}</td>
              <td>{teamLabel(championship)}</td>
              <td>{formatPeriod(championship)}</td>
              <td>{championship.expectedMatches}</td>
              <td><span className={`badge ${championship.active ? 'text-bg-success' : 'text-bg-secondary'}`}>{championship.active ? 'Active' : 'Inactive'}</span></td>
              <td>
                <div className="d-flex gap-2">
                  <Link className="btn btn-sm btn-outline-primary" to={`/championships/${championship.uuid}`}>View</Link>
                  <button
                    className={`btn btn-sm ${championship.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                    onClick={() => toggleStatus(championship)}
                    type="button"
                  >
                    {championship.active ? 'Deactivate' : 'Reactivate'}
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {championships.length === 0 && <p className="text-muted">No championships found.</p>}

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
  const identification = team.teamIdentification ?? team.identification ?? team.ageGroup
  return `${identification} ${formatTeamCategory(team.teamCategory)}`
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
