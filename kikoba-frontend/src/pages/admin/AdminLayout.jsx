import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import { ShieldAlert, Users, FileText, CheckSquare, LogOut, ArrowLeft, Settings, User, DollarSign } from 'lucide-react';
import Logo from '../../components/common/Logo';
import LanguageSwitcher from '../../components/common/LanguageSwitcher'; // <-- IMPORT
import './Admin.css';

const AdminLayout = () => {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const role = user?.adminRole;

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="admin-layout">
      <aside className="admin-sidebar">
        <div className="admin-sidebar-header">
          <Logo variant="full" style={{ color: 'white' }} />
          <span className="admin-badge">{t('admin.badge')}</span>
        </div>

        <nav className="admin-nav">
          <NavLink to="/admin" end className={({ isActive }) => isActive ? 'admin-nav-item active' : 'admin-nav-item'}>
            <ShieldAlert size={20} /> {t('admin.overview')}
          </NavLink>

          {(role === 'SUPER_ADMIN' || role === 'AUDITOR') && (
            <NavLink to="/admin/approvals" className={({ isActive }) => isActive ? 'admin-nav-item active' : 'admin-nav-item'}>
              <CheckSquare size={20} /> {t('admin.approvals')}
            </NavLink>
          )}

          {(role === 'SUPER_ADMIN' || role === 'SUPPORT_ADMIN') && (
            <NavLink to="/admin/users" className={({ isActive }) => isActive ? 'admin-nav-item active' : 'admin-nav-item'}>
              <Users size={20} /> {t('admin.userManagement')}
            </NavLink>
          )}

          {(role === 'SUPER_ADMIN' || role === 'AUDITOR') && (
            <NavLink to="/admin/audit-logs" className={({ isActive }) => isActive ? 'admin-nav-item active' : 'admin-nav-item'}>
              <FileText size={20} /> {t('admin.auditLogs')}
            </NavLink>
          )}

          {(role === 'FINANCE_ADMIN' || role === 'SUPER_ADMIN') && (
              <NavLink to="/admin/finance-requests" className={({ isActive }) => isActive ? 'admin-nav-item active' : 'admin-nav-item'}>
                <DollarSign size={20} /> Finance Requests
              </NavLink>
          )}

          {role === 'SUPER_ADMIN' && (
            <NavLink to="/admin/settings" className={({ isActive }) => isActive ? 'admin-nav-item active' : 'admin-nav-item'}>
              <Settings size={20} /> {t('admin.systemSettings')}
            </NavLink>
          )}

          <NavLink to="/admin/profile" className={({ isActive }) => isActive ? 'admin-nav-item active' : 'admin-nav-item'}>
            <User size={20} /> {t('admin.myProfile')}
          </NavLink>
        </nav>

        <div className="admin-sidebar-footer">
          <div className="admin-user-info">
            <p className="admin-user-name">{user?.firstName} {user?.lastName}</p>
            <p className="admin-user-role">{role?.replace('_', ' ')}</p>
          </div>
          <button onClick={handleLogout} className="admin-logout-btn">
            <LogOut size={16} /> {t('admin.logout')}
          </button>
        </div>
      </aside>

      <main className="admin-main">
        <header className="admin-header">
          <button className="admin-back-btn" onClick={() => navigate('/dashboard')}>
            <ArrowLeft size={16} /> {t('admin.backToApp')}
          </button>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <LanguageSwitcher /> {/* <-- Added Language Switcher here so admins can toggle directly */}
            <div className="admin-security-status">
              <span className="dot green"></span> {t('admin.secureSession')}
            </div>
          </div>
        </header>
        <div className="admin-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;