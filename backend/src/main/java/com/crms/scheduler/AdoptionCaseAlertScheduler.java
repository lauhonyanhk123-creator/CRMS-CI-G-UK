package com.crms.scheduler;

import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.repository.AdoptionCaseRepository;
import com.crms.domain.adoption.entity.AdoptionStage;
import com.crms.domain.adoption.enums.StageStatus;
import com.crms.domain.adoption.repository.AdoptionStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdoptionCaseAlertScheduler {
    
    private final AdoptionCaseRepository adoptionCaseRepository;
    private final AdoptionStageRepository adoptionStageRepository;
    
    /**
     * Runs daily at 7:00 AM to check for adoption cases nearing maintenance end
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void checkMaintenanceEndDates() {
        log.info("Running maintenance end date check...");
        
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        
        List<AdoptionCase> nearingEnd = adoptionCaseRepository.findCasesNearingMaintenanceEnd(thirtyDaysFromNow);
        
        if (nearingEnd.isEmpty()) {
            log.info("No adoption cases nearing maintenance end");
            return;
        }
        
        log.info("Found {} adoption cases with maintenance ending within 30 days", nearingEnd.size());
        
        for (AdoptionCase adoptionCase : nearingEnd) {
            sendMaintenanceEndAlert(adoptionCase);
        }
    }
    
    /**
     * Runs daily at 7:30 AM to check for overdue stages
     */
    @Scheduled(cron = "0 30 7 * * ?")
    public void checkOverdueStages() {
        log.info("Running overdue stage check...");
        
        LocalDate today = LocalDate.now();
        List<AdoptionStage> overdueStages = adoptionStageRepository.findOverdueStages(today);
        
        if (overdueStages.isEmpty()) {
            log.info("No overdue stages found");
            return;
        }
        
        log.warn("Found {} overdue stages", overdueStages.size());
        
        for (AdoptionStage stage : overdueStages) {
            log.info("Overdue: Case {}, Stage '{}', Planned: {}", 
                    stage.getAdoptionCase().getCaseRef(),
                    stage.getStageName(),
                    stage.getPlannedDate());
        }
        
        // In production, send notifications to responsible parties
    }
    
    /**
     * Runs monthly on the 1st at 8:00 AM to generate adoption case status report
     */
    @Scheduled(cron = "0 0 8 1 * ?")
    public void generateMonthlyStatusReport() {
        log.info("Generating monthly adoption case status report...");
        
        long preAppCount = adoptionCaseRepository.countByStatus(AdoptionStatus.PRE_APP);
        long applicationCount = adoptionCaseRepository.countByStatus(AdoptionStatus.APPLICATION);
        long designCount = adoptionCaseRepository.countByStatus(AdoptionStatus.DESIGN);
        long techAcceptCount = adoptionCaseRepository.countByStatus(AdoptionStatus.TECHNICAL_ACCEPTANCE);
        long constructionCount = adoptionCaseRepository.countByStatus(AdoptionStatus.CONSTRUCTION);
        long maintenanceCount = adoptionCaseRepository.countByStatus(AdoptionStatus.MAINTENANCE);
        long adoptionCount = adoptionCaseRepository.countByStatus(AdoptionStatus.ADOPTION);
        long completedCount = adoptionCaseRepository.countByStatus(AdoptionStatus.COMPLETED);
        
        log.info("Adoption Case Status Summary:");
        log.info("  PRE_APP: {}", preAppCount);
        log.info("  APPLICATION: {}", applicationCount);
        log.info("  DESIGN: {}", designCount);
        log.info("  TECHNICAL_ACCEPTANCE: {}", techAcceptCount);
        log.info("  CONSTRUCTION: {}", constructionCount);
        log.info("  MAINTENANCE: {}", maintenanceCount);
        log.info("  ADOPTION: {}", adoptionCount);
        log.info("  COMPLETED: {}", completedCount);
        
        long total = preAppCount + applicationCount + designCount + techAcceptCount + 
                     constructionCount + maintenanceCount + adoptionCount + completedCount;
        log.info("  TOTAL: {}", total);
    }
    
    private void sendMaintenanceEndAlert(AdoptionCase adoptionCase) {
        log.info("Sending maintenance end alert for case {} ending on {}", 
                adoptionCase.getCaseRef(), adoptionCase.getMaintenanceEndDate());
        
        java.time.Period daysUntilEnd = LocalDate.now().until(adoptionCase.getMaintenanceEndDate());
        
        String alertMessage = String.format(
                "Maintenance Period Ending: Adoption Case %s (Type: %s) maintenance period ends on %s. " +
                "Client: %s. Contract: %s",
                adoptionCase.getCaseRef(),
                adoptionCase.getAdoptionType(),
                adoptionCase.getMaintenanceEndDate(),
                adoptionCase.getClient().getName(),
                adoptionCase.getContract().getContractRef()
        );
        
        log.info("Alert: {}", alertMessage);
        
        // In production, send notification and potentially trigger adoption workflow
    }
}
