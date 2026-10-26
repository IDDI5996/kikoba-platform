package com.kikoba.kikoba_backend.dividend.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DividendCalculator {

    /**
     * Calculate total profit.
     * Formula: Profit = Interest Earned + Penalties Collected − Expenses
     */
    public BigDecimal calculateTotalProfit(
            BigDecimal interestEarned,
            BigDecimal penaltiesCollected,
            BigDecimal expenses
    ) {
        BigDecimal interest = interestEarned != null ? interestEarned : BigDecimal.ZERO;
        BigDecimal penalties = penaltiesCollected != null ? penaltiesCollected : BigDecimal.ZERO;
        BigDecimal exp = expenses != null ? expenses : BigDecimal.ZERO;

        BigDecimal profit = interest.add(penalties).subtract(exp);
        return profit.max(BigDecimal.ZERO);
    }

    /**
     * Calculate dividend per share.
     * Formula: Dividend Per Share = Total Profit / Total Shares
     * Stored with scale 4 for precision.
     */
    public BigDecimal calculateDividendPerShare(BigDecimal totalProfit, Integer totalShares) {
        if (totalShares == null || totalShares <= 0) {
            throw new IllegalArgumentException("Total shares must be positive");
        }

        return totalProfit
                .divide(BigDecimal.valueOf(totalShares), 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculate a member's payout.
     * Formula: Payout = Member Shares × Dividend Per Share
     * Rounded to 2 decimals for money.
     */
    public BigDecimal calculateMemberPayout(Integer memberShares, BigDecimal dividendPerShare) {
        if (memberShares == null || memberShares <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(memberShares)
                .multiply(dividendPerShare)
                .setScale(2, RoundingMode.HALF_UP);
    }
}