import { useState, useEffect } from 'react';
import { Bell } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import API from '../../services/api';
import './NotificationBell.css';

const NotificationBell = () => {
  const { t } = useTranslation();
  const [count, setCount] = useState(0);

  useEffect(() => {
    const fetchCount = async () => {
      try {
        const { data } = await API.get('/notifications/unread/count');
        setCount(data.unreadCount || 0);
      } catch {
        // Silent fail
      }
    };
    fetchCount();
    const interval = setInterval(fetchCount, 60000); // Poll every minute
    return () => clearInterval(interval);
  }, []);

  return (
    <Link
      to="/notifications"
      className="notification-bell"
      aria-label={`${t('notifications.title')}: ${count} ${t('notifications.unread')}`}
    >
      <Bell size={20} aria-hidden="true" />
      {count > 0 && (
        <span className="notification-badge">
          {count > 99 ? '99+' : count}
        </span>
      )}
    </Link>
  );
};

export default NotificationBell;