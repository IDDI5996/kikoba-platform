import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { TrendingUp, Plus } from 'lucide-react';
import useApi from '../../hooks/useApi';
import sharesService from '../../services/sharesService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import StatCard from '../../components/common/StatCard';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import './Shares.css';

const Shares = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();

  const summary = useApi(() => sharesService.mySummary(groupId), [groupId]);
  const shares = useApi(() => sharesService.myInGroup(groupId), [groupId]);

  if (summary.loading) return <LoadingState fullScreen />;
  if (summary.error) return <ErrorState error={summary.error} onRetry={summary.refetch} />;

  const s = summary.data;

  return (
    <div className="shares-page">
      <div className="shares-header">
        <h1 className="page-title">{t('shares.title')}</h1>
        <Link to={`/groups/${groupId}/shares/buy`}>
          <Button variant="primary" leftIcon={Plus}>
            {t('shares.buyShares')}
          </Button>
        </Link>
      </div>

      <div className="shares-stats-grid">
        <StatCard
          icon={TrendingUp}
          label={t('shares.sharesHeld')}
          value={s?.totalShares ?? 0}
        />
        <StatCard
          icon={TrendingUp}
          label={t('shares.sharePrice')}
          value={Number(s?.currentSharePrice ?? 0).toLocaleString()}
          currency="TZS"
        />
        <StatCard
          icon={TrendingUp}
          label={t('shares.currentValue')}
          value={Number(s?.totalValue ?? 0).toLocaleString()}
          currency="TZS"
        />
      </div>

      <Card padding="none">
        <div className="shares-history-header">
          <h2 className="meeting-section-title">{t('shares.purchaseHistory')}</h2>
        </div>
        {shares.loading ? (
          <LoadingState />
        ) : !shares.data || shares.data.length === 0 ? (
          <EmptyState
            icon={TrendingUp}
            title={t('shares.purchaseHistory')}
            description={t('common.noData')}
          />
        ) : (
          <ul className="shares-list">
            {shares.data.map((share) => (
              <li key={share.id} className="share-item">
                <div className="share-icon">
                  <TrendingUp size={18} />
                </div>
                <div className="share-info">
                  <p className="share-title">
                    {share.numberOfShares} {t('shares.sharesHeld').toLowerCase()}
                  </p>
                  <p className="share-meta">
                    {new Date(share.createdAt).toLocaleDateString('sw-TZ')}
                  </p>
                </div>
                <div className="share-amount">
                  {Number(share.totalAmount).toLocaleString()}
                  <span className="currency">TZS</span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
};

export default Shares;