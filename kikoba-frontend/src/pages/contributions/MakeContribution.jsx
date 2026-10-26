import { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import { ArrowLeft, CheckCircle2 } from 'lucide-react';
import { useGroup } from '../../hooks/useGroups';
import useApi from '../../hooks/useApi';
import contributionsService from '../../services/contributionsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingState from '../../components/common/LoadingState';
import AmountInput from '../../components/financial/AmountInput';
import PaymentMethodSelect from '../../components/financial/PaymentMethodSelect';
import './Contributions.css';

const MakeContribution = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { groupId } = useParams();

  const { data: group, loading: gLoading } = useGroup(groupId);
  const { data: summary } = useApi(
    () => contributionsService.mySummary(groupId),
    [groupId]
  );

  const [amount, setAmount] = useState('');
  const [method, setMethod] = useState('MPESA');
  const [reference, setReference] = useState('');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    if (group?.minContribution && !amount) {
      setAmount(String(group.minContribution));
    }
  }, [group]);

  if (gLoading) return <LoadingState fullScreen />;

  const validate = () => {
    const amt = Number(amount);
    if (!amt || amt <= 0) return t('errors.required');
    if (group?.minContribution && amt < Number(group.minContribution)) {
      return t('contributions.amountBelowMin', {
        min: Number(group.minContribution).toLocaleString(),
      });
    }
    if (group?.maxContribution && amt > Number(group.maxContribution)) {
      return t('contributions.amountAboveMax', {
        max: Number(group.maxContribution).toLocaleString(),
      });
    }
    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationError = validate();
    if (validationError) {
      toast.error(validationError);
      return;
    }

    setSubmitting(true);
    try {
      const contribution = await contributionsService.make(groupId, {
        amount: Number(amount),
        paymentMethod: method,
        transactionReference: reference || null,
        notes: notes || null,
      });
      setSuccess(contribution);
      toast.success(t('contributions.submitSuccess'));
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  // ---- Success screen ----
  if (success) {
    return (
      <div className="contribution-success">
        <div className="contribution-success-icon">
          <CheckCircle2 size={48} aria-hidden="true" />
        </div>
        <h1 className="contribution-success-title">{t('contributions.submitSuccess')}</h1>
        <p className="contribution-success-text">
          {t('contributions.confirmSuccess')}
        </p>

        <Card padding="md" className="contribution-success-card">
          <div className="success-row">
            <span className="success-label">{t('contributions.contributionAmount')}</span>
            <span className="success-value">
              {Number(success.amount).toLocaleString()} TZS
            </span>
          </div>
          <div className="success-row">
            <span className="success-label">{t('contributions.paymentMethod')}</span>
            <span className="success-value">
              {t(`contributions.paymentMethods.${success.paymentMethod}`)}
            </span>
          </div>
          <div className="success-row">
            <span className="success-label">{t('contributions.reference')}</span>
            <span className="success-value success-reference">
              {success.transactionReference}
            </span>
          </div>
        </Card>

        <div className="contribution-success-actions">
          <Link to={`/groups/${groupId}`}>
            <Button variant="secondary">{t('common.back')}</Button>
          </Link>
          <Link to="/contributions">
            <Button variant="primary">{t('contributions.myContributions')}</Button>
          </Link>
        </div>
      </div>
    );
  }

  // ---- Form ----
  return (
    <div className="contribution-page">
      <Link to={`/groups/${groupId}`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <div className="contribution-title-block">
        <h1 className="contribution-title">
          {t('contributions.makeContribution')}
        </h1>
        <p className="contribution-subtitle">{group.name}</p>
      </div>

      {summary && (
        <Card padding="md" className="contribution-balance-card">
          <p className="contribution-balance-label">
            {t('contributions.currentBalance')}
          </p>
          <p className="contribution-balance-value">
            {Number(summary.totalContributed || 0).toLocaleString()}
            <span className="currency">TZS</span>
          </p>
        </Card>
      )}

      <Card padding="md">
        <form onSubmit={handleSubmit} className="contribution-form" noValidate>
          <AmountInput
            label={t('contributions.contributionAmount')}
            value={amount}
            onChange={setAmount}
            currency={group.currency || 'TZS'}
            min={group.minContribution}
            max={group.maxContribution}
            quickAmounts={
              group.minContribution && group.maxContribution
                ? [
                    Number(group.minContribution),
                    Math.round(Number(group.minContribution) * 2),
                    Math.round(Number(group.minContribution) * 5),
                    Number(group.maxContribution),
                  ]
                : [10000, 20000, 50000, 100000]
            }
          />

          <div className="form-group">
            <label className="form-label">
              {t('contributions.paymentMethod')} <span className="input-required">*</span>
            </label>
            <PaymentMethodSelect value={method} onChange={setMethod} />
          </div>

          <div className="form-group">
            <label htmlFor="reference" className="form-label">
              {t('contributions.reference')}
            </label>
            <input
              id="reference"
              type="text"
              className="form-input"
              value={reference}
              onChange={(e) => setReference(e.target.value)}
              placeholder="e.g., MPESA-ABC123"
              maxLength={100}
            />
          </div>

          <div className="form-group">
            <label htmlFor="notes" className="form-label">
              {t('contributions.notes')}
            </label>
            <textarea
              id="notes"
              className="form-textarea"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={2}
              maxLength={500}
            />
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            loading={submitting}
          >
            {t('contributions.makeContribution')}
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default MakeContribution;