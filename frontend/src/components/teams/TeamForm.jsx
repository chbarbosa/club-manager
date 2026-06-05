import { useEffect, useState } from 'react'

const EMPTY_FORM = {
  ageGroup: '',
  teamCategory: 'MASCULINE',
  trainerUuid: '',
}

export default function TeamForm({ initialTeam, onCancel, onSubmit, trainers }) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    setForm(initialTeam ? {
      ageGroup: initialTeam.ageGroup ?? '',
      teamCategory: initialTeam.teamCategory ?? 'MASCULINE',
      trainerUuid: initialTeam.trainerUuid ?? '',
    } : {
      ...EMPTY_FORM,
      trainerUuid: trainers[0]?.uuid ?? '',
    })
  }, [initialTeam, trainers])

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      await onSubmit({
        ageGroup: form.ageGroup.trim(),
        teamCategory: form.teamCategory,
        trainerUuid: form.trainerUuid,
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit}>
      <div className="row">
        <div className="col-md-4 mb-3">
          <label className="form-label" htmlFor="team-age-group">Age group</label>
          <input
            className="form-control"
            id="team-age-group"
            name="ageGroup"
            onChange={updateField}
            placeholder="Under 13"
            required
            value={form.ageGroup}
          />
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label" htmlFor="team-category">Team category</label>
          <select
            className="form-select"
            id="team-category"
            name="teamCategory"
            onChange={updateField}
            required
            value={form.teamCategory}
          >
            <option value="MASCULINE">Masculine</option>
            <option value="FEMININE">Feminine</option>
          </select>
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label" htmlFor="team-trainer">Trainer</label>
          <select
            className="form-select"
            id="team-trainer"
            name="trainerUuid"
            onChange={updateField}
            required
            value={form.trainerUuid}
          >
            <option value="" disabled>Select a trainer</option>
            {trainers.map((trainer) => (
              <option key={trainer.uuid} value={trainer.uuid}>
                {trainer.name}{trainer.active ? '' : ' (inactive)'}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div className="d-flex gap-2">
        <button className="btn btn-primary" disabled={submitting || trainers.length === 0} type="submit">
          {submitting ? 'Saving...' : 'Save team'}
        </button>
        <button className="btn btn-outline-secondary" onClick={onCancel} type="button">Cancel</button>
      </div>
    </form>
  )
}

