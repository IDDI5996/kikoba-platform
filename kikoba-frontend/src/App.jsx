import { Routes, Route, Navigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { AuthProvider } from './context/AuthContext';
import { RegistrationProvider } from './context/RegistrationContext';
import { ChatProvider } from './context/ChatContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';

// Public pages
import Landing from './pages/Landing';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import VerifyPhone from './pages/auth/VerifyPhone';
import VerifyEmail from './pages/auth/VerifyEmail';
import MfaLogin from './pages/auth/MfaLogin';
import About from './pages/About';
import Contact from './pages/Contact';
import ForgotPassword from './pages/auth/ForgotPassword';
import Terms from './pages/Terms';
import Privacy from './pages/Privacy';

// Core pages
import Dashboard from './pages/dashboard/Dashboard';
import Groups from './pages/groups/Groups';
import CreateGroup from './pages/groups/CreateGroup';
import JoinGroup from './pages/groups/JoinGroup';
import GroupDetail from './pages/groups/GroupDetail';

// Contributions
import MakeContribution from './pages/contributions/MakeContribution';
import MyContributions from './pages/contributions/MyContributions';
import GroupContributions from './pages/contributions/GroupContributions';

// Loans
import ApplyLoan from './pages/loans/ApplyLoan';
import MyLoans from './pages/loans/MyLoans';
import LoanDetail from './pages/loans/LoanDetail';
import PendingLoans from './pages/loans/PendingLoans';

// Meetings
import MeetingsHub from './pages/meetings/MeetingsHub';
import Meetings from './pages/meetings/Meetings';
import ScheduleMeeting from './pages/meetings/ScheduleMeeting';
import MeetingDetail from './pages/meetings/MeetingDetail';

// Polls
import PollsHub from './pages/polls/PollsHub';
import Polls from './pages/polls/Polls';
import CreatePoll from './pages/polls/CreatePoll';
import PollDetail from './pages/polls/PollDetail';

// Announcements
import Announcements from './pages/announcements/Announcements';
import CreateAnnouncement from './pages/announcements/CreateAnnouncement';

// Notifications
import Notifications from './pages/notifications/Notifications';
import NotificationPreferences from './pages/notifications/NotificationPreferences';

// Shares & Dividends
import Shares from './pages/shares/Shares';
import BuyShares from './pages/shares/BuyShares';
import Dividends from './pages/dividends/Dividends';

// Group Settings
import GroupSettings from './pages/groups/GroupSettings';

// Profile
import Profile from './pages/profile/Profile';

// Chat
import ChatPage from './pages/chat/ChatPage';

// Admin
import AdminProtectedRoute from './components/common/AdminProtectedRoute';
import AdminLayout from './pages/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminApprovals from './pages/admin/AdminApprovals';
import AdminUsers from './pages/admin/AdminUsers';
import AdminAuditLogs from './pages/admin/AdminAuditLogs';
import AdminSettings from './pages/admin/AdminSettings';
import AdminProfile from './pages/admin/AdminProfile';
import AdminFinanceRequests from './pages/admin/AdminFinanceRequests';

function App() {
  const { i18n } = useTranslation();
  document.documentElement.lang = i18n.language;

  return (
    <RegistrationProvider>
      <AuthProvider>
        {/* ChatProvider must be inside AuthProvider so it can read the current user */}
        <ChatProvider>
          <Routes>
            {/* ---------- PUBLIC ---------- */}
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/login/mfa" element={<MfaLogin />} />
            <Route path="/register" element={<Register />} />
            <Route path="/register/verify-phone" element={<VerifyPhone />} />
            <Route path="/register/verify-email" element={<VerifyEmail />} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/terms" element={<Terms />} />
            <Route path="/privacy" element={<Privacy />} />

            {/* ---------- PROTECTED (REGULAR USERS) ---------- */}
            <Route
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
            >
              {/* Dashboard */}
              <Route path="/dashboard" element={<Dashboard />} />

              {/* Groups */}
              <Route path="/groups" element={<Groups />} />
              <Route path="/groups/create" element={<CreateGroup />} />
              <Route path="/groups/join" element={<JoinGroup />} />
              <Route path="/groups/:groupId" element={<GroupDetail />} />

              {/* Contributions */}
              <Route path="/contributions" element={<MyContributions />} />
              <Route path="/groups/:groupId/contribute" element={<MakeContribution />} />
              <Route path="/groups/:groupId/contributions" element={<GroupContributions />} />

              {/* Loans */}
              <Route path="/loans" element={<MyLoans />} />
              <Route path="/loans/:loanId" element={<LoanDetail />} />
              <Route path="/groups/:groupId/loans/apply" element={<ApplyLoan />} />
              <Route path="/groups/:groupId/loans/pending" element={<PendingLoans />} />

              {/* Meetings — hub + group-scoped */}
              <Route path="/meetings" element={<MeetingsHub />} />
              <Route path="/meetings/:meetingId" element={<MeetingDetail />} />
              <Route path="/groups/:groupId/meetings" element={<Meetings />} />
              <Route path="/groups/:groupId/meetings/schedule" element={<ScheduleMeeting />} />

              {/* Polls — hub + group-scoped */}
              <Route path="/polls" element={<PollsHub />} />
              <Route path="/polls/:pollId" element={<PollDetail />} />
              <Route path="/groups/:groupId/polls" element={<Polls />} />
              <Route path="/groups/:groupId/polls/create" element={<CreatePoll />} />

              {/* Announcements */}
              <Route path="/groups/:groupId/announcements" element={<Announcements />} />
              <Route path="/groups/:groupId/announcements/create" element={<CreateAnnouncement />} />

              {/* Notifications */}
              <Route path="/notifications" element={<Notifications />} />
              <Route path="/profile/notification-preferences" element={<NotificationPreferences />} />

              {/* Shares & Dividends */}
              <Route path="/groups/:groupId/shares" element={<Shares />} />
              <Route path="/groups/:groupId/shares/buy" element={<BuyShares />} />
              <Route path="/groups/:groupId/dividends" element={<Dividends />} />

              {/* Groups Settings */}
              <Route path="/groups/:groupId/settings" element={<GroupSettings />} />

              {/* Profile */}
              <Route path="/profile" element={<Profile />} />

              {/* Chat */}
              <Route path="/chat" element={<ChatPage />} />
            </Route>

            {/* ---------- ADMIN PROTECTED ---------- */}
            <Route element={<AdminProtectedRoute allowedRoles={['SUPER_ADMIN', 'FINANCE_ADMIN', 'SUPPORT_ADMIN', 'AUDITOR']} />}>
              <Route path="/admin" element={<AdminLayout />}>
                <Route index element={<AdminDashboard />} />
                <Route path="approvals" element={<AdminApprovals />} />
                {/* ADDED THE NEW ROUTES HERE */}
                <Route path="users" element={<AdminUsers />} />
                <Route path="audit-logs" element={<AdminAuditLogs />} />
                <Route path="settings" element={<AdminSettings />} />
                <Route path="profile" element={<AdminProfile />} />
                <Route path="finance-requests" element={<AdminFinanceRequests />} />
              </Route>
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </ChatProvider>
      </AuthProvider>
    </RegistrationProvider>
  );
}

export default App;