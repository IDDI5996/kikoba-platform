import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';
import meetingsService from '../../services/meetingsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import './Meetings.css';

const ScheduleMeeting = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { groupId } = useParams();
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    setSubmitting(true);
    try {
      const meeting = await meetingsService.schedule(groupId, {
        title: data.title,
        agenda: data.agenda || null,
        scheduledAt: data.scheduledAt,
        location: data.location || null,
      });
      toast.success(t('common.success'));
      navigate(`/meetings/${meeting.id}`);
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="meeting-page">
      <Link to={`/groups/${groupId}/meetings`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="page-title">{t('meetings.scheduleMeeting')}</h1>

      <Card padding="md">
        <form onSubmit={handleSubmit(onSubmit)} className="meeting-form" noValidate>
          <Input
            label={t('meetings.meetingTitle')}
            placeholder="Monthly Group Meeting"
            required
            error={errors.title && t('errors.required')}
            {...register('title', { required: true, minLength: 3 })}
          />

          <div className="form-group">
            <label htmlFor="scheduledAt" className="form-label">
              {t('meetings.scheduledFor')} <span className="input-required">*</span>
            </label>
            <input
              id="scheduledAt"
              type="datetime-local"
              className="form-input"
              required
              {...register('scheduledAt', { required: true })}
            />
            {errors.scheduledAt && (
              <p className="input-error">{t('errors.required')}</p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="location" className="form-label">
              {t('meetings.location')}
            </label>
            <input
              id="location"
              type="text"
              className="form-input"
              placeholder="Community Hall"
              {...register('location')}
            />
          </div>

          <div className="form-group">
            <label htmlFor="agenda" className="form-label">
              {t('meetings.agenda')}
            </label>
            <textarea
              id="agenda"
              className="form-textarea"
              rows={5}
              placeholder="1. Review contributions\n2. Discuss loans\n3. Other business"
              {...register('agenda')}
            />
          </div>

          <div className="meeting-form-actions">
            <Link to={`/groups/${groupId}/meetings`}>
              <Button variant="secondary" type="button">
                {t('common.cancel')}
              </Button>
            </Link>
            <Button type="submit" variant="primary" loading={submitting}>
              {t('meetings.scheduleMeeting')}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default ScheduleMeeting;