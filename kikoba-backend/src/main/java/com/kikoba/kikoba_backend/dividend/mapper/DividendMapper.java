package com.kikoba.kikoba_backend.dividend.mapper;

import com.kikoba.kikoba_backend.dividend.dto.DividendPayoutResponse;
import com.kikoba.kikoba_backend.dividend.dto.DividendResponse;
import com.kikoba.kikoba_backend.dividend.entity.Dividend;
import com.kikoba.kikoba_backend.dividend.entity.DividendPayout;
import org.springframework.stereotype.Component;

@Component
public class DividendMapper {

    public DividendResponse toResponse(Dividend dividend) {
        if (dividend == null) return null;

        String calculatedByName = null;
        if (dividend.getCalculatedBy() != null) {
            calculatedByName = dividend.getCalculatedBy().getFirstName() + " "
                    + dividend.getCalculatedBy().getLastName();
        }

        String approvedByName = null;
        if (dividend.getApprovedBy() != null) {
            approvedByName = dividend.getApprovedBy().getFirstName() + " "
                    + dividend.getApprovedBy().getLastName();
        }

        return DividendResponse.builder()
                .id(dividend.getId())
                .groupId(dividend.getGroup().getId())
                .groupName(dividend.getGroup().getName())
                .cycleNumber(dividend.getCycleNumber())
                .totalInterestEarned(dividend.getTotalInterestEarned())
                .totalPenaltiesCollected(dividend.getTotalPenaltiesCollected())
                .totalExpenses(dividend.getTotalExpenses())
                .totalProfit(dividend.getTotalProfit())
                .totalShares(dividend.getTotalShares())
                .dividendPerShare(dividend.getDividendPerShare())
                .status(dividend.getStatus())
                .calculatedBy(dividend.getCalculatedBy() != null ? dividend.getCalculatedBy().getId() : null)
                .calculatedByName(calculatedByName)
                .calculatedAt(dividend.getCalculatedAt())
                .approvedBy(dividend.getApprovedBy() != null ? dividend.getApprovedBy().getId() : null)
                .approvedByName(approvedByName)
                .approvedAt(dividend.getApprovedAt())
                .distributedBy(dividend.getDistributedBy() != null ? dividend.getDistributedBy().getId() : null)
                .distributedAt(dividend.getDistributedAt())
                .notes(dividend.getNotes())
                .createdAt(dividend.getCreatedAt())
                .build();
    }

    public DividendPayoutResponse toPayoutResponse(DividendPayout payout) {
        if (payout == null) return null;

        var member = payout.getGroupMember();
        var user = member.getUser();

        return DividendPayoutResponse.builder()
                .id(payout.getId())
                .dividendId(payout.getDividend().getId())
                .cycleNumber(payout.getDividend().getCycleNumber())
                .memberId(member.getId())
                .userId(user.getId())
                .memberName(user.getFirstName() + " " + user.getLastName())
                .sharesHeld(payout.getSharesHeld())
                .amount(payout.getAmount())
                .status(payout.getStatus())
                .paidAt(payout.getPaidAt())
                .paymentReference(payout.getPaymentReference())
                .notes(payout.getNotes())
                .createdAt(payout.getCreatedAt())
                .build();
    }
}