import { useEffect, useState } from 'react'
import { createSupportAccess, getSupportAccesses, getSupportAccessViews, revokeSupportAccess } from '../api/supportAccess.js'

export default function SupportAccessPage() {
  const [email, setEmail] = useState('')
  const [accesses, setAccesses] = useState([])
  const [selectedAccess, setSelectedAccess] = useState(null)
  const [views, setViews] = useState([])
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    loadAccesses()
  }, [])

  async function loadAccesses() {
    setError('')
    try {
      const response = await getSupportAccesses({ page: 0, size: 50 })
      setAccesses(response.content ?? [])
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to load support access.')
    }
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      await createSupportAccess({ email })
      setEmail('')
      setMessage('Support access created. The support email notification was prepared.')
      await loadAccesses()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to create support access.')
    }
  }

  async function revoke(access) {
    if (!window.confirm(`Revoke support access for ${access.email}?`)) {
      return
    }
    setError('')
    setMessage('')
    try {
      await revokeSupportAccess(access.uuid)
      setMessage('Support access revoked.')
      await loadAccesses()
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to revoke support access.')
    }
  }

  async function showViews(access) {
    setSelectedAccess(access)
    setViews([])
    setError('')
    try {
      const response = await getSupportAccessViews(access.uuid, { page: 0, size: 100 })
      setViews(response.content ?? [])
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to load support view history.')
    }
  }

  return (
    <main className="container py-5">
      <h1>Support access</h1>
      <p className="text-muted">Create temporary read-only support access windows and review the data viewed by support.</p>

      {error && <p className="alert alert-danger">{error}</p>}
      {message && <p className="alert alert-success">{message}</p>}

      <section className="card mb-4">
        <div className="card-body">
          <h2 className="h4">Create support access</h2>
          <form className="row g-3 align-items-end" onSubmit={submit}>
            <div className="col-md-8">
              <label className="form-label" htmlFor="support-email">Support email</label>
              <input className="form-control" id="support-email" onChange={(event) => setEmail(event.target.value)} required type="email" value={email} />
            </div>
            <div className="col-md-4">
              <button className="btn btn-primary" type="submit">Create 5-hour access</button>
            </div>
          </form>
        </div>
      </section>

      <section className="card mb-4">
        <div className="card-body">
          <h2 className="h4">Access windows</h2>
          <table className="table table-striped align-middle">
            <thead>
              <tr>
                <th>Email</th>
                <th>Status</th>
                <th>Created</th>
                <th>Expires</th>
                <th>Created by</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {accesses.map((access) => (
                <tr key={access.uuid}>
                  <td>{access.email}</td>
                  <td><span className={`badge ${badgeClass(access.status)}`}>{access.status}</span></td>
                  <td>{formatDateTime(access.createdAt)}</td>
                  <td>{formatDateTime(access.expiresAt)}</td>
                  <td>{access.createdByAdminName}</td>
                  <td className="d-flex gap-2">
                    <button className="btn btn-sm btn-outline-primary" onClick={() => showViews(access)} type="button">Views</button>
                    <button className="btn btn-sm btn-outline-danger" disabled={access.status !== 'ACTIVE'} onClick={() => revoke(access)} type="button">Revoke</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {accesses.length === 0 && <p className="text-muted">No support access created yet.</p>}
        </div>
      </section>

      {selectedAccess && (
        <section className="card">
          <div className="card-body">
            <h2 className="h4">Viewed data for {selectedAccess.email}</h2>
            <table className="table table-striped align-middle">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Feature</th>
                  <th>Path</th>
                  <th>Entity UUID</th>
                </tr>
              </thead>
              <tbody>
                {views.map((view) => (
                  <tr key={view.uuid}>
                    <td>{formatDateTime(view.occurredAt)}</td>
                    <td>{view.feature}</td>
                    <td>{view.path}</td>
                    <td>{view.entityUuid ?? '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {views.length === 0 && <p className="text-muted">No viewed data recorded for this access window.</p>}
          </div>
        </section>
      )}
    </main>
  )
}

function badgeClass(status) {
  if (status === 'ACTIVE') {
    return 'text-bg-success'
  }
  if (status === 'REVOKED') {
    return 'text-bg-danger'
  }
  return 'text-bg-secondary'
}

function formatDateTime(value) {
  return value ? new Date(value).toLocaleString() : '-'
}
