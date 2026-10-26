package com.kikoba.kikoba_backend.share.mapper;

import com.kikoba.kikoba_backend.share.dto.ShareResponse;
import com.kikoba.kikoba_backend.share.entity.Share;
import org.springframework.stereotype.Component;

@Component
public class ShareMapper {

    public ShareResponse toResponse(Share share) {
        if (share == null) return null;

        var member = share.getGroupMember();
        var user = member.getUser();
        var group = member.getGroup();

        return ShareResponse.builder()
                .id(share.getId())
                .groupMemberId(member.getId())
                .userId(user.getId())
                .memberName(user.getFirstName() + " " + user.getLastName())
                .groupId(group.getId())
                .groupName(group.getName())
                .numberOfShares(share.getNumberOfShares())
                .pricePerShare(share.getPricePerShare())
                .totalAmount(share.getTotalAmount())
                .transactionType(share.getTransactionType())
                .transactionReference(share.getTransactionReference())
                .notes(share.getNotes())
                .confirmedAt(share.getConfirmedAt())
                .createdAt(share.getCreatedAt())
                .build();
    }
}