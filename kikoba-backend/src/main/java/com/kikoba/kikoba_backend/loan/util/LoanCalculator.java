package com.kikoba.kikoba_backend.loan.util;

import com.kikoba.kikoba_backend.loan.entity.LoanRepayment;
import com.kikoba.kikoba_backend.loan.enums.RepaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class LoanCalculator {

    /**
     * Calculate total repayable using SIMPLE interest.
     * Formula: Total = Principal + (Principal × Rate% × Months / 12)
     */
    public BigDecimal calculateTotalRepayable(BigDecimal principal, BigDecimal annualRatePercent, int months) {
        if (principal == null || principal.signum() <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        if (months <= 0) {
            throw new IllegalArgumentException("Months must be positive");
        }

        BigDecimal rate = annualRatePercent != null ? annualRatePercent : BigDecimal.ZERO;
        // Simple interest = principal * rate/100 * months/12
        BigDecimal interest = principal
                .multiply(rate)
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);

        return principal.add(interest).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generate amortization schedule with EQUAL monthly installments.
     * (Simple interest distributed evenly)
     */
    public List<LoanRepayment> generateSchedule(
            com.kikoba.kikoba_backend.loan.entity.Loan loan,
            LocalDate startDate,
            BigDecimal monthlyPenaltyRate
    ) {
        int months = loan.getRepaymentPeriodMonths();
        BigDecimal totalRepayable = loan.getTotalRepayable();
        BigDecimal principal = loan.getAmount();
        BigDecimal totalInterest = totalRepayable.subtract(principal);

        BigDecimal installmentAmount = totalRepayable.divide(
                BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        BigDecimal principalPerInstallment = principal.divide(
                BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        BigDecimal interestPerInstallment = totalInterest.divide(
                BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

        List<LoanRepayment> schedule = new ArrayList<>();
        BigDecimal accumulatedPrincipal = BigDecimal.ZERO;
        BigDecimal accumulatedInterest = BigDecimal.ZERO;

        for (int i = 1; i <= months; i++) {
            BigDecimal principalDue;
            BigDecimal interestDue;
            BigDecimal amountDue;

            if (i == months) {
                // Last installment — adjust for rounding
                principalDue = principal.subtract(accumulatedPrincipal);
                interestDue = totalInterest.subtract(accumulatedInterest);
                amountDue = totalRepayable.subtract(
                        installmentAmount.multiply(BigDecimal.valueOf(months - 1)));
            } else {
                principalDue = principalPerInstallment;
                interestDue = interestPerInstallment;
                amountDue = installmentAmount;
                accumulatedPrincipal = accumulatedPrincipal.add(principalDue);
                accumulatedInterest = accumulatedInterest.add(interestDue);
            }

            LoanRepayment repayment = LoanRepayment.builder()
                    .loan(loan)
                    .installmentNumber(i)
                    .dueDate(startDate.plusMonths(i))
                    .amountDue(amountDue)
                    .principalDue(principalDue)
                    .interestDue(interestDue)
                    .amountPaid(BigDecimal.ZERO)
                    .principalPaid(BigDecimal.ZERO)
                    .interestPaid(BigDecimal.ZERO)
                    .penaltyPaid(BigDecimal.ZERO)
                    .status(RepaymentStatus.PENDING)
                    .build();

            schedule.add(repayment);
        }

        return schedule;
    }

    /**
     * Calculate penalty for overdue installment.
     * Penalty = amountDue × monthlyPenaltyRate% × (daysOverdue / 30)
     */
    public BigDecimal calculatePenalty(BigDecimal amountDue, BigDecimal monthlyRatePercent, long daysOverdue) {
        if (daysOverdue <= 0 || amountDue == null || monthlyRatePercent == null) {
            return BigDecimal.ZERO;
        }

        return amountDue
                .multiply(monthlyRatePercent)
                .multiply(BigDecimal.valueOf(daysOverdue))
                .divide(BigDecimal.valueOf(3000), 2, RoundingMode.HALF_UP);
    }
}