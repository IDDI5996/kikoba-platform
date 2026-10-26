import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { ArrowLeft } from 'lucide-react';
import groupsService from '../../services/groupsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import './CreateGroup.css';

const JoinGroup = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const onSubmit = async (data) => {
    setSubmitting(true);
    try {
      const group = await groupsService.join(data.invitationCode.trim().toUpperCase());
      toast.success(t('groups.welcomeTo', { name: group.name }));
      navigate(`/groups/${group.id}`);
    } catch (err) {
      const message = err?.response?.data?.message || t('errors.serverError');
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="create-group-page">
      <Link to="/groups" className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="create-group-title">{t('groups.joinGroup')}</h1>

      <Card padding="md">
        <form onSubmit={handleSubmit(onSubmit)} className="create-group-form" noValidate>
          <Input
            label={t('groups.invitationCode')}
            placeholder="ABCD1234"
            required
            error={errors.invitationCode && t('errors.required')}
            {...register('invitationCode', { required: true, minLength: 6, maxLength: 20 })}
          />

          <div className="create-group-actions">
            <Link to="/groups">
              <Button variant="secondary" type="button">
                {t('common.cancel')}
              </Button>
            </Link>
            <Button type="submit" variant="primary" loading={submitting}>
              {t('groups.joinGroup')}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default JoinGroup;