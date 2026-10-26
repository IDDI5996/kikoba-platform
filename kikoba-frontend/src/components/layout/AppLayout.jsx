import { useState } from 'react';
import { Outlet, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  LayoutDashboard, Users, Wallet, HandCoins, Calendar,
  Vote, TrendingUp, Bell, User as UserIcon, LogOut,
  Menu, X, ChevronRight, MessageCircle
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useIsMobile } from '../../hooks/useMediaQuery';
import LanguageSwitcher from '../common/LanguageSwitcher';
import NotificationBell from './NotificationBell';
import Logo from '../common/Logo';
import './AppLayout.css';

const NAV_ITEMS = [
  { to: '/dashboard',    icon: LayoutDashboard, key: 'dashboard' },
  { to: '/chat',         icon: MessageCircle,   key: 'chat' },
  { to: '/groups',       icon: Users,           key: 'groups' },
  { to: '/contributions',icon: Wallet,          key: 'contributions' },
  { to: '/loans',        icon: HandCoins,       key: 'loans' },
  { to: '/meetings',     icon: Calendar,        key: 'meetings' },
  { to: '/polls',        icon: Vote,            key: 'polls' },
  { to: '/notifications',icon: Bell,            key: 'notifications' },
  { to: '/profile',      icon: UserIcon,        key: 'profile' },
];

const BOTTOM_NAV = [
  { to: '/dashboard',    icon: LayoutDashboard, key: 'dashboard' },
  { to: '/chat',         icon: MessageCircle,   key: 'chat' },
  { to: '/groups',       icon: Users,           key: 'groups' },
  { to: '/contributions',icon: Wallet,          key: 'contributions' },
  { to: '/loans',        icon: HandCoins,       key: 'loans' },
  { to: '/profile',      icon: UserIcon,        key: 'profile' },
];

const AppLayout = () => {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const isMobile = useIsMobile();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getInitials = (u) => {
    if (!u) return '?';
    return `${u.firstName?.[0] ?? ''}${u.lastName?.[0] ?? ''}`.toUpperCase();
  };

  // Helper function to get the correct image URL
  const getAvatarUrl = (url) => {
    if (!url) return null;
    return url.startsWith('http') ? url : `http://localhost:8080${url}`;
  };

  const avatarUrl = getAvatarUrl(user?.profilePictureUrl);

  return (
    <div className="app-shell">
      {/* Skip to content for accessibility */}
      <a href="#main-content" className="skip-link">
        {t('nav.dashboard')}
      </a>

      {/* Mobile overlay */}
      {isMobile && sidebarOpen && (
        <div
          className="app-overlay"
          onClick={() => setSidebarOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* Sidebar (desktop) */}
      {!isMobile && (
        <aside className="app-sidebar" aria-label="Main navigation">
          <div className="app-sidebar-header">
            <Logo variant="full" />
          </div>
          <nav className="app-sidebar-nav">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `app-nav-item ${isActive ? 'active' : ''}`
                }
              >
                <item.icon size={18} aria-hidden="true" />
                <span>{t(`nav.${item.key}`)}</span>
              </NavLink>
            ))}
          </nav>
          <div className="app-sidebar-footer">
            <NavLink to="/profile" className="app-user-card">
              <div className="app-avatar">
                {avatarUrl ? (
                  <img
                    src={avatarUrl}
                    alt="Profile"
                    style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }}
                  />
                ) : (
                  getInitials(user)
                )}
              </div>
              <div className="app-user-info">
                <span className="app-user-name">
                  {user?.firstName} {user?.lastName}
                </span>
                <span className="app-user-phone">{user?.phoneNumber}</span>
              </div>
              <ChevronRight size={16} aria-hidden="true" />
            </NavLink>
            <button
              type="button"
              className="app-nav-item app-nav-logout"
              onClick={handleLogout}
            >
              <LogOut size={18} aria-hidden="true" />
              <span>{t('nav.logout')}</span>
            </button>
          </div>
        </aside>
      )}

      {/* Mobile drawer */}
      {isMobile && sidebarOpen && (
        <aside className="app-drawer" aria-label="Main navigation">
          <div className="app-drawer-header">
            <Logo variant="full" />
            <button
              type="button"
              className="app-icon-btn"
              onClick={() => setSidebarOpen(false)}
              aria-label={t('common.close')}
            >
              <X size={20} />
            </button>
          </div>
          <nav className="app-drawer-nav">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={() => setSidebarOpen(false)}
                className={({ isActive }) =>
                  `app-nav-item ${isActive ? 'active' : ''}`
                }
              >
                <item.icon size={18} aria-hidden="true" />
                <span>{t(`nav.${item.key}`)}</span>
              </NavLink>
            ))}
            <button
              type="button"
              className="app-nav-item app-nav-logout"
              onClick={handleLogout}
            >
              <LogOut size={18} aria-hidden="true" />
              <span>{t('nav.logout')}</span>
            </button>
          </nav>
        </aside>
      )}

      {/* Main content */}
      <div className="app-main">
        <header className="app-header">
          <div className="app-header-left">
            {isMobile && (
              <button
                type="button"
                className="app-icon-btn"
                onClick={() => setSidebarOpen(true)}
                aria-label={t('common.search')}
              >
                <Menu size={20} />
              </button>
            )}
            {!isMobile && (
              <h1 className="app-page-title">
                {t(`nav.${location.pathname.split('/')[1] || 'dashboard'}`)}
              </h1>
            )}
          </div>
          <div className="app-header-right">
            <LanguageSwitcher />
            <NotificationBell />

            {/* TOP BAR AVATAR */}
            {!isMobile && (
              <div className="app-header-avatar" title={user?.firstName}>
                {avatarUrl ? (
                  <img
                    src={avatarUrl}
                    alt="Profile"
                    style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }}
                  />
                ) : (
                  getInitials(user)
                )}
              </div>
            )}
          </div>
        </header>

        <main id="main-content" className="app-content" tabIndex={-1}>
          <Outlet />
        </main>

        {/* Mobile bottom nav */}
        {isMobile && (
          <nav className="app-bottom-nav" aria-label="Quick navigation">
            {BOTTOM_NAV.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `app-bottom-nav-item ${isActive ? 'active' : ''}`
                }
              >
                <item.icon size={20} aria-hidden="true" />
                <span>{t(`nav.${item.key}`)}</span>
              </NavLink>
            ))}
          </nav>
        )}
      </div>
    </div>
  );
};

export default AppLayout;