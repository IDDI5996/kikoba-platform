import { useMemo } from 'react';
import { useApi } from './useApi';
import groupsService from '../services/groupsService';
import { useAuth } from '../context/AuthContext';

/**
 * Returns the current user's role within a specific group.
 *
 * Returns: { role, isManager, isChairperson, isTreasurer, isSecretary, loading, error }
 *
 * - role: 'MEMBER' | 'TREASURER' | 'CHAIRPERSON' | 'SECRETARY' | null
 * - isManager: true for CHAIRPERSON, TREASURER, SECRETARY
 */
export const useMyGroupRole = (groupId) => {
  const { user } = useAuth();
  const { data: members, loading, error, refetch } = useApi(
    () => (groupId ? groupsService.getMembers(groupId) : Promise.resolve([])),
    [groupId],
    { immediate: !!groupId }
  );

  const role = useMemo(() => {
    if (!members || !user) return null;
    const me = members.find((m) => m.userId === user.id);
    return me?.role || null;
  }, [members, user]);

  return {
    role,
    isChairperson: role === 'CHAIRPERSON',
    isTreasurer: role === 'TREASURER',
    isSecretary: role === 'SECRETARY',
    isManager: role === 'CHAIRPERSON' || role === 'TREASURER' || role === 'SECRETARY',
    isMember: role === 'MEMBER',
    loading,
    error,
    refetch,
  };
};

export default useMyGroupRole;