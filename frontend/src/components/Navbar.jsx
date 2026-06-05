import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useClub } from '../context/ClubContext.jsx'

export default function Navbar() {
  const { club } = useClub()
  const { isAuthenticated, logout, name } = useAuth()

  return (
    <nav className="navbar navbar-expand navbar-dark px-3">
      <Link className="navbar-brand" to="/dashboard">
        {club.name}
      </Link>
      {isAuthenticated() && (
        <div className="navbar-nav ms-auto align-items-center gap-2">
          <Link className="nav-link" to="/players">Players</Link>
          <Link className="nav-link" to="/trainers">Trainers</Link>
          <Link className="nav-link" to="/settings/club">Club settings</Link>
          <Link className="nav-link" to="/admins">Admins</Link>
          <span className="navbar-text text-white">{name}</span>
          <button className="btn btn-outline-light btn-sm" onClick={logout} type="button">
            Logout
          </button>
        </div>
      )}
    </nav>
  )
}
