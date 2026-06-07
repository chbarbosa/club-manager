import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { createPlayer, deactivatePlayer, getAllPlayers, reactivatePlayer } from '../api/players.js'
import PlayerForm from '../components/players/PlayerForm.jsx'

const PAGE_SIZE = 20

export default function PlayersPage() {
  const [players, setPlayers] = useState([])
  const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0 })
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const activeSearch = useDebouncedValue(search, 300)

  useEffect(() => {
    loadPlayers()
  }, [page, activeSearch])

  async function loadPlayers() {
    setError('')
    try {
      const response = await getAllPlayers({ page, size: PAGE_SIZE, name: activeSearch || undefined })
      setPlayers(response.content ?? [])
      setPageInfo({ number: response.number ?? 0, totalPages: response.totalPages ?? 0 })
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load players.')
    }
  }

  async function submitPlayer(data) {
    setError('')
    setMessage('')
    try {
      await createPlayer(data)
      setShowForm(false)
      setMessage('Player created.')
      setPage(0)
      await loadPlayers()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save player.')
    }
  }

  async function toggleStatus(player) {
    const action = player.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} player ${player.name}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      if (player.active) {
        await deactivatePlayer(player.uuid)
        setMessage('Player deactivated.')
      } else {
        await reactivatePlayer(player.uuid)
        setMessage('Player reactivated.')
      }
      await loadPlayers()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} player.`)
    }
  }

  function changeSearch(event) {
    setSearch(event.target.value)
    setPage(0)
  }

  const canGoPrevious = pageInfo.number > 0
  const canGoNext = pageInfo.totalPages > 0 && pageInfo.number < pageInfo.totalPages - 1

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h1>Players</h1>
          <p className="text-muted mb-0">Register and manage young athletes for this club.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm(true)} type="button">
          Add Player
        </button>
      </div>

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      {showForm && (
        <div aria-modal="true" className="card mb-4" role="dialog">
          <div className="card-body">
            <h2 className="h4">Add Player</h2>
            <PlayerForm onCancel={() => setShowForm(false)} onSubmit={submitPlayer} />
          </div>
        </div>
      )}

      <div className="mb-3">
        <label className="form-label" htmlFor="player-search">Search players</label>
        <input
          className="form-control"
          id="player-search"
          onChange={changeSearch}
          placeholder="Search by name"
          value={search}
        />
      </div>

      <table className="table table-striped align-middle">
        <thead>
          <tr>
            <th>Name</th>
            <th>Age</th>
            <th>Team category</th>
            <th>Skill level</th>
            <th>Member Since</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {players.map((player) => (
            <tr className={player.active ? '' : 'text-muted'} key={player.uuid}>
              <td>{player.name}</td>
              <td>{player.age}</td>
              <td>{formatTeamCategory(player.teamCategory)}</td>
              <td>{formatSkillLevel(player.currentSkillLevel)}</td>
              <td>{formatDate(player.memberSince)}</td>
              <td>
                <span className={`badge ${player.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                  {player.active ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td>
                <div className="d-flex gap-2">
                  <Link className="btn btn-sm btn-outline-primary" to={`/players/${player.uuid}`}>View</Link>
                  <Link className="btn btn-sm btn-outline-secondary" to={`/players/${player.uuid}?edit=1`}>Edit</Link>
                  <button
                    className={`btn btn-sm ${player.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                    onClick={() => toggleStatus(player)}
                    type="button"
                  >
                    {player.active ? 'Deactivate' : 'Reactivate'}
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {players.length === 0 && <p className="text-muted">No players found.</p>}

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

function formatDate(value) {
  return value ? new Date(`${value}T00:00:00`).toLocaleDateString() : '-'
}

function formatTeamCategory(value) {
  if (value === 'MASCULINE') {
    return 'Masculine'
  }
  if (value === 'FEMININE') {
    return 'Feminine'
  }
  return value ?? '-'
}

function formatSkillLevel(value) {
  if (value === 'DEBUTANT') {
    return 'Debutant'
  }
  if (value === 'ADVANCED') {
    return 'Advanced'
  }
  if (value === 'SKILLED') {
    return 'Skilled'
  }
  return '-'
}

function useDebouncedValue(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value)

  useEffect(() => {
    const timeoutId = window.setTimeout(() => setDebouncedValue(value), delay)
    return () => window.clearTimeout(timeoutId)
  }, [value, delay])

  return debouncedValue
}
