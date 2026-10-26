import { useApi } from './useApi';
import groupsService from '../services/groupsService';

export const useMyGroups = () =>
  useApi(() => groupsService.list(), []);

export const useGroup = (groupId) =>
  useApi(() => groupsService.get(groupId), [groupId]);

export const useGroupMembers = (groupId) =>
  useApi(() => groupsService.getMembers(groupId), [groupId]);