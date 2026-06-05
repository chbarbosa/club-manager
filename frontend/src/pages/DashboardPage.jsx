import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function DashboardPage() {
  const { isAuthenticated } = useAuth()

  return (
    <main className="container py-5">
      <h1>Dashboard</h1>
      <p className="text-muted">Your club management workspace is ready.</p>
      {isAuthenticated() ? (
        <div className="d-flex gap-2">
          <Link className="btn btn-primary" to="/players">Players</Link>
          <Link className="btn btn-primary" to="/trainers">Trainers</Link>
          <Link className="btn btn-primary" to="/teams">Teams</Link>
          <Link className="btn btn-primary" to="/settings/club">Club settings</Link>
          <Link className="btn btn-outline-secondary" to="/admins">Admins</Link>
        </div>
      ) : (
        <Link className="btn btn-primary" to="/login">Admin login</Link>
      )}
    </main>
  )
}
