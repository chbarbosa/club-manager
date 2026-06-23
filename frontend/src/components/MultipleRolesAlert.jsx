import { useAuth } from '../context/AuthContext.jsx'

const ROLE_LABELS = {
  ADMIN: 'Admin',
  TRAINER: 'Trainer',
  SUPPORT: 'Support',
}

export default function MultipleRolesAlert() {
  const { multipleRoles, availableRoles, role } = useAuth()

  if (!multipleRoles || !role) {
    return null
  }

  const effectiveRole = ROLE_LABELS[role] ?? role
  const roles = (availableRoles ?? []).map((availableRole) => ROLE_LABELS[availableRole] ?? availableRole).join(', ')

  return (
    <div className="alert alert-warning multiple-roles-alert shadow-sm" role="alert">
      <strong>You have multiple roles.</strong> Using {effectiveRole} access for this session.
      {roles && <span className="d-block small">Available roles: {roles}</span>}
    </div>
  )
}
