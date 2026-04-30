package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.enums.CompanyStatus;
import com.crms.domain.company.enums.CompanyType;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.dto.request.CompanyRequest;
import com.crms.dto.response.CISVerificationResponse;
import com.crms.dto.response.CompanyResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.exception.ResourceNotFoundException;
import com.crms.subcontractor.enums.CisStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CompanyServiceImpl.
 * Tests cover company CRUD operations, Companies House integration, and CIS verification.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company testCompany;
    private CompanyRequest companyRequest;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(1L)
                .name("Test Construction Ltd")
                .companyType(CompanyType.SUBCONTRACTOR)
                .registrationNumber("12345678")
                .vatNumber("GB123456789")
                .phone("01234 567890")
                .email("info@testconstruction.co.uk")
                .sicCode("4120")
                .cisStatus(CisStatus.VERIFIED)
                .companiesHouseId("NI123456")
                .hmrcVerificationRef("VRN123456")
                .hmrcDeductionRate(new BigDecimal("20.00"))
                .status(CompanyStatus.ACTIVE)
                .bankName("Test Bank")
                .bankSortCode("123456")
                .bankAccountNumber("12345678")
                .bankAccountName("Test Construction Ltd")
                .build();

        companyRequest = CompanyRequest.builder()
                .name("Test Construction Ltd")
                .companyType(CompanyType.SUBCONTRACTOR)
                .registrationNumber("12345678")
                .vatNumber("GB123456789")
                .phone("01234 567890")
                .email("info@testconstruction.co.uk")
                .sicCode("4120")
                .cisStatus(CisStatus.VERIFIED)
                .companiesHouseId("NI123456")
                .hmrcVerificationRef("VRN123456")
                .hmrcDeductionRate(new BigDecimal("20.00"))
                .bankName("Test Bank")
                .bankSortCode("123456")
                .bankAccountNumber("12345678")
                .bankAccountName("Test Construction Ltd")
                .build();
    }

    // ================================================================
    // CRUD OPERATION TESTS
    // ================================================================

    @Nested
    @DisplayName("CRUD Operation Tests")
    class CrudOperationTests {

        @Test
        @DisplayName("findAll returns paginated companies")
        void findAll_returnsPagedCompanies() {
            // Given
            Page<Company> companyPage = new PageImpl<>(List.of(testCompany));
            when(companyRepository.findAll(any(Pageable.class))).thenReturn(companyPage);

            // When
            PageResponse<CompanyResponse> response = companyService.findAll(Map.of());

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("Test Construction Ltd", response.getContent().get(0).getName());
        }

        @Test
        @DisplayName("findAll filters by company type")
        void findAll_filtersByCompanyType() {
            // Given
            Page<Company> companyPage = new PageImpl<>(List.of(testCompany));
            when(companyRepository.findByCompanyType(eq(CompanyType.SUBCONTRACTOR), any(Pageable.class)))
                    .thenReturn(companyPage);

            // When
            PageResponse<CompanyResponse> response = companyService.findAll(
                    Map.of("type", "SUBCONTRACTOR"));

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            verify(companyRepository).findByCompanyType(eq(CompanyType.SUBCONTRACTOR), any(Pageable.class));
        }

        @Test
        @DisplayName("findById returns company when exists")
        void findById_returnsCompany_whenExists() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            CompanyResponse response = companyService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("Test Construction Ltd", response.getName());
            assertEquals("SUBCONTRACTOR", response.getCompanyType());
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> companyService.findById(999L));
        }

        @Test
        @DisplayName("create saves company with all fields")
        void create_savesCompanyWithAllFields() {
            // Given
            when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> {
                Company company = invocation.getArgument(0);
                company.setId(1L);
                return company;
            });

            // When
            CompanyResponse response = companyService.create(companyRequest);

            // Then
            assertNotNull(response);
            assertEquals("Test Construction Ltd", response.getName());
            assertEquals("SUBCONTRACTOR", response.getCompanyType());
            assertEquals("VERIFIED", response.getCisStatus());
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("update modifies company fields")
        void update_modifiesCompanyFields() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
            when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

            CompanyRequest updateRequest = CompanyRequest.builder()
                    .name("Updated Construction Ltd")
                    .companyType(CompanyType.SUBCONTRACTOR)
                    .build();

            // When
            CompanyResponse response = companyService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(companyRepository).save(any(Company.class));
        }

        @Test
        @DisplayName("update throws exception when company not found")
        void update_throwsException_whenNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class,
                    () -> companyService.update(999L, companyRequest));
        }

        @Test
        @DisplayName("delete removes company")
        void delete_removesCompany() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
            doNothing().when(companyRepository).delete(testCompany);

            // When
            companyService.delete(1L);

            // Then
            verify(companyRepository).delete(testCompany);
        }

        @Test
        @DisplayName("delete throws exception when company not found")
        void delete_throwsException_whenNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> companyService.delete(999L));
        }
    }

    // ================================================================
    // COMPANIES HOUSE INTEGRATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Companies House Integration Tests")
    class CompaniesHouseIntegrationTests {

        @Test
        @DisplayName("refreshCompaniesHouse returns company data")
        void refreshCompaniesHouse_returnsCompanyData() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            CompanyResponse response = companyService.refreshCompaniesHouse(1L);

            // Then
            assertNotNull(response);
            assertEquals("Test Construction Ltd", response.getName());
        }

        @Test
        @DisplayName("refreshCompaniesHouse throws exception when company not found")
        void refreshCompaniesHouse_throwsException_whenNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class,
                    () -> companyService.refreshCompaniesHouse(999L));
        }
    }

    // ================================================================
    // CIS VERIFICATION TESTS
    // ================================================================

    @Nested
    @DisplayName("CIS Verification Tests")
    class CISVerificationTests {

        @Test
        @DisplayName("verifyCIS returns verification response")
        void verifyCIS_returnsVerificationResponse() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            CISVerificationResponse response = companyService.verifyCIS(1L);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.getCompanyId());
            assertEquals("Test Construction Ltd", response.getCompanyName());
            assertEquals("VERIFIED", response.getVerificationStatus());
            assertEquals("20.00", response.getHmrcDeductionRate());
            assertNotNull(response.getVerifiedAt());
        }

        @Test
        @DisplayName("verifyCIS returns UNKNOWN status when cisStatus is null")
        void verifyCIS_returnsUnknownStatus_whenCisStatusNull() {
            // Given
            testCompany.setCisStatus(null);
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            CISVerificationResponse response = companyService.verifyCIS(1L);

            // Then
            assertNotNull(response);
            assertEquals("UNKNOWN", response.getVerificationStatus());
        }

        @Test
        @DisplayName("verifyCIS throws exception when company not found")
        void verifyCIS_throwsException_whenNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> companyService.verifyCIS(999L));
        }
    }

    // ================================================================
    // SUBBIE GATE STATUS TESTS
    // ================================================================

    @Nested
    @DisplayName("Subbie Gate Status Tests")
    class SubbieGateStatusTests {

        @Test
        @DisplayName("getSubbieGateStatus returns gate status with HMRC verified flag")
        void getSubbieGateStatus_returnsStatusWithHMRCFlag() {
            // Given
            testCompany.setHmrcVerificationRef("VRN123456");
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            SubbieGateStatus status = companyService.getSubbieGateStatus(1L);

            // Then
            assertNotNull(status);
            assertEquals(1L, status.getOperativeId());
            assertTrue(status.isHMRCVerified());
            assertTrue(status.isCSCSValid());
            assertTrue(status.isRAMSValid());
            assertTrue(status.isInductionValid());
            assertTrue(status.isPlantTicketValid());
            assertTrue(status.isGateOpen());
        }

        @Test
        @DisplayName("getSubbieGateStatus returns HMRC not verified when ref is null")
        void getSubbieGateStatus_returnsNotVerified_whenRefNull() {
            // Given
            testCompany.setHmrcVerificationRef(null);
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            SubbieGateStatus status = companyService.getSubbieGateStatus(1L);

            // Then
            assertNotNull(status);
            assertFalse(status.isHMRCVerified());
        }

        @Test
        @DisplayName("getSubbieGateStatus throws exception when company not found")
        void getSubbieGateStatus_throwsException_whenNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class,
                    () -> companyService.getSubbieGateStatus(999L));
        }
    }

    // ================================================================
    // BANK ACCOUNT MASKING TESTS
    // ================================================================

    @Nested
    @DisplayName("Bank Account Masking Tests")
    class BankAccountMaskingTests {

        @Test
        @DisplayName("bank account number is masked in response")
        void bankAccountNumber_isMasked() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            CompanyResponse response = companyService.findById(1L);

            // Then
            assertNotNull(response);
            assertTrue(response.getBankAccountNumber().startsWith("****"));
            assertEquals("****5678", response.getBankAccountNumber());
        }

        @Test
        @DisplayName("short bank account number is not masked")
        void shortBankAccountNumber_notMasked() {
            // Given
            testCompany.setBankAccountNumber("123");
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            CompanyResponse response = companyService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("123", response.getBankAccountNumber());
        }

        @Test
        @DisplayName("null bank account number is handled correctly")
        void nullBankAccountNumber_handledCorrectly() {
            // Given
            testCompany.setBankAccountNumber(null);
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));

            // When
            CompanyResponse response = companyService.findById(1L);

            // Then
            assertNotNull(response);
            assertNull(response.getBankAccountNumber());
        }
    }

    // ================================================================
    // PAGINATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("findAll uses default pagination when params missing")
        void findAll_usesDefaultPagination() {
            // Given
            Page<Company> companyPage = new PageImpl<>(List.of(testCompany));
            when(companyRepository.findAll(any(Pageable.class))).thenReturn(companyPage);

            // When
            PageResponse<CompanyResponse> response = companyService.findAll(Map.of());

            // Then
            assertNotNull(response);
            assertEquals(0, response.getPage());
            assertEquals(20, response.getSize());
        }

        @Test
        @DisplayName("findAll uses custom pagination when params provided")
        void findAll_usesCustomPagination() {
            // Given
            Page<Company> companyPage = new PageImpl<>(List.of(testCompany));
            when(companyRepository.findAll(any(Pageable.class))).thenReturn(companyPage);

            // When
            PageResponse<CompanyResponse> response = companyService.findAll(
                    Map.of("page", "2", "size", "10", "sort", "name"));

            // Then
            assertNotNull(response);
            assertEquals(2, response.getPage());
            assertEquals(10, response.getSize());
        }
    }
}
