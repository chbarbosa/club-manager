import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { confirmTrainerPassword } from '../api/auth.js'

export default function TrainerPasswordConfirmPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', code: '', password: '', confirmPassword: '' })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
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
      await confirmTrainerPassword({ email: form.email, code: form.code, password: form.password })
      setMessage('Password confirmed. You can log in now.')
      setTimeout(() => navigate('/login'), 900)
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to confirm trainer password.')
    }
  }

  return (
    <main className="container py-5">
      <div className="login-panel">
        <h1>Trainer access</h1>
        <p className="text-muted">Use the five-digit code sent by email to create your first password.</p>
        <form className="card mt-4" onSubmit={submit}>
          <div className="card-body">
            {message && <p className="alert alert-success">{message}</p>}
            {error && <p className="alert alert-danger">{error}</p>}
            <div className="mb-3">
              <label className="form-label" htmlFor="trainer-email">Email</label>
              <input autoComplete="email" className="form-control" id="trainer-email" name="email" onChange={updateField} required type="email" value={form.email} />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="trainer-code">Email code</label>
              <input className="form-control" id="trainer-code" inputMode="numeric" maxLength="5" name="code" onChange={updateField} required value={form.code} />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="trainer-password">Password</label>
              <input autoComplete="new-password" className="form-control" id="trainer-password" name="password" onChange={updateField} required type="password" value={form.password} />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="trainer-confirm-password">Confirm password</label>
              <input autoComplete="new-password" className="form-control" id="trainer-confirm-password" name="confirmPassword" onChange={updateField} required type="password" value={form.confirmPassword} />
            </div>
            <button className="btn btn-primary" type="submit">Confirm password</button>
            <Link className="btn btn-link" to="/login">Back to login</Link>
          </div>
        </form>
      </div>
    </main>
  )
}
