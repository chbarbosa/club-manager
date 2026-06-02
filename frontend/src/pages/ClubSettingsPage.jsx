import { useEffect, useState } from 'react'
import { getAllSetup, updateClub, updateSetup } from '../api/club.js'
import SetupTagEditor from '../components/settings/SetupTagEditor.jsx'
import { useClub } from '../context/ClubContext.jsx'

export default function ClubSettingsPage() {
  const { club, updateClubContext } = useClub()
  const [form, setForm] = useState(club)
  const [setupEntries, setSetupEntries] = useState([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => setForm(club), [club])

  useEffect(() => {
    getAllSetup()
      .then(setSetupEntries)
      .catch((requestError) => {
        setError(requestError.response?.data?.message ?? 'Unable to load setup values.')
      })
  }, [])

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function saveClub(event) {
    event.preventDefault()
    setMessage('')
    setError('')
    try {
      const savedClub = await updateClub(form)
      updateClubContext(savedClub)
      setMessage('Club settings saved.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to save club settings.')
    }
  }

  async function saveSetup(uuid, data) {
    const savedSetup = await updateSetup(uuid, data)
    setSetupEntries((entries) => entries.map((entry) => (entry.uuid === uuid ? savedSetup : entry)))
  }

  return (
    <main className="container py-5">
      <h1>Club settings</h1>
      <p className="text-muted">Manage the identity and configurable lookup values for this club.</p>

      <form className="card mb-4" onSubmit={saveClub}>
        <div className="card-body">
          <div className="mb-3">
            <label className="form-label" htmlFor="club-name">Club name</label>
            <input
              className="form-control"
              id="club-name"
              name="name"
              onChange={updateField}
              required
              value={form.name}
            />
          </div>
          <div className="mb-3">
            <label className="form-label" htmlFor="club-description">Description</label>
            <textarea
              className="form-control"
              id="club-description"
              maxLength="500"
              name="description"
              onChange={updateField}
              rows="3"
              value={form.description ?? ''}
            />
          </div>
          <div className="row mb-3">
            <div className="col-md-6">
              <label className="form-label" htmlFor="club-primary">Primary colour</label>
              <input
                className="form-control form-control-color"
                id="club-primary"
                name="colour1"
                onChange={updateField}
                type="color"
                value={form.colour1}
              />
            </div>
            <div className="col-md-6">
              <label className="form-label" htmlFor="club-secondary">Secondary colour</label>
              <input
                className="form-control form-control-color"
                id="club-secondary"
                name="colour2"
                onChange={updateField}
                type="color"
                value={form.colour2}
              />
            </div>
          </div>
          <h2 className="h5">Live preview</h2>
          <div className="border rounded overflow-hidden mb-3">
            <div className="p-3 text-white" style={{ backgroundColor: form.colour1 }}>
              {form.name || 'Club name'}
            </div>
            <div className="p-3" style={{ backgroundColor: form.colour2 }}>
              <button className="btn text-white" style={{ backgroundColor: form.colour1 }} type="button">
                Primary action
              </button>
            </div>
          </div>
          {error && <p className="text-danger">{error}</p>}
          {message && <p className="text-success">{message}</p>}
          <button className="btn btn-primary" type="submit">Save club settings</button>
        </div>
      </form>

      <h2>Lookup values</h2>
      {setupEntries.map((setup) => (
        <SetupTagEditor key={setup.uuid} onSave={saveSetup} setup={setup} />
      ))}
    </main>
  )
}

