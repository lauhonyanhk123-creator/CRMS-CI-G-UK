package com.crms.integration.cache;

import com.crms.integration.dto.CompanySearchResult;
import com.crms.integration.dto.HmrcCisVerificationResponse;
import com.crms.integration.dto.HmrcCisDeductionRateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache for external API responses.
 * Used for graceful degradation when external services are unavailable.
 */
@Slf4j
@Service
public class IntegrationCacheService {

    // Cache entries with expiration
    private final Map<String, CacheEntry<HmrcCisVerificationResponse>> cisVerificationCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<HmrcCisDeductionRateResponse>> cisDeductionCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<CompanySearchResult>> companyProfileCache = new ConcurrentHashMap<>();

    // Default cache TTL: 24 hours
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    /**
     * Cache an HMRC CIS verification response.
     */
    public void cacheCisVerification(String utr, HmrcCisVerificationResponse response) {
        String key = "cis_verification:" + utr;
        cisVerificationCache.put(key, new CacheEntry<>(response, LocalDateTime.now(), DEFAULT_TTL));
        log.debug("Cached CIS verification for UTR: {}", utr);
    }

    /**
     * Get cached HMRC CIS verification response.
     */
    public HmrcCisVerificationResponse getCachedCisVerification(String utr) {
        String key = "cis_verification:" + utr;
        CacheEntry<HmrcCisVerificationResponse> entry = cisVerificationCache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Returning cached CIS verification for UTR: {}", utr);
            return entry.getData();
        }
        return null;
    }

    /**
     * Cache an HMRC CIS deduction rate response.
     */
    public void cacheCisDeductionRate(String supplierUtr, String contractorUtr, HmrcCisDeductionRateResponse response) {
        String key = "cis_deduction:" + supplierUtr + ":" + contractorUtr;
        cisDeductionCache.put(key, new CacheEntry<>(response, LocalDateTime.now(), DEFAULT_TTL));
        log.debug("Cached CIS deduction rate for {}/{}", supplierUtr, contractorUtr);
    }

    /**
     * Get cached HMRC CIS deduction rate response.
     */
    public HmrcCisDeductionRateResponse getCachedCisDeductionRate(String supplierUtr, String contractorUtr) {
        String key = "cis_deduction:" + supplierUtr + ":" + contractorUtr;
        CacheEntry<HmrcCisDeductionRateResponse> entry = cisDeductionCache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Returning cached CIS deduction rate for {}/{}", supplierUtr, contractorUtr);
            return entry.getData();
        }
        return null;
    }

    /**
     * Cache a Companies House company profile.
     */
    public void cacheCompanyProfile(String companyNumber, CompanySearchResult response) {
        String key = "company_profile:" + companyNumber;
        companyProfileCache.put(key, new CacheEntry<>(response, LocalDateTime.now(), DEFAULT_TTL));
        log.debug("Cached company profile for: {}", companyNumber);
    }

    /**
     * Get cached Companies House company profile.
     */
    public CompanySearchResult getCachedCompanyProfile(String companyNumber) {
        String key = "company_profile:" + companyNumber;
        CacheEntry<CompanySearchResult> entry = companyProfileCache.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Returning cached company profile for: {}", companyNumber);
            return entry.getData();
        }
        return null;
    }

    /**
     * Clear all caches.
     */
    public void clearAllCaches() {
        cisVerificationCache.clear();
        cisDeductionCache.clear();
        companyProfileCache.clear();
        log.info("Cleared all integration caches");
    }

    /**
     * Clean up expired entries. Runs every hour.
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredEntries() {
        int removed = 0;
        
        removed += cisVerificationCache.size();
        cisVerificationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        removed -= cisVerificationCache.size();
        
        removed += cisDeductionCache.size();
        cisDeductionCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        removed -= cisDeductionCache.size();
        
        removed += companyProfileCache.size();
        companyProfileCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        removed -= companyProfileCache.size();
        
        if (removed > 0) {
            log.debug("Cleaned up {} expired cache entries", removed);
        }
    }

    /**
     * Get cache statistics.
     */
    public CacheStats getStats() {
        return new CacheStats(
            cisVerificationCache.size(),
            cisDeductionCache.size(),
            companyProfileCache.size()
        );
    }

    /**
     * Cache entry with expiration.
     */
    private static class CacheEntry<T> {
        private final T data;
        private final LocalDateTime createdAt;
        private final Duration ttl;

        CacheEntry(T data, LocalDateTime createdAt, Duration ttl) {
            this.data = data;
            this.createdAt = createdAt;
            this.ttl = ttl;
        }

        T getData() {
            return data;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(createdAt.plus(ttl));
        }
    }

    /**
     * Cache statistics record.
     */
    public record CacheStats(int cisVerifications, int cisDeductions, int companyProfiles) {}
}
