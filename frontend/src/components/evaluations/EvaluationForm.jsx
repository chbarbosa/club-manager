import { useEffect, useState } from 'react'

const EMPTY_FORM = {
  title: '',
  ageGroup: '',
  teamCategory: 'MASCULINE',
  limitDate: '',
}

export default function EvaluationForm({ initialEvaluation, onCancel, onSubmit }) {
  const [form, setForm] = useState(EMPTY_FORM)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    setForm(initialEvaluation ? {
      title: initialEvaluation.title ?? '',
      ageGroup: initialEvaluation.ageGroup ?? '',
      teamCategory: initialEvaluation.teamCategory ?? 'MASCULINE',
      limitDate: initialEvaluation.limitDate ?? '',
    } : EMPTY_FORM)
  }, [initialEvaluation])

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      await onSubmit({
        title: form.title.trim(),
        ageGroup: form.ageGroup.trim(),
        teamCategory: form.teamCategory,
        limitDate: form.limitDate || null,
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={submit}>
      <div className="row">
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="evaluation-title">Title</label>
          <input
            className="form-control"
            id="evaluation-title"
            name="title"
            onChange={updateField}
            placeholder="Spring Tryouts"
            required
            value={form.title}
          />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="evaluation-age-group">Age group</label>
          <input
            className="form-control"
            id="evaluation-age-group"
            name="ageGroup"
            onChange={updateField}
            placeholder="Under 15"
            required
            value={form.ageGroup}
          />
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="evaluation-team-category">Team category</label>
          <select
            className="form-select"
            id="evaluation-team-category"
            name="teamCategory"
            onChange={updateField}
            required
            value={form.teamCategory}
          >
            <option value="MASCULINE">Masculine</option>
            <option value="FEMININE">Feminine</option>
          </select>
        </div>
        <div className="col-md-6 mb-3">
          <label className="form-label" htmlFor="evaluation-limit-date">Limit date</label>
          <input
            className="form-control"
            id="evaluation-limit-date"
            name="limitDate"
            onChange={updateField}
            type="date"
            value={form.limitDate}
          />
        </div>
      </div>
      <div className="d-flex gap-2">
        <button className="btn btn-primary" disabled={submitting} type="submit">
          {submitting ? 'Saving...' : 'Save evaluation'}
        </button>
        <button className="btn btn-outline-secondary" onClick={onCancel} type="button">Cancel</button>
      </div>
    </form>
  )
}
