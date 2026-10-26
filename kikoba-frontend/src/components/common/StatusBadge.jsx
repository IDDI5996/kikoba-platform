import { useTranslation } from 'react-i18next';
import Badge from './Badge';

const STATUS_VARIANTS = {
  // Contributions
  PENDING: 'warning',
  COMPLETED: 'success',
  FAILED: 'danger',
  CANCELLED: 'neutral',

  // Loans
  APPROVED: 'info',
  REJECTED: 'danger',
  ACTIVE: 'primary',
  PAID: 'success',
  DEFAULTED: 'danger',

  // Meetings
  SCHEDULED: 'info',
  ONGOING: 'warning',

  // Dividends
  CALCULATED: 'info',
  DISTRIBUTED: 'success',

  // Payouts
  PAID_OUT: 'success',
};

const StatusBadge = ({ status, size = 'md' }) => {
  const { t } = useTranslation();
  const variant = STATUS_VARIANTS[status] || 'neutral';

  // Try several i18n keys before falling back to raw status
  const label =
    t(`contributions.${status.toLowerCase()}`, { defaultValue: null }) ||
    t(`loans.status.${status}`, { defaultValue: null }) ||
    t(`meetings.${status.toLowerCase()}`, { defaultValue: null }) ||
    t(`dividends.${status.toLowerCase()}`, { defaultValue: null }) ||
    t(`polls.${status.toLowerCase()}`, { defaultValue: null }) ||
    status;

  return <Badge variant={variant} size={size}>{label}</Badge>;
};

export default StatusBadge;