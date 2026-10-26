import clsx from 'clsx';
import { Loader2 } from 'lucide-react';
import './Button.css';

const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  type = 'button',
  fullWidth = false,
  loading = false,
  disabled = false,
  leftIcon: LeftIcon,
  rightIcon: RightIcon,
  onClick,
  ...props
}) => {
  return (
    <button
      type={type}
      className={clsx(
        'btn',
        `btn-${variant}`,
        `btn-${size}`,
        fullWidth && 'btn-full',
        loading && 'btn-loading'
      )}
      disabled={disabled || loading}
      onClick={onClick}
      aria-busy={loading}
      {...props}
    >
      {loading && <Loader2 className="btn-spinner" size={16} />}
      {!loading && LeftIcon && <LeftIcon size={18} aria-hidden="true" />}
      <span>{children}</span>
      {!loading && RightIcon && <RightIcon size={18} aria-hidden="true" />}
    </button>
  );
};

export default Button;