import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../components/ProtectedRoute.jsx'
import AdminsPage from '../pages/AdminsPage.jsx'
import AccountPasswordPage from '../pages/AccountPasswordPage.jsx'
import ChampionshipDetailPage from '../pages/ChampionshipDetailPage.jsx'
import ChampionshipsPage from '../pages/ChampionshipsPage.jsx'
import ClubAnalysisDetailPage from '../pages/ClubAnalysisDetailPage.jsx'
import ClubAnalysisPage from '../pages/ClubAnalysisPage.jsx'
import ClubSettingsPage from '../pages/ClubSettingsPage.jsx'
import DashboardPage from '../pages/DashboardPage.jsx'
import EvaluationDetailPage from '../pages/EvaluationDetailPage.jsx'
import EvaluationsPage from '../pages/EvaluationsPage.jsx'
import LoginPage from '../pages/LoginPage.jsx'
import PlayerDetailPage from '../pages/PlayerDetailPage.jsx'
import PlayersPage from '../pages/PlayersPage.jsx'
import SchedulesPage from '../pages/SchedulesPage.jsx'
import SupportAccessPage from '../pages/SupportAccessPage.jsx'
import TeamDetailPage from '../pages/TeamDetailPage.jsx'
import TeamMatchDetailPage from '../pages/TeamMatchDetailPage.jsx'
import TeamsPage from '../pages/TeamsPage.jsx'
import TrainerDetailPage from '../pages/TrainerDetailPage.jsx'
import TrainerPasswordConfirmPage from '../pages/TrainerPasswordConfirmPage.jsx'
import TrainerProfilePage from '../pages/TrainerProfilePage.jsx'
import TrainersPage from '../pages/TrainersPage.jsx'
import UnavailablePage from '../pages/UnavailablePage.jsx'

const ADMIN = ['ADMIN']
const TRAINER = ['TRAINER']
const ADMIN_OR_SUPPORT = ['ADMIN', 'SUPPORT']
const OPERATIONAL_READ = ['ADMIN', 'SUPPORT', 'TRAINER']
const AUTHENTICATED = ['ADMIN', 'TRAINER', 'SUPPORT']

function protectedElement(page, roles = AUTHENTICATED) {
  return <ProtectedRoute roles={roles}>{page}</ProtectedRoute>
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/trainer-password/confirm" element={<TrainerPasswordConfirmPage />} />
      <Route path="/unavailable" element={<UnavailablePage />} />
      <Route path="/dashboard" element={protectedElement(<DashboardPage />)} />
      <Route path="/settings/club" element={protectedElement(<ClubSettingsPage />, ADMIN)} />
      <Route path="/admins" element={protectedElement(<AdminsPage />, ADMIN)} />
      <Route path="/support-access" element={protectedElement(<SupportAccessPage />, ADMIN)} />
      <Route path="/players" element={protectedElement(<PlayersPage />, OPERATIONAL_READ)} />
      <Route path="/players/:uuid" element={protectedElement(<PlayerDetailPage />, OPERATIONAL_READ)} />
      <Route path="/trainers" element={protectedElement(<TrainersPage />, ADMIN_OR_SUPPORT)} />
      <Route path="/trainers/me" element={protectedElement(<TrainerProfilePage />, TRAINER)} />
      <Route path="/trainers/:uuid" element={protectedElement(<TrainerDetailPage />, ADMIN_OR_SUPPORT)} />
      <Route path="/teams" element={protectedElement(<TeamsPage />, OPERATIONAL_READ)} />
      <Route path="/teams/:uuid" element={protectedElement(<TeamDetailPage />, OPERATIONAL_READ)} />
      <Route path="/teams/:teamUuid/matches/:matchUuid" element={protectedElement(<TeamMatchDetailPage />, OPERATIONAL_READ)} />
      <Route path="/schedules" element={protectedElement(<SchedulesPage />, OPERATIONAL_READ)} />
      <Route path="/championships" element={protectedElement(<ChampionshipsPage />, ADMIN_OR_SUPPORT)} />
      <Route path="/championships/:uuid" element={protectedElement(<ChampionshipDetailPage />, OPERATIONAL_READ)} />
      <Route path="/club-analysis" element={protectedElement(<ClubAnalysisPage />, ADMIN)} />
      <Route path="/club-analysis/:uuid" element={protectedElement(<ClubAnalysisDetailPage />, ADMIN)} />
      <Route path="/evaluations" element={protectedElement(<EvaluationsPage />, OPERATIONAL_READ)} />
      <Route path="/evaluations/:uuid" element={protectedElement(<EvaluationDetailPage />, OPERATIONAL_READ)} />
      <Route path="/account/password" element={protectedElement(<AccountPasswordPage />, TRAINER)} />
      <Route path="*" element={<UnavailablePage />} />
    </Routes>
  )
}
