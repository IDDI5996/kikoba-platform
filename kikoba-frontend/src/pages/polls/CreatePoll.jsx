import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { ArrowLeft, Plus, X } from 'lucide-react';
import pollsService from '../../services/pollsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import './Polls.css';

const CreatePoll = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { groupId } = useParams();
  const [submitting, setSubmitting] = useState(false);
  const [options, setOptions] = useState(['', '']);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      isAnonymous: false,
    },
  });

  const updateOption = (i, value) => {
    const next = [...options];
    next[i] = value;
    setOptions(next);
  };

  const addOption = () => {
    if (options.length >= 10) return;
    setOptions([...options, '']);
  };

  const removeOption = (i) => {
    if (options.length <= 2) return;
    setOptions(options.filter((_, idx) => idx !== i));
  };

  const onSubmit = async (data) => {
    const cleaned = options.map((o) => o.trim()).filter((o) => o);
    if (cleaned.length < 2) {
      toast.error(t('polls.options'));
      return;
    }

    setSubmitting(true);
    try {
      const poll = await pollsService.create(groupId, {
        title: data.title,
        description: data.description || null,
        options: cleaned,
        isAnonymous: !!data.isAnonymous,
        allowMultipleChoices: false,
      });
      toast.success(t('common.success'));
      navigate(`/polls/${poll.id}`);
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="poll-page">
      <Link to={`/groups/${groupId}/polls`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <h1 className="page-title">{t('polls.createPoll')}</h1>

      <Card padding="md">
        <form onSubmit={handleSubmit(onSubmit)} className="poll-form" noValidate>
          <Input
            label={t('polls.pollTitle')}
            placeholder="Should we increase contributions?"
            required
            error={errors.title && t('errors.required')}
            {...register('title', { required: true, minLength: 3 })}
          />

          <div className="form-group">
            <label htmlFor="description" className="form-label">
              {t('polls.pollDescription')}
            </label>
            <textarea
              id="description"
              className="form-textarea"
              rows={3}
              {...register('description')}
            />
          </div>

          <div className="form-group">
            <label className="form-label">
              {t('polls.options')} <span className="input-required">*</span>
            </label>
            <div className="poll-options-editor">
              {options.map((option, i) => (
                <div key={i} className="poll-option-row">
                  <input
                    type="text"
                    className="form-input"
                    value={option}
                    onChange={(e) => updateOption(i, e.target.value)}
                    placeholder={`${t('polls.options')} ${i + 1}`}
                    maxLength={100}
                  />
                  {options.length > 2 && (
                    <button
                      type="button"
                      className="poll-option-remove"
                      onClick={() => removeOption(i)}
                      aria-label={t('common.delete')}
                    >
                      <X size={16} />
                    </button>
                  )}
                </div>
              ))}
              {options.length < 10 && (
                <button
                  type="button"
                  className="poll-add-option"
                  onClick={addOption}
                >
                  <Plus size={14} />
                  <span>{t('polls.addOption')}</span>
                </button>
              )}
            </div>
          </div>

          <div className="poll-form-checkbox">
            <input
              type="checkbox"
              id="isAnonymous"
              {...register('isAnonymous')}
            />
            <label htmlFor="isAnonymous">{t('polls.anonymousVoting')}</label>
          </div>

          <div className="poll-form-actions">
            <Link to={`/groups/${groupId}/polls`}>
              <Button variant="secondary" type="button">
                {t('common.cancel')}
              </Button>
            </Link>
            <Button type="submit" variant="primary" loading={submitting}>
              {t('polls.createPoll')}
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
};

export default CreatePoll;