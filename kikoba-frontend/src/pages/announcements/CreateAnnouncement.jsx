import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';
import announcementsService from '../../services/announcementsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import './Announcements.css';

const CATEGORIES = ['GENERAL', 'FINANCIAL', 'MEETING', 'EMERGENCY', 'POLICY'];

const CreateAnnouncement = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { groupId } = useParams();
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: { category: 'GENERAL', pinned: false },
  });

  const onSubmit = async (data) => {
    setSubmitting(true);
    try {
      await announcementsService.create(groupId, {
        title: data.title,
        body: data.body,
        category: data.category,
        pinned: !!data.pinned,
      });
      toast.success(t('common.success'));
      navigate(`/groups/${groupId}/announcements`);
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="announcement-page">
      <Link to={`/groups/${groupId}/announcements`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="page-title">{t('announcements.create')}</h1>

      <Card padding="md">
        <form onSubmit={handleSubmit(onSubmit)} className="poll-form" noValidate>
          <Input
            label={t('announcements.announcementTitle')}
            required
            error={errors.title && t('errors.required')}
            {...register('title', { required: true, minLength: 3 })}
          />

          <div className="form-group">
            <label htmlFor="category" className="form-label">
              {t('announcements.category')}
            </label>
            <select
              id="category"
              className="form-select"
              {...register('category')}
            >
              {CATEGORIES.map((cat) => (
                <option key={cat} value={cat}>
                  {t(`announcements.categories.${cat}`)}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="body" className="form-label">
              {t('announcements.body')} <span className="input-required">*</span>
            </label>
            <textarea
              id="body"
              className="form-textarea"
              rows={6}
              required
              {...register('body', { required: true, minLength: 5 })}
            />
          </div>

          <div className="poll-form-checkbox">
            <input type="checkbox" id="pinned" {...register('pinned')} />
            <label htmlFor="pinned">{t('announcements.pin')}</label>
          </div>

          <div className="poll-form-actions">
            <Link to={`/groups/${groupId}/announcements`}>
              <Button variant="secondary" type="button">
                {t('common.cancel')}
              </Button>
            </Link>
            <Button type="submit" variant="primary" loading={submitting}>
              {t('announcements.create')}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default CreateAnnouncement;