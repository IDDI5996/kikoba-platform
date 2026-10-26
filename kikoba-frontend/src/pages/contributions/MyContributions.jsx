import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Wallet, ArrowLeft, ExternalLink } from 'lucide-react';
import useApi from '../../hooks/useApi';
import contributionsService from '../../services/contributionsService';
import Card from '../../components/common/Card';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import './Contributions.css';

const MyContributions = () => {
  const { t } = useTranslation();
  const { data, loading, error, refetch } = useApi(
    () => contributionsService.myAll(),
    []
  );

  if (loading) return <LoadingState fullScreen />;
  if (error) return <ErrorState error={error} onRetry={refetch} />;

  return (
    <div className="contributions-page">
      <Link to="/dashboard" className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="contributions-title">{t('contributions.myContributions')}</h1>

      {!data || data.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={Wallet}
            title={t('contributions.myContributions')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <Card padding="none">
          <ul className="contribution-list">
            {data.map((c) => (
              <li key={c.id} className="contribution-list-item">
                <div className="contribution-item-icon">
                  <Wallet size={18} aria-hidden="true" />
                </div>
                <div className="contribution-item-info">
                  <p className="contribution-item-title">
                    {c.groupName}
                  </p>
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
                </div>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
};

export default MyContributions;