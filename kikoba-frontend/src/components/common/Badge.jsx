import clsx from 'clsx';
import './Badge.css';

const Badge = ({ children, variant = 'neutral', size = 'md' }) => (
  <span className={clsx('badge', `badge-${variant}`, `badge-${size}`)}>
    {children}
  </span>
);

export default Badge;