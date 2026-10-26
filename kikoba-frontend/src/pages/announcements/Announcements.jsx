import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Megaphone, Plus, Pin, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import useApi from '../../hooks/useApi';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import announcementsService from '../../services/announcementsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import EmptyState from '../../components/common/EmptyState';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import './Announcements.css';

const Announcements = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();

  const { role } = useMyGroupRole(groupId);
  const canCreate = can(role, 'CREATE_ANNOUNCEMENT');
  const canDelete = can(role, 'DELETE_ANNOUNCEMENT');
  const canPin = can(role, 'CREATE_ANNOUNCEMENT'); // same as create

  const list = useApi(() => announcementsService.list(groupId), [groupId]);

  const handleTogglePin = async (id) => {
    try {
      await announcementsService.togglePin(id);
      toast.success(t('common.success'));
      list.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('common.confirm'))) return;
    try {
      await announcementsService.delete(id);
      toast.success(t('common.success'));
      list.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    }
  };

  if (list.loading) return <LoadingState fullScreen />;
  if (list.error) return <ErrorState error={list.error} onRetry={list.refetch} />;

  return (
    <div className="announcements-page">
      <div className="announcements-header">
        <h1 className="page-title">{t('announcements.title')}</h1>

        {/* ✅ Only managers see the Create button */}
        {canCreate && (
          <Link to={`/groups/${groupId}/announcements/create`}>
            <Button variant="primary" leftIcon={Plus}>
              {t('announcements.create')}
            </Button>
          </Link>
        )}
      </div>

      {!list.data || list.data.length === 0 ? (
        <Card padding="none">
          <EmptyState
            icon={Megaphone}
            title={t('announcements.title')}
            description={t('common.noData')}
          />
        </Card>
      ) : (
        <div className="announcements-list">
          {list.data.map((a) => (
            <Card
              key={a.id}
              padding="md"
              className={`announcement-card ${a.pinned ? 'pinned' : ''}`}
            >
              <div className="announcement-header">
                <div className="announcement-badges">
                  {a.pinned && (
                    <Badge variant="warning" size="sm">
                      <Pin size={10} /> {t('announcements.pinned')}
                    </Badge>
                  )}
                  <Badge variant="primary" size="sm">
                    {t(`announcements.categories.${a.category}`)}
                  </Badge>
                </div>

                {/* ✅ Only managers see the actions */}
                {canCreate && (
                  <div className="announcement-actions">
                    <button
                      type="button"
                      className="announcement-action"
                      onClick={() => handleTogglePin(a.id)}
                      aria-label={t('announcements.pin')}
                    >
                      <Pin size={14} />
                    </button>

                    {/* ✅ Only Chairperson can delete */}
                    {canDelete && (
                      <button
                        type="button"
                        className="announcement-action danger"
                        onClick={() => handleDelete(a.id)}
                        aria-label={t('common.delete')}
                      >
                        <Trash2 size={14} />
                      </button>
                    )}
                  </div>
                )}
              </div>

              <h2 className="announcement-title">{a.title}</h2>
              <p className="announcement-body">{a.body}</p>

              <div className="announcement-footer">
                <span className="announcement-author">{a.createdByName}</span>
                <span className="announcement-time">
                  {new Date(a.createdAt).toLocaleDateString('sw-TZ', {
                    dateStyle: 'medium',
                  })}
                </span>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

export default Announcements;