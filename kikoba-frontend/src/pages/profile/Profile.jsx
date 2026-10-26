import { useRef, useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import {
  Lock, Shield, Globe, LogOut, Sun, Moon, Bell,
  KeyRound, ChevronRight, IdCard, Users, Camera,
  CheckCircle2, XCircle, AlertCircle, Mail, Phone,
  BadgeCheck,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useMyGroups } from '../../hooks/useGroups';
import usersService from '../../services/usersService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Modal from '../../components/common/Modal';
import Badge from '../../components/common/Badge';
import LoadingState from '../../components/common/LoadingState';
import './Profile.css';

const Profile = () => {
  const { t, i18n } = useTranslation();
  const { user, logout, setUser } = useAuth();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const { data: groups, loading: groupsLoading } = useMyGroups();

  const [editOpen, setEditOpen] = useState(false);
  const [pwdOpen, setPwdOpen] = useState(false);
  const [kycOpen, setKycOpen] = useState(false);
  const [mfaOpen, setMfaOpen] = useState(false);
  const [editData, setEditData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
  });
  const [pwdData, setPwdData] = useState({ current: '', next: '', confirm: '' });
  const [kycUrl, setKycUrl] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [uploading, setUploading] = useState(false);

  const theme = localStorage.getItem('kikoba_theme') || 'light';

  // ---- Avatar upload ----
  const handleAvatarClick = () => fileInputRef.current?.click();

  const handleAvatarChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > 2 * 1024 * 1024) {
      toast.error(t('profile.avatarTooLarge') || 'File must be under 2 MB');
      return;
    }

    setUploading(true);
    try {
      const updated = await usersService.uploadAvatar(file);
      setUser(updated);
      toast.success(t('profile.avatarUpdated') || 'Profile picture updated');
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  // ---- Profile edit ----
  const handleSaveProfile = async () => {
    setSubmitting(true);
    try {
      const updated = await usersService.updateProfile(editData);
      setUser(updated);
      toast.success(t('common.success'));
      setEditOpen(false);
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  // ---- Password change ----
  const handleChangePassword = async () => {
    if (pwdData.next !== pwdData.confirm) {
      toast.error(t('errors.passwordMismatch'));
      return;
    }
    if (pwdData.next.length < 8) {
      toast.error(t('errors.passwordTooShort'));
      return;
    }
    // TODO: wire to backend when endpoint is built
    toast.info(t('profile.passwordChangeComingSoon') || 'Password change endpoint coming soon');
    setPwdOpen(false);
    setPwdData({ current: '', next: '', confirm: '' });
  };

  // ---- MFA toggle ----
  const handleToggleMfa = async () => {
    setSubmitting(true);
    try {
      const updated = user?.mfaEnabled
        ? await usersService.disableMfa()
        : await usersService.enableMfa();
      setUser(updated);
      toast.success(t('common.success'));
      setMfaOpen(false);
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  // ---- KYC submission ----
  const handleSubmitKyc = async () => {
    if (!kycUrl || kycUrl.trim().length < 5) {
      toast.error(t('profile.kycUrlRequired') || 'Document URL is required');
      return;
    }
    setSubmitting(true);
    try {
      await usersService.submitKyc(kycUrl.trim());
      const refreshed = await usersService.me();
      setUser(refreshed);
      toast.success(t('profile.kycSubmitted') || 'KYC submitted for review');
      setKycOpen(false);
      setKycUrl('');
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  // ---- Language ----
  const handleLanguageChange = (lang) => {
    i18n.changeLanguage(lang);
    localStorage.setItem('kikoba_language', lang);
    toast.success(t('common.success'));
  };

  // ---- Theme ----
  const toggleTheme = () => {
    const next = theme === 'light' ? 'dark' : 'light';
    localStorage.setItem('kikoba_theme', next);
    document.documentElement.setAttribute('data-theme', next);
    toast.success(t('common.success'));
  };

  // ---- Logout ----
  const handleLogout = () => {
    if (!window.confirm(t('profile.confirmLogout') || 'Sign out of your account?')) return;
    logout();
    navigate('/login');
  };

  const getInitials = () => {
    if (!user) return '?';
    return `${user.firstName?.[0] || ''}${user.lastName?.[0] || ''}`.toUpperCase();
  };

  return (
    <div className="profile-page">
      <h1 className="page-title">{t('profile.title')}</h1>

      {/* ---- Identity card ---- */}
      <Card padding="lg" className="profile-identity">
        <div className="profile-avatar-wrap">
          <div className="profile-avatar-large">
            {user?.profilePictureUrl ? (
              <img src={user.profilePictureUrl} alt="Profile" />
            ) : (
              <span>{getInitials()}</span>
            )}
          </div>
          <button
            type="button"
            className="profile-avatar-upload"
            onClick={handleAvatarClick}
            disabled={uploading}
            aria-label={t('profile.changePhoto') || 'Change photo'}
          >
            <Camera size={14} />
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp"
            onChange={handleAvatarChange}
            style={{ display: 'none' }}
          />
        </div>

        <div className="profile-identity-info">
          <h2 className="profile-name">
            {user?.firstName} {user?.lastName}
          </h2>
          <div className="profile-contact">
            <span className="profile-contact-item">
              <Phone size={12} /> {user?.phoneNumber}
            </span>
            {user?.email && (
              <span className="profile-contact-item">
                <Mail size={12} /> {user.email}
              </span>
            )}
          </div>
          <div className="profile-badges">
            {user?.kycVerified ? (
              <Badge variant="success" size="sm">
                <BadgeCheck size={11} /> {t('profile.kycVerified')}
              </Badge>
            ) : (
              <Badge variant="warning" size="sm">
                <AlertCircle size={11} /> {t('profile.kycNotVerified')}
              </Badge>
            )}
            {user?.mfaEnabled && (
              <Badge variant="primary" size="sm">
                <Lock size={11} /> MFA
              </Badge>
            )}
          </div>
        </div>

        <Button variant="secondary" onClick={() => setEditOpen(true)}>
          {t('common.edit')}
        </Button>
      </Card>

      {/* ---- Security ---- */}
      <Card padding="none" className="profile-section">
        <div className="profile-section-header">
          <Shield size={16} />
          <h3 className="profile-section-title">{t('profile.security')}</h3>
        </div>
        <ul className="profile-menu">
          <li>
            <button type="button" className="profile-menu-item" onClick={() => setPwdOpen(true)}>
              <KeyRound size={18} />
              <span>{t('profile.changePassword')}</span>
              <ChevronRight size={16} />
            </button>
          </li>

          <li>
            <button type="button" className="profile-menu-item" onClick={() => setMfaOpen(true)}>
              <Lock size={18} />
              <span>{t('profile.mfaSettings') || 'Two-Factor Authentication'}</span>
              {user?.mfaEnabled ? (
                <Badge variant="success" size="sm">
                  <CheckCircle2 size={10} /> {t('profile.enabled') || 'Enabled'}
                </Badge>
              ) : (
                <Badge variant="neutral" size="sm">
                  <XCircle size={10} /> {t('profile.disabled') || 'Disabled'}
                </Badge>
              )}
            </button>
          </li>

          <li>
            <button type="button" className="profile-menu-item" onClick={() => setKycOpen(true)}>
              <IdCard size={18} />
              <span>{t('profile.kycStatus')}</span>
              {user?.kycVerified ? (
                <Badge variant="success" size="sm">
                  <CheckCircle2 size={10} /> {t('profile.kycVerified')}
                </Badge>
              ) : (
                <Badge variant="warning" size="sm">
                  {t('profile.kycNotVerified')}
                </Badge>
              )}
            </button>
          </li>
        </ul>
      </Card>

      {/* ---- Preferences ---- */}
      <Card padding="none" className="profile-section">
        <div className="profile-section-header">
          <Globe size={16} />
          <h3 className="profile-section-title">{t('profile.preferences')}</h3>
        </div>
        <ul className="profile-menu">
          <li>
            <div className="profile-menu-item">
              <Globe size={18} />
              <span>{t('profile.language')}</span>
              <div className="profile-lang-toggle">
                <button
                  type="button"
                  className={i18n.language?.startsWith('sw') ? 'active' : ''}
                  onClick={() => handleLanguageChange('sw')}
                >
                  SW
                </button>
                <button
                  type="button"
                  className={i18n.language?.startsWith('en') ? 'active' : ''}
                  onClick={() => handleLanguageChange('en')}
                >
                  EN
                </button>
              </div>
            </div>
          </li>
          <li>
            <button type="button" className="profile-menu-item" onClick={toggleTheme}>
              {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
              <span>{t('profile.theme')}</span>
              <Badge variant="neutral" size="sm">
                {theme === 'light' ? t('profile.themeLight') : t('profile.themeDark')}
              </Badge>
            </button>
          </li>
          <li>
            <Link to="/profile/notification-preferences" className="profile-menu-item">
              <Bell size={18} />
              <span>{t('profile.notificationPreferences')}</span>
              <ChevronRight size={16} />
            </Link>
          </li>
        </ul>
      </Card>

      {/* ---- Groups ---- */}
      <Card padding="none" className="profile-section">
        <div className="profile-section-header">
          <Users size={16} />
          <h3 className="profile-section-title">{t('profile.joinedGroups')}</h3>
        </div>

        {groupsLoading ? (
          <LoadingState />
        ) : !groups || groups.length === 0 ? (
          <p className="profile-empty-groups">{t('groups.noGroups')}</p>
        ) : (
          <ul className="profile-groups-list">
            {groups.map((g) => (
              <li key={g.id}>
                <Link to={`/groups/${g.id}`} className="profile-group-item">
                  <div className="profile-group-icon">
                    <Users size={16} />
                  </div>
                  <div className="profile-group-info">
                    <p className="profile-group-name">{g.name}</p>
                    <p className="profile-group-meta">
                      {t(`groups.types.${g.type}`)} ·{' '}
                      {t('dashboard.memberCount', { count: g.memberCount })}
                    </p>
                  </div>
                  <Badge
                    variant={
                      g.myRole === 'CHAIRPERSON'
                        ? 'primary'
                        : g.myRole === 'TREASURER'
                        ? 'info'
                        : g.myRole === 'SECRETARY'
                        ? 'warning'
                        : 'neutral'
                    }
                    size="sm"
                  >
                    {t(`groups.roles.${g.myRole || 'MEMBER'}`)}
                  </Badge>
                  <ChevronRight size={16} className="profile-group-arrow" />
                </Link>
              </li>
            ))}
          </ul>
        )}
      </Card>

      {/* ---- Logout ---- */}
      <Card padding="none" className="profile-section">
        <button type="button" className="profile-menu-item danger" onClick={handleLogout}>
          <LogOut size={18} />
          <span>{t('profile.logout')}</span>
        </button>
      </Card>

      {/* ============================================================
          MODALS
          ============================================================ */}

      {/* Edit Profile */}
      <Modal
        isOpen={editOpen}
        onClose={() => setEditOpen(false)}
        title={t('profile.editProfile') || 'Edit Profile'}
        footer={
          <>
            <Button variant="secondary" onClick={() => setEditOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button variant="primary" loading={submitting} onClick={handleSaveProfile}>
              {t('common.save')}
            </Button>
          </>
        }
      >
        <div className="modal-form">
          <Input
            label={t('auth.firstName')}
            value={editData.firstName}
            onChange={(e) => setEditData({ ...editData, firstName: e.target.value })}
          />
          <Input
            label={t('auth.lastName')}
            value={editData.lastName}
            onChange={(e) => setEditData({ ...editData, lastName: e.target.value })}
          />
          <Input
            label={t('auth.email')}
            type="email"
            value={editData.email}
            onChange={(e) => setEditData({ ...editData, email: e.target.value })}
          />
        </div>
      </Modal>

      {/* Change Password */}
      <Modal
        isOpen={pwdOpen}
        onClose={() => setPwdOpen(false)}
        title={t('profile.changePassword')}
        footer={
          <>
            <Button variant="secondary" onClick={() => setPwdOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button variant="primary" loading={submitting} onClick={handleChangePassword}>
              {t('common.save')}
            </Button>
          </>
        }
      >
        <div className="modal-form">
          <Input
            label={t('profile.currentPassword') || 'Current Password'}
            type="password"
            value={pwdData.current}
            onChange={(e) => setPwdData({ ...pwdData, current: e.target.value })}
          />
          <Input
            label={t('profile.newPassword') || 'New Password'}
            type="password"
            value={pwdData.next}
            onChange={(e) => setPwdData({ ...pwdData, next: e.target.value })}
          />
          <Input
            label={t('auth.confirmPassword')}
            type="password"
            value={pwdData.confirm}
            onChange={(e) => setPwdData({ ...pwdData, confirm: e.target.value })}
          />
        </div>
      </Modal>

      {/* MFA Toggle */}
      <Modal
        isOpen={mfaOpen}
        onClose={() => setMfaOpen(false)}
        title={t('profile.mfaSettings') || 'Two-Factor Authentication'}
        footer={
          <>
            <Button variant="secondary" onClick={() => setMfaOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button
              variant={user?.mfaEnabled ? 'danger' : 'primary'}
              loading={submitting}
              onClick={handleToggleMfa}
            >
              {user?.mfaEnabled
                ? t('profile.disableMfa')
                : t('profile.enableMfa')}
            </Button>
          </>
        }
      >
        <div className="modal-info">
          <div className="modal-info-icon">
            <Shield size={24} />
          </div>
          <p className="modal-info-text">
            {user?.mfaEnabled
              ? t('profile.mfaEnabledHint') ||
                'Two-factor authentication is active. You will be asked for a code when you log in from a new device.'
              : t('profile.mfaDisabledHint') ||
                'Add an extra layer of security to your account. After enabling, you will need an SMS code to sign in.'}
          </p>
        </div>
      </Modal>

      {/* KYC */}
      <Modal
        isOpen={kycOpen}
        onClose={() => setKycOpen(false)}
        title={t('profile.kycStatus')}
        footer={
          user?.kycVerified ? (
            <Button variant="secondary" onClick={() => setKycOpen(false)}>
              {t('common.close')}
            </Button>
          ) : (
            <>
              <Button variant="secondary" onClick={() => setKycOpen(false)}>
                {t('common.cancel')}
              </Button>
              <Button variant="primary" loading={submitting} onClick={handleSubmitKyc}>
                {t('common.submit')}
              </Button>
            </>
          )
        }
      >
        {user?.kycVerified ? (
          <div className="modal-info">
            <div className="modal-info-icon modal-info-success">
              <BadgeCheck size={28} />
            </div>
            <p className="modal-info-text">
              {t('profile.kycVerifiedHint') ||
                'Your identity has been verified. You have full access to all features.'}
            </p>
          </div>
        ) : (
          <div className="modal-form">
            <p className="modal-info-text">
              {t('profile.kycSubmitHint') ||
                'Submit a link to your ID document to complete verification.'}
            </p>
            <Input
              label={t('profile.documentUrl') || 'Document URL'}
              placeholder="https://..."
              value={kycUrl}
              onChange={(e) => setKycUrl(e.target.value)}
            />
          </div>
        )}
      </Modal>
    </div>
  );
};

export default Profile;