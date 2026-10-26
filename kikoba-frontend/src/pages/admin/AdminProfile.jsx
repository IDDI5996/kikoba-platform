import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import API from '../../services/api';
import { useTranslation } from 'react-i18next'; // <-- ADDED
import { toast } from 'sonner';
import { Lock, User, Shield } from 'lucide-react';

const AdminProfile = () => {
  const { t } = useTranslation(); // <-- ADDED
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const handleChangePassword = async (e) => {
    e.preventDefault();
    if (formData.newPassword !== formData.confirmPassword) {
      toast.error(t('admin.profile.passwordMismatch'));
      return;
    }
    if (formData.newPassword.length < 8) {
      toast.error(t('admin.profile.passwordTooShort'));
      return;
    }

    setLoading(true);
    try {
      await API.put('/users/me/password', {
        currentPassword: formData.currentPassword,
        newPassword: formData.newPassword,
      });
      toast.success(t('admin.profile.success'));
      setFormData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      toast.error(err.response?.data?.message || t('admin.profile.error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div className="admin-page-header">
        <h1>{t('admin.profile.title')}</h1>
        <p>{t('admin.profile.subtitle')}</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        {/* Account Details Card */}
        <div className="admin-settings-card">
          <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <User size={20} /> {t('admin.profile.accountDetails')}
          </h2>
          <div style={{ marginBottom: '12px' }}>
            <label style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>{t('admin.profile.fullName')}</label>
            <p style={{ fontSize: '16px', fontWeight: '500' }}>{user?.firstName} {user?.lastName}</p>
          </div>
          <div style={{ marginBottom: '12px' }}>
            <label style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>{t('admin.profile.phoneNumber')}</label>
            <p style={{ fontSize: '16px', fontWeight: '500' }}>{user?.phoneNumber}</p>
          </div>
          <div>
            <label style={{ fontSize: '12px', color: '#64748b', fontWeight: 'bold' }}>{t('admin.profile.role')}</label>
            <p style={{ fontSize: '14px', fontWeight: '600', color: '#ef4444', marginTop: '4px' }}>
              <Shield size={14} style={{ display: 'inline', marginRight: '4px' }} />
              {user?.adminRole?.replace('_', ' ')}
            </p>
          </div>
        </div>

        {/* Change Password Card */}
        <div className="admin-settings-card">
          <h2 style={{ fontSize: '18px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Lock size={20} /> {t('admin.profile.changePassword')}
          </h2>
          <form onSubmit={handleChangePassword}>
            <div className="admin-form-group">
              <label>{t('admin.profile.currentPassword')}</label>
              <input
                type="password"
                className="admin-input"
                value={formData.currentPassword}
                onChange={(e) => setFormData({ ...formData, currentPassword: e.target.value })}
                required
              />
            </div>
            <div className="admin-form-group">
              <label>{t('admin.profile.newPassword')}</label>
              <input
                type="password"
                className="admin-input"
                value={formData.newPassword}
                onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                required
              />
            </div>
            <div className="admin-form-group">
              <label>{t('admin.profile.confirmPassword')}</label>
              <input
                type="password"
                className="admin-input"
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                required
              />
            </div>
            <button type="submit" className="admin-btn-approve" disabled={loading}>
              {loading ? t('admin.profile.updating') : t('admin.profile.updatePassword')}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AdminProfile;