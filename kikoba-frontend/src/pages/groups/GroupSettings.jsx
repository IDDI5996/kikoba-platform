import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'sonner';
import {
  ArrowLeft, Crown, Wallet, Shield, User as UserIcon,
  AlertTriangle, Lock, Info,
} from 'lucide-react';
import { useGroup, useGroupMembers } from '../../hooks/useGroups';
import useMyGroupRole from '../../hooks/useMyGroupRole';
import { can } from '../../config/permissions';
import groupsService from '../../services/groupsService';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import LoadingState from '../../components/common/LoadingState';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import Modal from '../../components/common/Modal';
import './Groups.css';
import './GroupSettings.css';

const ROLE_META = {
  CHAIRPERSON: { icon: Crown, label: 'Chairperson', variant: 'primary' },
  TREASURER:   { icon: Wallet, label: 'Treasurer',   variant: 'info' },
  SECRETARY:   { icon: Shield, label: 'Secretary',   variant: 'warning' },
  MEMBER:      { icon: UserIcon, label: 'Member',    variant: 'neutral' },
};

const GroupSettings = () => {
  const { t } = useTranslation();
  const { groupId } = useParams();

  const { role } = useMyGroupRole(groupId);
  const canManage = can(role, 'UPDATE_MEMBER_ROLE');

  const group = useGroup(groupId);
  const members = useGroupMembers(groupId);

  const [changeModal, setChangeModal] = useState(null); // { member }
  const [newRole, setNewRole] = useState('');
  const [reason, setReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (group.loading || members.loading) return <LoadingState fullScreen />;
  if (group.error) return <ErrorState error={group.error} onRetry={group.refetch} />;

  const g = group.data;
  const list = members.data || [];

  const openChangeModal = (member) => {
    setChangeModal(member);
    setNewRole(member.role);
    setReason('');
  };

  const handleChangeRole = async () => {
    if (!reason || reason.trim().length < 10) {
      toast.error(t('groups.reasonTooShort') || 'Reason must be at least 10 characters');
      return;
    }

    setSubmitting(true);
    try {
      // Now passing all 4 args in the correct order
      await groupsService.updateRole(groupId, changeModal.id, newRole, reason.trim());
      toast.success(t('groups.roleUpdated') || 'Role updated');
      setChangeModal(null);
      members.refetch();
    } catch (err) {
      toast.error(err?.response?.data?.message || t('errors.serverError'));
    } finally {
      setSubmitting(false);
    }
  };

  // Get current leaders
  const leaders = {
    CHAIRPERSON: list.find((m) => m.role === 'CHAIRPERSON'),
    TREASURER: list.find((m) => m.role === 'TREASURER'),
    SECRETARY: list.find((m) => m.role === 'SECRETARY'),
  };

  return (
    <div className="group-settings-page">
      <Link to={`/groups/${groupId}`} className="back-link">
        <ArrowLeft size={16} />
        <span>{t('common.back')}</span>
      </Link>

      <div className="group-settings-header">
        <h1 className="page-title">{t('groups.settings') || 'Group Settings'}</h1>
        <p className="group-settings-subtitle">{g.name}</p>
      </div>

      {/* ---- Warning banner for managers ---- */}
      {canManage && (
        <Card padding="md" className="settings-warning-card">
          <div className="settings-warning-content">
            <AlertTriangle size={20} aria-hidden="true" />
            <div>
              <p className="settings-warning-title">
                {t('groups.transparencyWarning') || 'All changes are public'}
              </p>
              <p className="settings-warning-text">
                {t('groups.transparencyHint') ||
                  'Any role change you make will be visible to every member of this group. Every change is logged permanently.'}
              </p>
            </div>
          </div>
        </Card>
      )}

      {/* ---- Current leadership ---- */}
      <Card padding="md">
        <h2 className="settings-section-title">
          {t('groups.currentLeadership') || 'Current Leadership'}
        </h2>

        <div className="leadership-grid">
          {Object.entries(leaders).map(([roleKey, member]) => {
            const meta = ROLE_META[roleKey];
            const Icon = meta.icon;
            return (
              <div key={roleKey} className="leadership-card">
                <div className="leadership-icon">
                  <Icon size={20} aria-hidden="true" />
                </div>
                <p className="leadership-role">{t(`groups.roles.${roleKey}`)}</p>
                {member ? (
                  <p className="leadership-name">
                    {member.firstName} {member.lastName}
                  </p>
                ) : (
                  <p className="leadership-empty">
                    {t('groups.vacant') || 'Vacant'}
                  </p>
                )}
              </div>
            );
          })}
        </div>
      </Card>

      {/* ---- Members list ---- */}
      <Card padding="none">
        <div className="settings-section-header">
          <h2 className="settings-section-title">
            {t('groups.members')} ({list.length})
          </h2>
        </div>

        {list.length === 0 ? (
          <EmptyState
            icon={UserIcon}
            title={t('common.noData')}
            description={t('common.noData')}
          />
        ) : (
          <ul className="settings-members-list">
            {list.map((member) => {
              const meta = ROLE_META[member.role] || ROLE_META.MEMBER;
              const Icon = meta.icon;
              const isMe = member.userId === (group.data?.createdBy || null);

              return (
                <li key={member.id} className="settings-member-item">
                  <div className="settings-member-avatar">
                    {member.firstName?.[0]}{member.lastName?.[0]}
                  </div>
                  <div className="settings-member-info">
                    <p className="settings-member-name">
                      {member.firstName} {member.lastName}
                    </p>
                    <p className="settings-member-phone">{member.phoneNumber}</p>
                  </div>
                  <Badge variant={meta.variant} size="sm">
                    <Icon size={12} />
                    {t(`groups.roles.${member.role}`)}
                  </Badge>
                  {canManage && (
                    <button
                      type="button"
                      className="settings-change-btn"
                      onClick={() => openChangeModal(member)}
                    >
                      {t('common.edit') || 'Change'}
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </Card>

      {/* ---- Role change modal ---- */}
      <Modal
        isOpen={!!changeModal}
        onClose={() => setChangeModal(null)}
        title={
          changeModal
            ? `${t('groups.changeRole') || 'Change Role'}: ${changeModal.firstName} ${changeModal.lastName}`
            : ''
        }
        footer={
          <>
            <Button variant="secondary" onClick={() => setChangeModal(null)}>
              {t('common.cancel')}
            </Button>
            <Button variant="primary" loading={submitting} onClick={handleChangeRole}>
              {t('common.confirm')}
            </Button>
          </>
        }
      >
        <div className="role-change-form">
          <div className="form-group">
            <label className="form-label">
              {t('groups.role')} <span className="input-required">*</span>
            </label>
            <select
              className="form-select"
              value={newRole}
              onChange={(e) => setNewRole(e.target.value)}
            >
              <option value="MEMBER">{t('groups.roles.MEMBER')}</option>
              <option value="TREASURER">{t('groups.roles.TREASURER')}</option>
              <option value="SECRETARY">{t('groups.roles.SECRETARY')}</option>
            </select>
            {newRole === 'CHAIRPERSON' && (
              <p className="settings-inline-warning">
                <Lock size={14} />
                {t('groups.chairpersonTransferBlocked') ||
                  'Transferring Chairperson role requires group vote (coming soon)'}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="reason" className="form-label">
              {t('groups.changeReason') || 'Reason for change'}{' '}
              <span className="input-required">*</span>
            </label>
            <textarea
              id="reason"
              className="form-textarea"
              rows={4}
              maxLength={500}
              placeholder={t('groups.changeReasonPlaceholder') ||
                'e.g., Sarah has been elected Treasurer at the December meeting'}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
            <p className="form-hint">
              {reason.length}/500 · {t('groups.reasonMinimum') || 'Minimum 10 characters'}
            </p>
          </div>

          <div className="settings-inline-info">
            <Info size={16} />
            <span>
              {t('groups.notifyAllHint') ||
                'All ' + list.length + ' members will be notified of this change.'}
            </span>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default GroupSettings;