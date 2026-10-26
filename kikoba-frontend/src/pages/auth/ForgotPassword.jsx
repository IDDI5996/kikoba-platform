import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Smartphone, KeyRound, Lock, ShieldCheck } from 'lucide-react';
import { toast } from 'sonner';
import Logo from '../../components/common/Logo';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import LanguageSwitcher from '../../components/common/LanguageSwitcher';
import authService from '../../services/authService';
import './Auth.css';

const ForgotPassword = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    phoneNumber: '',
    otp: '',
    newPassword: '',
    confirmPassword: '',
  });

  const handleRequestOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await authService.forgotPassword(formData.phoneNumber);
      toast.success(t('auth.otpSent', { phone: formData.phoneNumber }));
      setStep(2);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (formData.newPassword !== formData.confirmPassword) {
      toast.error(t('errors.passwordMismatch'));
      return;
    }
    setLoading(true);
    try {
      await authService.resetPassword(
        formData.phoneNumber,
        formData.otp,
        formData.newPassword
      );
      toast.success('Password reset successfully! Please login.');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to reset password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout">
      {/* HEADER (Matches your CSS) */}
      <header className="auth-header">
        <Link to="/">
          <Logo variant="full" />
        </Link>
        <LanguageSwitcher />
      </header>

      {/* MAIN CONTAINER (Grid layout) */}
      <div className="auth-container">

        {/* LEFT SIDE: ILLUSTRATION PANEL */}
        <div className="auth-illustration">
          <div className="auth-illustration-icon" style={{ padding: '40px', background: 'var(--color-primary-50)', borderRadius: '50%', marginBottom: '24px' }}>
            <ShieldCheck size={80} color="var(--color-primary-700)" />
          </div>
          <h1 className="auth-illustration-title">
            {t('auth.secureAccount') || 'Secure your account'}
          </h1>
          <p className="auth-illustration-subtitle">
            {t('auth.forgotPasswordHint') || "Don't worry, it happens. We'll help you reset your password and get you back to your group."}
          </p>
        </div>

        {/* RIGHT SIDE: FORM PANEL */}
        <div className="auth-form-panel">
          <div className="auth-form-inner">

            <div className="auth-form-header">
              <h2 className="auth-title">
                {step === 1 ? t('auth.forgotPassword') : t('auth.resetPassword') || 'Reset Password'}
              </h2>
              <p className="auth-subtitle">
                {step === 1
                  ? t('auth.enterPhoneToReset') || 'Enter your phone number to receive a reset code.'
                  : t('auth.enterOtpAndNewPassword') || 'Enter the 6-digit code and your new password.'}
              </p>
            </div>

            {step === 1 ? (
              <form onSubmit={handleRequestOtp} className="auth-form">
                <div className="input-with-icon">
                  <Smartphone size={20} className="input-icon" />
                  <Input
                    label={t('auth.phoneNumber')}
                    type="tel"
                    placeholder="+255 712 345 678"
                    required
                    value={formData.phoneNumber}
                    onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                    className="input-field"
                  />
                </div>

                <Button type="submit" variant="primary" loading={loading} className="w-full">
                  {t('auth.sendResetCode') || 'Send Reset Code'}
                </Button>
              </form>
            ) : (
              <form onSubmit={handleResetPassword} className="auth-form">
                <div className="input-with-icon">
                  <KeyRound size={20} className="input-icon" />
                  <Input
                    label={t('auth.otpCode') || '6-Digit Code'}
                    type="text"
                    placeholder="123456"
                    maxLength={6}
                    required
                    value={formData.otp}
                    onChange={(e) => setFormData({ ...formData, otp: e.target.value })}
                    className="input-field"
                  />
                </div>

                <div className="input-with-icon">
                  <Lock size={20} className="input-icon" />
                  <Input
                    label={t('auth.newPassword') || 'New Password'}
                    type="password"
                    required
                    value={formData.newPassword}
                    onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                    className="input-field"
                  />
                </div>

                <div className="input-with-icon">
                  <Lock size={20} className="input-icon" />
                  <Input
                    label={t('auth.confirmPassword')}
                    type="password"
                    required
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    className="input-field"
                  />
                </div>

                <Button type="submit" variant="primary" loading={loading} className="w-full">
                  {t('auth.resetPassword') || 'Reset Password'}
                </Button>
              </form>
            )}

            <div className="auth-form-footer">
              <Link to="/login" className="back-link">
                <ArrowLeft size={16} /> {t('auth.backToLogin') || 'Back to Login'}
              </Link>
            </div>

          </div>
        </div>

      </div>
    </div>
  );
};

export default ForgotPassword;