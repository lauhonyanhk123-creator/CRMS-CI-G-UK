package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.entity.Contact;
import com.crms.domain.company.enums.CompanyType;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.company.repository.ContactRepository;
import com.crms.dto.request.ContactRequest;
import com.crms.dto.response.ContactResponse;
import com.crms.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContactServiceImpl.
 * Covers CRUD operations, company reassignment during update, and not-found error paths.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private Company testCompany;
    private Company otherCompany;
    private Contact testContact;
    private ContactRequest testRequest;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(100L)
                .name("Acme Construction Ltd")
                .companyType(CompanyType.CLIENT)
                .build();

        otherCompany = Company.builder()
                .id(200L)
                .name("Beta Contractors Ltd")
                .companyType(CompanyType.SUBCONTRACTOR)
                .build();

        testContact = Contact.builder()
                .id(1L)
                .company(testCompany)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@acme.com")
                .phone("01234 567890")
                .mobile("07700 900123")
                .jobTitle("Site Manager")
                .isPrimary(true)
                .notes("Main point of contact")
                .build();

        testRequest = ContactRequest.builder()
                .companyId(100L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@acme.com")
                .phone("01234 567890")
                .mobile("07700 900123")
                .jobTitle("Site Manager")
                .isPrimary(true)
                .notes("Main point of contact")
                .build();

        // Default save answer – return whatever entity is passed in
        when(contactRepository.save(any(Contact.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("returns response when contact exists")
        void findById_existingContact_returnsResponse() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));

            ContactResponse response = contactService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getFirstName()).isEqualTo("Jane");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getEmail()).isEqualTo("jane.smith@acme.com");
            assertThat(response.getCompanyId()).isEqualTo(100L);
            assertThat(response.getCompanyName()).isEqualTo("Acme Construction Ltd");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact not found")
        void findById_notFound_throwsResourceNotFoundException() {
            when(contactRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.findById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates and returns contact when company exists")
        void create_validRequest_returnsContactResponse() {
            when(companyRepository.findById(100L)).thenReturn(Optional.of(testCompany));

            ContactResponse response = contactService.create(testRequest);

            assertThat(response).isNotNull();
            assertThat(response.getFirstName()).isEqualTo("Jane");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getEmail()).isEqualTo("jane.smith@acme.com");
            assertThat(response.getCompanyId()).isEqualTo(100L);
            verify(contactRepository).save(any(Contact.class));
        }

        @Test
        @DisplayName("sets isPrimary to false when not specified in request")
        void create_noPrimaryFlag_defaultsFalse() {
            ContactRequest requestNoPrimary = ContactRequest.builder()
                    .companyId(100L)
                    .firstName("Bob")
                    .lastName("Jones")
                    .isPrimary(null)
                    .build();
            when(companyRepository.findById(100L)).thenReturn(Optional.of(testCompany));

            // Capture the saved entity via the answer stub
            ContactResponse response = contactService.create(requestNoPrimary);

            assertThat(response.getIsPrimary()).isFalse();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when company not found")
        void create_companyNotFound_throwsResourceNotFoundException() {
            when(companyRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.create(testRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Company");
        }

        @Test
        @DisplayName("persists all provided fields")
        void create_allFieldsProvided_persistsCorrectly() {
            when(companyRepository.findById(100L)).thenReturn(Optional.of(testCompany));

            ContactResponse response = contactService.create(testRequest);

            assertThat(response.getPhone()).isEqualTo("01234 567890");
            assertThat(response.getMobile()).isEqualTo("07700 900123");
            assertThat(response.getJobTitle()).isEqualTo("Site Manager");
            assertThat(response.getIsPrimary()).isTrue();
            assertThat(response.getNotes()).isEqualTo("Main point of contact");
        }
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("updates fields on existing contact")
        void update_existingContact_updatesFields() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));

            ContactRequest updatedRequest = ContactRequest.builder()
                    .companyId(100L)
                    .firstName("Janet")
                    .lastName("Smith-Jones")
                    .email("janet.sj@acme.com")
                    .phone("01234 000000")
                    .mobile("07700 000000")
                    .jobTitle("Senior Site Manager")
                    .isPrimary(false)
                    .notes("Updated notes")
                    .build();

            ContactResponse response = contactService.update(1L, updatedRequest);

            assertThat(response.getFirstName()).isEqualTo("Janet");
            assertThat(response.getLastName()).isEqualTo("Smith-Jones");
            assertThat(response.getEmail()).isEqualTo("janet.sj@acme.com");
            assertThat(response.getJobTitle()).isEqualTo("Senior Site Manager");
            assertThat(response.getIsPrimary()).isFalse();
            verify(contactRepository).save(testContact);
        }

        @Test
        @DisplayName("reassigns company when companyId changes during update")
        void update_differentCompanyId_reassignsCompany() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            when(companyRepository.findById(200L)).thenReturn(Optional.of(otherCompany));

            ContactRequest requestWithNewCompany = ContactRequest.builder()
                    .companyId(200L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@beta.com")
                    .isPrimary(true)
                    .build();

            ContactResponse response = contactService.update(1L, requestWithNewCompany);

            assertThat(response.getCompanyId()).isEqualTo(200L);
            assertThat(response.getCompanyName()).isEqualTo("Beta Contractors Ltd");
            verify(companyRepository).findById(200L);
        }

        @Test
        @DisplayName("does not reload company when companyId is unchanged")
        void update_sameCompanyId_doesNotReloadCompany() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));

            ContactRequest sameCompanyRequest = ContactRequest.builder()
                    .companyId(100L)   // same as existing
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@acme.com")
                    .isPrimary(true)
                    .build();

            contactService.update(1L, sameCompanyRequest);

            verify(companyRepository, never()).findById(100L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact not found on update")
        void update_contactNotFound_throwsResourceNotFoundException() {
            when(contactRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.update(99L, testRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when new company not found during update")
        void update_newCompanyNotFound_throwsResourceNotFoundException() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));
            when(companyRepository.findById(200L)).thenReturn(Optional.empty());

            ContactRequest requestWithMissingCompany = ContactRequest.builder()
                    .companyId(200L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            assertThatThrownBy(() -> contactService.update(1L, requestWithMissingCompany))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Company");
        }
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deletes contact when it exists")
        void delete_existingContact_callsRepositoryDelete() {
            when(contactRepository.findById(1L)).thenReturn(Optional.of(testContact));

            contactService.delete(1L);

            verify(contactRepository).delete(testContact);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact not found on delete")
        void delete_contactNotFound_throwsResourceNotFoundException() {
            when(contactRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("returns all contacts when no companyId filter is supplied")
        void findAll_noFilter_returnsAllContacts() {
            Page<Contact> page = new PageImpl<>(List.of(testContact));
            when(contactRepository.findAll(any(Pageable.class))).thenReturn(page);

            var response = contactService.findAll(Map.of("page", "0", "size", "20"));

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getFirstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("delegates to findByCompanyId when companyId param is present")
        void findAll_withCompanyIdFilter_delegatesToFindByCompanyId() {
            Page<Contact> page = new PageImpl<>(List.of(testContact));
            when(contactRepository.findByCompanyId(eq(100L), any(Pageable.class))).thenReturn(page);

            var response = contactService.findAll(Map.of("companyId", "100"));

            assertThat(response.getContent()).hasSize(1);
            verify(contactRepository).findByCompanyId(eq(100L), any(Pageable.class));
            verify(contactRepository, never()).findAll(any(Pageable.class));
        }
    }
}
