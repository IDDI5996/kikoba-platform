/**
 * Central permissions definition.
 * Each entry maps an action to the roles allowed to perform it.
 */
export const PERMISSIONS = {
  // Group management
  CREATE_MEETING: ['CHAIRPERSON', 'SECRETARY'],
  CREATE_POLL: ['CHAIRPERSON', 'SECRETARY'],
  CREATE_ANNOUNCEMENT: ['CHAIRPERSON', 'SECRETARY'],
  DELETE_ANNOUNCEMENT: ['CHAIRPERSON'],
  UPDATE_MEMBER_ROLE: ['CHAIRPERSON'],
  REMOVE_MEMBER: ['CHAIRPERSON', 'TREASURER'],
  DEACTIVATE_GROUP: ['CHAIRPERSON'],

  // Financial
  CONFIRM_CONTRIBUTION: ['CHAIRPERSON', 'TREASURER'],
  APPROVE_LOAN: ['CHAIRPERSON', 'TREASURER'],
  REJECT_LOAN: ['CHAIRPERSON', 'TREASURER'],
  DISBURSE_LOAN: ['CHAIRPERSON', 'TREASURER'],
  CONFIRM_REPAYMENT: ['CHAIRPERSON', 'TREASURER'],
  CALCULATE_DIVIDEND: ['CHAIRPERSON', 'TREASURER'],
  APPROVE_DIVIDEND: ['CHAIRPERSON', 'TREASURER'],
  DISTRIBUTE_DIVIDEND: ['CHAIRPERSON', 'TREASURER'],
  CANCEL_DIVIDEND: ['CHAIRPERSON'],
  CONFIRM_PAYOUT: ['CHAIRPERSON', 'TREASURER'],

  // Meeting controls (during ongoing meeting)
  START_MEETING: ['CHAIRPERSON', 'SECRETARY'],
  COMPLETE_MEETING: ['CHAIRPERSON', 'SECRETARY'],
  CANCEL_MEETING: ['CHAIRPERSON', 'SECRETARY'],
  UPDATE_MEETING_MINUTES: ['CHAIRPERSON', 'SECRETARY'],
  UPDATE_ATTENDANCE: ['CHAIRPERSON', 'SECRETARY'],
};

/**
 * Check if a role is allowed to perform an action.
 */
export const can = (role, action) => {
  if (!role) return false;
  const allowed = PERMISSIONS[action];
  if (!allowed) return false;
  return allowed.includes(role);
};

/**
 * Build an object with boolean flags for all permissions given a role.
 */
export const permissionsFor = (role) => {
  const result = {};
  for (const [action, roles] of Object.entries(PERMISSIONS)) {
    result[action] = roles.includes(role);
  }
  return result;
};