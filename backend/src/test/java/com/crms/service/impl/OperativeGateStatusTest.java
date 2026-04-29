package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.healthsafety.entity.RAMSSignOn;
import com.crms.domain.healthsafety.repository.RAMSSignOnRepository;
import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Induction;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.enums.CardType;
import com.crms.domain.operative.enums.OperativeStatus;
import com.crms.domain.operative.enums.QualificationType;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.InductionRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.operative.repository.QualificationRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.OperativeRequest;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OperativeServiceImpl gate status logic.
 * Tests cover smartCheckCard and getSubbieGateStatus methods with their
 * various validation scenarios including CSCS card, RAMS, induction,
 * and plant ticket checks.
 */
@ExtendWith(MockitoExtension.class)
class OperativeGateStatusTest {

    @Mock
    private OperativeRepository operativeRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private RAMSSignOnRepository ramsSignOnRepository;

    @Mock
    private InductionRepository inductionRepository;

    @Mock
    private QualificationRepository qualificationRepository;

    @InjectMocks
    private OperativeServiceImpl operativeService;

    // Test data
    private Operative testOperative;
    private Card validCSCSCard;
    private Card validCPCSCard;
    private Card expiredCard;
    private Card nullExpiryCard;
    private RAMSSignOn validRAMSSignOn;
    private RAMSSignOn expiredRAMSSignOn;
    private Induction validInduction;
    private Induction expiredInduction;
    private Qualification validNPORSQualification;
    private Qualification validCPCSQualification;
    private Qualification expiredQualification;

    @BeforeEach
    void setUp() {
        testOperative = Operative.builder()
                .id(1L)
                .employeeRef("OP-001")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .hmrcVerified(true)
                .status(OperativeStatus.ACTIVE)
                .cards(new ArrayList<>())
                .build();

        // Valid CSCS card
        validCSCSCard = Card.builder()
                .id(1L)
                .cardType(CardType.CSCS)
                .cardNumber("CSCS-123456")
                .expiryDate(LocalDate.now().plusMonths(6))
                .operative(testOperative)
                .build();

        // Valid CPCS card
        validCPCSCard = Card.builder()
                .id(2L)
                .cardType(CardType.CPCS)
                .cardNumber("CPCS-789012")
                .expiryDate(LocalDate.now().plusMonths(12))
                .operative(testOperative)
                .build();

        // Expired card
        expiredCard = Card.builder()
                .id(3L)
                .cardType(CardType.CSCS)
                .cardNumber("CSCS-EXPIRED")
                .expiryDate(LocalDate.now().minusDays(1))
                .operative(testOperative)
                .build();

        // Card with null expiry
        nullExpiryCard = Card.builder()
                .id(4L)
                .cardType(CardType.CSCS)
                .cardNumber("CSCS-NULL-EXPIRY")
                .expiryDate(null)
                .operative(testOperative)
                .build();

        // Valid RAMS sign-on
        validRAMSSignOn = RAMSSignOn.builder()
                .id(1L)
                .operative(testOperative)
                .signedAt(LocalDateTime.now().minusHours(2))
                .validUntil(LocalDateTime.now().plusDays(7))
                .build();

        // Expired RAMS sign-on
        expiredRAMSSignOn = RAMSSignOn.builder()
                .id(2L)
                .operative(testOperative)
                .signedAt(LocalDateTime.now().minusDays(10))
                .validUntil(LocalDateTime.now().minusDays(1))
                .build();

        // Valid induction
        validInduction = Induction.builder()
                .id(1L)
                .operative(testOperative)
                .inductedAt(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusMonths(1))
                .build();

        // Expired induction
        expiredInduction = Induction.builder()
                .id(2L)
                .operative(testOperative)
                .inductedAt(LocalDateTime.now().minusMonths(2))
                .validUntil(LocalDateTime.now().minusDays(1))
                .build();

        // Valid NPORS qualification
        validNPORSQualification = Qualification.builder()
                .id(1L)
                .operative(testOperative)
                .qualificationType(QualificationType.NPORS)
                .expiryDate(LocalDate.now().plusYears(1))
                .build();

        // Valid CPCS qualification
        validCPCSQualification = Qualification.builder()
                .id(2L)
                .operative(testOperative)
                .qualificationType(QualificationType.CPCS)
                .expiryDate(LocalDate.now().plusYears(2))
                .build();

        // Expired qualification
        expiredQualification = Qualification.builder()
                .id(3L)
                .operative(testOperative)
                .qualificationType(QualificationType.NPORS)
                .expiryDate(LocalDate.now().minusDays(1))
                .build();
    }

