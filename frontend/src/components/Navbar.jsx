import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useClub } from '../context/ClubContext.jsx'

export default function Navbar() {
  const { club } = useClub()
  const { isAuthenticated, logout, name, role } = useAuth()
  const isAdmin = role === 'ADMIN'
  const isTrainer = role === 'TRAINER'

  return (
    <nav className="navbar navbar-expand navbar-dark px-3">
      <Link className="navbar-brand" to="/dashboard">
        {club.name}
      </Link>
      {isAuthenticated() && (
        <div className="navbar-nav ms-auto align-items-center gap-2">
          {isAdmin && (
            <>
              <Link className="nav-link" to="/players">Players</Link>
              <Link className="nav-link" to="/trainers">Trainers</Link>
              <Link className="nav-link" to="/teams">Teams</Link>
              <Link className="nav-link" to="/schedules">Schedules</Link>
              <Link className="nav-link" to="/championships">Championships</Link>
              <Link className="nav-link" to="/evaluations">Evaluations</Link>
              <Link className="nav-link" to="/settings/club">Club settings</Link>
              <Link className="nav-link" to="/admins">Admins</Link>
            </>
          )}
          {isTrainer && <Link className="nav-link" to="/account/password">Password</Link>}
          <span className="navbar-text text-white">{name}</span>
          <button className="btn btn-outline-light btn-sm" onClick={logout} type="button">
            Logout
          </button>
        </div>
      )}
    </nav>
  )
}
