import './StatCard.css';

const StatCard = ({ icon: Icon, label, value, currency, trend, trendUp, loading }) => {
  if (loading) {
    return (
      <div className="stat-card stat-card-skeleton">
        <div className="skeleton skeleton-icon" />
        <div className="skeleton skeleton-line" />
        <div className="skeleton skeleton-amount" />
      </div>
    );
  }

  return (
    <div className="stat-card">
      <div className="stat-card-top">
        {Icon && (
          <div className="stat-card-icon">
            <Icon size={20} aria-hidden="true" />
          </div>
        )}
        {trend && (
          <span className={`stat-card-trend ${trendUp ? 'up' : ''}`}>
            {trend}
          </span>
        )}
      </div>
      <p className="stat-card-label">{label}</p>
      <p className="stat-card-value">
        <span className="stat-card-amount">{value}</span>
        {currency && <span className="stat-card-currency">{currency}</span>}
      </p>
    </div>
  );
};

export default StatCard;