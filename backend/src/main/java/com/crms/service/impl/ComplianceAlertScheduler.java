package com.crms.service.impl;

import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.QualificationRepository;
import com.crms.domain.plant.entity.LOLERExamination;
import com.crms.domain.plant.repository.LOLERExaminationRepository;
import com.crms.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Daily compliance summary email — cards expiring in 30 days,
 * qualifications expiring in 30 days, and LOLER exams due in 14 days.
 */
@Component
@Slf4j
public class ComplianceAlertScheduler {

    private final CardRepository cardRepository;
    private final QualificationRepository qualificationRepository;
    private final LOLERExaminationRepository lolerRepository;
    private final EmailService emailService;
    private final boolean smtpEnabled;

    public ComplianceAlertScheduler(
            CardRepository cardRepository,
            QualificationRepository qualificationRepository,
            LOLERExaminationRepository lolerRepository,
            EmailService emailService,
            @Value("${spring.mail.host:disabled}") String mailHost) {
        this.cardRepository = cardRepository;
        this.qualificationRepository = qualificationRepository;
        this.lolerRepository = lolerRepository;
        this.emailService = emailService;
        this.smtpEnabled = !"disabled".equalsIgnoreCase(mailHost) && !mailHost.isBlank();
    }

    /** Run every day at 07:00 */
    @Scheduled(cron = "0 0 7 * * *")
    public void sendDailyComplianceAlerts() {
        if (!smtpEnabled) {
            log.debug("ComplianceAlertScheduler: SMTP disabled, skipping daily compliance email");
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        LocalDate in14Days = today.plusDays(14);

        // Cards expiring within 30 days
        List<Card> expiringCards = cardRepository.findExpiringCards(in30Days, today);

        // Qualifications expiring within 30 days
        List<Qualification> expiringQuals = qualificationRepository.findExpiringQualifications(in30Days, today);

        // LOLER examinations due within 14 days (not yet overdue)
        List<LOLERExamination> lolerDue = lolerRepository.findDueExaminations(in14Days).stream()
                .filter(l -> l.getNextDueDate() != null && !l.getNextDueDate().isBefore(today))
                .collect(Collectors.toList());

        if (expiringCards.isEmpty() && expiringQuals.isEmpty() && lolerDue.isEmpty()) {
            log.info("Daily compliance check: nothing expiring/due within thresholds — no alert sent");
            return;
        }

        log.info("Daily compliance alert: {} cards, {} qualifications, {} LOLER exams",
                expiringCards.size(), expiringQuals.size(), lolerDue.size());

        StringBuilder body = new StringBuilder();
        body.append("CRMS Daily Compliance Summary — ").append(today).append("\n\n");

        if (!expiringCards.isEmpty()) {
            body.append("=== OPERATIVE CARDS EXPIRING WITHIN 30 DAYS (").append(expiringCards.size()).append(") ===\n");
            expiringCards.forEach(c -> body.append(String.format("  %s — %s — %s — expires %s%n",
                    c.getOperative() != null ? c.getOperative().getFullName() : "Unknown",
                    c.getCardType() != null ? c.getCardType().name() : "?",
                    c.getCardNumber(),
                    c.getExpiryDate())));
            body.append("\n");
        }

        if (!expiringQuals.isEmpty()) {
            body.append("=== QUALIFICATIONS EXPIRING WITHIN 30 DAYS (").append(expiringQuals.size()).append(") ===\n");
            expiringQuals.forEach(q -> body.append(String.format("  %s — %s — expires %s%n",
                    q.getOperative() != null ? q.getOperative().getFullName() : "Unknown",
                    q.getQualificationType() != null ? q.getQualificationType().name() : "?",
                    q.getExpiryDate())));
            body.append("\n");
        }

        if (!lolerDue.isEmpty()) {
            body.append("=== LOLER EXAMINATIONS DUE WITHIN 14 DAYS (").append(lolerDue.size()).append(") ===\n");
            lolerDue.forEach(l -> body.append(String.format("  %s — %s — due %s%n",
                    l.getPlant() != null ? l.getPlant().getPlantRef() : "?",
                    l.getPlant() != null ? l.getPlant().getDescription() : "?",
                    l.getNextDueDate())));
            body.append("\n");
        }

        body.append("Please action the above items to maintain compliance.\n");

        int total = expiringCards.size() + expiringQuals.size() + lolerDue.size();
        emailService.sendAdminAlert(
                String.format("CRMS Daily Compliance Alert: %d item(s) require attention", total),
                body.toString());
    }
}
