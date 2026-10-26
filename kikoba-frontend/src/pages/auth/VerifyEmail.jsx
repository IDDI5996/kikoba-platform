import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import API from '../../services/api';
import { useRegistration } from '../../context/RegistrationContext';
import Button from '../../components/common/Button';
import Logo from '../../components/common/Logo';
import LanguageSwitcher from '../../components/common/LanguageSwitcher';
import OtpInput from '../../components/auth/OtpInput';
import './Auth.css';

const VerifyEmail = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { pending, setPending, clear } = useRegistration();
  const [code, setCode] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [countdown, setCountdown] = useState(60);
  const [resending, setResending] = useState(false);

  // Redirect if no pending registration
  useEffect(() => {
    if (!pending) {
      navigate('/register');
    }
  }, [pending, navigate]);

  // Countdown timer
  useEffect(() => {
    if (countdown <= 0) return;
    const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    return () => clearTimeout(timer);
  }, [countdown]);

  const onSubmit = async (e) => {
    e.preventDefault();
    if (code.length !== 6 || !pending) return;

    setSubmitting(true);
    try {
      // ✅ Send the RAW email, not the masked one
      const { data: response } = await API.post('/auth/register/verify-email', {
        recipient: pending.email,
        code,
      });

      setPending({ ...pending, ...response });
      toast.success(t('common.success'));

      // ✅ Registration complete — clear state and go to login
      clear();
      navigate('/dashboard');
    } catch (err) {
      toast.error(err?.response?.data?.message || t('common.error'));
      setCode('');
    } finally {
      setSubmitting(false);
    }
  };

  const handleResend = async () => {
    if (!pending) return;
    setResending(true);
    try {
      const { data } = await API.post(
        `/auth/register/resend-otp?recipient=${encodeURIComponent(pending.email)}&purpose=EMAIL_VERIFICATION`
      );
      setPending({ ...pending, ...data });
      setCountdown(60);
      toast.success(t('common.success'));
    } catch (err) {
      toast.error(err?.response?.data?.message || t('common.error'));
    } finally {
      setResending(false);
    }
  };

  if (!pending) return null;

  return (
    <div className="auth-layout">
      <div className="auth-header">
        <Logo variant="full" />
        <LanguageSwitcher />
      </div>

      <div className="auth-container auth-container-center">
        <div className="auth-form-panel">
          <div className="auth-form-inner">
            <div className="auth-steps">
              <span className="auth-step active">1</span>
              <span className="auth-step-line active" />
              <span className="auth-step active">2</span>
              <span className="auth-step-line active" />
              <span className="auth-step active">3</span>
            </div>

            <div className="auth-form-header">
              <h1 className="auth-title">{t('auth.verifyEmail')}</h1>
              <p className="auth-subtitle">
                {t('auth.otpSent', { phone: pending.maskedEmail })}
              </p>
            </div>

            <form onSubmit={onSubmit} className="auth-form" noValidate>
              <OtpInput value={code} onChange={setCode} length={6} autoFocus />

              <div className="otp-resend-row">
                <span className="otp-resend-label">{t('auth.resendCode')}</span>
                {countdown > 0 ? (
                  <span className="otp-resend-timer">
                    {t('auth.resendIn', { seconds: countdown })}
                  </span>
                ) : (
                  <button
                    type="button"
                    className="auth-link"
                    onClick={handleResend}
                    disabled={resending}
                  >
                    {t('auth.resendCode')}
                  </button>
                )}
              </div>

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

            <div className="auth-form-footer">
              <Link to="/register" className="auth-link">
                {t('auth.changeNumber')}
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmail;