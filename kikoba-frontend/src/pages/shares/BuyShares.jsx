import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';
import sharesService from '../../services/sharesService';
import useApi from '../../hooks/useApi';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import AmountInput from '../../components/financial/AmountInput';
import './Shares.css';

const BuyShares = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { groupId } = useParams();
  const [count, setCount] = useState('1');
  const [submitting, setSubmitting] = useState(false);

  const summary = useApi(() => sharesService.mySummary(groupId), [groupId]);
  const price = Number(summary.data?.currentSharePrice ?? 100);
  const total = price * Number(count || 0);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!count || Number(count) < 1) {
      toast.error(t('errors.required'));
      return;
    }
    setSubmitting(true);
    try {
      await sharesService.buy(groupId, {
        numberOfShares: Number(count),
      });
      toast.success(t('common.success'));
      navigate(`/groups/${groupId}/shares`);
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="shares-page">
      <Link to={`/groups/${groupId}/shares`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="page-title">{t('shares.buyShares')}</h1>

      <Card padding="md">
        <form onSubmit={handleSubmit} className="poll-form" noValidate>
          <AmountInput
            label={t('shares.numberOfShares')}
            value={count}
            onChange={setCount}
            currency=""
            quickAmounts={[1, 5, 10, 25, 50]}
          />

          <div className="loan-summary">
            <div className="loan-summary-row">
              <span className="loan-summary-label">{t('shares.sharePrice')}</span>
              <span className="loan-summary-value">{price.toLocaleString()} TZS</span>
            </div>
            <div className="loan-summary-row highlight">
              <span className="loan-summary-label">{t('shares.totalCost')}</span>
              <span className="loan-summary-value">{total.toLocaleString()} TZS</span>
            </div>
          </div>

          <Button type="submit" variant="primary" size="lg" fullWidth loading={submitting}>
            {t('shares.buyShares')}
          </Button>
        </form>
      </Card>
    </div>
  );
};

export default BuyShares;