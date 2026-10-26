import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import { ArrowLeft, CheckCircle2 } from 'lucide-react';
import pollsService from '../../services/pollsService';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/StatusBadge';
import './Polls.css';

const PollDetail = () => {
  const { t } = useTranslation();
  const { pollId } = useParams();
  const [submitting, setSubmitting] = useState(null);

  const poll = useApi(() => pollsService.get(pollId), [pollId]);

  const { role } = useMyGroupRole(poll.data?.groupId);
  const canClose = can(role, 'CREATE_POLL');

  if (poll.loading) return <LoadingState fullScreen />;
  if (poll.error) return <ErrorState error={poll.error} onRetry={poll.refetch} />;
  if (!poll.data) return null;

  const p = poll.data;
  const results = p.results || {};
  const total = p.totalVotes || 0;

  const handleVote = async (choice) => {
    setSubmitting(choice);
    try {
      await pollsService.vote(pollId, choice);
      toast.success(t('polls.voteRecorded'));
      poll.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(null);
    }
  };

  const handleClose = async () => {
    try {
      await pollsService.close(pollId);
      toast.success(t('common.success'));
      poll.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const getPercentage = (count) => {
    if (!total) return 0;
    return Math.round((count / total) * 100);
  };

  return (
    <div className="poll-page">
      <Link to={`/groups/${p.groupId}/polls`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <div className="poll-detail-header">
        <h1 className="page-title">{p.title}</h1>
        <StatusBadge status={p.status} />
      </div>

      {p.description && (
        <p className="poll-detail-description">{p.description}</p>
      )}

      <Card padding="md">
        <div className="poll-options-list">
          {p.options.map((option) => {
            const count = results[option] || 0;
            const pct = getPercentage(count);
            const hasVoted = p.hasVoted;

            return (
              <div key={option} className="poll-option">
                {p.status === 'ACTIVE' && !hasVoted ? (
                  <button
                    type="button"
                    className="poll-option-btn"
                    onClick={() => handleVote(option)}
                    disabled={!!submitting}
                  >
                    <span>{option}</span>
                    {submitting === option && (
                      <div className="poll-option-spinner" />
                    )}
                  </button>
                ) : (
                  <div className="poll-option-result">
                    <div className="poll-option-bar">
                      <div
                        className="poll-option-bar-fill"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                    <div className="poll-option-label">
                      <span className="poll-option-text">{option}</span>
                      <span className="poll-option-stats">
                        {count} ({pct}%)
                      </span>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <div className="poll-detail-footer">
          <div className="poll-detail-meta">
            <span>
              {t('polls.totalVotes')}: <strong>{total}</strong>
            </span>
            {p.isAnonymous && (
              <span className="poll-detail-anon">
                {t('polls.anonymousVoting')}
              </span>
            )}
          </div>

          {p.hasVoted && (
            <div className="poll-detail-voted">
              <CheckCircle2 size={14} />
              <span>{t('polls.hasVoted')}</span>
            </div>
          )}
        </div>

        {/* ✅ Only managers see the Close button */}
        {p.status === 'ACTIVE' && canClose && (
          <Button variant="secondary" fullWidth onClick={handleClose}>
            {t('polls.closePoll')}
          </Button>
        )}
      </Card>
    </div>
  );
};

export default PollDetail;