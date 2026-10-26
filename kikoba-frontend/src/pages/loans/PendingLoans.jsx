import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import { ArrowLeft, CheckCircle2, XCircle, HandCoins } from 'lucide-react';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import loansService from '../../services/loansService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import './Loans.css';

const PendingLoans = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();

  const { role } = useMyGroupRole(groupId);
  const canApprove = can(role, 'APPROVE_LOAN');
  const canReject = can(role, 'REJECT_LOAN');

  const pending = useApi(() => loansService.groupPending(groupId), [groupId]);

  const handleApprove = async (loanId) => {
    try {
      await loansService.approve(loanId, {});
      toast.success(t('common.success'));
      pending.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const handleReject = async (loanId) => {
    const reason = window.prompt(t('loans.rejectionReason'));
    if (!reason) return;
    try {
      await loansService.reject(loanId, reason);
      toast.success(t('common.success'));
      pending.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  if (pending.loading) return <LoadingState fullScreen />;
  if (pending.error) return <ErrorState error={pending.error} onRetry={pending.refetch} />;

  return (
    <div className="loans-page">
      <Link to={`/groups/${groupId}`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="loan-title">{t('loans.pendingLoans')}</h1>

      {!pending.data || pending.data.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={HandCoins}
            title={t('loans.pendingLoans')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <div className="pending-loans-list">
          {pending.data.map((loan) => (
            <Card key={loan.id} padding="md">
              <div className="pending-loan-header">
                <div>
                  <p className="pending-loan-name">{loan.memberName}</p>
                  <p className="pending-loan-meta">
                    {loan.repaymentPeriodMonths} mo • {loan.interestRate}%
                  </p>
                </div>
                <p className="pending-loan-amount">
                  {Number(loan.amount).toLocaleString()}
                  <span className="currency">TZS</span>
                </p>
              </div>

              {loan.purpose && (
                <p className="pending-loan-purpose">{loan.purpose}</p>
              )}

              {/* ✅ Only managers see Approve/Reject buttons */}
              {canApprove && (
                <div className="pending-loan-actions">
                  <Button
                    variant="primary"
                    leftIcon={CheckCircle2}
                    onClick={() => handleApprove(loan.id)}
                    fullWidth
                  >
                    {t('loans.approveLoan')}
                  </Button>
                  {canReject && (
                    <Button
                      variant="secondary"
                      leftIcon={XCircle}
                      onClick={() => handleReject(loan.id)}
                      fullWidth
                    >
                      {t('loans.rejectLoan')}
                    </Button>
                  )}
                </div>
              )}

              {/* Non-managers see a "waiting for approval" hint */}
              {!canApprove && (
                <p className="pending-loan-hint">
                  {t('loans.awaitingApproval') || 'Awaiting manager approval'}
                </p>
              )}
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default PendingLoans;