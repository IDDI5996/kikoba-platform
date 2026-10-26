package com.kikoba.kikoba_backend.loan.mapper;

import com.kikoba.kikoba_backend.loan.dto.LoanRepaymentResponse;
import com.kikoba.kikoba_backend.loan.dto.LoanResponse;
import com.kikoba.kikoba_backend.loan.entity.Loan;
import com.kikoba.kikoba_backend.loan.entity.LoanRepayment;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanResponse toResponse(Loan loan) {
        if (loan == null) return null;

        var member = loan.getGroupMember();
        var user = member.getUser();
        var group = member.getGroup();

        String approvedByName = null;
        if (loan.getApprovedBy() != null) {
            approvedByName = loan.getApprovedBy().getFirstName() + " " + loan.getApprovedBy().getLastName();
        }

        return LoanResponse.builder()
                .id(loan.getId())
                .memberId(member.getId())
                .userId(user.getId())
                .memberName(user.getFirstName() + " " + user.getLastName())
                .groupId(group.getId())
                .groupName(group.getName())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .totalRepayable(loan.getTotalRepayable())
                .balance(loan.getBalance())
                .purpose(loan.getPurpose())
                .status(loan.getStatus())
                .approvedBy(loan.getApprovedBy() != null ? loan.getApprovedBy().getId() : null)
                .approvedByName(approvedByName)
                .approvedAt(loan.getApprovedAt())
                .disbursedBy(loan.getDisbursedBy() != null ? loan.getDisbursedBy().getId() : null)
                .disbursedAt(loan.getDisbursedAt())
                .repaymentPeriodMonths(loan.getRepaymentPeriodMonths())
                .repaymentFrequency(loan.getRepaymentFrequency())
                .firstDueDate(loan.getFirstDueDate())
                .rejectionReason(loan.getRejectionReason())
                .collateralDescription(loan.getCollateralDescription())
                .closedAt(loan.getClosedAt())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    public LoanRepaymentResponse toRepaymentResponse(LoanRepayment repayment) {
        if (repayment == null) return null;

        return LoanRepaymentResponse.builder()
                .id(repayment.getId())
                .loanId(repayment.getLoan().getId())
                .installmentNumber(repayment.getInstallmentNumber())
                .dueDate(repayment.getDueDate())
                .amountDue(repayment.getAmountDue())
                .principalDue(repayment.getPrincipalDue())
                .interestDue(repayment.getInterestDue())
                .amountPaid(repayment.getAmountPaid())
                .principalPaid(repayment.getPrincipalPaid())
                .interestPaid(repayment.getInterestPaid())
                .penaltyPaid(repayment.getPenaltyPaid())
                .status(repayment.getStatus())
                .paymentMethod(repayment.getPaymentMethod())
                .transactionReference(repayment.getTransactionReference())
                .paymentDate(repayment.getPaymentDate())
                .confirmedAt(repayment.getConfirmedAt())
                .notes(repayment.getNotes())
                .build();
    }
}