import { useEffect, useState } from 'react'

const EMPTY_FORM = {
  identification: '',
  ageCategory: 'U13',
  teamCategory: 'MASCULINE',
  trainerUuid: '',
  subTrainerUuid: '',
  assistantAdminUuid: '',
}

const AGE_CATEGORIES = [
  ['U7', '7'],
  ['U8', '8'],
  ['U9', '9'],
  ['U10', '10'],
  ['U11', '11'],
  ['U12', '12'],
  ['U13', '13'],
  ['U14', '14'],
  ['U15', '15'],
  ['U16', '16'],
  ['U17_18', '17-18'],
  ['U19_PLUS', '19+'],
]

export default function TeamForm({ admins = [], initialTeam, onCancel, onSubmit, trainers }) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (initialTeam) {
      setForm({
        identification: initialTeam.identification ?? initialTeam.ageGroup ?? '',
        ageCategory: initialTeam.ageCategory ?? 'U13',
        teamCategory: initialTeam.teamCategory ?? 'MASCULINE',
        trainerUuid: initialTeam.trainerUuid ?? '',
        subTrainerUuid: initialTeam.subTrainerUuid ?? '',
        assistantAdminUuid: initialTeam.assistantAdminUuid ?? '',
      })
      return
    }

    setForm((currentForm) => {
      if (currentForm.trainerUuid || !trainers[0]?.uuid) {
        return currentForm
      }
      return {
        ...currentForm,
        trainerUuid: trainers[0].uuid,
      }
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
        identification: form.identification.trim(),
        ageCategory: form.ageCategory,
        teamCategory: form.teamCategory,
        trainerUuid: form.trainerUuid,
        subTrainerUuid: form.subTrainerUuid || null,
        assistantAdminUuid: form.assistantAdminUuid || null,
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit}>
      <div className="row">
        <div className="col-md-4 mb-3">
          <label className="form-label" htmlFor="team-identification">Identification</label>
          <input
            className="form-control"
            id="team-identification"
            name="identification"
            onChange={updateField}
            placeholder="Under 13 A"
            required
            value={form.identification}
          />
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label" htmlFor="team-age-category">Age category</label>
          <select
            className="form-select"
            id="team-age-category"
            name="ageCategory"
            onChange={updateField}
            required
            value={form.ageCategory}
          >
            {AGE_CATEGORIES.map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
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
        <div className="col-md-4 mb-3">
          <label className="form-label" htmlFor="team-sub-trainer">Sub trainer / assistant</label>
          <select
            className="form-select"
            id="team-sub-trainer"
            name="subTrainerUuid"
            onChange={updateField}
            value={form.subTrainerUuid}
          >
            <option value="">No sub trainer</option>
            {trainers.map((trainer) => (
              <option key={trainer.uuid} value={trainer.uuid}>
                {trainer.name}{trainer.active ? '' : ' (inactive)'}
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-4 mb-3">
          <label className="form-label" htmlFor="team-assistant-admin">Administrative assistant</label>
          <select
            className="form-select"
            id="team-assistant-admin"
            name="assistantAdminUuid"
            onChange={updateField}
            value={form.assistantAdminUuid}
          >
            <option value="">No administrative assistant</option>
            {admins.map((admin) => (
              <option key={admin.uuid} value={admin.uuid}>
                {admin.name}{admin.active ? '' : ' (inactive)'}
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
