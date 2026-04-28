package com.crms.integration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * External API integration configuration properties.
 * Supports environment variable overrides.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "crms.integration")
public class IntegrationProperties {

    private boolean demoMode = true;
    
    private HmrcProperties hmrc = new HmrcProperties();
    private CompaniesHouseProperties companiesHouse = new CompaniesHouseProperties();
    private CscsProperties cscs = new CscsProperties();

    @Data
    public static class HmrcProperties {
        private String baseUrl = "https://test-api.service.hmrc.gov.uk";
        private String clientId;
        private String clientSecret;
        private String vatRate = "20.0";
        private String landfillTaxStandard = "126.15";
        private String landfillTaxInert = "4.05";
    }

    @Data
    public static class CompaniesHouseProperties {
        private String baseUrl = "https://api.companyinformation.service.gov.uk";
        private String apiKey;
        private int connectionTimeout = 10000;
        private int readTimeout = 30000;
    }

    @Data
    public static class CscsProperties {
        private String baseUrl = "https://api.cscs.uk";
        private String apiKey;
        private String clientId;
        private String clientSecret;
        private int connectionTimeout = 15000;
        private int readTimeout = 30000;
    }
}
