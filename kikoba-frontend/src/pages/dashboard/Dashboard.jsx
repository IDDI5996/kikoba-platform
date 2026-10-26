import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Wallet, HandCoins, TrendingUp, Users, Calendar, Bell, Plus, UserPlus,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useMyGroups } from '../../hooks/useGroups';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import usePendingActions from '../../hooks/usePendingActions';
import contributionsService from '../../services/contributionsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import StatCard from '../../components/common/StatCard';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import Badge from '../../components/common/Badge';
import ManagerActionsCard from '../../components/dashboard/ManagerActionsCard';
import './Dashboard.css';

const Dashboard = () => {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { data: groups, loading, error, refetch } = useMyGroups();

  const activeGroup = useMemo(
    () => (groups && groups.length > 0 ? groups[0] : null),
    [groups]
  );

  const { role } = useMyGroupRole(activeGroup?.id);
  const isManager = role === 'CHAIRPERSON' || role === 'TREASURER' || role === 'SECRETARY';
  const isTreasurer = role === 'CHAIRPERSON' || role === 'TREASURER';

  const { data: summary, loading: summaryLoading } = useApi(
    () => activeGroup
      ? contributionsService.mySummary(activeGroup.id)
      : Promise.resolve(null),
    [activeGroup?.id],
    { immediate: !!activeGroup }
  );

  const { pendingContributions, pendingLoans } = usePendingActions(
    activeGroup?.id,
    isTreasurer
  );

  const greeting = t('dashboard.greeting', { name: user?.firstName || '' });

  if (loading) return <LoadingState message={t('common.loading')} fullScreen />;
  if (error) return <ErrorState error={error} onRetry={refetch} />;

  if (!groups || groups.length === 0) {
    return (
      <div className="dashboard">
        <div className="dashboard-welcome">
          <h1 className="dashboard-greeting">{greeting}</h1>
          <p className="dashboard-subtitle">{t('app.tagline')}</p>
        </div>
        <Card padding="none">
          <EmptyState
            icon={Users}
            title={t('groups.noGroups')}
            description={t('groups.noGroupsHint')}
            action={
              <div className="dashboard-empty-actions">
                <Link to="/groups/create">
                  <Button variant="primary" leftIcon={Plus}>
                    {t('groups.createGroup')}
                  </Button>
                </Link>
                <Link to="/groups/join">
                  <Button variant="secondary" leftIcon={UserPlus}>
                    {t('groups.joinGroup')}
                  </Button>
                </Link>
              </div>
            }
          />
        </Card>
      </div>
    );
  }

  return (
    <div className="dashboard">
      {/* Welcome + role badge */}
      <div className="dashboard-welcome">
        <div className="dashboard-welcome-row">
          <h1 className="dashboard-greeting">{greeting}</h1>
          {role && (
            <Badge
              variant={
                role === 'CHAIRPERSON'
                  ? 'primary'
                  : role === 'TREASURER'
                  ? 'info'
                  : role === 'SECRETARY'
                  ? 'warning'
                  : 'neutral'
              }
              size="md"
            >
              {t(`groups.roles.${role}`)}
            </Badge>
          )}
        </div>
        <p className="dashboard-subtitle">{t('app.tagline')}</p>
      </div>

      {/* Personal stats */}
      <section aria-label={t('dashboard.groupSummary')}>
        <div className="dashboard-stats-grid">
          <StatCard
            icon={Wallet}
            label={t('dashboard.totalSavings')}
            value={summary?.totalContributed ?? '0'}
            currency="TZS"
            loading={summaryLoading}
          />
          <StatCard
            icon={HandCoins}
            label={t('dashboard.loanBalance')}
            value="0"
            currency="TZS"
          />
          <StatCard
            icon={TrendingUp}
            label={t('dashboard.dividendsReceived')}
            value="0"
            currency="TZS"
          />
        </div>
      </section>

      {/* Quick actions */}
      <section className="dashboard-section">
        <h2 className="dashboard-section-title">{t('dashboard.quickActions')}</h2>
        <div className="dashboard-actions-grid">
          <Link to={`/groups/${activeGroup.id}/contribute`} className="dashboard-action">
            <Wallet size={20} aria-hidden="true" />
            <span>{t('dashboard.contribute')}</span>
          </Link>
          <Link to={`/groups/${activeGroup.id}/loans/apply`} className="dashboard-action">
            <HandCoins size={20} aria-hidden="true" />
            <span>{t('dashboard.requestLoan')}</span>
          </Link>
          <Link to="/contributions" className="dashboard-action">
            <TrendingUp size={20} aria-hidden="true" />
            <span>{t('dashboard.viewTransactions')}</span>
          </Link>
          <Link to="/meetings" className="dashboard-action">
            <Calendar size={20} aria-hidden="true" />
            <span>{t('dashboard.joinMeeting')}</span>
          </Link>
        </div>
      </section>

      {/* Manager-only section */}
      {isManager && (
        <section className="dashboard-section">
          <ManagerActionsCard
            groupId={activeGroup.id}
            role={role}
            pendingContributions={pendingContributions}
            pendingLoans={pendingLoans}
          />
        </section>
      )}

      {/* Two-column section */}
      <div className="dashboard-columns">
        <section className="dashboard-section">
          <Card padding="md">
            <div className="dashboard-card-header">
              <h3 className="dashboard-card-title">{t('dashboard.recentActivity')}</h3>
              <Link to="/contributions" className="dashboard-card-link">
                {t('common.viewAll')}
              </Link>
            </div>
            <p className="dashboard-empty-inline">{t('common.noData')}</p>
          </Card>
        </section>

        <section className="dashboard-section">
          <Card padding="md">
            <div className="dashboard-card-header">
              <h3 className="dashboard-card-title">{t('dashboard.upcomingEvents')}</h3>
            </div>
            <ul className="dashboard-activity-list">
              <li className="dashboard-activity-item">
                <div className="activity-icon activity-icon-info">
                  <Calendar size={16} aria-hidden="true" />
                </div>
                <div className="activity-content">
                  <p className="activity-title">{t('dashboard.nextMeeting')}</p>
                  <p className="activity-meta">{t('common.noData')}</p>
                </div>
              </li>
              <li className="dashboard-activity-item">
                <div className="activity-icon activity-icon-warning">
                  <Bell size={16} aria-hidden="true" />
                </div>
                <div className="activity-content">
                  <p className="activity-title">{t('dashboard.contributionDue')}</p>
                  <p className="activity-meta">{t('common.noData')}</p>
                </div>
              </li>
            </ul>
          </Card>
        </section>
      </div>

      {/* Group summary */}
      <section className="dashboard-section">
        <Card padding="md">
          <div className="dashboard-card-header">
            <h3 className="dashboard-card-title">{t('dashboard.groupSummary')}</h3>
            <Link to={`/groups/${activeGroup.id}`} className="dashboard-card-link">
              {t('common.viewDetails')}
            </Link>
          </div>
          <div className="dashboard-group-info">
            <div className="dashboard-group-meta">
              <div className="dashboard-group-icon">
                <Users size={20} aria-hidden="true" />
              </div>
              <div>
                <p className="dashboard-group-name">{activeGroup.name}</p>
                <div className="dashboard-group-badges">
                  <Badge variant="primary" size="sm">
                    {t(`groups.types.${activeGroup.type}`)}
                  </Badge>
                  <span className="dashboard-group-detail">
                    {t('dashboard.memberCount', { count: activeGroup.memberCount })}
                  </span>
                </div>
              </div>
            </div>
            <div className="dashboard-group-total">
              <p className="dashboard-group-total-label">
                {t('dashboard.totalGroupSavings')}
              </p>
              <p className="dashboard-group-total-value">
                <span>{summary?.totalContributed ?? '0'}</span>
                <span className="currency">TZS</span>
              </p>
            </div>
          </div>
        </Card>
      </section>
    </div>
  );
};

export default Dashboard;