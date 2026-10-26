import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Users, Vote, ChevronRight } from 'lucide-react';
import { useMyGroups } from '../../hooks/useGroups';
import Card from '../../components/common/Card';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import Badge from '../../components/common/Badge';
import '../../pages/groups/Groups.css';

const PollsHub = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { data: groups, loading, error, refetch } = useMyGroups();

  useEffect(() => {
    if (!loading && groups && groups.length === 1) {
      navigate(`/groups/${groups[0].id}/polls`, { replace: true });
    }
  }, [groups, loading, navigate]);

  if (loading) return <LoadingState fullScreen />;
  if (error) return <ErrorState error={error} onRetry={refetch} />;

  if (!groups || groups.length === 0) {
    return (
      <div className="groups-page">
        <h1 className="page-title">{t('polls.title')}</h1>
        <Card padding="none">
          <EmptyState
            icon={Users}
            title={t('groups.noGroups')}
            description={t('groups.noGroupsHint')}
          />
        </Card>
      </div>
    );
  }

  return (
    <div className="groups-page">
      <h1 className="page-title">{t('polls.title')}</h1>
      <p className="groups-subtitle">{t('groups.title')}</p>

      <div className="groups-grid">
        {groups.map((group) => (
          <Link
            key={group.id}
            to={`/groups/${group.id}/polls`}
            className="group-card-link"
          >
            <Card padding="md" className="group-card" elevated>
              <div className="group-card-header">
                <div className="group-card-icon">
                  <Vote size={20} aria-hidden="true" />
                </div>
                <Badge variant="primary" size="sm">
                  {t(`groups.types.${group.type}`)}
                </Badge>
              </div>
              <h3 className="group-card-name">{group.name}</h3>
              <div className="group-card-footer">
                <span className="group-card-meta">
                  {t('dashboard.memberCount', { count: group.memberCount })}
                </span>
                <ChevronRight size={16} />
              </div>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default PollsHub;