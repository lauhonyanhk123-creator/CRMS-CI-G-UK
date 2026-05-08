package com.crms.scheduler;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAlertScheduler {

    private final ApplicationForPaymentRepository applicationRepository;
    private final EmailService emailService;

    /** Daily at 08:00 — alert on submitted AFPs whose due date has passed */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkOverdueApplications() {
        LocalDate today = LocalDate.now();
        List<ApplicationForPayment> overdue = applicationRepository.findOverdueApplications(today);

        if (overdue.isEmpty()) {
            log.info("Payment alert check: no overdue applications for payment");
            return;
        }

        log.warn("Payment alert check: {} application(s) for payment overdue", overdue.size());

        String lines = overdue.stream()
                .map(a -> String.format("  %s — Contract: %s — Due: %s — Amount: £%,.2f",
                        a.getApplicationRef(),
                        a.getContract() != null ? a.getContract().getContractRef() : "?",
                        a.getDueDate(),
                        a.getGrossValue() != null ? a.getGrossValue() : java.math.BigDecimal.ZERO))
                .collect(Collectors.joining("\n"));

        emailService.sendAdminAlert(
                String.format("CRMS Alert: %d Application(s) for Payment Overdue", overdue.size()),
                "The following applications for payment have passed their due date without being paid:\n\n"
                        + lines + "\n\nPlease follow up with the client to progress payment.");
    }

    /** Weekly Monday at 08:00 — summary of pending applications due in the next 14 days */
    @Scheduled(cron = "0 0 8 ? * MON")
    public void weeklyPaymentDueSummary() {
        LocalDate today = LocalDate.now();
        LocalDate twoWeeks = today.plusDays(14);

        List<ApplicationForPayment> upcoming = applicationRepository
                .findCashflowRelevantByDateRange(today, twoWeeks).stream()
                .filter(a -> a.getDueDate() != null
                        && !a.getDueDate().isBefore(today)
                        && !a.getDueDate().isAfter(twoWeeks))
                .collect(Collectors.toList());

        if (upcoming.isEmpty()) {
            log.info("Weekly payment summary: no applications due in next 14 days");
            return;
        }

        java.math.BigDecimal totalExpected = upcoming.stream()
                .map(a -> a.getGrossValue() != null ? a.getGrossValue() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        String lines = upcoming.stream()
                .map(a -> String.format("  %s — Contract: %s — Due: %s — £%,.2f",
                        a.getApplicationRef(),
                        a.getContract() != null ? a.getContract().getContractRef() : "?",
                        a.getDueDate(),
                        a.getGrossValue() != null ? a.getGrossValue() : java.math.BigDecimal.ZERO))
                .collect(Collectors.joining("\n"));

        log.info("Weekly payment summary: {} application(s) due in next 14 days, total £{}",
                upcoming.size(), totalExpected);

        emailService.sendAdminAlert(
                String.format("CRMS Weekly: %d Payment(s) Due in Next 14 Days (£%,.2f)", upcoming.size(), totalExpected),
                "Payments due in the next 14 days:\n\n"
                        + lines + "\n\nTotal expected: £" + String.format("%,.2f", totalExpected));
    }
}
