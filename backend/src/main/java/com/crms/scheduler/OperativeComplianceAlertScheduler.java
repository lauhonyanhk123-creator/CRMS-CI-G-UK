package com.crms.scheduler;

import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.operative.repository.QualificationRepository;
import com.crms.domain.operative.enums.OperativeStatus;
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
public class OperativeComplianceAlertScheduler {

    private final CardRepository cardRepository;
    private final QualificationRepository qualificationRepository;
    private final OperativeRepository operativeRepository;
    private final EmailService emailService;

    /** Daily at 06:30 — CSCS and operative cards expiring within 30 days */
    @Scheduled(cron = "0 30 6 * * ?")
    public void checkExpiringCards() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(30);

        List<Card> expiring = cardRepository.findExpiringCards(threshold, today);

        if (expiring.isEmpty()) {
            log.info("Operative compliance check: no cards expiring within 30 days");
            return;
        }

        log.info("Operative compliance check: {} card(s) expiring within 30 days", expiring.size());

        String lines = expiring.stream()
                .map(c -> String.format("  %s %s — %s (card no. %s) — expires %s",
                        c.getOperative().getFirstName(),
                        c.getOperative().getLastName(),
                        c.getCardType(),
                        c.getCardNumber() != null ? c.getCardNumber() : "N/A",
                        c.getExpiryDate()))
                .collect(Collectors.joining("\n"));

        emailService.sendAdminAlert(
                String.format("CRMS Alert: %d Operative Card(s) Expiring Within 30 Days", expiring.size()),
                "The following operative cards are expiring within the next 30 days:\n\n"
                        + lines + "\n\nPlease arrange renewal to maintain site compliance.");
    }

    /** Daily at 06:45 — training qualifications expiring within 30 days */
    @Scheduled(cron = "0 45 6 * * ?")
    public void checkExpiringQualifications() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(30);

        List<Qualification> expiring = qualificationRepository.findExpiringQualifications(threshold, today);

        if (expiring.isEmpty()) {
            log.info("Operative compliance check: no qualifications expiring within 30 days");
            return;
        }

        log.info("Operative compliance check: {} qualification(s) expiring within 30 days", expiring.size());

        String lines = expiring.stream()
                .map(q -> String.format("  %s %s — %s (%s) — expires %s",
                        q.getOperative().getFirstName(),
                        q.getOperative().getLastName(),
                        q.getQualificationType(),
                        q.getAwardingBody() != null ? q.getAwardingBody() : "N/A",
                        q.getExpiryDate()))
                .collect(Collectors.joining("\n"));

        emailService.sendAdminAlert(
                String.format("CRMS Alert: %d Operative Qualification(s) Expiring Within 30 Days", expiring.size()),
                "The following operative qualifications are expiring within the next 30 days:\n\n"
                        + lines + "\n\nPlease arrange refresher training to maintain compliance.");
    }

    /** Weekly Monday at 07:00 — right-to-work documents expiring within 90 days */
    @Scheduled(cron = "0 0 7 ? * MON")
    public void checkRightToWorkExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(90);

        List<Operative> expiring = operativeRepository.findByStatus(OperativeStatus.ACTIVE).stream()
                .filter(o -> o.getRightToWorkExpiry() != null
                        && !o.getRightToWorkExpiry().isBefore(today)
                        && o.getRightToWorkExpiry().isBefore(threshold))
                .collect(Collectors.toList());

        if (expiring.isEmpty()) {
            log.info("Right-to-work check: no documents expiring within 90 days");
            return;
        }

        log.warn("Right-to-work check: {} operative(s) with documents expiring within 90 days", expiring.size());

        String lines = expiring.stream()
                .map(o -> String.format("  %s %s (Ref: %s) — right-to-work expires %s",
                        o.getFirstName(), o.getLastName(),
                        o.getEmployeeRef(), o.getRightToWorkExpiry()))
                .collect(Collectors.joining("\n"));

        emailService.sendAdminAlert(
                String.format("CRMS Alert: %d Operative Right-to-Work Document(s) Expiring Within 90 Days", expiring.size()),
                "The following operatives have right-to-work documents expiring within 90 days. "
                        + "Failure to re-verify may result in illegal working:\n\n"
                        + lines + "\n\nPlease verify and obtain updated documentation.");
    }
}
