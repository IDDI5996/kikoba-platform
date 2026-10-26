import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Users, Plus, UserPlus, ChevronRight } from 'lucide-react';
import { useMyGroups } from '../../hooks/useGroups';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import Badge from '../../components/common/Badge';
import './Groups.css';

const Groups = () => {
  const { t } = useTranslation();
  const { data: groups, loading, error, refetch } = useMyGroups();

  if (loading) return <LoadingState fullScreen />;
  if (error) return <ErrorState error={error} onRetry={refetch} />;

  return (
    <div className="groups-page">
      <header className="groups-header">
        <div>
          <h1 className="groups-title">{t('groups.title')}</h1>
          <p className="groups-subtitle">
            {groups?.length || 0} {t('groups.members').toLowerCase()}
          </p>
        </div>
        <div className="groups-header-actions">
          <Link to="/groups/join">
            <Button variant="secondary" leftIcon={UserPlus}>
              {t('groups.joinGroup')}
            </Button>
          </Link>
          <Link to="/groups/create">
            <Button variant="primary" leftIcon={Plus}>
              {t('groups.createGroup')}
            </Button>
          </Link>
        </div>
      </header>

      {!groups || groups.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={Users}
            title={t('groups.noGroups')}
            description={t('groups.noGroupsHint')}
          />
        </Card>
      ) : (
        <div className="groups-grid">
          {groups.map((group) => (
            <Link
              key={group.id}
              to={`/groups/${group.id}`}
              className="group-card-link"
            >
              <Card padding="md" className="group-card" elevated>
                <div className="group-card-header">
                  <div className="group-card-icon">
                    <Users size={20} aria-hidden="true" />
                  </div>
                  <Badge variant="primary" size="sm">
                    {t(`groups.types.${group.type}`)}
                  </Badge>
                </div>
                <h3 className="group-card-name">{group.name}</h3>
                {group.description && (
                  <p className="group-card-description">{group.description}</p>
                )}
                <div className="group-card-footer">
                  <span className="group-card-meta">
                    {t('dashboard.memberCount', { count: group.memberCount })}
                  </span>
                  <ChevronRight size={16} aria-hidden="true" />
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default Groups;