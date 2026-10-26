import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { Phone, Lock, Mail, User } from 'lucide-react';
import { useRegistration } from '../../context/RegistrationContext';
import API from '../../services/api';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import LanguageSwitcher from '../../components/common/LanguageSwitcher';
import Logo from '../../components/common/Logo';
import './Auth.css';

const Register = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { setPending } = useRegistration();     // ← THIS WAS MISSING
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm();

  const password = watch('password');

  const onSubmit = async (data) => {
    setSubmitting(true);
    try {
      const { data: response } = await API.post('/auth/register/initiate', {
        phoneNumber: data.phoneNumber,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email || null,
        password: data.password,
      });

      // Save state for the next screens
      setPending({
        ...response,
        phoneNumber: data.phoneNumber,
        email: data.email || null,
        firstName: data.firstName,
        lastName: data.lastName,
      });

      // Route based on what the backend says is next
      if (response.nextStep === 'VERIFY_EMAIL') {
        // RESUMING — skip phone verification, go to email step
        toast.success(t('auth.resumeEmailStep'));
        navigate('/register/verify-email');
      } else if (response.nextStep === 'COMPLETE') {
        // Already complete — go to login
        toast.success(t('auth.registrationComplete'));
        navigate('/login');
      } else {
        // Fresh registration — go to phone verification
        toast.success(t('auth.otpSent', { phone: response.maskedPhone }));
        navigate('/register/verify-phone');
      }
    } catch (err) {
      const message =
        err?.response?.data?.message || t('errors.networkError');
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
        <div className="auth-illustration" aria-hidden="true">
          <img
            src="/images/auth-community.svg"
            alt=""
            className="auth-illustration-img"
          />
        </div>

        <div className="auth-form-panel">
          <div className="auth-form-inner">
            {/* Step indicator */}
            <div className="auth-steps">
              <span className="auth-step active">1</span>
              <span className="auth-step-line" />
              <span className="auth-step">2</span>
              <span className="auth-step-line" />
              <span className="auth-step">3</span>
            </div>

            <div className="auth-form-header">
              <h1 className="auth-title">{t('auth.createAccount')}</h1>
              <p className="auth-subtitle">{t('auth.registerSubtitle')}</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="auth-form" noValidate>
              <Input
                label={t('auth.phoneNumber')}
                type="tel"
                placeholder="+255 712 345 678"
                leftIcon={Phone}
                autoComplete="tel"
                required
                error={errors.phoneNumber && t('errors.invalidPhone')}
                {...register('phoneNumber', {
                  required: true,
                  pattern: /^\+?[0-9]{10,15}$/,
                })}
              />

              <div className="auth-form-grid-2">
                <Input
                  label={t('auth.firstName')}
                  type="text"
                  placeholder="Iddi"
                  leftIcon={User}
                  required
                  error={errors.firstName && t('errors.required')}
                  {...register('firstName', { required: true, minLength: 2 })}
                />
                <Input
                  label={t('auth.lastName')}
                  type="text"
                  placeholder="Hemedi"
                  required
                  error={errors.lastName && t('errors.required')}
                  {...register('lastName', { required: true, minLength: 2 })}
                />
              </div>

              <Input
                label={t('auth.emailOptional')}
                type="email"
                placeholder="jina@mfano.com"
                leftIcon={Mail}
                autoComplete="email"
                {...register('email')}
              />

              <Input
                label={t('auth.password')}
                type="password"
                placeholder="••••••••"
                leftIcon={Lock}
                autoComplete="new-password"
                required
                hint={t('errors.passwordTooShort')}
                error={errors.password && t('errors.passwordTooShort')}
                {...register('password', { required: true, minLength: 8 })}
              />

              <Input
                label={t('auth.confirmPassword')}
                type="password"
                placeholder="••••••••"
                leftIcon={Lock}
                autoComplete="new-password"
                required
                error={errors.confirmPassword && t('errors.passwordMismatch')}
                {...register('confirmPassword', {
                  required: true,
                  validate: (v) => v === password,
                })}
              />

              <div className="auth-terms">
                <input
                  type="checkbox"
                  id="terms"
                  {...register('terms', { required: true })}
                />
                <label htmlFor="terms">{t('auth.termsAgree')}</label>
              </div>

              <Button
                type="submit"
                variant="primary"
                size="lg"
                fullWidth
                loading={submitting}
                disabled={!watch('terms')}
              >
                {t('auth.createAccount')}
              </Button>
            </form>

            <div className="auth-form-footer">
              <span>{t('auth.hasAccount')} </span>
              <Link to="/login" className="auth-link auth-link-strong">
                {t('auth.login')}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;