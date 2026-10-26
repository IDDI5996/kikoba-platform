import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import {
  ArrowLeft, Calendar, TrendingUp, HandCoins, CheckCircle2,
} from 'lucide-react';
import loansService from '../../services/loansService';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import { useAuth } from '../../context/AuthContext';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import AmountInput from '../../components/financial/AmountInput';
import PaymentMethodSelect from '../../components/financial/PaymentMethodSelect';
import './Loans.css';

const LoanDetail = () => {
  const { t } = useTranslation();
  const { loanId } = useParams();
  const { user } = useAuth();

  const loan = useApi(() => loansService.get(loanId), [loanId]);
  const schedule = useApi(() => loansService.schedule(loanId), [loanId]);

  const { role } = useMyGroupRole(loan.data?.groupId);
  const canDisburse = can(role, 'DISBURSE_LOAN');
  const canConfirmRepayment = can(role, 'CONFIRM_REPAYMENT');

  // Borrower check
  const isBorrower = loan.data?.userId && user?.id === loan.data.userId;
  const canRepay = isBorrower && loan.data?.status === 'ACTIVE';

  const [repayOpen, setRepayOpen] = useState(false);
  const [repayAmount, setRepayAmount] = useState('');
  const [repayMethod, setRepayMethod] = useState('MPESA');
  const [submitting, setSubmitting] = useState(false);

  if (loan.loading) return <LoadingState fullScreen />;
  if (loan.error) return <ErrorState error={loan.error} onRetry={loan.refetch} />;
  if (!loan.data) return null;

  const l = loan.data;

  const handleRepay = async () => {
    if (!repayAmount || Number(repayAmount) <= 0) {
      toast.error(t('errors.required'));
      return;
    }
    setSubmitting(true);
    try {
      await loansService.repay(loanId, {
        amount: Number(repayAmount),
        paymentMethod: repayMethod,
      });
      toast.success(t('common.success'));
      setRepayOpen(false);
      setRepayAmount('');
      loan.refetch();
      schedule.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDisburse = async () => {
    if (!window.confirm(t('common.confirm'))) return;
    try {
      await loansService.disburse(loanId);
      toast.success(t('common.success'));
      loan.refetch();
      schedule.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const handleCancel = async () => {
    const reason = window.prompt(t('common.confirm'));
    if (!reason) return;
    try {
      await loansService.cancel(loanId);
      toast.success(t('common.success'));
      loan.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const nextDue = schedule.data?.find(
    (s) => s.status === 'PENDING' || s.status === 'PARTIAL' || s.status === 'OVERDUE'
  );

  return (
    <div className="loan-page">
      <Link to="/loans" className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <div className="loan-header">
        <div>
          <h1 className="loan-title">{t('loans.loanAmount')}</h1>
          <p className="loan-subtitle">{l.groupName}</p>
        </div>
        <StatusBadge status={l.status} />
      </div>

      <Card padding="md" className="loan-amount-card">
        <p className="loan-amount-label">{t('loans.loanAmount')}</p>
        <p className="loan-amount-value">
          {Number(l.amount).toLocaleString()}
          <span className="currency">TZS</span>
        </p>
        <div className="loan-amount-meta">
          <span>{l.repaymentPeriodMonths} {t('loans.months')}</span>
          <span>•</span>
          <span>{l.interestRate}%</span>
        </div>
      </Card>

      <div className="loan-stats-grid">
        <Card padding="md">
          <p className="group-info-label">{t('loans.totalRepayable')}</p>
          <p className="group-info-value">
            {Number(l.totalRepayable).toLocaleString()}
            <span className="currency">TZS</span>
          </p>
        </Card>
        <Card padding="md">
          <p className="group-info-label">{t('loans.balance')}</p>
          <p className="group-info-value">
            {Number(l.balance).toLocaleString()}
            <span className="currency">TZS</span>
          </p>
        </Card>
      </div>

      {/* ✅ Disburse button — only managers, only when APPROVED */}
      {l.status === 'APPROVED' && canDisburse && (
        <Card padding="md" className="loan-action-card">
          <p className="loan-action-text">
            {t('loans.awaitingDisbursement') || 'This loan is approved and awaiting disbursement.'}
          </p>
          <Button
            variant="primary"
            fullWidth
            leftIcon={CheckCircle2}
            onClick={handleDisburse}
          >
            {t('loans.disburseLoan')}
          </Button>
        </Card>
      )}

      {/* ✅ Make Repayment — only the borrower, only when ACTIVE */}
      {nextDue && canRepay && (
        <Card padding="md" className="next-due-card">
          <div className="next-due-header">
            <Calendar size={16} aria-hidden="true" />
            <span>{t('loans.dueDate')}</span>
          </div>
          <p className="next-due-amount">
            {Number(nextDue.amountDue).toLocaleString()} TZS
          </p>
          <p className="next-due-date">{nextDue.dueDate}</p>

          <Button
            variant="primary"
            fullWidth
            onClick={() => {
              setRepayAmount(String(nextDue.amountDue - (nextDue.amountPaid || 0)));
              setRepayOpen(true);
            }}
          >
            {t('loans.makeRepayment')}
          </Button>
        </Card>
      )}

      {/* If loan is ACTIVE but user isn't the borrower, show info */}
      {l.status === 'ACTIVE' && !canRepay && (
        <Card padding="md" className="loan-action-card">
          <p className="loan-action-text">
            {t('loans.notYourLoan') || 'This is another member\'s loan. They are repaying it.'}
          </p>
        </Card>
      )}

      <Card padding="none">
        <div className="loan-schedule-header">
          <TrendingUp size={16} aria-hidden="true" />
          <h2 className="loan-schedule-title">{t('loans.repaymentSchedule')}</h2>
        </div>

        {schedule.loading ? (
          <LoadingState />
        ) : !schedule.data || schedule.data.length === 0 ? (
          <EmptyState
            icon={Calendar}
            title={t('common.noData')}
            description={t('common.noData')}
          />
        ) : (
          <ul className="loan-schedule-list">
            {schedule.data.map((s) => (
              <li key={s.id} className="loan-schedule-item">
                <div className="loan-schedule-num">
                  <span>#{s.installmentNumber}</span>
                </div>
                <div className="loan-schedule-info">
                  <p className="loan-schedule-date">{s.dueDate}</p>
                  <p className="loan-schedule-amount">
                    {Number(s.amountDue).toLocaleString()} TZS
                  </p>
                </div>
                <StatusBadge status={s.status} size="sm" />
              </li>
            ))}
          </ul>
        )}
      </Card>

      <Modal
        isOpen={repayOpen}
        onClose={() => setRepayOpen(false)}
        title={t('loans.makeRepayment')}
        footer={
          <>
            <Button variant="secondary" onClick={() => setRepayOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button variant="primary" loading={submitting} onClick={handleRepay}>
              {t('common.confirm')}
            </Button>
          </>
        }
      >
        <div className="repay-form">
          <AmountInput
            label={t('loans.amountDue')}
            value={repayAmount}
            onChange={setRepayAmount}
            currency="TZS"
            quickAmounts={[]}
          />
          <div className="form-group">
            <label className="form-label">{t('contributions.paymentMethod')}</label>
            <PaymentMethodSelect value={repayMethod} onChange={setRepayMethod} />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default LoanDetail;