import { useTranslation } from 'react-i18next';
import './AmountInput.css';

const AmountInput = ({
  value,
  onChange,
  currency = 'TZS',
  quickAmounts = [10000, 20000, 50000, 100000],
  min,
  max,
  error,
  label,
}) => {
  const { t } = useTranslation();

  const handleInput = (e) => {
    const raw = e.target.value.replace(/[^0-9]/g, '');
    onChange(raw);
  };

  const formatDisplay = (val) => {
    if (!val) return '';
    return Number(val).toLocaleString('en-US');
  };

  return (
    <div className="amount-input">
      {label && <label className="form-label">{label}</label>}

      <div className={`amount-input-field ${error ? 'error' : ''}`}>
        <span className="amount-input-currency">{currency}</span>
        <input
          type="text"
          inputMode="numeric"
          className="amount-input-native"
          value={formatDisplay(value)}
          onChange={handleInput}
          placeholder="0"
          aria-invalid={!!error}
        />
      </div>

      {error && <p className="input-error">{error}</p>}

      {quickAmounts.length > 0 && (
        <div className="amount-quick-row">
          <span className="amount-quick-label">{t('contributions.quickAmounts')}</span>
          <div className="amount-quick-buttons">
            {quickAmounts.map((amt) => (
              <button
                key={amt}
                type="button"
                className={`amount-quick-btn ${Number(value) === amt ? 'active' : ''}`}
                onClick={() => onChange(String(amt))}
              >
                {amt.toLocaleString()}
              </button>
            ))}
          </div>
        </div>
      )}

      {(min || max) && (
        <p className="amount-hint">
          {min && max && `Min: ${Number(min).toLocaleString()} • Max: ${Number(max).toLocaleString()}`}
          {min && !max && `Min: ${Number(min).toLocaleString()}`}
          {!min && max && `Max: ${Number(max).toLocaleString()}`}
        </p>
      )}
    </div>
  );
};

export default AmountInput;