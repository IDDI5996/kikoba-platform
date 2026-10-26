import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import API from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import Button from '../../components/common/Button';
import Logo from '../../components/common/Logo';
import LanguageSwitcher from '../../components/common/LanguageSwitcher';
import OtpInput from '../../components/auth/OtpInput';
import './Auth.css';

const MfaLogin = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { setUser, setToken } = useAuth();
  const [code, setCode] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Session token passed via location.state from Login
  const sessionToken = window.history.state?.usr?.sessionToken;

  if (!sessionToken) {
    navigate('/login');
    return null;
  }

  const onSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const { data } = await API.post('/auth/login/verify-mfa', {
        sessionToken,
        code,
      });

      localStorage.setItem('kikoba_token', data.accessToken);
      localStorage.setItem('kikoba_refresh', data.refreshToken);
      setToken(data.accessToken);
      setUser(data.user);

      toast.success(t('common.success'));
      navigate('/dashboard');
    } catch (err) {
      toast.error(err?.response?.data?.message || t('common.error'));
      setCode('');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-layout">
      <div className="auth-header">
        <Logo variant="full" />
        <LanguageSwitcher />
      </div>

      <div className="auth-container auth-container-center">
        <div className="auth-form-panel">
          <div className="auth-form-inner">
            <div className="auth-form-header">
              <h1 className="auth-title">{t('auth.mfaTitle')}</h1>
              <p className="auth-subtitle">{t('auth.mfaSubtitle')}</p>
            </div>

            <form onSubmit={onSubmit} className="auth-form" noValidate>
              <OtpInput value={code} onChange={setCode} length={6} autoFocus />

              <Button
                type="submit"
                variant="primary"
                size="lg"
                fullWidth
                loading={submitting}
                disabled={code.length !== 6}
              >
                {t('auth.verify')}
              </Button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MfaLogin;