import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const FEATURES = [
  {
    title: 'Players',
    description: 'Register athletes, keep personal data current, and follow each player status.',
    icon: 'PL',
    path: '/players',
    action: 'Manage players',
  },
  {
    title: 'Trainers',
    description: 'Maintain trainer records and prepare the staff information used by teams.',
    icon: 'TR',
    path: '/trainers',
    action: 'Manage trainers',
  },
  {
    title: 'Teams',
    description: 'Organize teams by age group and category, then manage each active roster.',
    icon: 'TM',
    path: '/teams',
    action: 'Manage teams',
  },
  {
    title: 'Evaluations',
    description: 'Create evaluation groups, schedule events, and record participation and skill levels.',
    icon: 'EV',
    path: '/evaluations',
    action: 'Manage evaluations',
  },
  {
    title: 'Club settings',
    description: 'Adjust club identity, colours, and setup values used across the workspace.',
    icon: 'CS',
    path: '/settings/club',
    action: 'Open settings',
  },
  {
    title: 'Admins',
    description: 'Create and manage administrator access for this club instance.',
    icon: 'AD',
    path: '/admins',
    action: 'Manage admins',
    secondary: true,
  },
]

export default function DashboardPage() {
  const { isAuthenticated } = useAuth()

  return (
    <main className="container py-5">
      <h1>Dashboard</h1>
      <p className="text-muted">Your club management workspace is ready.</p>
      {isAuthenticated() ? (
        <div className="row g-4 mt-2">
          {FEATURES.map((feature) => (
            <div className="col-md-6 col-xl-4" key={feature.title}>
              <section className="card dashboard-card h-100">
                <div className="card-body d-flex flex-column text-center">
                  <div className="dashboard-card-icon mx-auto mb-3" aria-hidden="true">{feature.icon}</div>
                  <h2 className="h5">{feature.title}</h2>
                  <p className="text-muted flex-grow-1">{feature.description}</p>
                  <Link className={`btn ${feature.secondary ? 'btn-outline-secondary' : 'btn-primary'}`} to={feature.path}>
                    {feature.action}
                  </Link>
                </div>
              </section>
            </div>
          ))}
        </div>
      ) : (
        <Link className="btn btn-primary" to="/login">Admin login</Link>
      )}
    </main>
  )
}
