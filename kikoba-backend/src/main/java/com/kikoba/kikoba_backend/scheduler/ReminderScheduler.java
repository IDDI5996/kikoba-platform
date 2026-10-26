package com.kikoba.kikoba_backend.scheduler;

import com.kikoba.kikoba_backend.governance.entity.Meeting;
import com.kikoba.kikoba_backend.governance.enums.MeetingStatus;
import com.kikoba.kikoba_backend.governance.repository.MeetingAttendanceRepository;
import com.kikoba.kikoba_backend.governance.repository.MeetingRepository;
import com.kikoba.kikoba_backend.group.entity.GroupMember;
import com.kikoba.kikoba_backend.group.repository.GroupMemberRepository;
import com.kikoba.kikoba_backend.loan.entity.Loan;
import com.kikoba.kikoba_backend.loan.enums.LoanStatus;
import com.kikoba.kikoba_backend.loan.repository.LoanRepaymentRepository;
import com.kikoba.kikoba_backend.loan.repository.LoanRepository;
import com.kikoba.kikoba_backend.notification.enums.NotificationPriority;
import com.kikoba.kikoba_backend.notification.enums.NotificationType;
import com.kikoba.kikoba_backend.notification.service.NotificationService;
import com.kikoba.kikoba_backend.auth.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final MeetingRepository meetingRepository;
    private final MeetingAttendanceRepository attendanceRepository;
    private final LoanRepository loanRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final NotificationService notificationService;
    private final OtpService otpService;

    /**
     * Meeting reminder: runs hourly, notifies members of meetings starting in the next 24 hours.
     */
    @Scheduled(cron = "0 0 * * * *")  // Every hour on the hour
    @Transactional(readOnly = true)
    public void sendMeetingReminders() {
        log.info("[SCHEDULER] Running meeting reminders...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        List<Meeting> upcoming = meetingRepository.findAll().stream()
                .filter(m -> m.getStatus() == MeetingStatus.SCHEDULED)
                .filter(m -> m.getScheduledAt().isAfter(now) && m.getScheduledAt().isBefore(in24Hours))
                .toList();

        for (Meeting meeting : upcoming) {
            List<GroupMember> members = groupMemberRepository.findByGroupIdAndIsActiveTrue(meeting.getGroup().getId());
            for (GroupMember member : members) {
                notificationService.notifyUserAsync(
                        member.getUser().getId(),
                        NotificationType.MEETING_REMINDER,
                        "Reminder: " + meeting.getTitle(),
                        "Meeting starts at " + meeting.getScheduledAt() + ".",
                        "/meetings/" + meeting.getId(),
                        "MeetingReminder",
                        meeting.getId(),
                        NotificationPriority.HIGH
                );
            }
        }

        log.info("[SCHEDULER] Sent reminders for {} meetings", upcoming.size());
    }

    /**
     * Loan payment reminder: runs daily at 8 AM, notifies borrowers of payments due in 3 days.
     */
    @Scheduled(cron = "0 0 8 * * *")  // Daily at 8 AM
    @Transactional(readOnly = true)
    public void sendLoanPaymentReminders() {
        log.info("[SCHEDULER] Running loan payment reminders...");

        LocalDate targetDate = LocalDate.now().plusDays(3);

        List<Loan> activeLoans = loanRepository.findActiveLoans();
        int notified = 0;

        for (Loan loan : activeLoans) {
            var nextInstallment = loanRepaymentRepository.findNextUnpaidInstallment(loan.getId());
            if (nextInstallment.isEmpty()) continue;

            var installment = nextInstallment.get();
            if (installment.getDueDate().equals(targetDate)) {
                notificationService.notifyUserAsync(
                        loan.getGroupMember().getUser().getId(),
                        NotificationType.LOAN_PAYMENT_DUE,
                        "Loan Payment Due in 3 Days",
                        "Your next loan payment of " + installment.getAmountDue()
                                + " TZS is due on " + installment.getDueDate() + ".",
                        "/loans/" + loan.getId(),
                        "LoanRepayment",
                        installment.getId(),
                        NotificationPriority.HIGH
                );
                notified++;
            }
        }

        log.info("[SCHEDULER] Sent {} loan payment reminders", notified);
    }

    /**
     * Overdue loan detection: runs daily at 9 AM, flags overdue loans and penalizes.
     */
    @Scheduled(cron = "0 0 9 * * *")  // Daily at 9 AM
    @Transactional
    public void flagOverdueLoans() {
        log.info("[SCHEDULER] Checking for overdue loans...");

        LocalDate today = LocalDate.now();
        List<com.kikoba.kikoba_backend.loan.entity.LoanRepayment> overdue =
                loanRepaymentRepository.findOverdueRepayments(today);

        for (var repayment : overdue) {
            repayment.setStatus(com.kikoba.kikoba_backend.loan.enums.RepaymentStatus.OVERDUE);

            long daysOverdue = ChronoUnit.DAYS.between(repayment.getDueDate(), today);

            notificationService.notifyUserAsync(
                    repayment.getLoan().getGroupMember().getUser().getId(),
                    NotificationType.LOAN_OVERDUE,
                    "Loan Payment Overdue",
                    "Your loan payment of " + repayment.getAmountDue() + " TZS is "
                            + daysOverdue + " days overdue. Please pay immediately.",
                    "/loans/" + repayment.getLoan().getId(),
                    "LoanRepayment",
                    repayment.getId(),
                    NotificationPriority.URGENT
            );
        }

        loanRepaymentRepository.saveAll(overdue);
        log.info("[SCHEDULER] Flagged {} overdue repayments", overdue.size());
    }

    /**
     * Notification cleanup: runs daily at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredNotifications() {
        log.info("[SCHEDULER] Cleaning up expired notifications...");
        int count = notificationService.cleanupExpiredNotifications();
        log.info("[SCHEDULER] Marked {} notifications as expired", count);
    }

    /**
     * Notification retry: runs every 30 minutes.
     */
    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void retryFailedNotifications() {
        log.info("[SCHEDULER] Retrying failed notifications...");
        int count = notificationService.retryFailedNotifications();
        log.info("[SCHEDULER] Retried {} failed notifications", count);
    }

    @Scheduled(cron = "0 0 */2 * * *")   // Every 2 hours
    @Transactional
    public void cleanupExpiredOtps() {
        otpService.cleanupExpired();
    }
}