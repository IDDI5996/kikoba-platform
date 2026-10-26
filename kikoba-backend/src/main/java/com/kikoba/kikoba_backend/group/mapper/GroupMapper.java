package com.kikoba.kikoba_backend.group.mapper;

import com.kikoba.kikoba_backend.group.dto.GroupMemberResponse;
import com.kikoba.kikoba_backend.group.dto.GroupResponse;
import com.kikoba.kikoba_backend.group.entity.Group;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupResponse toResponse(Group group) {
        return toResponse(group, 0);
    }

    public GroupResponse toResponse(Group group, long memberCount) {
        if (group == null) return null;

        String createdByName = null;
        if (group.getCreatedBy() != null) {
            createdByName = group.getCreatedBy().getFirstName() + " " + group.getCreatedBy().getLastName();
        }

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .type(group.getType())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy() != null ? group.getCreatedBy().getId() : null)
                .createdByName(createdByName)
                .invitationCode(group.getInvitationCode())
                .meetingFrequency(group.getMeetingFrequency())
                .meetingDay(group.getMeetingDay())
                .meetingTime(group.getMeetingTime())
                .minContribution(group.getMinContribution())
                .maxContribution(group.getMaxContribution())
                .interestRate(group.getInterestRate())
                .cycleDurationMonths(group.getCycleDurationMonths())
                .currency(group.getCurrency())
                .isActive(group.getIsActive())
                .memberCount(memberCount)
                .createdAt(group.getCreatedAt())
                .build();
    }

    public GroupMemberResponse toMemberResponse(GroupMember member) {
        if (member == null) return null;

        return GroupMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .firstName(member.getUser().getFirstName())
                .lastName(member.getUser().getLastName())
                .phoneNumber(member.getUser().getPhoneNumber())
                .email(member.getUser().getEmail())
                .role(member.getRole())
                .totalContributed(member.getTotalContributed())
                .totalLoanBalance(member.getTotalLoanBalance())
                .shareBalance(member.getShareBalance())
                .joinedAt(member.getJoinedAt())
                .isActive(member.getIsActive())
                .build();
    }

    public GroupResponse toResponse(Group group, long memberCount, String myRole) {
        GroupResponse response = toResponse(group, memberCount);
        if (response != null) {
            response.setMyRole(myRole);
        }
        return response;
    }
}