import { useEffect, useState } from 'react'
import { deactivateAdmin, getAllAdmins, reactivateAdmin } from '../api/admins.js'
import { registerAdmin } from '../api/auth.js'

const EMPTY_FORM = {
  name: '',
  email: '',
  username: '',
  password: '',
}

export default function AdminsPage() {
  const [admins, setAdmins] = useState([])
  const [form, setForm] = useState(EMPTY_FORM)
  const [showForm, setShowForm] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadAdmins()
  }, [])

  async function loadAdmins() {
    setError('')
    try {
      setAdmins(await getAllAdmins())
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to load admins.')
    }
  }

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      await registerAdmin(form)
      setForm(EMPTY_FORM)
      setShowForm(false)
      setMessage('Admin registered.')
      await loadAdmins()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to register admin.')
    }
  }

  async function toggleAdminStatus(admin) {
    const action = admin.active ? 'deactivate' : 'reactivate'
    if (!window.confirm(`${action[0].toUpperCase()}${action.slice(1)} admin ${admin.username}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      if (admin.active) {
        await deactivateAdmin(admin.uuid)
        setMessage('Admin deactivated.')
      } else {
        await reactivateAdmin(admin.uuid)
        setMessage('Admin reactivated.')
      }
      await loadAdmins()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? `Unable to ${action} admin.`)
    }
  }

  return (
    <main className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h1>Admins</h1>
          <p className="text-muted mb-0">Manage administrator access for this club instance.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm(true)} type="button">
          Register admin
        </button>
      </div>

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      {showForm && (
        <div aria-modal="true" className="card mb-4" role="dialog">
          <div className="card-body">
            <h2 className="h4">Register new admin</h2>
            <form onSubmit={submit}>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label className="form-label" htmlFor="admin-name">Name</label>
                  <input className="form-control" id="admin-name" name="name" onChange={updateField} required value={form.name} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label" htmlFor="admin-email">Email</label>
                  <input className="form-control" id="admin-email" name="email" onChange={updateField} required type="email" value={form.email} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label" htmlFor="admin-username">Username</label>
                  <input className="form-control" id="admin-username" name="username" onChange={updateField} required value={form.username} />
                </div>
                <div className="col-md-6 mb-3">
                  <label className="form-label" htmlFor="admin-password">Password</label>
                  <input className="form-control" id="admin-password" minLength="6" name="password" onChange={updateField} required type="password" value={form.password} />
                </div>
              </div>
              <div className="d-flex gap-2">
                <button className="btn btn-primary" type="submit">Save admin</button>
                <button className="btn btn-outline-secondary" onClick={() => setShowForm(false)} type="button">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <table className="table table-striped align-middle">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Username</th>
            <th>Status</th>
            <th>Created</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {admins.map((admin) => (
            <tr key={admin.uuid}>
              <td>{admin.name}</td>
              <td>{admin.email}</td>
              <td>{admin.username}</td>
              <td>
                <span className={`badge ${admin.active ? 'text-bg-success' : 'text-bg-secondary'}`}>
                  {admin.active ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td>{admin.createdAt ? new Date(admin.createdAt).toLocaleString() : '-'}</td>
              <td>
                <button
                  className={`btn btn-sm ${admin.active ? 'btn-outline-danger' : 'btn-outline-success'}`}
                  disabled={admin.active && admins.filter((entry) => entry.active).length === 1}
                  onClick={() => toggleAdminStatus(admin)}
                  type="button"
                >
                  {admin.active ? 'Deactivate' : 'Reactivate'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </main>
  )
}
