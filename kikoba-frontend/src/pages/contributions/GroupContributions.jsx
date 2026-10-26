import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, CheckCircle2, XCircle, Wallet } from 'lucide-react';
import { toast } from 'sonner';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import contributionsService from '../../services/contributionsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import Tabs from '../../components/common/Tabs';
import './Contributions.css';

const GroupContributions = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();
  const [tab, setTab] = useState('pending');

  const { role } = useMyGroupRole(groupId);
  const canConfirm = can(role, 'CONFIRM_CONTRIBUTION');
  const canCancel = can(role, 'CONFIRM_CONTRIBUTION');

  const all = useApi(() => contributionsService.groupAll(groupId), [groupId]);
  const pending = useApi(() => contributionsService.groupPending(groupId), [groupId]);

  const list = tab === 'pending' ? pending.data : all.data;
  const loading = tab === 'pending' ? pending.loading : all.loading;
  const error = tab === 'pending' ? pending.error : all.error;
  const refetch = tab === 'pending' ? pending.refetch : all.refetch;

  const handleConfirm = async (contributionId) => {
    try {
      await contributionsService.confirm(contributionId, null);
      toast.success(t('contributions.confirmSuccess'));
      pending.refetch();
      all.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const handleCancel = async (contributionId) => {
    if (!window.confirm(t('common.confirm'))) return;
    try {
      await contributionsService.cancel(contributionId);
      toast.success(t('common.success'));
      pending.refetch();
      all.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const tabs = [
    { key: 'pending', label: t('contributions.pendingContributions') },
    { key: 'all', label: t('contributions.groupContributions') },
  ];

  return (
    <div className="contributions-page">
      <Link to={`/groups/${groupId}`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="contributions-title">{t('contributions.groupContributions')}</h1>

      <Tabs tabs={tabs} active={tab} onChange={setTab} />

      {loading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !list || list.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={Wallet}
            title={t('common.noData')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <Card padding="none">
          <ul className="contribution-list">
            {list.map((c) => (
              <li key={c.id} className="contribution-list-item">
                <div className="contribution-item-icon">
                  <Wallet size={18} aria-hidden="true" />
                </div>
                <div className="contribution-item-info">
                  <p className="contribution-item-title">{c.memberName}</p>
                  <p className="contribution-item-meta">
                    {c.contributionPeriod} • {t(`contributions.paymentMethods.${c.paymentMethod}`)}
                  </p>
                  {c.transactionReference && (
                    <p className="contribution-item-ref">{c.transactionReference}</p>
                  )}
                </div>
                <div className="contribution-item-right">
                  <p className="contribution-item-amount">
                    {Number(c.amount).toLocaleString()}
                    <span className="currency">TZS</span>
                  </p>
                  <StatusBadge status={c.status} size="sm" />

                  {/* ✅ Only managers see the confirm/cancel buttons */}
                  {tab === 'pending' && c.status === 'PENDING' && canConfirm && (
                    <div className="contribution-item-actions">
                      <button
                        type="button"
                        className="contribution-action-btn success"
                        onClick={() => handleConfirm(c.id)}
                        aria-label={t('contributions.confirmContribution')}
                      >
                        <CheckCircle2 size={16} />
                      </button>
                      {canCancel && (
                        <button
                          type="button"
                          className="contribution-action-btn danger"
                          onClick={() => handleCancel(c.id)}
                          aria-label={t('common.cancel')}
                        >
                          <XCircle size={16} />
                        </button>
                      )}
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
};

export default GroupContributions;