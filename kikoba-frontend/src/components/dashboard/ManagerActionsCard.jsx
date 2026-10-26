import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Wallet, HandCoins, Vote, Megaphone, Shield,
  ChevronRight, AlertCircle,
} from 'lucide-react';
import Card from '../common/Card';
import Badge from '../common/Badge';
import './ManagerActionsCard.css';

const ManagerActionsCard = ({
  groupId,
  pendingContributions = [],
  pendingLoans = [],
  role,
}) => {
  const { t } = useTranslation();

  const totalPending = pendingContributions.length + pendingLoans.length;

  // Build dynamic action items based on role and pending counts
  const actions = [];

  if ((role === 'CHAIRPERSON' || role === 'TREASURER') && pendingContributions.length > 0) {
    actions.push({
      icon: Wallet,
      label: t('dashboard.pendingContributionsAction'),
      count: pendingContributions.length,
      to: `/groups/${groupId}/contributions`,
      variant: 'warning',
    });
  }

  if ((role === 'CHAIRPERSON' || role === 'TREASURER') && pendingLoans.length > 0) {
    actions.push({
      icon: HandCoins,
      label: t('dashboard.pendingLoansAction'),
      count: pendingLoans.length,
      to: `/groups/${groupId}/loans/pending`,
      variant: 'warning',
    });
  }

  // Always-show manager links
  if (role === 'CHAIRPERSON' || role === 'SECRETARY') {
    actions.push({
      icon: Vote,
      label: t('dashboard.managePolls'),
      to: `/groups/${groupId}/polls`,
      variant: 'info',
    });
    actions.push({
      icon: Megaphone,
      label: t('dashboard.manageAnnouncements'),
      to: `/groups/${groupId}/announcements`,
      variant: 'info',
    });
  }

  if (role === 'CHAIRPERSON') {
    actions.push({
      icon: Shield,
      label: t('dashboard.manageRoles'),
      to: `/groups/${groupId}/settings`,
      variant: 'primary',
    });
  }

  if (actions.length === 0) return null;

  return (
    <Card padding="none" className="manager-actions-card">
      <div className="manager-actions-header">
        <h2 className="manager-actions-title">
          {t('dashboard.managerActions')}
        </h2>
        {totalPending > 0 && (
          <Badge variant="warning" size="sm">
            {totalPending} {t('dashboard.needAttention')}
          </Badge>
        )}
      </div>

      <ul className="manager-actions-list">
        {actions.map((action, i) => (
          <li key={i}>
            <Link to={action.to} className="manager-action-item">
              <div className={`manager-action-icon manager-action-${action.variant}`}>
                <action.icon size={18} aria-hidden="true" />
              </div>
              <span className="manager-action-label">{action.label}</span>
              {action.count > 0 && (
                <span className="manager-action-count">
                  {action.count}
                </span>
              )}
              <ChevronRight size={16} className="manager-action-arrow" />
            </Link>
          </li>
        ))}
      </ul>
    </Card>
  );
};

export default ManagerActionsCard;