    // ========================================================================
    // smartCheckCard - Gate Status Logic Tests
    // ========================================================================

    @Nested
    @DisplayName("smartCheckCard - Gate Opens Only When CSCS/CPCS Card is Valid")
    class SmartCheckCardTests {

        @Test
        @DisplayName("Gate opens when operative has valid CSCS card")
        void gateOpens_whenValidCSCSCard() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isGateOpen());
            assertTrue(status.isCSCSValid());
            assertFalse(status.isRAMSValid());
            assertFalse(status.isInductionValid());
            assertFalse(status.isPlantTicketValid());
            assertEquals("RAMS sign-on required for this site", status.getStatusMessage());
        }

        @Test
        @DisplayName("Gate opens when operative has valid CPCS card")
        void gateOpens_whenValidCPCSCard() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(validCPCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 2L);

            // Then
            assertTrue(status.isGateOpen());
            assertTrue(status.isCSCSValid()); // isCSCSValid reflects card validity
            assertEquals("RAMS sign-on required for this site", status.getStatusMessage());
        }

        @Test
        @DisplayName("Gate locked when card is expired")
        void gateLocked_whenCardExpired() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(3L)).thenReturn(Optional.of(expiredCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 3L);

            // Then
            assertFalse(status.isGateOpen());
            assertFalse(status.isCSCSValid());
            assertEquals("CSCS/CPCS card missing or expired — gate locked", status.getStatusMessage());
        }

        @Test
        @DisplayName("Gate locked when card has null expiry")
        void gateLocked_whenCardExpiryIsNull() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(4L)).thenReturn(Optional.of(nullExpiryCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 4L);

            // Then
            assertFalse(status.isGateOpen());
            assertFalse(status.isCSCSValid());
            assertEquals("CSCS/CPCS card missing or expired — gate locked", status.getStatusMessage());
        }

        @Test
        @DisplayName("Gate locked when card expires today (considered expired)")
        void gateLocked_whenCardExpiresToday() {
            // Given
            Card cardExpiringToday = Card.builder()
                    .id(5L)
                    .cardType(CardType.CSCS)
                    .cardNumber("CSCS-TODAY")
                    .expiryDate(LocalDate.now())
                    .operative(testOperative)
                    .build();

            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(5L)).thenReturn(Optional.of(cardExpiringToday));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 5L);

            // Then
            assertFalse(status.isGateOpen());
            assertFalse(status.isCSCSValid());
        }

        @Test
        @DisplayName("Advisory message for missing RAMS when card is valid")
        void advisoryMessage_forMissingRAMS() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isGateOpen()); // Gate still open due to valid card
            assertFalse(status.isRAMSValid());
            assertEquals("RAMS sign-on required for this site", status.getStatusMessage());
        }

        @Test
        @DisplayName("Advisory message for missing induction when card is valid")
        void advisoryMessage_forMissingInduction() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isGateOpen());
            assertFalse(status.isInductionValid());
            assertEquals("Site induction required", status.getStatusMessage());
        }

        @Test
        @DisplayName("Advisory message for missing plant ticket")
        void advisoryMessage_forMissingPlantTicket() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isGateOpen());
            assertFalse(status.isPlantTicketValid());
            assertEquals("Plant ticket recommended — contact supervisor", status.getStatusMessage());
        }

        @Test
        @DisplayName("All checks passed message when everything is valid")
        void allChecksPassed_whenAllValid() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isGateOpen());
            assertTrue(status.isCSCSValid());
            assertTrue(status.isRAMSValid());
            assertTrue(status.isInductionValid());
            assertTrue(status.isPlantTicketValid());
            assertEquals("All checks passed — gate open", status.getStatusMessage());
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when operative not found")
        void throwsException_whenOperativeNotFound() {
            // Given
            when(operativeRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, 
                    () -> operativeService.smartCheckCard(999L, 1L));
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when card not found")
        void throwsException_whenCardNotFound() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, 
                    () -> operativeService.smartCheckCard(1L, 999L));
        }
    }

    // ========================================================================
    // smartCheckCard - Plant Ticket Qualification Types Tests
    // ========================================================================

    @Nested
    @DisplayName("smartCheckCard - Plant Ticket Qualification Type Recognition")
    class PlantTicketTypeTests {

        @Test
        @DisplayName("NPORS qualification grants plant ticket validity")
        void nporsQualification_valid() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isPlantTicketValid());
        }

        @Test
        @DisplayName("CPCS qualification grants plant ticket validity")
        void cpcsQualification_valid() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validCPCSQualification));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isPlantTicketValid());
        }

        @Test
        @DisplayName("CPCS_BLUE qualification grants plant ticket validity")
        void cpcsBlueQualification_valid() {
            // Given
            Qualification cpcsBlue = Qualification.builder()
                    .id(4L)
                    .operative(testOperative)
                    .qualificationType(QualificationType.CPCS_BLUE)
                    .expiryDate(LocalDate.now().plusYears(1))
                    .build();

            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(cpcsBlue));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isPlantTicketValid());
        }

        @Test
        @DisplayName("CITY_AND_GUILDS qualification grants plant ticket validity")
        void cityAndGuildsQualification_valid() {
            // Given
            Qualification cityAndGuilds = Qualification.builder()
                    .id(5L)
                    .operative(testOperative)
                    .qualificationType(QualificationType.CITY_AND_GUILDS)
                    .expiryDate(LocalDate.now().plusYears(1))
                    .build();

            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(cityAndGuilds));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isPlantTicketValid());
        }

        @Test
        @DisplayName("Expired qualification does not grant plant ticket validity")
        void expiredQualification_invalid() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(expiredQualification));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertFalse(status.isPlantTicketValid());
            assertEquals("Plant ticket recommended — contact supervisor", status.getStatusMessage());
        }

        @Test
        @DisplayName("Valid qualification with null expiry grants plant ticket validity")
        void nullExpiryQualification_stillValid() {
            // Given
            Qualification nullExpiryQual = Qualification.builder()
                    .id(6L)
                    .operative(testOperative)
                    .qualificationType(QualificationType.NPORS)
                    .expiryDate(null)
                    .build();

            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(nullExpiryQual));

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isPlantTicketValid());
        }
    }

    // ========================================================================
    // smartCheckCard - RAMS Validity Tests
    // ========================================================================

    @Nested
    @DisplayName("smartCheckCard - RAMS Validity Checks")
    class RAMSValidityTests {

        @Test
        @DisplayName("RAMS is valid when validUntil is in the future")
        void ramsValid_whenValidUntilFuture() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isRAMSValid());
        }

        @Test
        @DisplayName("RAMS is valid when validUntil is null")
        void ramsValid_whenValidUntilNull() {
            // Given
            RAMSSignOn noExpiryRAMS = RAMSSignOn.builder()
                    .id(3L)
                    .operative(testOperative)
                    .signedAt(LocalDateTime.now().minusHours(2))
                    .validUntil(null)
                    .build();

            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(noExpiryRAMS));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isRAMSValid());
        }

        @Test
        @DisplayName("RAMS is invalid when validUntil is in the past")
        void ramsInvalid_whenValidUntilPast() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(expiredRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertFalse(status.isRAMSValid());
            assertEquals("RAMS sign-on required for this site", status.getStatusMessage());
        }

        @Test
        @DisplayName("RAMS is valid when at least one sign-on is valid")
        void ramsValid_whenOneSignOnValid() {
            // Given - one valid, one expired
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L))
                    .thenReturn(List.of(validRAMSSignOn, expiredRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isRAMSValid());
        }
    }

    // ========================================================================
    // smartCheckCard - Induction Validity Tests
    // ========================================================================

    @Nested
    @DisplayName("smartCheckCard - Induction Validity Checks")
    class InductionValidityTests {

        @Test
        @DisplayName("Induction is valid when validUntil is in the future")
        void inductionValid_whenValidUntilFuture() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isInductionValid());
        }

        @Test
        @DisplayName("Induction is valid when validUntil is null")
        void inductionValid_whenValidUntilNull() {
            // Given
            Induction noExpiryInduction = Induction.builder()
                    .id(3L)
                    .operative(testOperative)
                    .inductedAt(LocalDateTime.now().minusDays(1))
                    .validUntil(null)
                    .build();

            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(noExpiryInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isInductionValid());
        }

        @Test
        @DisplayName("Induction is invalid when validUntil is in the past")
        void inductionInvalid_whenValidUntilPast() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(expiredInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertFalse(status.isInductionValid());
            assertEquals("Site induction required", status.getStatusMessage());
        }

        @Test
        @DisplayName("Induction is valid when at least one induction is valid")
       void inductionValid_whenOneInductionValid() {
            // Given - one valid, one expired
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L))
                    .thenReturn(List.of(validInduction, expiredInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertTrue(status.isInductionValid());
        }
    }

    // ========================================================================
    // getSubbieGateStatus - Tests
    // ========================================================================

    @Nested
    @DisplayName("getSubbieGateStatus - Aggregate Card Check Tests")
    class GetSubbieGateStatusTests {

        @Test
        @DisplayName("Gate open when operative has at least one valid CSCS card")
        void gateOpen_whenOneValidCSCSCard() {
            // Given
            testOperative.getCards().add(validCSCSCard);
            testOperative.getCards().add(expiredCard);
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.getSubbieGateStatus(1L);

            // Then
            assertTrue(status.isGateOpen());
            assertTrue(status.isCSCSValid());
            assertEquals("Gate Open — all checks passed", status.getStatusMessage());
        }

        @Test
        @DisplayName("Gate open when operative has at least one valid CPCS card")
        void gateOpen_whenOneValidCPCSCard() {
            // Given
            testOperative.getCards().add(validCPCSCard);
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.getSubbieGateStatus(1L);

            // Then
            assertTrue(status.isGateOpen());
            assertTrue(status.isCSCSValid());
        }

        @Test
        @DisplayName("Gate closed when operative has no valid cards")
        void gateClosed_whenNoValidCards() {
            // Given
            testOperative.getCards().add(expiredCard);
            testOperative.getCards().add(nullExpiryCard);
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.getSubbieGateStatus(1L);

            // Then
            assertFalse(status.isGateOpen());
            assertFalse(status.isCSCSValid());
            assertEquals("Card validation required — CSCS/CPCS card missing or expired", status.getStatusMessage());
        }

        @Test
        @DisplayName("Gate closed when operative has no cards at all")
        void gateClosed_whenNoCards() {
            // Given - operative with no cards
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.getSubbieGateStatus(1L);

            // Then
            assertFalse(status.isGateOpen());
            assertFalse(status.isCSCSValid());
        }

        @Test
        @DisplayName("Status message reflects first failed check")
        void statusMessage_reflectsFirstFailedCheck() {
            // Given - RAMS missing
            testOperative.getCards().add(validCSCSCard);
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.getSubbieGateStatus(1L);

            // Then
            assertTrue(status.isGateOpen()); // Card valid, gate still opens
            assertEquals("RAMS sign-on required", status.getStatusMessage());
        }

        @Test
        @DisplayName("HMRC verified flag is correctly propagated")
        void hmrcVerified_flagPropagated() {
            // Given
            testOperative.setHmrcVerified(true);
            testOperative.getCards().add(validCSCSCard);
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(List.of(validNPORSQualification));

            // When
            SubbieGateStatus status = operativeService.getSubbieGateStatus(1L);

            // Then
            assertTrue(status.isHMRCVerified());
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when operative not found")
        void throwsException_whenOperativeNotFound() {
            // Given
            when(operativeRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, 
                    () -> operativeService.getSubbieGateStatus(999L));
        }
    }

    // ========================================================================
    // Status Message Priority Tests
    // ========================================================================

    @Nested
    @DisplayName("Status Message Priority")
    class StatusMessagePriorityTests {

        @Test
        @DisplayName("Card message takes precedence over RAMS message")
        void cardMessage_takesPrecedence() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(3L)).thenReturn(Optional.of(expiredCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 3L);

            // Then
            assertThat(status.getStatusMessage())
                    .isEqualTo("CSCS/CPCS card missing or expired — gate locked");
        }

        @Test
        @DisplayName("RAMS message takes precedence over induction message")
        void ramsMessage_takesPrecedence() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertThat(status.getStatusMessage())
                    .isEqualTo("RAMS sign-on required for this site");
        }

        @Test
        @DisplayName("Induction message takes precedence over plant ticket message")
        void inductionMessage_takesPrecedence() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertThat(status.getStatusMessage())
                    .isEqualTo("Site induction required");
        }

        @Test
        @DisplayName("Plant ticket message shown when all else valid")
        void plantTicketMessage_whenAllElseValid() {
            // Given
            when(operativeRepository.findById(1L)).thenReturn(Optional.of(testOperative));
            when(cardRepository.findById(1L)).thenReturn(Optional.of(validCSCSCard));
            when(ramsSignOnRepository.findByOperativeId(1L)).thenReturn(List.of(validRAMSSignOn));
            when(inductionRepository.findByOperativeId(1L)).thenReturn(List.of(validInduction));
            when(qualificationRepository.findByOperativeId(1L)).thenReturn(Collections.emptyList());

            // When
            SubbieGateStatus status = operativeService.smartCheckCard(1L, 1L);

            // Then
            assertThat(status.getStatusMessage())
                    .isEqualTo("Plant ticket recommended — contact supervisor");
        }
    }
}