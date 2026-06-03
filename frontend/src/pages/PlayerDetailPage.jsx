import { useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { deactivatePlayer, getPlayer, reactivatePlayer, updatePlayer } from '../api/players.js'
import PlayerForm from '../components/players/PlayerForm.jsx'

export default function PlayerDetailPage() {
  const { uuid } = useParams()
  const location = useLocation()
  const [player, setPlayer] = useState(null)
  const [editing, setEditing] = useState(new URLSearchParams(location.search).get('edit') === '1')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadPlayer()
  }, [uuid])

  async function loadPlayer() {
    setError('')
    try {
      setPlayer(await getPlayer(uuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load player.')
    }
  }

  async function submitPlayer(data) {
    setError('')
    setMessage('')
    try {
      setPlayer(await updatePlayer(uuid, data))
      setEditing(false)
      setMessage('Player updated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save player.')
    }
  }

  async function toggleStatus() {
    const action = player.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} player ${player.name}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      const updated = player.active ? await deactivatePlayer(uuid) : await reactivatePlayer(uuid)
      setPlayer(updated)
      setMessage(player.active ? 'Player deactivated.' : 'Player reactivated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? `Unable to ${action} player.`)
    }
  }

  return (
    <main className="container py-5">
      <Link to="/players">&larr; Back to players</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {message && <p className="alert alert-success mt-3">{message}</p>}

      {!player && !error && <p className="mt-3">Loading player...</p>}

      {player && (
        <>
          <div className="d-flex justify-content-between align-items-center mt-3 mb-4">
            <div>
              <h1>{player.name}</h1>
              <span className={`badge ${player.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                {player.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <div className="d-flex gap-2">
              <button className="btn btn-outline-primary" onClick={() => setEditing(true)} type="button">Edit</button>
              <button
                className={`btn ${player.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                onClick={toggleStatus}
                type="button"
              >
                {player.active ? 'Deactivate' : 'Reactivate'}
              </button>
            </div>
          </div>

          {editing ? (
            <div className="card">
              <div className="card-body">
                <h2 className="h4">Edit player</h2>
                <PlayerForm initialPlayer={player} onCancel={() => setEditing(false)} onSubmit={submitPlayer} />
              </div>
            </div>
          ) : (
            <dl className="row">
              <dt className="col-sm-3">Birth country</dt>
              <dd className="col-sm-9">{player.birthCountry}</dd>
              <dt className="col-sm-3">Living country</dt>
              <dd className="col-sm-9">{player.livingCountry}</dd>
              <dt className="col-sm-3">Birthdate</dt>
              <dd className="col-sm-9">{formatDate(player.birthdate)}</dd>
              <dt className="col-sm-3">Age</dt>
              <dd className="col-sm-9">{player.age}</dd>
              <dt className="col-sm-3">Team category</dt>
              <dd className="col-sm-9">{formatTeamCategory(player.teamCategory)}</dd>
              <dt className="col-sm-3">Registration number</dt>
              <dd className="col-sm-9">{player.registrationNumber || '-'}</dd>
              <dt className="col-sm-3">Register date</dt>
              <dd className="col-sm-9">{formatDate(player.registerDate)}</dd>
              <dt className="col-sm-3">Member since</dt>
              <dd className="col-sm-9">{formatDate(player.memberSince)}</dd>
            </dl>
          )}
        </>
      )}
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
