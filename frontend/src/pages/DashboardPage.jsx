import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

const FEATURES = [
  {
    title: 'Players',
    description: 'Register athletes, keep personal data current, and follow each player status.',
    icon: 'players',
    path: '/players',
    action: 'Manage players',
  },
  {
    title: 'Trainers',
    description: 'Maintain trainer records and prepare the staff information used by teams.',
    icon: 'trainers',
    path: '/trainers',
    action: 'Manage trainers',
  },
  {
    title: 'Teams',
    description: 'Organize teams by age group and category, then manage each active roster.',
    icon: 'teams',
    path: '/teams',
    action: 'Manage teams',
  },
  {
    title: 'Evaluations',
    description: 'Create evaluation groups, schedule events, and record participation and skill levels.',
    icon: 'evaluations',
    path: '/evaluations',
    action: 'Manage evaluations',
  },
  {
    title: 'Schedules',
    description: 'Plan team sessions by field, date, time, duration, and type.',
    icon: 'schedules',
    path: '/schedules',
    action: 'Manage schedules',
  },
  {
    title: 'Championships',
    description: 'Create competition periods and manage the selected roster for each team.',
    icon: 'championships',
    path: '/championships',
    action: 'Manage championships',
  },
  {
    title: 'Club Analysis',
    description: 'Review the daily health snapshot with operational findings for players, teams, and competitions.',
    icon: 'analysis',
    path: '/club-analysis',
    action: 'Open analysis',
  },
  {
    title: 'Club settings',
    description: 'Adjust club identity, colours, and setup values used across the workspace.',
    icon: 'settings',
    path: '/settings/club',
    action: 'Open settings',
  },
  {
    title: 'Admins',
    description: 'Create and manage administrator access for this club instance.',
    icon: 'admins',
    path: '/admins',
    action: 'Manage admins',
    secondary: true,
  },
]

export default function DashboardPage() {
  const { isAuthenticated, role } = useAuth()
  const visibleFeatures = role === 'ADMIN' ? FEATURES : []

  return (
    <main className="container py-5">
      <h1>Dashboard</h1>
      <p className="text-muted">Your club management workspace is ready.</p>
      {!isAuthenticated() && <Link className="btn btn-primary" to="/login">Admin login</Link>}
      {isAuthenticated() && role === 'TRAINER' && (
        <div className="card mt-4">
          <div className="card-body">
            <h2 className="h4">Trainer access ready</h2>
            <p className="mb-0 text-muted">Your trainer workspace features will be enabled in a future step.</p>
          </div>
        </div>
      )}
      {isAuthenticated() && role === 'ADMIN' && (
        <div className="row g-4 mt-2">
          {visibleFeatures.map((feature) => (
            <div className="col-md-6 col-xl-4" key={feature.title}>
              <section className="card dashboard-card h-100">
                <div className="card-body d-flex flex-column text-center">
                  <div className="dashboard-card-icon mx-auto mb-3" aria-hidden="true">
                    <DashboardIcon name={feature.icon} />
                  </div>
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
      )}
    </main>
  )
}

function DashboardIcon({ name }) {
  const icons = {
    players: (
      <>
        <path d="M10 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
        <path d="M3 21a7 7 0 0 1 14 0" />
        <path d="M17 10a3 3 0 1 0 0-6" />
        <path d="M18 14a5 5 0 0 1 4 5" />
      </>
    ),
    trainers: (
      <>
        <path d="M4 5h16v10H4z" />
        <path d="M8 19h8" />
        <path d="M12 15v4" />
        <path d="M8 9h5" />
        <path d="M8 12h8" />
      </>
    ),
    teams: (
      <>
        <path d="M12 3 4 7l8 4 8-4-8-4Z" />
        <path d="M4 12l8 4 8-4" />
        <path d="M4 17l8 4 8-4" />
      </>
    ),
    evaluations: (
      <>
        <path d="M9 11l2 2 4-5" />
        <path d="M5 4h14v16H5z" />
        <path d="M8 17h8" />
      </>
    ),
    schedules: (
      <>
        <path d="M7 3v3" />
        <path d="M17 3v3" />
        <path d="M4 8h16" />
        <path d="M5 5h14v16H5z" />
        <path d="M8 12h3" />
        <path d="M13 12h3" />
        <path d="M8 16h3" />
      </>
    ),
    championships: (
      <>
        <path d="M8 21h8" />
        <path d="M12 17v4" />
        <path d="M7 4h10v3a5 5 0 0 1-10 0V4Z" />
        <path d="M7 6H4a3 3 0 0 0 3 3" />
        <path d="M17 6h3a3 3 0 0 1-3 3" />
        <path d="M9 17h6" />
      </>
    ),
    settings: (
      <>
        <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
        <path d="M19 12a7 7 0 0 0-.1-1.2l2-1.5-2-3.5-2.4 1a7 7 0 0 0-2-1.2L14.2 3h-4.4l-.3 2.6a7 7 0 0 0-2 1.2l-2.4-1-2 3.5 2 1.5A7 7 0 0 0 5 12c0 .4 0 .8.1 1.2l-2 1.5 2 3.5 2.4-1a7 7 0 0 0 2 1.2l.3 2.6h4.4l.3-2.6a7 7 0 0 0 2-1.2l2.4 1 2-3.5-2-1.5c.1-.4.1-.8.1-1.2Z" />
      </>
    ),
    admins: (
      <>
        <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
        <path d="M5 21a7 7 0 0 1 14 0" />
        <path d="M17 4l2 2 3-3" />
      </>
    ),
    analysis: (
      <>
        <path d="M4 19V5" />
        <path d="M4 19h16" />
        <path d="M8 15v-4" />
        <path d="M12 15V8" />
        <path d="M16 15v-6" />
        <path d="M7 5h10" />
        <path d="M17 5l3 3-3 3" />
      </>
    ),
  }

  return (
    <svg className="dashboard-card-svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8">
      {icons[name]}
    </svg>
  )
}
