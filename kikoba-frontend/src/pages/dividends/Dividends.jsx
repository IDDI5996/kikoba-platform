import { useTranslation } from 'react-i18next';
import { Link, useParams } from 'react-router-dom';
import { TrendingUp, ArrowLeft } from 'lucide-react';
import useApi from '../../hooks/useApi';
import dividendsService from '../../services/dividendsService';
import Card from '../../components/common/Card';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import './Dividends.css';

const Dividends = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();

  const list = useApi(() => dividendsService.list(groupId), [groupId]);

  if (list.loading) return <LoadingState fullScreen />;
  if (list.error) return <ErrorState error={list.error} onRetry={list.refetch} />;

  return (
    <div className="dividends-page">
      <Link to={`/groups/${groupId}`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="page-title">{t('dividends.title')}</h1>

      {!list.data || list.data.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={TrendingUp}
            title={t('dividends.title')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <div className="dividends-list">
          {list.data.map((d) => (
            <Card key={d.id} padding="md" className="dividend-card">
              <div className="dividend-card-header">
                <div>
                  <p className="dividend-cycle">
                    {t('dividends.cycleNumber')} {d.cycleNumber}
                  </p>
                  <p className="dividend-date">
                    {new Date(d.createdAt).toLocaleDateString('sw-TZ', {
                      dateStyle: 'medium',
                    })}
                  </p>
                </div>
                <StatusBadge status={d.status} size="sm" />
              </div>

              <div className="dividend-stats">
                <div className="dividend-stat">
                  <p className="dividend-stat-label">{t('dividends.totalProfit')}</p>
                  <p className="dividend-stat-value">
                    {Number(d.totalProfit).toLocaleString()} TZS
                  </p>
                </div>
                <div className="dividend-stat">
                  <p className="dividend-stat-label">{t('dividends.dividendPerShare')}</p>
                  <p className="dividend-stat-value">
                    {Number(d.dividendPerShare).toLocaleString(undefined, {
                      minimumFractionDigits: 2,
                      maximumFractionDigits: 4,
                    })}
                  </p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default Dividends;