import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAllTrainers } from '../api/trainers.js'
import { createTeam, deactivateTeam, getAllTeams, reactivateTeam } from '../api/teams.js'
import TeamForm from '../components/teams/TeamForm.jsx'

const PAGE_SIZE = 20

export default function TeamsPage() {
  const [teams, setTeams] = useState([])
  const [trainers, setTrainers] = useState([])
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0 })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [teamCategory, setTeamCategory] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const activeSearch = useDebouncedValue(search, 300)

  useEffect(() => {
    loadTeams()
  }, [page, activeSearch, teamCategory])

  useEffect(() => {
    loadTrainers()
  }, [])

  async function loadTeams() {
    setError('')
    try {
      const response = await getAllTeams({
        page,
        size: PAGE_SIZE,
        ageGroup: activeSearch || undefined,
        teamCategory: teamCategory || undefined,
      })
      setTeams(response.content ?? [])
      setPageInfo({ number: response.number ?? 0, totalPages: response.totalPages ?? 0 })
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load teams.')
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
      await createTeam(data)
      setShowForm(false)
      setMessage('Team created.')
      setPage(0)
      await loadTeams()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save team.')
    }
  }

  async function toggleStatus(team) {
    const action = team.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} team ${teamLabel(team)}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      if (team.active) {
        await deactivateTeam(team.uuid)
        setMessage('Team deactivated.')
      } else {
        await reactivateTeam(team.uuid)
        setMessage('Team reactivated.')
      }
      await loadTeams()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} team.`)
    }
  }

  function changeSearch(event) {
    setSearch(event.target.value)
    setPage(0)
  }

  function changeTeamCategory(event) {
    setTeamCategory(event.target.value)
    setPage(0)
  }

  const canGoPrevious = pageInfo.number > 0
  const canGoNext = pageInfo.totalPages > 0 && pageInfo.number < pageInfo.totalPages - 1

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h1>Teams</h1>
          <p className="text-muted mb-0">Create and manage club teams by age group and category.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm(true)} type="button">
          Add Team
        </button>
      </div>

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      {showForm && (
        <div aria-modal="true" className="card mb-4" role="dialog">
          <div className="card-body">
            <h2 className="h4">Add Team</h2>
            {trainers.length === 0 && <p className="alert alert-warning">Create a trainer before adding a team.</p>}
            <TeamForm onCancel={() => setShowForm(false)} onSubmit={submitTeam} trainers={trainers} />
          </div>
        </div>
      )}

      <div className="row mb-3">
        <div className="col-md-8">
          <label className="form-label" htmlFor="team-search">Search teams</label>
          <input
            className="form-control"
            id="team-search"
            onChange={changeSearch}
            placeholder="Search by age group"
            value={search}
          />
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="team-category-filter">Filter by team category</label>
          <select className="form-select" id="team-category-filter" onChange={changeTeamCategory} value={teamCategory}>
            <option value="">All categories</option>
            <option value="MASCULINE">Masculine</option>
            <option value="FEMININE">Feminine</option>
          </select>
        </div>
      </div>

      <table className="table table-striped align-middle">
        <thead>
          <tr>
            <th>Age Group</th>
            <th>Category</th>
            <th>Trainer</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {teams.map((team) => (
            <tr className={team.active ? '' : 'text-muted'} key={team.uuid}>
              <td>{team.ageGroup}</td>
              <td>{formatTeamCategory(team.teamCategory)}</td>
              <td>{team.trainerName}</td>
              <td>
                <span className={`badge ${team.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                  {team.active ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td>
                <div className="d-flex gap-2">
                  <Link className="btn btn-sm btn-outline-primary" to={`/teams/${team.uuid}`}>View</Link>
                  <Link className="btn btn-sm btn-outline-secondary" to={`/teams/${team.uuid}?edit=1`}>Edit</Link>
                  <button
                    className={`btn btn-sm ${team.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                    onClick={() => toggleStatus(team)}
                    type="button"
                  >
                    {team.active ? 'Deactivate' : 'Reactivate'}
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {teams.length === 0 && <p className="text-muted">No teams found.</p>}

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

function formatTeamCategory(value) {
  return value === 'FEMININE' ? 'Feminine' : 'Masculine'
}

function teamLabel(team) {
  return `${team.ageGroup} ${formatTeamCategory(team.teamCategory)}`
}

function useDebouncedValue(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value)

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setDebouncedValue(value), delay)
    return () => window.clearTimeout(timeoutId)
  }, [value, delay])

  return debouncedValue
}
