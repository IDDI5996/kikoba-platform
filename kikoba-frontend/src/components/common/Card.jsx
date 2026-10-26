import clsx from 'clsx';
import './Card.css';

const Card = ({ children, className, padding = 'md', elevated = false, ...props }) => {
  return (
    <div
      className={clsx(
        'card',
        `card-padding-${padding}`,
        elevated && 'card-elevated',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
};

export default Card;