import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Calendar, Plus, Video, MapPin, Clock, ChevronRight } from 'lucide-react';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import meetingsService from '../../services/meetingsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/StatusBadge';
import Tabs from '../../components/common/Tabs';
import './Meetings.css';

const Meetings = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();
  const [tab, setTab] = useState('upcoming');

  const { role } = useMyGroupRole(groupId);           // ← NEW
  const canSchedule = can(role, 'CREATE_MEETING');     // ← NEW

  const all = useApi(() => meetingsService.list(groupId), [groupId]);
  const upcoming = useApi(() => meetingsService.upcoming(groupId), [groupId]);

  const list = tab === 'upcoming' ? upcoming.data : all.data;
  const loading = tab === 'upcoming' ? upcoming.loading : all.loading;
  const error = tab === 'upcoming' ? upcoming.error : all.error;
  const refetch = tab === 'upcoming' ? upcoming.refetch : all.refetch;

  const tabs = [
    { key: 'upcoming', label: t('meetings.upcoming') },
    { key: 'all', label: t('common.all') },
  ];

  return (
    <div className="meetings-page">
      <div className="meetings-header">
        <div>
          <h1 className="page-title">{t('meetings.title')}</h1>
        </div>
        {/* ✅ Only managers see this button */}
        {canSchedule && (
          <Link to={`/groups/${groupId}/meetings/schedule`}>
            <Button variant="primary" leftIcon={Plus}>
              {t('meetings.scheduleMeeting')}
            </Button>
          </Link>
        )}
      </div>

      <Tabs tabs={tabs} active={tab} onChange={setTab} />

      {loading ? (
        <LoadingState />
      ) : error ? (
        <ErrorState error={error} onRetry={refetch} />
      ) : !list || list.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={Calendar}
            title={t('meetings.title')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <div className="meetings-list">
          {list.map((meeting) => (
            <Link
              key={meeting.id}
              to={`/meetings/${meeting.id}`}
              className="meeting-card-link"
            >
              <Card padding="md" elevated className="meeting-card">
                <div className="meeting-card-header">
                  <div className="meeting-card-icon">
                    <Calendar size={20} aria-hidden="true" />
                  </div>
                  <StatusBadge status={meeting.status} size="sm" />
                </div>

                <h3 className="meeting-card-title">{meeting.title}</h3>

                <div className="meeting-card-meta">
                  <div className="meeting-meta-item">
                    <Clock size={14} />
                    <span>
                      {new Date(meeting.scheduledAt).toLocaleDateString('sw-TZ', {
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  </div>
                  {meeting.location && (
                    <div className="meeting-meta-item">
                      <MapPin size={14} />
                      <span>{meeting.location}</span>
                    </div>
                  )}
                  {meeting.videoMeetingUrl && (
                    <div className="meeting-meta-item">
                      <Video size={14} />
                      <span>{t('meetings.online')}</span>
                    </div>
                  )}
                </div>

                <div className="meeting-card-footer">
                  <span className="meeting-attendance-stat">
                    {t('meetings.present')}: {meeting.presentCount ?? 0}
                  </span>
                  <ChevronRight size={16} />
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default Meetings;