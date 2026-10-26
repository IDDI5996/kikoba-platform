import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { Phone, Lock } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import API from '../../services/api';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import LanguageSwitcher from '../../components/common/LanguageSwitcher';
import Logo from '../../components/common/Logo';
import './Auth.css';

const Login = () => {
  const { t } = useTranslation();
  const { setUser, setToken } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    setSubmitting(true);
    try {
      const { data: response } = await API.post('/auth/login/initiate', {
        identifier: data.identifier,
        password: data.password,
      });

      // ---- MFA required: navigate to MFA screen with session token ----
      if (response.mfaRequired) {
        toast.info(t('auth.mfaTitle'));
        navigate('/login/mfa', {
          state: { sessionToken: response.sessionToken },
        });
        return;
      }

      // ---- No MFA: login complete ----
      localStorage.setItem('kikoba_token', response.accessToken);
      localStorage.setItem('kikoba_refresh', response.refreshToken);

      if (setToken) setToken(response.accessToken);
      if (setUser) setUser(response.user);

      toast.success(t('common.success'));
      navigate('/dashboard');
    } catch (err) {
      // If the response is not JSON (e.g., HTML error page), fall back to generic message
      const message =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        t('errors.networkError');
      toast.error(message);
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

      <div className="auth-container">
        {/* Left: illustration */}
        <div className="auth-illustration" aria-hidden="true">
          <img
            src="/images/auth-community.svg"
            alt=""
            className="auth-illustration-img"
          />
          <div className="auth-illustration-text">
            <h2 className="auth-illustration-title">{t('app.tagline')}</h2>
            <p className="auth-illustration-subtitle">{t('auth.subtitle')}</p>
          </div>
        </div>

        {/* Right: form */}
        <div className="auth-form-panel">
          <div className="auth-form-inner">
            <div className="auth-form-header">
              <h1 className="auth-title">{t('auth.welcome')}</h1>
              <p className="auth-subtitle">{t('auth.signInTo')}</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="auth-form" noValidate>
              <Input
                label={t('auth.phoneOrEmail')}
                type="text"
                placeholder={t('auth.phoneOrEmailPlaceholder')}
                leftIcon={Phone}
                autoComplete="username"
                required
                error={errors.identifier && t('errors.required')}
                {...register('identifier', { required: true })}
              />
              <Input
                label={t('auth.password')}
                type="password"
                placeholder="••••••••"
                leftIcon={Lock}
                autoComplete="current-password"
                required
                error={errors.password && t('errors.required')}
                {...register('password', { required: true })}
              />

              <div className="auth-form-row">
                <Link to="/forgot-password" className="forgot-password-link">
                  {t('auth.forgotPassword')}
                </Link>
              </div>

              <Button
                type="submit"
                variant="primary"
                size="lg"
                fullWidth
                loading={submitting}
              >
                {t('auth.login')}
              </Button>
            </form>

            <div className="auth-form-footer">
              <span>{t('auth.noAccount')} </span>
              <Link to="/register" className="auth-link auth-link-strong">
                {t('auth.register')}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;