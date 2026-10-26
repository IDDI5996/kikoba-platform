import { useTranslation } from 'react-i18next';
import clsx from 'clsx';
import PaymentMethodLogo from './PaymentMethodLogo';
import './PaymentMethodSelect.css';

const METHODS = [
  { key: 'MPESA',    label: 'M-Pesa' },
  { key: 'TIGOPESA', label: 'Mixx by Yas' },
  { key: 'AIRTEL',   label: 'Airtel Money' },
  { key: 'HALOPESA', label: 'HaloPesa' },
  { key: 'AZAMPESA', label: 'AzamPesa' },
  { key: 'BANK',     label: 'Bank Transfer' },
  { key: 'CASH',     label: 'Cash' },
];

const PaymentMethodSelect = ({
  value,
  onChange,
  methods = METHODS.map((m) => m.key),
}) => {
  const { t } = useTranslation();
  const visible = METHODS.filter((m) => methods.includes(m.key));

  return (
    <div className="payment-method-select">
      {visible.map((method) => (
        <button
          key={method.key}
          type="button"
          className={clsx('payment-method-btn', value === method.key && 'active')}
          onClick={() => onChange(method.key)}
          aria-pressed={value === method.key}
          aria-label={method.label}
        >
          <div className="payment-method-logo-wrap">
            <PaymentMethodLogo method={method.key} size={52} />
          </div>
          <span className="payment-method-label">
            {t(`contributions.paymentMethods.${method.key}`) || method.label}
          </span>
        </button>
      ))}
    </div>
  );
};

export default PaymentMethodSelect;