import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { ClubProvider } from './context/ClubContext.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import ClubSettingsPage from './pages/ClubSettingsPage.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import AdminsPage from './pages/AdminsPage.jsx'
import PlayersPage from './pages/PlayersPage.jsx'
import PlayerDetailPage from './pages/PlayerDetailPage.jsx'

export default function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <ClubProvider>
          <Navbar />
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route
              path="/settings/club"
              element={
                <ProtectedRoute>
                  <ClubSettingsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admins"
              element={
                <ProtectedRoute>
                  <AdminsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/players"
              element={
                <ProtectedRoute>
                  <PlayersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/players/:uuid"
              element={
                <ProtectedRoute>
                  <PlayerDetailPage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </ClubProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
