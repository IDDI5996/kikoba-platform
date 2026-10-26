import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import notificationsService from '../../services/notificationsService';
import Card from '../../components/common/Card';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import './Notifications.css';

const CHANNELS = ['IN_APP', 'SMS', 'EMAIL', 'PUSH'];
const KEY_TYPES = [
  'CONTRIBUTION_CONFIRMED',
  'LOAN_APPROVED',
  'LOAN_PAYMENT_DUE',
  'MEETING_SCHEDULED',
  'MEETING_REMINDER',
  'POLL_CREATED',
  'DIVIDEND_DISTRIBUTED',
  'SECURITY_ALERT',
];

const NotificationPreferences = () => {
  const { t } = useTranslation();
  const prefs = useApi(() => notificationsService.preferences(), []);

  const getPreference = (type, channel) => {
    const pref = prefs.data?.find(
      (p) => p.notificationType === type && p.channel === channel
    );
    return pref?.enabled !== false;
  };

  const handleToggle = async (type, channel, value) => {
    try {
      await notificationsService.updatePreference({
        notificationType: type,
        channel,
        enabled: value,
      });
      prefs.refetch();
    } catch (err) {
      toast.error(t('errors.serverError'));
    }
  };

  if (prefs.loading) return <LoadingState fullScreen />;
  if (prefs.error) return <ErrorState error={prefs.error} onRetry={prefs.refetch} />;

  return (
    <div className="notifications-page">
      <Link to="/notifications" className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="page-title">{t('profile.notificationPreferences')}</h1>

      <Card padding="none">
        <div className="prefs-table-wrapper">
          <table className="prefs-table">
            <thead>
              <tr>
                <th>{t('notifications.title')}</th>
                {CHANNELS.map((c) => (
                  <th key={c} className="prefs-channel-header">
                    {t(`notifications.channels.${c}`)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {KEY_TYPES.map((type) => (
                <tr key={type}>
                  <td className="prefs-type-cell">
                    {t(`notifications.types.${type}`)}
                  </td>
                  {CHANNELS.map((channel) => (
                    <td key={channel} className="prefs-toggle-cell">
                      <label className="prefs-toggle">
                        <input
                          type="checkbox"
                          checked={getPreference(type, channel)}
                          onChange={(e) => handleToggle(type, channel, e.target.checked)}
                        />
                        <span className="prefs-toggle-slider" />
                      </label>
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
};

export default NotificationPreferences;