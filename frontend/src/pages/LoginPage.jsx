import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function LoginPage() {
  const { isAuthenticated, login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  if (isAuthenticated()) {
    return <Navigate to="/dashboard" replace />
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    try {
      await login(username, password)
      navigate('/dashboard')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to log in.')
    }
  }

  return (
    <main className="container py-5">
      <div className="login-panel">
        <h1>Login</h1>
        <form className="card mt-4" onSubmit={submit}>
          <div className="card-body">
            <div className="mb-3">
              <label className="form-label" htmlFor="username">Username or email</label>
              <input
                autoComplete="username"
                className="form-control"
                id="username"
                onChange={(event) => setUsername(event.target.value)}
                required
                value={username}
              />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="password">Password</label>
              <input
                autoComplete="current-password"
                className="form-control"
                id="password"
                onChange={(event) => setPassword(event.target.value)}
                required
                type="password"
                value={password}
              />
            </div>
            {error && <p className="text-danger">{error}</p>}
            <button className="btn btn-primary" type="submit">Login</button>
            <p className="mt-3 mb-0">
              <a href="/trainer-password/confirm">First trainer access?</a>
            </p>
          </div>
        </form>
      </div>
    </main>
  )
}
