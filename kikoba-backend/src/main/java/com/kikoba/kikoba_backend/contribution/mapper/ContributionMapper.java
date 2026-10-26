package com.kikoba.kikoba_backend.contribution.mapper;

import com.kikoba.kikoba_backend.contribution.dto.ContributionResponse;
import com.kikoba.kikoba_backend.contribution.entity.Contribution;
import org.springframework.stereotype.Component;

@Component
public class ContributionMapper {

    public ContributionResponse toResponse(Contribution contribution) {
        if (contribution == null) return null;

        var member = contribution.getGroupMember();
        var user = member.getUser();
        var group = member.getGroup();

        return ContributionResponse.builder()
                .id(contribution.getId())
                .memberId(member.getId())
                .userId(user.getId())
                .memberName(user.getFirstName() + " " + user.getLastName())
                .groupId(group.getId())
                .groupName(group.getName())
                .amount(contribution.getAmount())
                .paymentMethod(contribution.getPaymentMethod())
                .status(contribution.getStatus())
                .transactionReference(contribution.getTransactionReference())
                .contributionPeriod(contribution.getContributionPeriod())
                .notes(contribution.getNotes())
                .receiptUrl(contribution.getReceiptUrl())
                .confirmedAt(contribution.getConfirmedAt())
                .createdAt(contribution.getCreatedAt())
                .build();
    }
}