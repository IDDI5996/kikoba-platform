import mpesaLogo from '../../assets/logos/payment/mpesa.svg';
import mixxLogo from '../../assets/logos/payment/mixx.svg';
import airtelLogo from '../../assets/logos/payment/airtel.svg';
import halopesaLogo from '../../assets/logos/payment/halopesa.svg';
import azampesaLogo from '../../assets/logos/payment/azampesa.svg';
import bankLogo from '../../assets/logos/payment/bank.svg';
import cashLogo from '../../assets/logos/payment/cash.svg';

const LOGOS = {
  MPESA:    { src: mpesaLogo,    alt: 'M-Pesa' },
  TIGOPESA: { src: mixxLogo,     alt: 'Mixx by Yas' },
  AIRTEL:   { src: airtelLogo,   alt: 'Airtel Money' },
  HALOPESA: { src: halopesaLogo, alt: 'HaloPesa' },
  AZAMPESA: { src: azampesaLogo, alt: 'AzamPesa' },
  BANK:     { src: bankLogo,     alt: 'Bank Transfer' },
  CASH:     { src: cashLogo,     alt: 'Cash' },
};

const PaymentMethodLogo = ({ method, size = 56 }) => {
  const logo = LOGOS[method];
  if (!logo) return null;

  return (
    <img
      src={logo.src}
      alt={logo.alt}
      width={size}
      height={size}
      className="payment-method-logo"
      draggable={false}
      loading="lazy"
    />
  );
};

export default PaymentMethodLogo;