import { useState } from 'react'
import { confirmTrainerPasswordReset, requestTrainerPasswordReset } from '../api/auth.js'

export default function AccountPasswordPage() {
  const [codeRequested, setCodeRequested] = useState(false)
  const [form, setForm] = useState({ code: '', password: '', confirmPassword: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function requestCode() {
    setMessage('')
    setError('')
    try {
      await requestTrainerPasswordReset()
      setCodeRequested(true)
      setMessage('Password reset code sent to your email.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to request password reset.')
    }
  }

  async function submit(event) {
    event.preventDefault()
    setMessage('')
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('Password confirmation does not match.')
      return
    }
    try {
      await confirmTrainerPasswordReset({ code: form.code, password: form.password })
      setForm({ code: '', password: '', confirmPassword: '' })
      setCodeRequested(false)
      setMessage('Password updated.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to reset password.')
    }
  }

  return (
    <main className="container py-5">
      <h1>Account password</h1>
      <p className="text-muted">Request a five-digit email code before changing your trainer password.</p>
      {message && <p className="alert alert-success">{message}</p>}
      {error && <p className="alert alert-danger">{error}</p>}
      <button className="btn btn-outline-primary mb-4" onClick={requestCode} type="button">Send reset code</button>
      {codeRequested && (
        <form className="card" onSubmit={submit}>
          <div className="card-body">
            <div className="mb-3">
              <label className="form-label" htmlFor="reset-code">Email code</label>
              <input className="form-control" id="reset-code" inputMode="numeric" maxLength="5" name="code" onChange={updateField} required value={form.code} />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="reset-password">New password</label>
              <input autoComplete="new-password" className="form-control" id="reset-password" name="password" onChange={updateField} required type="password" value={form.password} />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="reset-confirm-password">Confirm new password</label>
              <input autoComplete="new-password" className="form-control" id="reset-confirm-password" name="confirmPassword" onChange={updateField} required type="password" value={form.confirmPassword} />
            </div>
            <button className="btn btn-primary" type="submit">Update password</button>
          </div>
        </form>
      )}
    </main>
  )
}
