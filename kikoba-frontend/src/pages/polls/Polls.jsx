import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Vote, Plus, ChevronRight } from 'lucide-react';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import pollsService from '../../services/pollsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/StatusBadge';
import Tabs from '../../components/common/Tabs';
import './Polls.css';

const Polls = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();
  const [tab, setTab] = useState('active');

  const { role } = useMyGroupRole(groupId);        // ← NEW
  const canCreatePoll = can(role, 'CREATE_POLL');  // ← NEW

  const all = useApi(() => pollsService.list(groupId), [groupId]);
  const active = useApi(() => pollsService.active(groupId), [groupId]);

  const list = tab === 'active' ? active.data : all.data;
  const loading = tab === 'active' ? active.loading : all.loading;
  const error = tab === 'active' ? active.error : all.error;
  const refetch = tab === 'active' ? active.refetch : all.refetch;

  const tabs = [
    { key: 'active', label: t('polls.activePolls') },
    { key: 'all', label: t('common.all') },
  ];

  return (
    <div className="polls-page">
      <div className="polls-header">
        <h1 className="page-title">{t('polls.title')}</h1>
        {/* ✅ Only managers see this button */}
        {canCreatePoll && (
          <Link to={`/groups/${groupId}/polls/create`}>
            <Button variant="primary" leftIcon={Plus}>
              {t('polls.createPoll')}
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
            icon={Vote}
            title={t('polls.title')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <div className="polls-list">
          {list.map((poll) => (
            <Link
              key={poll.id}
              to={`/polls/${poll.id}`}
              className="poll-card-link"
            >
              <Card padding="md" elevated className="poll-card">
                <div className="poll-card-header">
                  <div className="poll-card-icon">
                    <Vote size={20} aria-hidden="true" />
                  </div>
                  <StatusBadge status={poll.status} size="sm" />
                </div>

                <h3 className="poll-card-title">{poll.title}</h3>

                {poll.description && (
                  <p className="poll-card-description">{poll.description}</p>
                )}

                <div className="poll-card-footer">
                  <div className="poll-card-stats">
                    <span className="poll-stat">
                      {poll.totalVotes} {t('polls.totalVotes').toLowerCase()}
                    </span>
                    {poll.isAnonymous && (
                      <span className="poll-stat poll-stat-anon">
                        {t('polls.anonymousVoting')}
                      </span>
                    )}
                  </div>
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

export default Polls;