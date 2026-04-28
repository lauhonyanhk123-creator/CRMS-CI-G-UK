package com.crms.integration.companieshouse;

import com.crms.integration.config.IntegrationProperties;
import com.crms.integration.dto.CompanySearchResult;
import com.crms.integration.dto.CompanySearchResult.AddressDto;
import com.crms.integration.dto.CompanySearchResult.ChargeDto;
import com.crms.integration.dto.CompanySearchResult.OfficerDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Companies House API Service implementation.
 * Uses Companies House public API - no authentication required for basic search.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompaniesHouseServiceImpl implements CompaniesHouseService {

    private final RestTemplate companiesHouseRestTemplate;
    private final IntegrationProperties properties;
    private final ObjectMapper objectMapper;

    // Demo mode company data
    private static final Map<String, DemoCompanyData> DEMO_COMPANIES = new HashMap<>();

    static {
        DEMO_COMPANIES.put("SC123456", DemoCompanyData.builder()
                .companyNumber("SC123456")
                .title("DEMO CONSTRUCTION UK LIMITED")
                .companyType("private-audit-exemption-subsidiary")
                .companyStatus("active")
                .jurisdiction("uk-scotland")
                .dateOfCreation("2020-01-15")
                .registeredOfficeAddress(AddressDto.builder()
                        .addressLine1("123 Construction Way")
                        .locality("Glasgow")
                        .postalCode("G1 1AA")
                        .country("Scotland")
                        .build())
                .sicCodes(Arrays.asList("41201", "43999"))
                .build());

        DEMO_COMPANIES.put("NI123456", DemoCompanyData.builder()
                .companyNumber("NI123456")
                .title("DEMO GROUNDWORKS NI LIMITED")
                .companyType("private-limited-company")
                .companyStatus("active")
                .jurisdiction("northern-ireland")
                .dateOfCreation("2019-06-20")
                .registeredOfficeAddress(AddressDto.builder()
                        .addressLine1("45 Belfast Road")
                        .locality("Belfast")
                        .postalCode("BT1 1AA")
                        .country("Northern Ireland")
                        .build())
                .sicCodes(Arrays.asList("43120", "41201"))
                .build());

        DEMO_COMPANIES.put("01234567", DemoCompanyData.builder()
                .companyNumber("01234567")
                .title("DEMO CIVIL ENGINEERING PLC")
                .companyType("public-limited-company")
                .companyStatus("active")
                .jurisdiction("england-wales")
                .dateOfCreation("2015-03-10")
                .registeredOfficeAddress(AddressDto.builder()
                        .addressLine1("100 Engineering House")
                        .addressLine2("Thames Street")
                        .locality("London")
                        .postalCode("EC2M 1AA")
                        .country("England")
                        .build())
                .sicCodes(Arrays.asList("42110", "42120", "42910"))
                .hasInsolvency(true)
                .build());
    }

    @Override
    public List<CompanySearchResult> searchCompanies(String query) {
        log.info("Searching companies with query: {}", query);

        if (isDemoMode()) {
            return mockSearchCompanies(query);
        }

        try {
            HttpHeaders headers = createCompaniesHouseHeaders();
            String url = properties.getCompaniesHouse().getBaseUrl() + "/search/companies?q=" +
                    java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = companiesHouseRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapSearchResults(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Companies House API error: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to search companies: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public CompanySearchResult getCompanyProfile(String companyNumber) {
        log.info("Getting company profile: {}", companyNumber);

        if (isDemoMode()) {
            return mockGetCompanyProfile(companyNumber);
        }

        try {
            HttpHeaders headers = createCompaniesHouseHeaders();
            String url = properties.getCompaniesHouse().getBaseUrl() + "/company/" + companyNumber;

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = companiesHouseRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapToCompanyProfile(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Companies House API error: {}", e.getMessage());
            return createErrorCompanyProfile(companyNumber);
        } catch (Exception e) {
            log.error("Failed to get company profile: {}", e.getMessage());
            return createErrorCompanyProfile(companyNumber);
        }
    }

    @Override
    public List<OfficerDto> getCompanyOfficers(String companyNumber) {
        log.info("Getting company officers: {}", companyNumber);

        if (isDemoMode()) {
            return mockGetCompanyOfficers(companyNumber);
        }

        try {
            HttpHeaders headers = createCompaniesHouseHeaders();
            String url = properties.getCompaniesHouse().getBaseUrl() + "/company/" + companyNumber + "/officers";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = companiesHouseRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapOfficers(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get company officers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ChargeDto> getCompanyCharges(String companyNumber) {
        log.info("Getting company charges: {}", companyNumber);

        if (isDemoMode()) {
            return mockGetCompanyCharges(companyNumber);
        }

        try {
            HttpHeaders headers = createCompaniesHouseHeaders();
            String url = properties.getCompaniesHouse().getBaseUrl() + "/company/" + companyNumber + "/charges";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = companiesHouseRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapCharges(response.getBody());
        } catch (Exception e) {
            log.error("Failed to get company charges: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public CompanySearchResult getCompanyInsolvency(String companyNumber) {
        log.info("Getting company insolvency: {}", companyNumber);

        if (isDemoMode()) {
            return mockGetCompanyInsolvency(companyNumber);
        }

        try {
            HttpHeaders headers = createCompaniesHouseHeaders();
            String url = properties.getCompaniesHouse().getBaseUrl() + "/company/" + companyNumber + "/insolvency";

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = companiesHouseRestTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return mapInsolvency(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            // No insolvency record - this is expected for most companies
            return CompanySearchResult.builder()
                    .companyNumber(companyNumber)
                    .insolvencyCases(Collections.emptyMap())
                    .build();
        } catch (Exception e) {
            log.error("Failed to get insolvency data: {}", e.getMessage());
            return CompanySearchResult.builder()
                    .companyNumber(companyNumber)
                    .insolvencyCases(Collections.emptyMap())
                    .build();
        }
    }

    @Override
    public boolean isDemoMode() {
        return properties.isDemoMode() ||
               properties.getCompaniesHouse().getApiKey() == null ||
               properties.getCompaniesHouse().getApiKey().isEmpty();
    }

    // ==================== Authentication ====================

    private HttpHeaders createCompaniesHouseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        if (!isDemoMode() && properties.getCompaniesHouse().getApiKey() != null) {
            String auth = properties.getCompaniesHouse().getApiKey() + ":";
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encoded);
        }
        
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    // ==================== Mock Methods ====================

    private List<CompanySearchResult> mockSearchCompanies(String query) {
        log.info("[DEMO MODE] Searching companies: {}", query);

        List<CompanySearchResult> results = new ArrayList<>();

        // Return all demo companies if query is broad
        if (query.length() < 3) {
            return results;
        }

        for (DemoCompanyData demo : DEMO_COMPANIES.values()) {
            if (demo.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    demo.getCompanyNumber().contains(query)) {
                results.add(mapDemoToSearchResult(demo));
            }
        }

        // Add generic result if no matches
        if (results.isEmpty()) {
            results.add(CompanySearchResult.builder()
                    .companyNumber("DEMO-001")
                    .title("DEMO " + query.toUpperCase() + " LIMITED")
                    .companyType("private-limited-company")
                    .companyStatus("active")
                    .jurisdiction("england-wales")
                    .dateOfCreation("2020-01-01")
                    .build());
        }

        return results;
    }

    private CompanySearchResult mockGetCompanyProfile(String companyNumber) {
        log.info("[DEMO MODE] Getting company profile: {}", companyNumber);

        DemoCompanyData demo = DEMO_COMPANIES.get(companyNumber);
        if (demo == null) {
            // Generate a generic demo profile
            return CompanySearchResult.builder()
                    .companyNumber(companyNumber)
                    .title("DEMO COMPANY " + companyNumber)
                    .companyType("private-limited-company")
                    .companyStatus("active")
                    .jurisdiction("england-wales")
                    .dateOfCreation("2020-01-01")
                    .registeredOfficeAddress(AddressDto.builder()
                            .addressLine1("123 Demo Street")
                            .locality("London")
                            .postalCode("EC1 1AA")
                            .country("England")
                            .build())
                    .build();
        }

        return mapDemoToSearchResult(demo);
    }

    private List<OfficerDto> mockGetCompanyOfficers(String companyNumber) {
        log.info("[DEMO MODE] Getting officers: {}", companyNumber);

        DemoCompanyData demo = DEMO_COMPANIES.get(companyNumber);
        if (demo == null || demo.getSicCodes() == null) {
            return createDefaultOfficers(companyNumber);
        }

        return createDefaultOfficers(companyNumber);
    }

    private List<OfficerDto> createDefaultOfficers(String companyNumber) {
        return Arrays.asList(
                OfficerDto.builder()
                        .name("DEMO DIRECTOR ONE")
                        .officerRole("director")
                        .nationality("British")
                        .occupation("Construction Director")
                        .dateOfBirth(LocalDate.of(1975, 5, 15))
                        .address(AddressDto.builder()
                                .addressLine1("10 Director Lane")
                                .locality("London")
                                .postalCode("SW1 1AA")
                                .country("England")
                                .build())
                        .appointedDate(LocalDate.of(2020, 1, 1))
                        .build(),
                OfficerDto.builder()
                        .name("DEMO DIRECTOR TWO")
                        .officerRole("director")
                        .nationality("British")
                        .occupation("Finance Director")
                        .dateOfBirth(LocalDate.of(1980, 8, 20))
                        .address(AddressDto.builder()
                                .addressLine1("20 Finance Avenue")
                                .locality("London")
                                .postalCode("EC2 2AA")
                                .country("England")
                                .build())
                        .appointedDate(LocalDate.of(2020, 1, 1))
                        .build()
        );
    }

    private List<ChargeDto> mockGetCompanyCharges(String companyNumber) {
        log.info("[DEMO MODE] Getting charges: {}", companyNumber);

        DemoCompanyData demo = DEMO_COMPANIES.get(companyNumber);
        if (demo != null && demo.isHasInsolvency()) {
            return Arrays.asList(
                    ChargeDto.builder()
                            .chargeId("DEMO-CHG-001")
                            .status("outstanding")
                            .natureOfCharge("Legal charge over freehold property")
                            .personsEntitled("DEMO BANK PLC")
                            .createdDate(LocalDate.of(2020, 6, 1))
                            .amountSecured(new BigDecimal("500000"))
                            .build()
            );
        }

        return Collections.emptyList();
    }

    private CompanySearchResult mockGetCompanyInsolvency(String companyNumber) {
        log.info("[DEMO MODE] Checking insolvency: {}", companyNumber);

        DemoCompanyData demo = DEMO_COMPANIES.get(companyNumber);
        if (demo != null && demo.isHasInsolvency()) {
            Map<String, Object> insolvencyData = new HashMap<>();
            insolvencyData.put("hasInsolvency", true);
            insolvencyData.put("cases", Arrays.asList(
                    Map.of(
                            "type", "creditors-voluntary-liquidation",
                            "date", "2024-01-15",
                            "practitioner", "DEMO LIQUIDATOR & CO"
                    )
            ));

            return CompanySearchResult.builder()
                    .companyNumber(companyNumber)
                    .insolvencyCases(insolvencyData)
                    .build();
        }

        return CompanySearchResult.builder()
                .companyNumber(companyNumber)
                .insolvencyCases(Collections.emptyMap())
                .build();
    }

    // ==================== Mapping Methods ====================

    private CompanySearchResult mapDemoToSearchResult(DemoCompanyData demo) {
        return CompanySearchResult.builder()
                .companyNumber(demo.getCompanyNumber())
                .title(demo.getTitle())
                .companyType(demo.getCompanyType())
                .companyStatus(demo.getCompanyStatus())
                .jurisdiction(demo.getJurisdiction())
                .dateOfCreation(demo.getDateOfCreation())
                .dateOfCessation(demo.getDateOfCessation())
                .registeredOfficeAddress(demo.getRegisteredOfficeAddress())
                .build();
    }

    private List<CompanySearchResult> mapSearchResults(Map<String, Object> body) {
        List<CompanySearchResult> results = new ArrayList<>();

        if (body == null || !body.containsKey("items")) {
            return results;
        }

        try {
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            for (Map<String, Object> item : items) {
                results.add(CompanySearchResult.builder()
                        .companyNumber((String) item.get("company_number"))
                        .title((String) item.get("title"))
                        .companyType((String) item.get("company_type"))
                        .companyStatus((String) item.get("company_status"))
                        .jurisdiction((String) item.get("jurisdiction"))
                        .dateOfCreation((String) item.get("date_of_creation"))
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to map search results: {}", e.getMessage());
        }

        return results;
    }

    private CompanySearchResult mapToCompanyProfile(Map<String, Object> body) {
        Map<String, Object> registeredOffice = (Map<String, Object>) body.get("registered_office_address");
        AddressDto address = null;
        
        if (registeredOffice != null) {
            address = AddressDto.builder()
                    .addressLine1((String) registeredOffice.get("address_line_1"))
                    .addressLine2((String) registeredOffice.get("address_line_2"))
                    .careOf((String) registeredOffice.get("care_of"))
                    .country((String) registeredOffice.get("country"))
                    .locality((String) registeredOffice.get("locality"))
                    .poBox((String) registeredOffice.get("po_box"))
                    .postalCode((String) registeredOffice.get("postal_code"))
                    .premises((String) registeredOffice.get("premises"))
                    .region((String) registeredOffice.get("region"))
                    .build();
        }

        return CompanySearchResult.builder()
                .companyNumber((String) body.get("company_number"))
                .title((String) body.get("company_name"))
                .companyType((String) body.get("type"))
                .companyStatus((String) body.get("company_status"))
                .jurisdiction((String) body.get("jurisdiction"))
                .dateOfCreation((String) body.get("incorporation_date"))
                .dateOfCessation((String) body.get("dissolved_on"))
                .registeredOfficeAddress(address)
                .build();
    }

    private List<OfficerDto> mapOfficers(Map<String, Object> body) {
        List<OfficerDto> officers = new ArrayList<>();

        if (body == null || !body.containsKey("items")) {
            return officers;
        }

        try {
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            for (Map<String, Object> item : items) {
                Map<String, Object> address = (Map<String, Object>) item.get("address");
                AddressDto addressDto = null;
                
                if (address != null) {
                    addressDto = AddressDto.builder()
                            .addressLine1((String) address.get("address_line_1"))
                            .locality((String) address.get("locality"))
                            .postalCode((String) address.get("postal_code"))
                            .country((String) address.get("country"))
                            .build();
                }

                officers.add(OfficerDto.builder()
                        .name((String) item.get("name"))
                        .officerRole((String) item.get("officer_role"))
                        .nationality((String) item.get("nationality"))
                        .occupation((String) item.get("occupation"))
                        .dateOfBirth(parseDateOfBirth((Map<String, Object>) item.get("date_of_birth")))
                        .address(addressDto)
                        .appointedDate(parseDate((String) item.get("appointed_on")))
                        .resignedDate(parseDate((String) item.get("resigned_on")))
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to map officers: {}", e.getMessage());
        }

        return officers;
    }

    private List<ChargeDto> mapCharges(Map<String, Object> body) {
        List<ChargeDto> charges = new ArrayList<>();

        if (body == null || !body.containsKey("items")) {
            return charges;
        }

        try {
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            for (Map<String, Object> item : items) {
                charges.add(ChargeDto.builder()
                        .chargeId((String) item.get("id"))
                        .status((String) item.get("status"))
                        .natureOfCharge((String) item.get("nature_transport"))
                        .personsEntitled(String.join(", ", (List<String>) item.getOrDefault("persons_entitled", Collections.emptyList())))
                        .createdDate(parseDate((String) item.get("created_on")))
                        .acquiredDate(parseDate((String) item.get("acquired_on")))
                        .amountSecured(parseAmount(item.get("amount_secured")))
                        .build());
            }
        } catch (Exception e) {
            log.error("Failed to map charges: {}", e.getMessage());
        }

        return charges;
    }

    private CompanySearchResult mapInsolvency(Map<String, Object> body) {
        Map<String, Object> insolvencyCases = new HashMap<>();
        if (body != null && body.containsKey("cases")) {
            insolvencyCases.put("cases", body.get("cases"));
        }

        return CompanySearchResult.builder()
                .insolvencyCases(insolvencyCases)
                .build();
    }

    private CompanySearchResult createErrorCompanyProfile(String companyNumber) {
        return CompanySearchResult.builder()
                .companyNumber(companyNumber)
                .title("COMPANY NOT FOUND")
                .companyStatus("not-found")
                .build();
    }

    private LocalDate parseDateOfBirth(Map<String, Object> dob) {
        if (dob == null) return null;
        try {
            Integer month = (Integer) dob.get("month");
            Integer year = (Integer) dob.get("year");
            return LocalDate.of(year, month, 1);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseAmount(Object amount) {
        if (amount == null) return null;
        try {
            if (amount instanceof String) {
                return new BigDecimal((String) amount);
            } else if (amount instanceof Number) {
                return new BigDecimal(amount.toString());
            }
        } catch (Exception e) {
            log.warn("Failed to parse amount: {}", amount);
        }
        return null;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class DemoCompanyData {
        private String companyNumber;
        private String title;
        private String companyType;
        private String companyStatus;
        private String jurisdiction;
        private String dateOfCreation;
        private String dateOfCessation;
        private AddressDto registeredOfficeAddress;
        private List<String> sicCodes;
        private boolean hasInsolvency;
    }
}
