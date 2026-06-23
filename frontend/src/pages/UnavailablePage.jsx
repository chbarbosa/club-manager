import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function UnavailablePage() {
  const { isAuthenticated } = useAuth()
  const backPath = isAuthenticated() ? '/dashboard' : '/login'
  const backLabel = isAuthenticated() ? 'Back to dashboard' : 'Go to login'

  return (
    <main className="container py-5">
      <div className="card border-secondary">
        <div className="card-body text-center py-5">
          <div className="unavailable-icon mx-auto mb-4" aria-hidden="true">
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8">
              <path d="M12 9v4" />
              <path d="M12 17h.01" />
              <path d="M10.3 4.3 2.6 18a2 2 0 0 0 1.7 3h15.4a2 2 0 0 0 1.7-3L13.7 4.3a2 2 0 0 0-3.4 0Z" />
            </svg>
          </div>
          <h1 className="h3">Page unavailable</h1>
          <p className="text-muted mb-4">
            This area is not available in the club workspace yet, or the address may be incorrect.
          </p>
          <Link className="btn btn-primary" to={backPath}>
            {backLabel}
          </Link>
        </div>
      </div>
    </main>
  )
}
