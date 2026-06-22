import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import AccessDeniedPage from '../pages/AccessDeniedPage.jsx'

export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, role } = useAuth()

  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />
  }

  if (roles?.length && !roles.includes(role)) {
    return <AccessDeniedPage />
  }

  return children
}
