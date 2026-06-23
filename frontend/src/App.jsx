import { BrowserRouter } from 'react-router-dom'
import MultipleRolesAlert from './components/MultipleRolesAlert.jsx'
import Navbar from './components/Navbar.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { ClubProvider } from './context/ClubContext.jsx'
import AppRoutes from './routes/AppRoutes.jsx'

export default function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <ClubProvider>
          <Navbar />
          <MultipleRolesAlert />
          <AppRoutes />
        </ClubProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
