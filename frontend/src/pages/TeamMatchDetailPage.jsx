import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getAllSetup } from '../api/club.js'
import { getTeamMatch, saveMatchPlayerAnalysis, updateTeamMatch } from '../api/matches.js'

const IMPROVEMENT_TYPE = 'MATCH_IMPROVEMENT_OPPORTUNITY'
const HIGHLIGHT_TYPE = 'MATCH_HIGHLIGHT'

export default function TeamMatchDetailPage() {
  const { teamUuid, matchUuid } = useParams()
  const [match, setMatch] = useState(null)
  const [setup, setSetup] = useState({ improvements: [], highlights: [] })
  const [form, setForm] = useState(null)
  const [analysisForms, setAnalysisForms] = useState({})
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadData()
  }, [teamUuid, matchUuid])

  async function loadData() {
    setError('')
    try {
      const [matchResponse, setupResponse] = await Promise.all([
        getTeamMatch(teamUuid, matchUuid),
        getAllSetup(),
      ])
      setMatch(matchResponse)
      setForm(toMatchForm(matchResponse))
      setAnalysisForms(toAnalysisForms(matchResponse.playerAnalyses ?? []))
      setSetup({
        improvements: setupValues(setupResponse, IMPROVEMENT_TYPE),
        highlights: setupValues(setupResponse, HIGHLIGHT_TYPE),
      })
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load match analysis.')
    }
  }

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submitMatch(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const updated = await updateTeamMatch(teamUuid, matchUuid, {
        opponent: form.opponent.trim(),
        place: form.place.trim(),
        matchDateTime: `${form.matchDate}T${form.matchTime}`,
        teamScore: form.teamScore === '' ? null : Number(form.teamScore),
        opponentScore: form.opponentScore === '' ? null : Number(form.opponentScore),
        notes: form.notes.trim() || null,
      })
      setMatch((current) => ({ ...current, ...updated, playerAnalyses: current.playerAnalyses }))
      setMessage('Match updated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save match.')
    }
  }

  function updateAnalysis(playerUuid, updater) {
    setAnalysisForms((forms) => ({
      ...forms,
      [playerUuid]: updater(forms[playerUuid] ?? emptyAnalysisForm()),
    }))
  }

  function toggleTag(playerUuid, field, tag) {
    updateAnalysis(playerUuid, (current) => {
      const values = new Set(current[field])
      values.has(tag) ? values.delete(tag) : values.add(tag)
      return { ...current, [field]: [...values] }
    })
  }

  async function saveAnalysis(player) {
    setError('')
    setMessage('')
    const data = analysisForms[player.playerUuid] ?? emptyAnalysisForm()
    try {
      const saved = await saveMatchPlayerAnalysis(teamUuid, matchUuid, player.playerUuid, {
        improvementTags: data.improvementTags,
        highlightTags: data.highlightTags,
        notes: data.notes.trim() || null,
      })
      setMatch((current) => ({
        ...current,
        playerAnalyses: current.playerAnalyses.map((analysis) => (
          analysis.playerUuid === saved.playerUuid ? saved : analysis
        )),
      }))
      setAnalysisForms((forms) => ({ ...forms, [saved.playerUuid]: toAnalysisForm(saved) }))
      setMessage(`Analysis saved for ${saved.playerName}.`)
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save player analysis.')
    }
  }

  return (
    <main className="container py-5">
      <Link to={`/teams/${teamUuid}`}>&larr; Back to team</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {message && <p className="alert alert-success mt-3">{message}</p>}
      {!match && !error && <p className="mt-3">Loading match analysis...</p>}

      {match && form && (
        <>
          <div className="mt-3 mb-4">
            <h1>Match analysis</h1>
            <p className="text-muted mb-0">
              {match.teamIdentification} vs {match.opponent}
              {match.championshipName ? ` · ${match.championshipName}` : ''}
            </p>
          </div>

          <form className="card mb-4" onSubmit={submitMatch}>
            <div className="card-body">
              <h2 className="h4">Match details</h2>
              <div className="row g-3">
                <div className="col-md-4">
                  <label className="form-label" htmlFor="match-edit-opponent">Opponent</label>
                  <input className="form-control" id="match-edit-opponent" name="opponent" onChange={updateField} required value={form.opponent} />
                </div>
                <div className="col-md-4">
                  <label className="form-label" htmlFor="match-edit-place">Place</label>
                  <input className="form-control" id="match-edit-place" name="place" onChange={updateField} required value={form.place} />
                </div>
                <div className="col-md-2">
                  <label className="form-label" htmlFor="match-edit-date">Date</label>
                  <input className="form-control" id="match-edit-date" name="matchDate" onChange={updateField} required type="date" value={form.matchDate} />
                </div>
                <div className="col-md-2">
                  <label className="form-label" htmlFor="match-edit-time">Time</label>
                  <input className="form-control" id="match-edit-time" name="matchTime" onChange={updateField} required type="time" value={form.matchTime} />
                </div>
                <div className="col-md-2">
                  <label className="form-label" htmlFor="match-edit-team-score">Team score</label>
                  <input className="form-control" id="match-edit-team-score" min="0" name="teamScore" onChange={updateField} type="number" value={form.teamScore} />
                </div>
                <div className="col-md-2">
                  <label className="form-label" htmlFor="match-edit-opponent-score">Opponent score</label>
                  <input className="form-control" id="match-edit-opponent-score" min="0" name="opponentScore" onChange={updateField} type="number" value={form.opponentScore} />
                </div>
                <div className="col-md-8">
                  <label className="form-label" htmlFor="match-edit-notes">Notes</label>
                  <input className="form-control" id="match-edit-notes" name="notes" onChange={updateField} value={form.notes} />
                </div>
                <div className="col-12">
                  <button className="btn btn-primary" type="submit">Save match details</button>
                </div>
              </div>
            </div>
          </form>

          <section>
            <h2 className="h3">Player analysis</h2>
            <p className="text-muted">Record improvement opportunities, highlights, and trainer notes for current team players.</p>

            {match.playerAnalyses.map((player) => {
              const playerForm = analysisForms[player.playerUuid] ?? emptyAnalysisForm()
              return (
                <div className="card mb-3" key={player.playerUuid}>
                  <div className="card-body">
                    <div className="d-flex justify-content-between align-items-start gap-3">
                      <div>
                        <h3 className="h5">{player.playerName}</h3>
                        <p className="text-muted mb-0">{formatPositions(player.playerPositions)}</p>
                      </div>
                      <Link className="btn btn-sm btn-outline-secondary" to={`/players/${player.playerUuid}`}>View player</Link>
                    </div>

                    <div className="row mt-3">
                      <div className="col-md-6">
                        <h4 className="h6">Improvement opportunities</h4>
                        <TagCheckboxes
                          field="improvementTags"
                          playerUuid={player.playerUuid}
                          selected={playerForm.improvementTags}
                          tags={setup.improvements}
                          toggleTag={toggleTag}
                        />
                      </div>
                      <div className="col-md-6">
                        <h4 className="h6">Highlights</h4>
                        <TagCheckboxes
                          field="highlightTags"
                          playerUuid={player.playerUuid}
                          selected={playerForm.highlightTags}
                          tags={setup.highlights}
                          toggleTag={toggleTag}
                        />
                      </div>
                    </div>

                    <div className="mt-3">
                      <label className="form-label" htmlFor={`notes-${player.playerUuid}`}>Trainer notes</label>
                      <textarea
                        className="form-control"
                        id={`notes-${player.playerUuid}`}
                        onChange={(event) => updateAnalysis(player.playerUuid, (current) => ({ ...current, notes: event.target.value }))}
                        rows="2"
                        value={playerForm.notes}
                      />
                    </div>

                    <button className="btn btn-primary mt-3" onClick={() => saveAnalysis(player)} type="button">
                      Save analysis
                    </button>
                  </div>
                </div>
              )
            })}

            {match.playerAnalyses.length === 0 && <p className="text-muted">No active players are assigned to this team.</p>}
          </section>
        </>
      )}
    </main>
  )
}

