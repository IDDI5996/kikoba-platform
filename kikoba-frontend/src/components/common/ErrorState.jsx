import { AlertCircle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import Button from './Button';
import './ErrorState.css';

const ErrorState = ({ error, onRetry, title }) => {
  const { t } = useTranslation();

  const message =
    error?.response?.data?.message ||
    error?.message ||
    t('errors.serverError');

  return (
    <div className="error-state" role="alert">
      <div className="error-state-icon">
        <AlertCircle size={32} aria-hidden="true" />
      </div>
      <h3 className="error-state-title">{title || t('common.error')}</h3>
      <p className="error-state-message">{message}</p>
      {onRetry && (
        <Button variant="secondary" onClick={onRetry}>
          {t('common.retry')}
        </Button>
      )}
    </div>
  );
};

export default ErrorState;