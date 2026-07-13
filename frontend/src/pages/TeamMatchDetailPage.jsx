import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getAllSetup } from '../api/club.js'
import { getTeamMatch, saveMatchPlayerAnalysis, updateTeamMatch } from '../api/matches.js'
import { exportMatchAnalysisCsv } from '../api/reports.js'
import { useAuth } from '../context/AuthContext.jsx'

const IMPROVEMENT_TYPE = 'MATCH_IMPROVEMENT_OPPORTUNITY'
const HIGHLIGHT_TYPE = 'MATCH_HIGHLIGHT'

export default function TeamMatchDetailPage() {
  const { teamUuid, matchUuid } = useParams()
  const { role } = useAuth()
  const canManage = role === 'ADMIN'
  const canExport = role !== 'TRAINER'
  const [match, setMatch] = useState(null)
  const [setup, setSetup] = useState({ improvements: [], highlights: [] })
  const [form, setForm] = useState(null)
  const [analysisForms, setAnalysisForms] = useState({})
  const [selectedPlayerUuid, setSelectedPlayerUuid] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadData()
  }, [teamUuid, matchUuid])

  async function loadData() {
    setError('')
    try {
      const matchResponse = await getTeamMatch(teamUuid, matchUuid)
      setMatch(matchResponse)
      setForm(toMatchForm(matchResponse))
      setAnalysisForms(toAnalysisForms(matchResponse.playerAnalyses ?? []))
      setSelectedPlayerUuid((current) => selectInitialPlayer(matchResponse.playerAnalyses ?? [], current))
      if (canManage) {
        const setupResponse = await getAllSetup()
        setSetup({
          improvements: setupValues(setupResponse, IMPROVEMENT_TYPE),
          highlights: setupValues(setupResponse, HIGHLIGHT_TYPE),
        })
      }
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
      const updatedAnalyses = match.playerAnalyses.map((analysis) => (
        analysis.playerUuid === saved.playerUuid ? saved : analysis
      ))
      setMatch((current) => ({ ...current, playerAnalyses: updatedAnalyses }))
      setAnalysisForms((forms) => ({ ...forms, [saved.playerUuid]: toAnalysisForm(saved) }))
      const nextPlayer = findNextPendingAnalysis(updatedAnalyses, saved.playerUuid)
      if (nextPlayer) {
        setSelectedPlayerUuid(nextPlayer.playerUuid)
        setMessage(`Analysis saved for ${saved.playerName}.`)
      } else {
        setSelectedPlayerUuid(saved.playerUuid)
        setMessage('All players were analyzed.')
      }
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to save player analysis.')
    }
  }

  async function exportCsv() {
    setError('')
    setMessage('')
    try {
      await exportMatchAnalysisCsv(teamUuid, matchUuid)
      setMessage('Match analysis CSV export started.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to export match analysis.')
    }
  }

  const analyses = match?.playerAnalyses ?? []
  const selectedPlayer = analyses.find((player) => player.playerUuid === selectedPlayerUuid) ?? analyses[0]
  const selectedForm = selectedPlayer ? analysisForms[selectedPlayer.playerUuid] ?? emptyAnalysisForm() : emptyAnalysisForm()
  const analyzedPlayers = analyses.filter(isAnalysisComplete)
  const pendingPlayers = analyses.filter((player) => !isAnalysisComplete(player))
  const selectedIsPending = selectedPlayer ? !isAnalysisComplete(selectedPlayer) : false
  const buttonLabel = selectedIsPending && pendingPlayers.length > 1
    ? 'Save analysis and next'
    : 'Save analysis'

  return (
    <main className="container py-5">
      <Link to={`/teams/${teamUuid}`}>&larr; Back to team</Link>
      {error && <p className="alert alert-danger mt-3">{error}</p>}
      {message && <p className="alert alert-success mt-3">{message}</p>}
      {!match && !error && <p className="mt-3">Loading match analysis...</p>}

      {match && form && (
        <>
          <div className="d-flex justify-content-between align-items-start gap-3 mt-3 mb-4">
            <div>
              <h1>Match analysis</h1>
              <p className="text-muted mb-0">
                {match.teamIdentification} vs {match.opponent}
                {match.championshipName ? ` · ${match.championshipName}` : ''}
              </p>
            </div>
            {canExport && (
              <button className="btn btn-outline-primary" onClick={exportCsv} type="button">
                Export CSV
              </button>
            )}
          </div>

          {!canManage && <p className="alert alert-info">This workspace is read-only for your role.</p>}

          {canManage && (
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
          )}

          <section>
            <h2 className="h3">Player analysis</h2>
            <p className="text-muted">Record improvement opportunities, highlights, and trainer notes for current team players.</p>

            {analyses.length > 0 && (
              <div className="card mb-3">
                <div className="card-body">
                  <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-3">
                    <div>
                      <p className="text-muted mb-1">Progress</p>
                      <p className="h5 mb-0">{analyzedPlayers.length} of {analyses.length} players analyzed</p>
                    </div>
                    {analyzedPlayers.length > 0 && (
                      <div className="col-lg-4">
                        <label className="form-label" htmlFor="analyzed-player-select">Review analyzed player</label>
                        <select
                          className="form-select"
                          id="analyzed-player-select"
                          onChange={(event) => setSelectedPlayerUuid(event.target.value)}
                          value={analyzedPlayers.some((player) => player.playerUuid === selectedPlayer?.playerUuid) ? selectedPlayer.playerUuid : ''}
                        >
                          <option disabled value="">Select analyzed player</option>
                          {analyzedPlayers.map((player) => (
                            <option key={player.playerUuid} value={player.playerUuid}>{player.playerName}</option>
                          ))}
                        </select>
                      </div>
                    )}
                  </div>

                  {selectedPlayer && (
                    <>
                      {analyzedPlayers.length === analyses.length && (
                        <p className="alert alert-success">All players were analyzed.</p>
                      )}
                      <div className="card-body">
                        <div className="d-flex justify-content-between align-items-start gap-3">
                          <div>
                            <h3 className="h5">{selectedPlayer.playerName}</h3>
                            <div className="d-flex flex-wrap gap-2">
                              <span className="badge text-bg-light border">Age: {selectedPlayer.playerAge}</span>
                              <span className="badge text-bg-light border">Skill: {formatSkillLevel(selectedPlayer.playerCurrentSkillLevel)}</span>
                              <span className="badge text-bg-light border">Championships: {selectedPlayer.playerChampionshipCount}</span>
                              <span className="badge text-bg-light border">Positions: {formatPositions(selectedPlayer.playerPositions)}</span>
                            </div>
                          </div>
                        </div>

                    <div className="row mt-3">
                      <div className="col-md-6">
                        <h4 className="h6">Improvement opportunities</h4>
                        {canManage ? (
                          <TagCheckboxes
                            field="improvementTags"
                            playerUuid={selectedPlayer.playerUuid}
                            selected={selectedForm.improvementTags}
                            tags={setup.improvements}
                            toggleTag={toggleTag}
                          />
                        ) : (
                          <TagBadges values={selectedForm.improvementTags} />
                        )}
                      </div>
                      <div className="col-md-6">
                        <h4 className="h6">Highlights</h4>
                        {canManage ? (
                          <TagCheckboxes
                            field="highlightTags"
                            playerUuid={selectedPlayer.playerUuid}
                            selected={selectedForm.highlightTags}
                            tags={setup.highlights}
                            toggleTag={toggleTag}
                          />
                        ) : (
                          <TagBadges values={selectedForm.highlightTags} />
                        )}
                      </div>
                    </div>

                    <div className="mt-3">
                      <label className="form-label" htmlFor={`notes-${selectedPlayer.playerUuid}`}>Trainer notes</label>
                      <textarea
                        className="form-control"
                        disabled={!canManage}
                        id={`notes-${selectedPlayer.playerUuid}`}
                        onChange={(event) => updateAnalysis(selectedPlayer.playerUuid, (current) => ({ ...current, notes: event.target.value }))}
                        rows="2"
                        value={selectedForm.notes}
                      />
                    </div>

                    {canManage && (
                      <button className="btn btn-primary mt-3" onClick={() => saveAnalysis(selectedPlayer)} type="button">
                        {buttonLabel}
                      </button>
                    )}
                      </div>
                    </>
                  )}
                </div>
              </div>
            )}

            {analyses.length === 0 && <p className="text-muted">No active players are assigned to this team.</p>}
          </section>
        </>
      )}
    </main>
  )
}

