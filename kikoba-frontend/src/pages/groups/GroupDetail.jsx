import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Users, User, Crown, Wallet, Shield, Settings as SettingsIcon } from 'lucide-react';
import { useGroup, useGroupMembers } from '../../hooks/useGroups';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import contributionsService from '../../services/contributionsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import Tabs from '../../components/common/Tabs';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import './GroupDetail.css';

const GroupDetail = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();
  const [activeTab, setActiveTab] = useState('overview');

  const { data: group, loading: gLoading, error: gError, refetch: gRefetch } = useGroup(groupId);
  const { data: members, loading: mLoading } = useGroupMembers(groupId);
  const { role } = useMyGroupRole(groupId);
  const canManage = can(role, 'UPDATE_MEMBER_ROLE');

  const { data: summary } = useApi(
    () => contributionsService.mySummary(groupId),
    [groupId]
  );

  if (gLoading) return <LoadingState fullScreen />;
  if (gError) return <ErrorState error={gError} onRetry={gRefetch} />;
  if (!group) return null;

  const tabs = [
    { key: 'overview', label: t('groups.title') },
    { key: 'members', label: `${t('groups.members')} (${group.memberCount})` },
    { key: 'contributions', label: t('contributions.title') },
  ];

  return (
    <div className="group-detail">
      <Link to="/groups" className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <header className="group-detail-header">
        <div className="group-detail-icon">
          <Users size={24} aria-hidden="true" />
        </div>
        <div className="group-detail-title-block">
          <h1 className="group-detail-title">{group.name}</h1>
          <div className="group-detail-badges">
            <Badge variant="primary">{t(`groups.types.${group.type}`)}</Badge>
            <Badge variant="neutral">
              {t('dashboard.memberCount', { count: group.memberCount })}
            </Badge>
          </div>
        </div>

        {/* ✅ Settings link — only for managers (Chairperson) */}
        {canManage && (
          <Link to={`/groups/${groupId}/settings`} className="group-detail-settings-link">
            <Button variant="secondary" leftIcon={SettingsIcon}>
              {t('groups.settings') || 'Settings'}
            </Button>
          </Link>
        )}
      </header>

      {group.description && (
        <p className="group-detail-description">{group.description}</p>
      )}

      <Tabs tabs={tabs} active={activeTab} onChange={setActiveTab} />

      {/* ---- Overview Tab ---- */}
      {activeTab === 'overview' && (
        <div className="group-detail-grid">
          <Card padding="md">
            <p className="group-info-label">{t('contributions.mySummary')}</p>
            <p className="group-info-value">
              <span>{summary?.totalContributed ?? '0'}</span>
              <span className="currency">TZS</span>
            </p>
            <p className="group-info-sub">
              {summary?.totalTransactions ?? 0} {t('contributions.title').toLowerCase()}
            </p>
          </Card>

          <Card padding="md">
            <p className="group-info-label">{t('groups.minContribution')}</p>
            <p className="group-info-value">
              <span>{group.minContribution ?? '—'}</span>
              {group.minContribution && <span className="currency">TZS</span>}
            </p>
          </Card>

          <Card padding="md">
            <p className="group-info-label">{t('groups.maxContribution')}</p>
            <p className="group-info-value">
              <span>{group.maxContribution ?? '—'}</span>
              {group.maxContribution && <span className="currency">TZS</span>}
            </p>
          </Card>

          <Card padding="md">
            <p className="group-info-label">{t('groups.interestRate')}</p>
            <p className="group-info-value">
              <span>{group.interestRate ?? '—'}</span>
              {group.interestRate && <span className="currency">%</span>}
            </p>
          </Card>

          <Card padding="md">
            <p className="group-info-label">{t('groups.invitationCode')}</p>
            <p className="group-info-value group-info-code">
              {group.invitationCode}
            </p>
          </Card>

          <Card padding="md">
            <p className="group-info-label">{t('groups.cycleDuration')}</p>
            <p className="group-info-value">
              <span>{group.cycleDurationMonths ?? '—'}</span>
              {group.cycleDurationMonths && <span className="currency">mo</span>}
            </p>
          </Card>
        </div>
      )}

      {/* ---- Members Tab ---- */}
      {activeTab === 'members' && (
        <Card padding="none">
          {mLoading ? (
            <LoadingState />
          ) : !members || members.length === 0 ? (
            <EmptyState
              icon={User}
              title={t('groups.members')}
              description={t('common.noData')}
            />
          ) : (
            <ul className="members-list">
              {members.map((m) => (
                <li key={m.id} className="member-item">
                  <div className="member-avatar">
                    {m.firstName?.[0]}
                    {m.lastName?.[0]}
                  </div>
                  <div className="member-info">
                    <p className="member-name">
                      {m.firstName} {m.lastName}
                    </p>
                    <p className="member-phone">{m.phoneNumber}</p>
                  </div>
                  <Badge
                    variant={
                      m.role === 'CHAIRPERSON'
                        ? 'primary'
                        : m.role === 'TREASURER'
                        ? 'info'
                        : m.role === 'SECRETARY'
                        ? 'warning'
                        : 'neutral'
                    }
                    size="sm"
                  >
                    {m.role === 'CHAIRPERSON' && <Crown size={12} />}
                    {m.role === 'TREASURER' && <Wallet size={12} />}
                    {m.role === 'SECRETARY' && <Shield size={12} />}
                    {t(`groups.roles.${m.role}`)}
                  </Badge>
                </li>
              ))}
            </ul>
          )}
        </Card>
      )}

      {/* ---- Contributions Tab ---- */}
      {activeTab === 'contributions' && (
        <Card padding="md">
          <p className="group-info-label">{t('contributions.title')}</p>
          <p className="group-info-sub">
            {t('contributions.thisCycleTotal')}: {summary?.currentPeriodTotal ?? 0} TZS
          </p>
        </Card>
      )}
    </div>
  );
};

export default GroupDetail;