function TagCheckboxes({ field, playerUuid, selected, tags, toggleTag }) {
  return (
    <div className="d-flex flex-column gap-1">
      {tags.map((tag) => (
        <label className="form-check" key={tag}>
          <input
            checked={selected.includes(tag)}
            className="form-check-input"
            onChange={() => toggleTag(playerUuid, field, tag)}
            type="checkbox"
          />
          <span className="form-check-label">{tag}</span>
        </label>
      ))}
      {tags.length === 0 && <p className="text-muted mb-0">No tags configured.</p>}
    </div>
  )
}

function toMatchForm(match) {
  const [matchDate, rawTime] = match.matchDateTime.split('T')
  return {
    opponent: match.opponent,
    place: match.place,
    matchDate,
    matchTime: rawTime.slice(0, 5),
    teamScore: match.teamScore ?? '',
    opponentScore: match.opponentScore ?? '',
    notes: match.notes ?? '',
  }
}

function toAnalysisForms(analyses) {
  return Object.fromEntries(analyses.map((analysis) => [analysis.playerUuid, toAnalysisForm(analysis)]))
}

function toAnalysisForm(analysis) {
  return {
    improvementTags: analysis.improvementTags ?? [],
    highlightTags: analysis.highlightTags ?? [],
    notes: analysis.notes ?? '',
  }
}

function emptyAnalysisForm() {
  return { improvementTags: [], highlightTags: [], notes: '' }
}

function setupValues(setupEntries, type) {
  const setup = setupEntries.find((entry) => entry.type === type)
  return setup ? JSON.parse(setup.jsonData) : []
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
