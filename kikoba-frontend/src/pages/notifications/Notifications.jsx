import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import {
  Bell, Check, Trash2, Wallet, HandCoins, Calendar, Vote,
  Megaphone, TrendingUp, Shield, Settings,
} from 'lucide-react';
import useApi from '../../hooks/useApi';
import notificationsService from '../../services/notificationsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import Tabs from '../../components/common/Tabs';
import './Notifications.css';

const ICON_MAP = {
  CONTRIBUTION: Wallet,
  LOAN: HandCoins,
  MEETING: Calendar,
  POLL: Vote,
  ANNOUNCEMENT: Megaphone,
  DIVIDEND: TrendingUp,
  SECURITY: Shield,
  SYSTEM: Bell,
};

const Notifications = () => {
  const { t } = useTranslation();
  const [tab, setTab] = useState('all');

  const list = useApi(() => notificationsService.list(0, 50), []);
  const unread = useApi(() => notificationsService.unread(), []);

  const data = tab === 'unread' ? unread.data : list.data?.content;
  const loading = tab === 'unread' ? unread.loading : list.loading;
  const error = tab === 'unread' ? unread.error : list.error;
  const refetch = tab === 'unread' ? unread.refetch : list.refetch;

  const tabs = [
    { key: 'all', label: t('notifications.categories.all') },
    { key: 'unread', label: t('notifications.unread') },
  ];

  const handleMarkRead = async (id) => {
    try {
      await notificationsService.markAsRead(id);
      list.refetch();
      unread.refetch();
    } catch (err) {
      toast.error(t('errors.serverError'));
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationsService.markAllRead();
      toast.success(t('common.success'));
      list.refetch();
      unread.refetch();
    } catch (err) {
      toast.error(t('errors.serverError'));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('notifications.confirmDelete') || 'Delete this notification?')) {
      return;
    }
    try {
      await notificationsService.delete(id);
      list.refetch();
      unread.refetch();
      toast.success(t('common.success'));
    } catch (err) {
      toast.error(t('errors.serverError'));
    }
  };

  return (
    <div className="notifications-page">
      <div className="notifications-header">
        <h1 className="page-title">{t('notifications.title')}</h1>
        <div className="notifications-header-actions">
          <Link to="/profile/notification-preferences">
            <Button variant="secondary" leftIcon={Settings}>
              {t('profile.preferences')}
            </Button>
          </Link>
          <Button variant="secondary" leftIcon={Check} onClick={handleMarkAllRead}>
            {t('notifications.markAllRead')}
          </Button>
        </div>
      </div>

      <Tabs tabs={tabs} active={tab} onChange={setTab} />

      {loading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !data || data.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={Bell}
            title={t('notifications.empty')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <Card padding="none">
          <ul className="notifications-list">
            {data.map((n) => {
              const Icon = ICON_MAP[n.type] || Bell;
              return (
                <li
                  key={n.id}
                  className={`notification-item ${!n.isRead ? 'unread' : ''}`}
                  onClick={() => !n.isRead && handleMarkRead(n.id)}
                >
                  <div className={`notification-icon notif-${n.type?.toLowerCase()}`}>
                    <Icon size={18} aria-hidden="true" />
                  </div>
                  <div className="notification-body">
                    <div className="notification-title-row">
                      <p className="notification-title">{n.title}</p>
                      {!n.isRead && <span className="notification-dot" />}
                    </div>
                    <p className="notification-text">{n.body}</p>
                    <p className="notification-time">
                      {new Date(n.createdAt).toLocaleString('sw-TZ', {
                        dateStyle: 'short',
                        timeStyle: 'short',
                      })}
                    </p>
                  </div>
                  <button
                    type="button"
                    className="notification-delete"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(n.id);
                    }}
                    aria-label={t('common.delete')}
                  >
                    <Trash2 size={14} />
                  </button>
                </li>
              );
            })}
          </ul>
        </Card>
      )}
    </div>
  );
};

export default Notifications;