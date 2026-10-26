import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, HandCoins, ChevronRight } from 'lucide-react';
import useApi from '../../hooks/useApi';
import loansService from '../../services/loansService';
import Card from '../../components/common/Card';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import './Loans.css';

const MyLoans = () => {
  const { t } = useTranslation();
  const { data, loading, error, refetch } = useApi(
    () => loansService.myAll(),
    []
  );

  if (loading) return <LoadingState fullScreen />;
  if (error) return <ErrorState error={error} onRetry={refetch} />;

  return (
    <div className="loans-page">
      <Link to="/dashboard" className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="loan-title">{t('loans.myLoans')}</h1>

      {!data || data.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={HandCoins}
            title={t('loans.myLoans')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <Card padding="none">
          <ul className="loan-list">
            {data.map((loan) => (
              <li key={loan.id}>
                <Link to={`/loans/${loan.id}`} className="loan-list-item">
                  <div className="loan-item-icon">
                    <HandCoins size={18} aria-hidden="true" />
                  </div>
                  <div className="loan-item-info">
                    <p className="loan-item-title">{loan.groupName}</p>
                    <p className="loan-item-meta">
                      {loan.repaymentPeriodMonths} {t('loans.months')} • {loan.interestRate}%
                    </p>
                    {loan.purpose && (
                      <p className="loan-item-purpose">{loan.purpose}</p>
                    )}
                  </div>
                  <div className="loan-item-right">
                    <p className="loan-item-amount">
                      {Number(loan.amount).toLocaleString()}
                      <span className="currency">TZS</span>
                    </p>
                    <StatusBadge status={loan.status} size="sm" />
                  </div>
                  <ChevronRight size={16} className="loan-item-arrow" aria-hidden="true" />
                </Link>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
};

export default MyLoans;