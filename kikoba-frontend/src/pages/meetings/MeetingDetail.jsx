import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import {
  ArrowLeft, Calendar, MapPin, Video, Users, Clock, Play,
  XCircle, FileText, Copy, ExternalLink, Wifi, WifiOff, Timer,
} from 'lucide-react';
import meetingsService from '../../services/meetingsService';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import useMeetingHeartbeat from '../../hooks/useMeetingHeartbeat';
import { can } from '../../config/permissions';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import './Meetings.css';

const formatTimer = (totalSeconds) => {
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  if (h > 0) {
    return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};

const MeetingDetail = () => {
  const { t } = useTranslation();
  const { meetingId } = useParams();

  const meeting = useApi(() => meetingsService.get(meetingId), [meetingId]);
  const attendance = useApi(() => meetingsService.attendance(meetingId), [meetingId]);
  const myAttendance = useApi(() => meetingsService.myAttendance(meetingId), [meetingId]);

  const { role } = useMyGroupRole(meeting.data?.groupId);
  const canStart = can(role, 'START_MEETING');
  const canComplete = can(role, 'COMPLETE_MEETING');
  const canCancel = can(role, 'CANCEL_MEETING');

  const isOngoing = meeting.data?.status === 'ONGOING';
  const { elapsedSeconds, connected } = useMeetingHeartbeat(meetingId, isOngoing);

  const [minutesOpen, setMinutesOpen] = useState(false);
  const [minutes, setMinutes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [videoOpen, setVideoOpen] = useState(false);

  // Refresh attendance periodically during meeting
  useEffect(() => {
    if (!isOngoing) return;
    const interval = setInterval(() => {
      attendance.refetch().catch(() => {});
      myAttendance.refetch().catch(() => {});
    }, 60000);
    return () => clearInterval(interval);
  }, [isOngoing]);

  useEffect(() => {
    if (meeting.data?.minutes) setMinutes(meeting.data.minutes);
  }, [meeting.data?.minutes]);

  if (meeting.loading) return <LoadingState fullScreen />;
  if (meeting.error) return <ErrorState error={meeting.error} onRetry={meeting.refetch} />;
  if (!meeting.data) return null;

  const m = meeting.data;
  const showVideo = m.videoMeetingUrl && m.status !== 'CANCELLED' && m.status !== 'COMPLETED';
  const my = myAttendance.data;

  const copyToClipboard = (text, label) => {
    navigator.clipboard.writeText(text);
    toast.success(`${label} ${t('meetings.copied') || 'copied'}`);
  };

  const startMeeting = async () => {
    try {
      await meetingsService.start(meetingId);
      toast.success(t('common.success'));
      meeting.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const completeMeeting = async () => {
    setSubmitting(true);
    try {
      await meetingsService.complete(meetingId, minutes);
      toast.success(t('common.success'));
      setMinutesOpen(false);
      meeting.refetch();
      attendance.refetch();
      myAttendance.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  const cancelMeeting = async () => {
    const reason = window.prompt(t('common.confirm'));
    if (!reason) return;
    try {
      await meetingsService.cancel(meetingId, reason);
      toast.success(t('common.success'));
      meeting.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  return (
    <div className="meeting-page">
      <Link to={`/groups/${m.groupId}/meetings`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <div className="meeting-detail-header">
        <div>
          <h1 className="page-title">{m.title}</h1>
          <p className="meeting-detail-group">{m.groupName}</p>
        </div>
        <StatusBadge status={m.status} />
      </div>

      {/* Live presence banner */}
      {isOngoing && (
        <Card padding="md" className="meeting-presence-card">
          <div className="meeting-presence-row">
            <div className="meeting-presence-info">
              <div className="meeting-presence-icon">
                {connected ? <Wifi size={18} /> : <WifiOff size={18} />}
              </div>
              <div>
                <p className="meeting-presence-label">
                  {connected
                    ? t('meetings.connected') || 'Connected'
                    : t('meetings.reconnecting') || 'Reconnecting...'}
                </p>
                <p className="meeting-presence-hint">
                  {t('meetings.presenceHint') ||
                    'Your attendance is tracked. Stay connected to be counted.'}
                </p>
              </div>
            </div>
            <div className="meeting-presence-timer">
              <Timer size={16} />
              <span>{formatTimer(elapsedSeconds)}</span>
            </div>
          </div>
        </Card>
      )}

      {/* Attendance summary (after completion) */}
      {m.status === 'COMPLETED' && my && (
        <Card padding="md" className="meeting-summary-card">
          <h2 className="meeting-summary-title">
            {t('meetings.yourAttendance') || 'Your Attendance'}
          </h2>
          <div className="meeting-summary-grid">
            <div className="meeting-summary-stat">
              <p className="meeting-summary-label">{t('common.status')}</p>
              <StatusBadge status={my.status} />
            </div>
            <div className="meeting-summary-stat">
              <p className="meeting-summary-label">
                {t('meetings.timeInMeeting') || 'Time in meeting'}
              </p>
              <p className="meeting-summary-value">
                {formatTimer(my.totalDurationSeconds || 0)}
              </p>
            </div>
            <div className="meeting-summary-stat">
              <p className="meeting-summary-label">
                {t('meetings.sessions') || 'Sessions'}
              </p>
              <p className="meeting-summary-value">{my.sessionCount || 0}</p>
            </div>
          </div>
          {my.computedStatusReason && (
            <p className="meeting-summary-reason">{my.computedStatusReason}</p>
          )}
        </Card>
      )}

      {/* Join details */}
      {showVideo && (
        <Card padding="md" className="meeting-join-card">
          <div className="meeting-join-header">
            <Video size={18} aria-hidden="true" />
            <h2 className="meeting-join-title">{t('meetings.joinMeeting')}</h2>
          </div>

          <div className="meeting-join-fields">
            <div className="meeting-join-field">
              <label className="meeting-join-label">{t('meetings.meetingId')}</label>
              <div className="meeting-join-input-row">
                <code className="meeting-join-code">{m.videoMeetingId}</code>
                <button
                  type="button"
                  className="meeting-join-copy"
                  onClick={() => copyToClipboard(m.videoMeetingId, 'Meeting ID')}
                  aria-label="Copy Meeting ID"
                >
                  <Copy size={14} />
                </button>
              </div>
            </div>

            <div className="meeting-join-field">
              <label className="meeting-join-label">{t('meetings.joinLink')}</label>
              <div className="meeting-join-input-row">
                <code className="meeting-join-code meeting-join-link">
                  {m.videoMeetingUrl}
                </code>
                <button
                  type="button"
                  className="meeting-join-copy"
                  onClick={() => copyToClipboard(m.videoMeetingUrl, 'Link')}
                  aria-label="Copy Link"
                >
                  <Copy size={14} />
                </button>
              </div>
            </div>
          </div>

          <Button
            variant="primary"
            fullWidth
            leftIcon={ExternalLink}
            onClick={() => setVideoOpen(true)}
          >
            {t('meetings.joinMeeting')}
          </Button>
        </Card>
      )}

      {/* Meta card */}
      <Card padding="md">
        <div className="meeting-meta-grid">
          <div className="meeting-meta-row">
            <Clock size={16} />
            <div>
              <p className="meeting-meta-label">{t('meetings.scheduledFor')}</p>
              <p className="meeting-meta-value">
                {new Date(m.scheduledAt).toLocaleString('sw-TZ', {
                  dateStyle: 'long',
                  timeStyle: 'short',
                })}
              </p>
            </div>
          </div>

          {m.location && (
            <div className="meeting-meta-row">
              <MapPin size={16} />
              <div>
                <p className="meeting-meta-label">{t('meetings.location')}</p>
                <p className="meeting-meta-value">{m.location}</p>
              </div>
            </div>
          )}

          <div className="meeting-meta-row">
            <Users size={16} />
            <div>
              <p className="meeting-meta-label">{t('meetings.attendance')}</p>
              <p className="meeting-meta-value">
                {m.presentCount || 0} / {m.presentCount + m.absentCount || 0}
              </p>
            </div>
          </div>
        </div>
      </Card>

      {/* Actions — gated by role */}
      <div className="meeting-detail-actions">
        {m.status === 'SCHEDULED' && canStart && (
          <Button variant="primary" leftIcon={Play} onClick={startMeeting}>
            {t('meetings.startMeeting')}
          </Button>
        )}
        {m.status === 'SCHEDULED' && canCancel && (
          <Button variant="danger" leftIcon={XCircle} onClick={cancelMeeting}>
            {t('meetings.cancelMeeting')}
          </Button>
        )}
        {m.status === 'ONGOING' && canComplete && (
          <Button
            variant="secondary"
            leftIcon={FileText}
            onClick={() => setMinutesOpen(true)}
          >
            {t('meetings.endMeeting')}
          </Button>
        )}
      </div>

      {/* Agenda */}
      {m.agenda && (
        <Card padding="md">
          <h2 className="meeting-section-title">{t('meetings.agenda')}</h2>
          <p className="meeting-text">{m.agenda}</p>
        </Card>
      )}

      {/* Minutes */}
      {m.minutes && (
        <Card padding="md">
          <h2 className="meeting-section-title">{t('meetings.minutes')}</h2>
          <p className="meeting-text">{m.minutes}</p>
        </Card>
      )}

      {/* Attendance list */}
      <Card padding="none">
        <div className="meeting-attendance-header">
          <h2 className="meeting-section-title">{t('meetings.attendance')}</h2>
        </div>

        {attendance.loading ? (
          <LoadingState />
        ) : (
          <ul className="meeting-attendance-list">
            {attendance.data?.map((a) => (
              <li key={a.id} className="meeting-attendance-item">
                <div className="member-avatar">{a.memberName?.[0] || '?'}</div>
                <div className="meeting-attendance-info">
                  <p className="meeting-attendance-name">{a.memberName}</p>
                  {a.totalDurationSeconds > 0 && (
                    <p className="meeting-attendance-time">
                      {formatTimer(a.totalDurationSeconds)}
                      {a.sessionCount > 1 && ` · ${a.sessionCount} sessions`}
                    </p>
                  )}
                </div>
                <StatusBadge status={a.status} size="sm" />
              </li>
            ))}
          </ul>
        )}
      </Card>

      {/* Minutes Modal */}
      <Modal
        isOpen={minutesOpen}
        onClose={() => setMinutesOpen(false)}
        title={t('meetings.minutes')}
        size="lg"
        footer={
          <>
            <Button variant="secondary" onClick={() => setMinutesOpen(false)}>
              {t('common.cancel')}
            </Button>
            <Button variant="primary" loading={submitting} onClick={completeMeeting}>
              {t('meetings.endMeeting')}
            </Button>
          </>
        }
      >
        <textarea
          className="form-textarea"
          rows={12}
          value={minutes}
          onChange={(e) => setMinutes(e.target.value)}
          placeholder={t('meetings.minutes')}
        />
      </Modal>

      {/* Video Modal */}
      <Modal
        isOpen={videoOpen}
        onClose={() => setVideoOpen(false)}
        title={m.title}
        size="lg"
      >
        <div className="meeting-video-embed">
          {m.videoMeetingUrl ? (
            <iframe
              src={m.videoMeetingUrl}
              title={m.title}
              allow="camera; microphone; fullscreen; display-capture; autoplay"
              allowFullScreen
              className="meeting-video-iframe"
            />
          ) : (
            <p className="meeting-video-empty">{t('meetings.online')}</p>
          )}
        </div>
      </Modal>
    </div>
  );
};

export default MeetingDetail;