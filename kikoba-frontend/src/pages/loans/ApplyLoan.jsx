import { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import { ArrowLeft, CheckCircle2, XCircle, AlertCircle } from 'lucide-react';
import loansService from '../../services/loansService';
import useApi from '../../hooks/useApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingState from '../../components/common/LoadingState';
import AmountInput from '../../components/financial/AmountInput';
import './Loans.css';

const ApplyLoan = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { groupId } = useParams();

  // Eligibility (max loan based on savings)
  const { data: eligibility, loading: eLoading } = useApi(
    () => loansService.eligibility(groupId),
    [groupId]
  );

  // Group capacity (available liquid funds)
  const { data: capacity, loading: cLoading } = useApi(
    () => loansService.groupCapacity(groupId),
    [groupId]
  );

  const [amount, setAmount] = useState('');
  const [months, setMonths] = useState(6);
  const [purpose, setPurpose] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);

  // Effective max = min(member eligibility, group available funds)
  const effectiveMax = (() => {
    if (!eligibility?.maxLoanAmount) return 0;
    const eligMax = Number(eligibility.maxLoanAmount);
    const availMax = capacity?.availableFunds != null
      ? Number(capacity.availableFunds)
      : eligMax;
    return Math.max(0, Math.min(eligMax, availMax));
  })();

  // Pre-fill amount when data loads
  useEffect(() => {
    if (effectiveMax > 0 && !amount) {
      setAmount(String(Math.floor(effectiveMax / 2)));
    }
  }, [effectiveMax]);

  if (eLoading || cLoading) return <LoadingState fullScreen />;

  // ---- Not eligible ----
  if (!eligibility?.eligible) {
    return (
      <div className="loan-page">
        <Link to={`/groups/${groupId}`} className="back-link">
          <ArrowLeft size={16} />
          <span>{t('common.back')}</span>
        </Link>

        <Card padding="lg" className="not-eligible-card">
          <div className="not-eligible-icon">
            <XCircle size={32} aria-hidden="true" />
          </div>
          <h2 className="not-eligible-title">{t('loans.notEligible')}</h2>
          <p className="not-eligible-reason">{eligibility?.reason}</p>
        </Card>
      </div>
    );
  }

  // ---- Group has no available funds ----
  if (capacity && Number(capacity.availableFunds) <= 0) {
    return (
      <div className="loan-page">
        <Link to={`/groups/${groupId}`} className="back-link">
          <ArrowLeft size={16} />
          <span>{t('common.back')}</span>
        </Link>

        <Card padding="lg" className="not-eligible-card">
          <div className="not-eligible-icon">
            <XCircle size={32} aria-hidden="true" />
          </div>
          <h2 className="not-eligible-title">
            {t('loans.noGroupFunds') || 'No Group Funds Available'}
          </h2>
          <p className="not-eligible-reason">
            {t('loans.noGroupFundsReason') ||
              'The group currently has no funds available for new loans.'}
          </p>
        </Card>
      </div>
    );
  }

  // ---- Success screen ----
  if (result) {
    return (
      <div className="loan-page">
        <div className="contribution-success">
          <div className="contribution-success-icon">
            <CheckCircle2 size={48} aria-hidden="true" />
          </div>
          <h1 className="contribution-success-title">
            {t('loans.submitApplication')}
          </h1>
          <p className="contribution-success-text">
            {t('loans.pendingLoans')}
          </p>

          <Card padding="md" className="contribution-success-card">
            <div className="success-row">
              <span className="success-label">{t('loans.loanAmount')}</span>
              <span className="success-value">
                {Number(result.amount).toLocaleString()} TZS
              </span>
            </div>
            <div className="success-row">
              <span className="success-label">{t('loans.totalRepayable')}</span>
              <span className="success-value">
                {Number(result.totalRepayable).toLocaleString()} TZS
              </span>
            </div>
            <div className="success-row">
              <span className="success-label">{t('loans.repaymentPeriod')}</span>
              <span className="success-value">
                {result.repaymentPeriodMonths} {t('loans.months')}
              </span>
            </div>
          </Card>

          <div className="contribution-success-actions">
            <Link to={`/groups/${groupId}`}>
              <Button variant="secondary">{t('common.back')}</Button>
            </Link>
            <Link to="/loans">
              <Button variant="primary">{t('loans.myLoans')}</Button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // ---- Form ----
  const interestRate = eligibility.interestRate || 10;
  const total = Number(amount) * (1 + (interestRate / 100) * (months / 12));
  const monthly = months > 0 ? total / months : 0;

  // Whether the group funds are the limiting factor
  const limitedByGroup = capacity && Number(capacity.availableFunds) < Number(eligibility.maxLoanAmount);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!amount || Number(amount) <= 0) {
      toast.error(t('errors.required'));
      return;
    }

    if (Number(amount) > Number(eligibility.maxLoanAmount)) {
      toast.error(t('loans.exceedsEligibility'));
      return;
    }

    if (capacity && Number(amount) > Number(capacity.availableFunds)) {
      toast.error(
        t('loans.exceedsGroupFunds') ||
        'Amount exceeds the group\'s available funds'
      );
      return;
    }

    setSubmitting(true);
    try {
      const loan = await loansService.apply(groupId, {
        amount: Number(amount),
        repaymentPeriodMonths: Number(months),
        purpose: purpose || null,
      });
      setResult(loan);
      toast.success(t('common.success'));
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="loan-page">
      <Link to={`/groups/${groupId}`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="loan-title">{t('loans.applyForLoan')}</h1>

      <Card padding="md" className="eligibility-card">
        <p className="eligibility-label">{t('loans.eligibility')}</p>
        <p className="eligibility-value">
          {t('loans.eligibleUpTo', { amount: effectiveMax.toLocaleString() })} TZS
        </p>
        <p className="eligibility-sub">
          {eligibility.totalSavings?.toLocaleString()} TZS × {eligibility.savingsMultiplier}
        </p>

        {capacity && (
          <div className="eligibility-capacity">
            <div className="eligibility-capacity-row">
              <span>{t('loans.groupAvailable') || 'Group available funds'}</span>
              <span className="eligibility-capacity-value">
                {Number(capacity.availableFunds).toLocaleString()} TZS
              </span>
            </div>

            {limitedByGroup && (
              <div className="eligibility-capacity-warning">
                <AlertCircle size={14} />
                <span>
                  {t('loans.limitedByGroupFunds') ||
                    "You are limited by the group's current available funds."}
                </span>
              </div>
            )}
          </div>
        )}
      </Card>

      <Card padding="md">
        <form onSubmit={handleSubmit} className="contribution-form" noValidate>
          <AmountInput
            label={t('loans.loanAmount')}
            value={amount}
            onChange={setAmount}
            currency="TZS"
            max={effectiveMax}
            quickAmounts={
              effectiveMax > 0
                ? [
                    Math.floor(effectiveMax * 0.25),
                    Math.floor(effectiveMax * 0.5),
                    Math.floor(effectiveMax * 0.75),
                    effectiveMax,
                  ].filter((v, i, arr) => v > 0 && arr.indexOf(v) === i)
                : []
            }
          />

          <div className="form-group">
            <label className="form-label">
              {t('loans.repaymentPeriod')} <span className="input-required">*</span>
            </label>
            <div className="loan-period-options">
              {[3, 6, 12, 18, 24].map((m) => (
                <button
                  key={m}
                  type="button"
                  className={`loan-period-btn ${Number(months) === m ? 'active' : ''}`}
                  onClick={() => setMonths(m)}
                >
                  {m} {t('loans.months')}
                </button>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="purpose" className="form-label">
              {t('loans.loanPurpose')}
            </label>
            <textarea
              id="purpose"
              className="form-textarea"
              value={purpose}
              onChange={(e) => setPurpose(e.target.value)}
              rows={2}
              maxLength={255}
            />
          </div>

          <div className="loan-summary">
            <div className="loan-summary-row">
              <span className="loan-summary-label">{t('loans.loanAmount')}</span>
              <span className="loan-summary-value">
                {Number(amount || 0).toLocaleString()} TZS
              </span>
            </div>
            <div className="loan-summary-row">
              <span className="loan-summary-label">{t('loans.interestRate')}</span>
              <span className="loan-summary-value">{interestRate}%</span>
            </div>
            <div className="loan-summary-row highlight">
              <span className="loan-summary-label">{t('loans.totalRepayable')}</span>
              <span className="loan-summary-value">
                {Math.round(total).toLocaleString()} TZS
              </span>
            </div>
            <div className="loan-summary-row">
              <span className="loan-summary-label">{t('loans.monthlyInstallment')}</span>
              <span className="loan-summary-value">
                {Math.round(monthly).toLocaleString()} TZS
              </span>
            </div>
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            loading={submitting}
            disabled={effectiveMax <= 0}
          >
            {t('loans.submitApplication')}
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default ApplyLoan;