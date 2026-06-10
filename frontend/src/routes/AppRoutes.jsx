import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../components/ProtectedRoute.jsx'
import AdminsPage from '../pages/AdminsPage.jsx'
import ChampionshipDetailPage from '../pages/ChampionshipDetailPage.jsx'
import ChampionshipsPage from '../pages/ChampionshipsPage.jsx'
import ClubSettingsPage from '../pages/ClubSettingsPage.jsx'
import DashboardPage from '../pages/DashboardPage.jsx'
import EvaluationDetailPage from '../pages/EvaluationDetailPage.jsx'
import EvaluationsPage from '../pages/EvaluationsPage.jsx'
import LoginPage from '../pages/LoginPage.jsx'
import PlayerDetailPage from '../pages/PlayerDetailPage.jsx'
import PlayersPage from '../pages/PlayersPage.jsx'
import SchedulesPage from '../pages/SchedulesPage.jsx'
import TeamDetailPage from '../pages/TeamDetailPage.jsx'
import TeamsPage from '../pages/TeamsPage.jsx'
import TrainerDetailPage from '../pages/TrainerDetailPage.jsx'
import TrainersPage from '../pages/TrainersPage.jsx'

function protectedElement(page) {
  return <ProtectedRoute>{page}</ProtectedRoute>
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/settings/club" element={protectedElement(<ClubSettingsPage />)} />
      <Route path="/admins" element={protectedElement(<AdminsPage />)} />
      <Route path="/players" element={protectedElement(<PlayersPage />)} />
      <Route path="/players/:uuid" element={protectedElement(<PlayerDetailPage />)} />
      <Route path="/trainers" element={protectedElement(<TrainersPage />)} />
      <Route path="/trainers/:uuid" element={protectedElement(<TrainerDetailPage />)} />
      <Route path="/teams" element={protectedElement(<TeamsPage />)} />
      <Route path="/teams/:uuid" element={protectedElement(<TeamDetailPage />)} />
      <Route path="/schedules" element={protectedElement(<SchedulesPage />)} />
      <Route path="/championships" element={protectedElement(<ChampionshipsPage />)} />
      <Route path="/championships/:uuid" element={protectedElement(<ChampionshipDetailPage />)} />
      <Route path="/evaluations" element={protectedElement(<EvaluationsPage />)} />
      <Route path="/evaluations/:uuid" element={protectedElement(<EvaluationDetailPage />)} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
