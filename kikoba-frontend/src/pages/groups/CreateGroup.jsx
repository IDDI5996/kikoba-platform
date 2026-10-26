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

const GROUP_TYPES = ['FAMILY', 'FRIENDS', 'VICOBA', 'SACCO', 'BUSINESS'];

const CreateGroup = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      type: 'VICOBA',
      currency: 'TZS',
      interestRate: 10,
    },
  });

  const onSubmit = async (data) => {
    setSubmitting(true);
    try {
      const group = await groupsService.create({
        name: data.name,
        type: data.type,
        description: data.description || null,
        minContribution: data.minContribution ? Number(data.minContribution) : null,
        maxContribution: data.maxContribution ? Number(data.maxContribution) : null,
        interestRate: data.interestRate ? Number(data.interestRate) : 0,
        cycleDurationMonths: data.cycleDurationMonths ? Number(data.cycleDurationMonths) : null,
        currency: data.currency || 'TZS',
      });

      toast.success(t('common.success'));
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

      <h1 className="create-group-title">{t('groups.createGroup')}</h1>

      <Card padding="md">
        <form onSubmit={handleSubmit(onSubmit)} className="create-group-form" noValidate>
          <Input
            label={t('groups.groupName')}
            placeholder="Team Pamoja"
            required
            error={errors.name && t('errors.required')}
            {...register('name', { required: true, minLength: 3, maxLength: 255 })}
          />

          <div className="form-group">
            <label htmlFor="type" className="form-label">
              {t('groups.groupType')} <span className="input-required">*</span>
            </label>
            <select
              id="type"
              className="form-select"
              {...register('type', { required: true })}
            >
              {GROUP_TYPES.map((type) => (
                <option key={type} value={type}>
                  {t(`groups.types.${type}`)}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="description" className="form-label">
              {t('groups.description')}
            </label>
            <textarea
              id="description"
              className="form-textarea"
              rows={3}
              placeholder={t('groups.description')}
              {...register('description')}
            />
          </div>

          <div className="form-grid-2">
            <Input
              label={t('groups.minContribution')}
              type="number"
              placeholder="10000"
              {...register('minContribution')}
            />
            <Input
              label={t('groups.maxContribution')}
              type="number"
              placeholder="100000"
              {...register('maxContribution')}
            />
          </div>

          <div className="form-grid-2">
            <Input
              label={t('groups.interestRate')}
              type="number"
              step="0.1"
              placeholder="10"
              {...register('interestRate')}
            />
            <Input
              label={t('groups.cycleDuration')}
              type="number"
              placeholder="12"
              {...register('cycleDurationMonths')}
            />
          </div>

          <div className="create-group-actions">
            <Link to="/groups">
              <Button variant="secondary" type="button">
                {t('common.cancel')}
              </Button>
            </Link>
            <Button
              type="submit"
              variant="primary"
              loading={submitting}
            >
              {t('groups.createGroup')}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default CreateGroup;