package com.crms.scheduler;

import com.crms.domain.plant.entity.LOLERExamination;
import com.crms.domain.plant.repository.LOLERExaminationRepository;
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
public class PlantCertificationAlertScheduler {

    private final LOLERExaminationRepository lolerRepository;
    private final EmailService emailService;

    /** Daily at 07:00 — alert on plant LOLER/PUWER certs due within 30 days */
    @Scheduled(cron = "0 0 7 * * ?")
    public void checkLolerDueSoon() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(30);

        List<LOLERExamination> due = lolerRepository.findDueExaminations(threshold).stream()
                .filter(l -> l.getNextDueDate() != null && !l.getNextDueDate().isBefore(today))
                .collect(Collectors.toList());

        if (due.isEmpty()) {
            log.info("Plant certification check: no LOLER/PUWER exams due within 30 days");
            return;
        }

        log.info("Plant certification check: {} LOLER/PUWER exam(s) due within 30 days", due.size());

        String lines = due.stream()
                .map(l -> String.format("  %s — %s — due %s",
                        l.getPlant() != null ? l.getPlant().getPlantRef() : "?",
                        l.getPlant() != null ? l.getPlant().getDescription() : "?",
                        l.getNextDueDate()))
                .collect(Collectors.joining("\n"));

        emailService.sendAdminAlert(
                String.format("CRMS Alert: %d LOLER/PUWER Exam(s) Due Within 30 Days", due.size()),
                "The following plant items require LOLER/PUWER examination within the next 30 days:\n\n"
                        + lines + "\n\nPlease arrange inspection to maintain legal compliance.");
    }

    /** Daily at 07:15 — alert on plant certs already overdue */
    @Scheduled(cron = "0 15 7 * * ?")
    public void checkLolerOverdue() {
        LocalDate today = LocalDate.now();

        List<LOLERExamination> overdue = lolerRepository.findDueExaminations(today);

        if (overdue.isEmpty()) {
            log.info("Plant certification check: no overdue LOLER/PUWER exams");
            return;
        }

        log.warn("Plant certification check: {} LOLER/PUWER exam(s) OVERDUE", overdue.size());

        String lines = overdue.stream()
                .map(l -> String.format("  %s — %s — was due %s",
                        l.getPlant() != null ? l.getPlant().getPlantRef() : "?",
                        l.getPlant() != null ? l.getPlant().getDescription() : "?",
                        l.getNextDueDate()))
                .collect(Collectors.joining("\n"));

        emailService.sendAdminAlert(
                String.format("URGENT: %d Plant Item(s) With OVERDUE LOLER/PUWER Examination", overdue.size()),
                "The following plant items have OVERDUE LOLER/PUWER examinations. "
                        + "These items must not be used until inspected:\n\n"
                        + lines + "\n\nImmediate action required.");
    }
}
