package com.crms.service;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.healthsafety.entity.*;
import com.crms.domain.healthsafety.enums.*;
import com.crms.domain.healthsafety.repository.*;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.impl.HealthSafetyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for HealthSafetyServiceImpl.
 * Tests cover F10 notification, CPP, RAMS document management, 
 * incident reporting workflow, permits, and RAMS sign-on operations.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HealthSafetyServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private OperativeRepository operativeRepository;

    @Mock
    private F10NotificationRepository f10NotificationRepository;

    @Mock
    private ConstructionPhasePlanRepository cppRepository;

    @Mock
    private RAMSDocumentRepository ramsRepository;

    @Mock
    private RAMSSignOnRepository ramsSignOnRepository;

    @Mock
    private PermitToDigRepository permitToDigRepository;

    @Mock
    private IncidentReportRepository incidentReportRepository;

    @InjectMocks
    private HealthSafetyServiceImpl healthSafetyService;

    // Test data
    private Contract testContract;
    private Site testSite;
    private Operative testOperative;
    private F10Notification testF10;
    private ConstructionPhasePlan testCPP;
    private RAMSDocument testRAMS;
    private PermitToDig testPermit;
    private IncidentReport testIncident;

    @BeforeEach
    void setUp() {
        testContract = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .title("Test Contract")
                .build();

        testSite = Site.builder()
                .id(1L)
                .name("Test Site")
                .build();

        testOperative = Operative.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();

        testF10 = F10Notification.builder()
                .id(1L)
                .contract(testContract)
                .notificationNumber("F10-123456")
                .moreThan30Days(true)
                .moreThan500PersonDays(false)
                .constructionStartDate(LocalDate.of(2024, 3, 1))
                .constructionEndDate(LocalDate.of(2024, 6, 1))
                .isActive(true)
                .hdfAcknowledged(false)
                .build();

        testCPP = ConstructionPhasePlan.builder()
                .id(1L)
                .contract(testContract)
                .planRef("CPP-123456")
                .version("1")
                .status(CppStatus.DRAFT)
                .description("Construction Phase Plan")
                .build();

        testRAMS = RAMSDocument.builder()
                .id(1L)
                .contract(testContract)
                .ramsRef("RAMS-123456")
                .version("1")
                .status(RamsStatus.DRAFT)
                .title("Risk Assessment")
                .build();

        testPermit = PermitToDig.builder()
                .id(1L)
                .site(testSite)
                .permitNumber("PTD-123456")
                .worksDescription("Excavation works")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .status(PermitStatus.DRAFT)
                .build();

        testIncident = IncidentReport.builder()
                .id(1L)
                .site(testSite)
                .reportNumber("INC-123456")
                .incidentDate(LocalDateTime.now())
                .description("Test incident")
                .type(IncidentType.NEAR_MISS)
                .severity(Severity.MINOR)
                .status(IncidentStatus.DRAFT)
                .build();
    }

    // ================================================================
    // F10 NOTIFICATION TESTS
    // ================================================================

    @Nested
    @DisplayName("F10 Notification Tests")
    class F10NotificationTests {

        @Test
        @DisplayName("createF10 creates notification successfully")
        void createF10_createsNotification() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("moreThan30Days", "true");
            request.put("moreThan500PersonDays", "false");
            request.put("constructionStartDate", "2024-03-01");
            request.put("constructionEndDate", "2024-06-01");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(f10NotificationRepository.save(any(F10Notification.class))).thenAnswer(invocation -> {
                F10Notification f = invocation.getArgument(0);
                f.setId(1L);
                return f;
            });

            // When
            Map<String, Object> result = (Map<String, Object>) healthSafetyService.createF10(1L, request);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.get("id"));
            assertNotNull(result.get("notificationNumber"));
            assertTrue(result.get("notificationNumber").toString().startsWith("F10-"));
            assertEquals("DRAFT", result.get("status"));
            verify(f10NotificationRepository).save(any(F10Notification.class));
        }

        @Test
        @DisplayName("createF10 throws exception when contract not found")
        void createF10_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());
            Map<String, Object> request = new HashMap<>();

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.createF10(999L, request));
        }

        @Test
        @DisplayName("createF10 throws exception for invalid request type")
        void createF10_throwsException_forInvalidRequestType() {
            // Given
            Object invalidRequest = new Object();

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                healthSafetyService.createF10(1L, invalidRequest));
        }

        @Test
        @DisplayName("createF10 uses default values when not provided")
        void createF10_usesDefaultValues() {
            // Given
            Map<String, Object> request = new HashMap<>(); // Empty request

            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(f10NotificationRepository.save(any(F10Notification.class))).thenAnswer(invocation -> {
                F10Notification f = invocation.getArgument(0);
                f.setId(1L);
                return f;
            });

            // When
            Map<String, Object> result = (Map<String, Object>) healthSafetyService.createF10(1L, request);

            // Then
            assertNotNull(result);
            verify(f10NotificationRepository).save(argThat(f -> 
                f.getMoreThan30Days() == false && f.getMoreThan500PersonDays() == false));
        }
    }

    // ================================================================
    // CPP (Construction Phase Plan) TESTS
    // ================================================================

    @Nested
    @DisplayName("Construction Phase Plan (CPP) Tests")
    class CPPTests {

        @Test
        @DisplayName("createCPP creates plan successfully")
        void createCPP_createsPlan() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("description", "Construction Phase Plan Description");
            request.put("startDate", "2024-03-01");
            request.put("endDate", "2024-06-01");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(cppRepository.save(any(ConstructionPhasePlan.class))).thenAnswer(invocation -> {
                ConstructionPhasePlan c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            Map<String, Object> result = (Map<String, Object>) healthSafetyService.createCPP(1L, request);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.get("id"));
            assertNotNull(result.get("planRef"));
            assertTrue(result.get("planRef").toString().startsWith("CPP-"));
            assertEquals("DRAFT", result.get("status"));
            verify(cppRepository).save(any(ConstructionPhasePlan.class));
        }

        @Test
        @DisplayName("createCPP throws exception when contract not found")
        void createCPP_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());
            Map<String, Object> request = new HashMap<>();

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.createCPP(999L, request));
        }

        @Test
        @DisplayName("createCPP throws exception for invalid request type")
        void createCPP_throwsException_forInvalidRequestType() {
            // Given
            Object invalidRequest = new Object();

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                healthSafetyService.createCPP(1L, invalidRequest));
        }
    }

    // ================================================================
    // RAMS DOCUMENT TESTS
    // ================================================================

    @Nested
    @DisplayName("RAMS Document Tests")
    class RAMSDocumentTests {

        @Test
        @DisplayName("createRAMS creates document successfully")
        void createRAMS_createsDocument() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("title", "Risk Assessment for Excavation");
            request.put("description", "Detailed RAMS description");
            request.put("validUntil", "2024-12-31");

            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(ramsRepository.save(any(RAMSDocument.class))).thenAnswer(invocation -> {
                RAMSDocument r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            // When
            Map<String, Object> result = (Map<String, Object>) healthSafetyService.createRAMS(1L, request);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.get("id"));
            assertNotNull(result.get("ramsRef"));
            assertTrue(result.get("ramsRef").toString().startsWith("RAMS-"));
            assertEquals("DRAFT", result.get("status"));
            verify(ramsRepository).save(any(RAMSDocument.class));
        }

        @Test
        @DisplayName("createRAMS throws exception when contract not found")
        void createRAMS_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());
            Map<String, Object> request = new HashMap<>();

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.createRAMS(999L, request));
        }

        @Test
        @DisplayName("createRAMS throws exception for invalid request type")
        void createRAMS_throwsException_forInvalidRequestType() {
            // Given
            Object invalidRequest = new Object();

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                healthSafetyService.createRAMS(1L, invalidRequest));
        }
    }

    // ================================================================
    // RAMS SIGN-ON TESTS
    // ================================================================

    @Nested
    @DisplayName("RAMS Sign-On Tests")
    class RAMSSignOnTests {

        @Test
        @DisplayName("signRAMS creates sign-on record successfully")
        void signRAMS_createsSignOn() {
            // Given
            when(ramsRepository.findById(1L)).thenReturn(Optional.of(testRAMS));
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(ramsSignOnRepository.save(any(RAMSSignOn.class))).thenAnswer(invocation -> {
                RAMSSignOn s = invocation.getArgument(0);
                s.setId(1L);
                return s;
            });

            // When
            com.crms.dto.response.RAMSSignOnResponse result = healthSafetyService.signRAMS(1L, 1L, 1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(1L, result.getRamsId());
            assertEquals(1L, result.getOperativeId());
            assertEquals(1L, result.getSiteId());
            assertNotNull(result.getSignedAt());
            verify(ramsSignOnRepository).save(any(RAMSSignOn.class));
        }

        @Test
        @DisplayName("signRAMS throws exception when RAMS not found")
        void signRAMS_throwsException_whenRAMSNotFound() {
            // Given
            when(ramsRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.signRAMS(999L, 1L, 1L));
        }

        @Test
        @DisplayName("signRAMS throws exception when operative not found")
        void signRAMS_throwsException_whenOperativeNotFound() {
            // Given
            when(ramsRepository.findById(1L)).thenReturn(Optional.of(testRAMS));
            when(operativeRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.signRAMS(1L, 999L, 1L));
        }

        @Test
        @DisplayName("signRAMS throws exception when site not found")
        void signRAMS_throwsException_whenSiteNotFound() {
            // Given
            when(ramsRepository.findById(1L)).thenReturn(Optional.of(testRAMS));
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(siteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.signRAMS(1L, 1L, 999L));
        }
    }

    // ================================================================
    // PERMIT TESTS
    // ================================================================

    @Nested
    @DisplayName("Permit Tests")
    class PermitTests {

        @Test
        @DisplayName("createPermit creates permit to dig successfully")
        void createPermit_createsPermitToDig() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("permitType", "PERMIT_TO_DIG");
            request.put("siteId", "1");
            request.put("worksDescription", "Excavation for foundations");
            request.put("startDate", "2024-03-01");
            request.put("endDate", "2024-03-15");

            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(permitToDigRepository.save(any(PermitToDig.class))).thenAnswer(invocation -> {
                PermitToDig p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });

            // When
            Map<String, Object> result = (Map<String, Object>) healthSafetyService.createPermit(request);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.get("id"));
            assertNotNull(result.get("permitNumber"));
            assertTrue(result.get("permitNumber").toString().startsWith("PTD-"));
            assertEquals("DRAFT", result.get("status"));
            verify(permitToDigRepository).save(any(PermitToDig.class));
        }

        @Test
        @DisplayName("createPermit throws exception for unknown permit type")
        void createPermit_throwsException_forUnknownPermitType() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("permitType", "UNKNOWN_TYPE");
            request.put("siteId", "1");

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                healthSafetyService.createPermit(request));
        }

        @Test
        @DisplayName("createPermit throws exception when site not found")
        void createPermit_throwsException_whenSiteNotFound() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("permitType", "PERMIT_TO_DIG");
            request.put("siteId", "999");

            when(siteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.createPermit(request));
        }

        @Test
        @DisplayName("createPermit throws exception for invalid request type")
        void createPermit_throwsException_forInvalidRequestType() {
            // Given
            Object invalidRequest = new Object();

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                healthSafetyService.createPermit(invalidRequest));
        }

        @Test
        @DisplayName("approvePermit updates permit status to ISSUED")
        void approvePermit_updatesStatus() {
            // Given
            when(permitToDigRepository.findById(1L)).thenReturn(Optional.of(testPermit));
            when(permitToDigRepository.save(any(PermitToDig.class))).thenAnswer(invocation -> {
                PermitToDig p = invocation.getArgument(0);
                return p;
            });

            // When
            com.crms.dto.response.PermitToDigResponse result = healthSafetyService.approvePermit(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(com.crms.domain.healthsafety.enums.PermitStatus.ISSUED, result.getStatus());
            assertNotNull(result.getIssuedDate());
            verify(permitToDigRepository).save(any(PermitToDig.class));
        }

        @Test
        @DisplayName("approvePermit throws exception when permit not found")
        void approvePermit_throwsException_whenNotFound() {
            // Given
            when(permitToDigRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.approvePermit(999L));
        }
    }

    // ================================================================
    // INCIDENT REPORT TESTS
    // ================================================================

    @Nested
    @DisplayName("Incident Report Tests")
    class IncidentReportTests {

        @Test
        @DisplayName("createIncident creates incident report successfully")
        void createIncident_createsReport() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("siteId", "1");
            request.put("incidentDate", "2024-03-01");
            request.put("description", "Slip and fall incident");
            request.put("type", "NEAR_MISS");
            request.put("severity", "MINOR");
            request.put("locationDescription", "Main entrance");

            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(incidentReportRepository.save(any(IncidentReport.class))).thenAnswer(invocation -> {
                IncidentReport i = invocation.getArgument(0);
                i.setId(1L);
                return i;
            });

            // When
            Map<String, Object> result = (Map<String, Object>) healthSafetyService.createIncident(request);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.get("id"));
            assertNotNull(result.get("reportNumber"));
            assertTrue(result.get("reportNumber").toString().startsWith("INC-"));
            assertEquals("DRAFT", result.get("status"));
            verify(incidentReportRepository).save(any(IncidentReport.class));
        }

        @Test
        @DisplayName("createIncident with operative creates report with operative link")
        void createIncident_withOperative() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("siteId", "1");
            request.put("incidentDate", "2024-03-01");
            request.put("description", "Equipment malfunction");
            request.put("type", "NEAR_MISS");
            request.put("severity", "MINOR");
            request.put("operativeId", "1");

            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(incidentReportRepository.save(any(IncidentReport.class))).thenAnswer(invocation -> {
                IncidentReport i = invocation.getArgument(0);
                i.setId(1L);
                return i;
            });

            // When
            Map<String, Object> result = (Map<String, Object>) healthSafetyService.createIncident(request);

            // Then
            assertNotNull(result);
            verify(incidentReportRepository).save(any(IncidentReport.class));
        }

        @Test
        @DisplayName("createIncident throws exception when site not found")
        void createIncident_throwsException_whenSiteNotFound() {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("siteId", "999");

            when(siteRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                healthSafetyService.createIncident(request));
        }

        @Test
        @DisplayName("createIncident throws exception for invalid request type")
        void createIncident_throwsException_forInvalidRequestType() {
            // Given
            Object invalidRequest = new Object();

            // When/Then
            assertThrows(IllegalArgumentException.class, () -> 
                healthSafetyService.createIncident(invalidRequest));
        }
    }
}
