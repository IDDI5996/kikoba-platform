import { forwardRef } from 'react';
import clsx from 'clsx';
import './Input.css';

const Input = forwardRef(({
  label,
  type = 'text',
  error,
  hint,
  leftIcon: LeftIcon,
  rightIcon: RightIcon,
  required = false,
  id,
  ...props
}, ref) => {
  const inputId = id || `input-${label?.toLowerCase().replace(/\s+/g, '-')}`;
  const errorId = error ? `${inputId}-error` : undefined;
  const hintId = hint ? `${inputId}-hint` : undefined;

  return (
    <div className={clsx('input-wrapper', error && 'input-wrapper-error')}>
      {label && (
        <label htmlFor={inputId} className="input-label">
          {label}
          {required && <span className="input-required" aria-label="required">*</span>}
        </label>
      )}
      <div className="input-field-wrapper">
        {LeftIcon && <LeftIcon className="input-icon input-icon-left" size={18} aria-hidden="true" />}
        <input
          ref={ref}
          id={inputId}
          type={type}
          className={clsx('input-field', LeftIcon && 'input-with-left-icon', RightIcon && 'input-with-right-icon')}
          aria-invalid={!!error}
          aria-describedby={clsx(errorId, hintId) || undefined}
          required={required}
          {...props}
        />
        {RightIcon && <RightIcon className="input-icon input-icon-right" size={18} aria-hidden="true" />}
      </div>
      {hint && !error && (
        <p id={hintId} className="input-hint">{hint}</p>
      )}
      {error && (
        <p id={errorId} className="input-error" role="alert">{error}</p>
      )}
    </div>
  );
});

Input.displayName = 'Input';

export default Input;