function TagCheckboxes({ disabled = false, field, playerUuid, selected, tags, toggleTag }) {
  return (
    <div className="d-flex flex-column gap-1">
      {tags.map((tag) => (
        <label className="form-check" key={tag}>
          <input
            checked={selected.includes(tag)}
            className="form-check-input"
            disabled={disabled}
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

function TagBadges({ values = [] }) {
  if (!values.length) {
    return <p className="text-muted mb-0">No tags recorded.</p>
  }
  return (
    <div className="d-flex flex-wrap gap-2">
      {values.map((value) => (
        <span className="badge text-bg-light border" key={value}>{value}</span>
      ))}
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

function selectInitialPlayer(analyses, currentPlayerUuid) {
  if (currentPlayerUuid && analyses.some((analysis) => analysis.playerUuid === currentPlayerUuid)) {
    return currentPlayerUuid
  }
  return findNextPendingAnalysis(analyses)?.playerUuid ?? analyses[0]?.playerUuid ?? ''
}

function findNextPendingAnalysis(analyses, currentPlayerUuid = '') {
  return analyses.find((analysis) => analysis.playerUuid !== currentPlayerUuid && !isAnalysisComplete(analysis))
}

function isAnalysisComplete(analysis) {
  return Boolean(
    analysis?.notes?.trim()
      || analysis?.improvementTags?.length
      || analysis?.highlightTags?.length,
  )
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

function formatSkillLevel(value) {
  if (!value) {
    return 'Not defined'
  }
  if (value === 'DEBUTANT') return 'Debutant'
  if (value === 'ADVANCED') return 'Advanced'
  if (value === 'SKILLED') return 'Skilled'
  return value
}
