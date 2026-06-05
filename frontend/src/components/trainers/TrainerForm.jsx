import { useEffect, useState } from 'react'

const EMPTY_FORM = {
  name: '',
  email: '',
  phone: '',
  birthCountry: '',
  livingCountry: '',
  birthdate: '',
  memberSince: '',
}

export default function TrainerForm({ initialTrainer, onCancel, onSubmit }) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    setForm(initialTrainer ? {
      name: initialTrainer.name ?? '',
      email: initialTrainer.email ?? '',
      phone: initialTrainer.phone ?? '',
      birthCountry: initialTrainer.birthCountry ?? '',
      livingCountry: initialTrainer.livingCountry ?? '',
      birthdate: initialTrainer.birthdate ?? '',
      memberSince: initialTrainer.memberSince ?? '',
    } : EMPTY_FORM)
  }, [initialTrainer])

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      await onSubmit({
        ...form,
        email: form.email.trim() || null,
        phone: form.phone.trim() || null,
        birthCountry: form.birthCountry.trim() || null,
        livingCountry: form.livingCountry.trim() || null,
        birthdate: form.birthdate || null,
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit}>
      <div className="row">
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="trainer-name">Name</label>
          <input className="form-control" id="trainer-name" name="name" onChange={updateField} required value={form.name} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="trainer-email">Email</label>
          <input className="form-control" id="trainer-email" name="email" onChange={updateField} type="email" value={form.email} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="trainer-phone">Phone</label>
          <input className="form-control" id="trainer-phone" name="phone" onChange={updateField} value={form.phone} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="trainer-birthdate">Birthdate</label>
          <input className="form-control" id="trainer-birthdate" name="birthdate" onChange={updateField} type="date" value={form.birthdate} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="trainer-birth-country">Birth country</label>
          <input className="form-control" id="trainer-birth-country" name="birthCountry" onChange={updateField} value={form.birthCountry} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="trainer-living-country">Living country</label>
          <input className="form-control" id="trainer-living-country" name="livingCountry" onChange={updateField} value={form.livingCountry} />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="trainer-member-since">
            Date the trainer joined this club (can be in the past)
          </label>
          <input className="form-control" id="trainer-member-since" name="memberSince" onChange={updateField} required type="date" value={form.memberSince} />
        </div>
      </div>
      <div className="d-flex gap-2">
        <button className="btn btn-primary" disabled={submitting} type="submit">
          {submitting ? 'Saving...' : 'Save trainer'}
        </button>
        <button className="btn btn-outline-secondary" onClick={onCancel} type="button">Cancel</button>
      </div>
    </form>
  )
}
