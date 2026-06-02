import { Link } from 'react-router-dom'
import { useClub } from '../context/ClubContext.jsx'

export default function Navbar() {
  const { club } = useClub()

  return (
    <nav className="navbar navbar-dark px-3">
      <Link className="navbar-brand" to="/dashboard">
        {club.name}
      </Link>
    </nav>
  )
}
