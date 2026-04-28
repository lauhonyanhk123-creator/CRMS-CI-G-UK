package com.crms.scheduler;

import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.enums.BondStatus;
import com.crms.domain.adoption.repository.BondRepository;
import com.crms.service.BondService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BondExpiryAlertScheduler {
    
    private final BondRepository bondRepository;
    private final BondService bondService;
    
    /**
     * Runs daily at 8:00 AM to check for bonds expiring within 30 days
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkExpiringBonds() {
        log.info("Running bond expiry alert check...");
        
        LocalDate today = LocalDate.now();
        LocalDate alertDate = today.plusDays(30);
        
        List<Bond> expiringBonds = bondRepository.findBondsNeedingExpiryAlert(today, alertDate);
        
        if (expiringBonds.isEmpty()) {
            log.info("No bonds expiring within 30 days");
            return;
        }
        
        log.info("Found {} bonds expiring within 30 days", expiringBonds.size());
        
        for (Bond bond : expiringBonds) {
            sendExpiryAlert(bond);
        }
    }
    
    /**
     * Runs daily at 9:00 AM to check for expired active bonds
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkExpiredBonds() {
        log.info("Running expired bond check...");
        
        LocalDate today = LocalDate.now();
        List<Bond> expiredBonds = bondRepository.findExpiredActiveBonds(today);
        
        if (expiredBonds.isEmpty()) {
            log.info("No expired active bonds found");
            return;
        }
        
        log.warn("Found {} expired active bonds", expiredBonds.size());
        
        for (Bond bond : expiredBonds) {
            handleExpiredBond(bond);
        }
    }
    
    /**
     * Runs weekly on Monday at 8:30 AM to send summary report
     */
    @Scheduled(cron = "0 30 8 ? * MON")
    public void sendWeeklyBondReport() {
        log.info("Generating weekly bond status report...");
        
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        LocalDate monthFromNow = today.plusDays(30);
        
        List<Bond> expiring7Days = bondRepository.findBondsExpiringBetween(today, weekFromNow);
        List<Bond> expiring30Days = bondRepository.findBondsExpiringBetween(today, monthFromNow);
        List<Bond> expiredBonds = bondRepository.findExpiredActiveBonds(today);
        
        long activeBonds = bondRepository.findByStatus(BondStatus.ACTIVE).size();
        long partiallyReleased = bondRepository.findByStatus(BondStatus.PARTIALLY_RELEASED).size();
        long released = bondRepository.findByStatus(BondStatus.RELEASED).size();
        long called = bondRepository.findByStatus(BondStatus.CALLED).size();
        
        log.info("Bond Status Summary:");
        log.info("  Active: {}", activeBonds);
        log.info("  Partially Released: {}", partiallyReleased);
        log.info("  Released: {}", released);
        log.info("  Called: {}", called);
        log.info("  Expiring within 7 days: {}", expiring7Days.size());
        log.info("  Expiring within 30 days: {}", expiring30Days.size());
        log.info("  Expired (still active): {}", expiredBonds.size());
        
        // In a production system, this would send an email notification
        // emailService.sendWeeklyBondReport(summary);
    }
    
    private void sendExpiryAlert(Bond bond) {
        log.info("Sending expiry alert for bond {} expiring on {}", 
                bond.getBondNumber(), bond.getExpiryDate());
        
        // Build alert details
        String alertMessage = String.format(
                "Bond Expiry Alert: %s (Type: %s) for Adoption Case %s expires on %s. " +
                "Issuing Surety: %s. Bond Value: %s",
                bond.getBondNumber(),
                bond.getBondType(),
                bond.getAdoptionCase().getCaseRef(),
                bond.getExpiryDate(),
                bond.getIssuingSurety().getName(),
                bond.getBondValue()
        );
        
        // In production, this would send notification via email/SMS/push
        // notificationService.sendAlert(bond.getAdoptionCase().getContract().getProjectManager(), alertMessage);
        
        log.info("Alert: {}", alertMessage);
    }
    
    private void handleExpiredBond(Bond bond) {
        log.warn("Handling expired bond: {} for adoption case {}", 
                bond.getBondNumber(), bond.getAdoptionCase().getCaseRef());
        
        // Update bond status to reflect expiry (optional - depends on business rules)
        // Some organizations may want to keep ACTIVE status until manually reviewed
        
        // Send urgent notification
        String urgentMessage = String.format(
                "URGENT: Bond %s has expired on %s and is still marked as ACTIVE. " +
                "Adoption Case: %s. Immediate action required.",
                bond.getBondNumber(),
                bond.getExpiryDate(),
                bond.getAdoptionCase().getCaseRef()
        );
        
        log.warn("Urgent: {}", urgentMessage);
        
        // In production, this would trigger escalation workflow
        // escalationService.escalate(bond, UrgentLevel.HIGH);
    }
}
