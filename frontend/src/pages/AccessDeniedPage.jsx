import { Link } from 'react-router-dom'

export default function AccessDeniedPage() {
  return (
    <main className="container py-5">
      <div className="card border-warning">
        <div className="card-body">
          <h1 className="h3">Access denied</h1>
          <p className="text-muted mb-4">Your current access does not include this area.</p>
          <Link className="btn btn-primary" to="/dashboard">Back to dashboard</Link>
        </div>
      </div>
    </main>
  )
}
