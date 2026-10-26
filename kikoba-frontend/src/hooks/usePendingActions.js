import { useApi } from './useApi';
import contributionsService from '../services/contributionsService';
import loansService from '../services/loansService';
import meetingsService from '../services/meetingsService';

/**
 * Fetches pending items that need manager attention.
 * Only called when the user is a manager in the group.
 */
export const usePendingActions = (groupId, isManager) => {
  const contributions = useApi(
    () => (isManager ? contributionsService.groupPending(groupId) : Promise.resolve([])),
    [groupId, isManager],
    { immediate: !!groupId && !!isManager }
  );

  const loans = useApi(
    () => (isManager ? loansService.groupPending(groupId) : Promise.resolve([])),
    [groupId, isManager],
    { immediate: !!groupId && !!isManager }
  );

  const upcomingMeetings = useApi(
    () => (groupId ? meetingsService.upcoming(groupId) : Promise.resolve([])),
    [groupId],
    { immediate: !!groupId }
  );

  return {
    pendingContributions: contributions.data || [],
    pendingLoans: loans.data || [],
    upcomingMeetings: upcomingMeetings.data || [],
    loading: contributions.loading || loans.loading || upcomingMeetings.loading,
    refetch: () => {
      contributions.refetch().catch(() => {});
      loans.refetch().catch(() => {});
      upcomingMeetings.refetch().catch(() => {});
    },
  };
};

export default usePendingActions;