import { useEffect, useState } from 'react'

const EMPTY_FORM = {
  name: '',
  birthCountry: '',
  livingCountry: '',
  birthdate: '',
  teamCategory: 'MASCULINE',
  registrationNumber: '',
  memberSince: '',
}

export default function PlayerForm({ initialPlayer, onCancel, onSubmit }) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    setForm(initialPlayer ? {
      name: initialPlayer.name ?? '',
      birthCountry: initialPlayer.birthCountry ?? '',
      livingCountry: initialPlayer.livingCountry ?? '',
      birthdate: initialPlayer.birthdate ?? '',
      teamCategory: initialPlayer.teamCategory ?? 'MASCULINE',
      registrationNumber: initialPlayer.registrationNumber ?? '',
      memberSince: initialPlayer.memberSince ?? '',
    } : EMPTY_FORM)
  }, [initialPlayer])

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      await onSubmit({
        ...form,
        registrationNumber: form.registrationNumber.trim() || null,
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit}>
      <div className="row">
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="player-name">Name</label>
          <input className="form-control" id="player-name" name="name" onChange={updateField} required value={form.name} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="player-birthdate">Birthdate</label>
          <input className="form-control" id="player-birthdate" name="birthdate" onChange={updateField} required type="date" value={form.birthdate} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="player-birth-country">Birth country</label>
          <input className="form-control" id="player-birth-country" name="birthCountry" onChange={updateField} required value={form.birthCountry} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="player-living-country">Living country</label>
          <input className="form-control" id="player-living-country" name="livingCountry" onChange={updateField} required value={form.livingCountry} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="player-team-category">Team category</label>
          <select className="form-select" id="player-team-category" name="teamCategory" onChange={updateField} required value={form.teamCategory}>
            <option value="MASCULINE">Masculine</option>
            <option value="FEMININE">Feminine</option>
          </select>
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="player-registration-number">Registration number</label>
          <input className="form-control" id="player-registration-number" name="registrationNumber" onChange={updateField} value={form.registrationNumber} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="player-member-since">
            Date the player started at this club (can be in the past)
          </label>
          <input className="form-control" id="player-member-since" name="memberSince" onChange={updateField} required type="date" value={form.memberSince} />
        </div>
      </div>
      <div className="d-flex gap-2">
        <button className="btn btn-primary" disabled={submitting} type="submit">
          {submitting ? 'Saving...' : 'Save player'}
        </button>
        <button className="btn btn-outline-secondary" onClick={onCancel} type="button">Cancel</button>
      </div>
    </form>
  )
}
