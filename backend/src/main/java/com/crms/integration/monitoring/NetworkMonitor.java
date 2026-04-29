package com.crms.integration.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors network connectivity to external APIs.
 * Provides status information for graceful offline degradation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NetworkMonitor {

    private final RestTemplate restTemplate;

    @Value("${crms.integration.hmrc.baseUrl:https://test-api.service.hmrc.gov.uk}")
    private String hmrcBaseUrl;

    @Value("${crms.integration.companies-house.baseUrl:https://api.companyinformation.service.gov.uk}")
    private String companiesHouseBaseUrl;

    @Value("${crms.integration.cscs.baseUrl:https://api.cscs.uk}")
    private String cscsBaseUrl;

    private final AtomicBoolean hmrcReachable = new AtomicBoolean(true);
    private final AtomicBoolean companiesHouseReachable = new AtomicBoolean(true);
    private final AtomicBoolean cscsReachable = new AtomicBoolean(true);
    
    private final AtomicLong lastCheckTimestamp = new AtomicLong(0);
    private volatile boolean isOnline = true;

    /**
     * Check connectivity to all external services.
     * Runs every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void checkConnectivity() {
        log.debug("Checking connectivity to external services...");
        
        checkHmrcConnectivity();
        checkCompaniesHouseConnectivity();
        checkCscsConnectivity();
        
        lastCheckTimestamp.set(System.currentTimeMillis());
        
        boolean anyReachable = hmrcReachable.get() || companiesHouseReachable.get() || cscsReachable.get();
        if (isOnline && !anyReachable) {
            log.warn("All external services are unreachable. Entering offline mode.");
            isOnline = false;
        } else if (!isOnline && anyReachable) {
            log.info("External services are reachable. Exiting offline mode.");
            isOnline = true;
        }
    }

    private void checkHmrcConnectivity() {
        try {
            restTemplate.getForObject(hmrcBaseUrl + "/hello/health", String.class);
            if (!hmrcReachable.get()) {
                log.info("HMRC API is reachable again");
            }
            hmrcReachable.set(true);
        } catch (Exception e) {
            if (hmrcReachable.get()) {
                log.warn("HMRC API is unreachable: {}", e.getMessage());
            }
            hmrcReachable.set(false);
        }
    }

    private void checkCompaniesHouseConnectivity() {
        try {
            // Companies House has a /healthcheck endpoint
            restTemplate.getForObject(companiesHouseBaseUrl + "/healthcheck", String.class);
            if (!companiesHouseReachable.get()) {
                log.info("Companies House API is reachable again");
            }
            companiesHouseReachable.set(true);
        } catch (Exception e) {
            // Fallback: try a simple search endpoint
            try {
                restTemplate.getForObject(companiesHouseBaseUrl + "/search/companies?q=test", String.class);
                companiesHouseReachable.set(true);
            } catch (Exception ex) {
                if (companiesHouseReachable.get()) {
                    log.warn("Companies House API is unreachable: {}", ex.getMessage());
                }
                companiesHouseReachable.set(false);
            }
        }
    }

    private void checkCscsConnectivity() {
        try {
            restTemplate.getForObject(cscsBaseUrl + "/health", String.class);
            if (!cscsReachable.get()) {
                log.info("CSCS API is reachable again");
            }
            cscsReachable.set(true);
        } catch (Exception e) {
            if (cscsReachable.get()) {
                log.warn("CSCS API is unreachable: {}", e.getMessage());
            }
            cscsReachable.set(false);
        }
    }

    /**
     * Check if HMRC service is reachable.
     * 
     * @return true if HMRC is reachable
     */
    public boolean isHmrcReachable() {
        return hmrcReachable.get();
    }

    /**
     * Check if Companies House service is reachable.
     * 
     * @return true if Companies House is reachable
     */
    public boolean isCompaniesHouseReachable() {
        return companiesHouseReachable.get();
    }

    /**
     * Check if CSCS service is reachable.
     * 
     * @return true if CSCS is reachable
     */
    public boolean isCscsReachable() {
        return cscsReachable.get();
    }

    /**
     * Check if any external service is currently reachable.
     * 
     * @return true if at least one service is reachable
     */
    public boolean isAnyServiceReachable() {
        return isOnline;
    }

    /**
     * Check if the system is currently in offline mode.
     * 
     * @return true if offline mode is active (no services reachable)
     */
    public boolean isOfflineMode() {
        return !isOnline;
    }

    /**
     * Get the timestamp of the last connectivity check.
     * 
     * @return timestamp in milliseconds
     */
    public long getLastCheckTimestamp() {
        return lastCheckTimestamp.get();
    }

    /**
     * Force a connectivity check immediately.
     */
    public void forceCheck() {
        checkConnectivity();
    }
}
