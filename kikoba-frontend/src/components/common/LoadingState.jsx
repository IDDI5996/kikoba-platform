import './LoadingState.css';

const LoadingState = ({ message, fullScreen = false }) => {
  return (
    <div className={`loading-state ${fullScreen ? 'loading-full' : ''}`} role="status" aria-live="polite">
      <div className="loading-spinner" aria-hidden="true" />
      {message && <p className="loading-message">{message}</p>}
    </div>
  );
};

export default LoadingState;