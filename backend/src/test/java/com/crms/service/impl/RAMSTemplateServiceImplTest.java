package com.crms.service.impl;

import com.crms.domain.healthsafety.entity.RAMSTemplate;
import com.crms.domain.healthsafety.repository.RAMSTemplateRepository;
import com.crms.dto.request.RAMSTemplateRequest;
import com.crms.dto.response.RAMSTemplateResponse;
import com.crms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RAMSTemplateServiceImpl")
class RAMSTemplateServiceImplTest {

    @Mock
    private RAMSTemplateRepository ramsTemplateRepository;

    @InjectMocks
    private RAMSTemplateServiceImpl service;

    private RAMSTemplate existingTemplate;

    @BeforeEach
    void setUp() {
        existingTemplate = RAMSTemplate.builder()
                .id(1L)
                .title("Scaffolding RAMS")
                .description("Detailed scaffolding procedure")
                .trade("Scaffolding")
                .riskAssessment("Risk level: medium")
                .methodStatement("Erect scaffold per BS EN 12811")
                .ppeRequired("Hard hat, harness, gloves")
                .frequencyDays(90)
                .isActive(true)
                .build();
    }

    // ---------------------------------------------------------------------------
    // copyTemplate
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("copyTemplate()")
    class CopyTemplate {

        @Test
        @DisplayName("copies all fields from original and uses the supplied new title")
        void copiesTemplateWithNewTitle() {
            RAMSTemplate copy = RAMSTemplate.builder()
                    .id(2L)
                    .title("Scaffolding RAMS v2")
                    .description(existingTemplate.getDescription())
                    .trade(existingTemplate.getTrade())
                    .riskAssessment(existingTemplate.getRiskAssessment())
                    .methodStatement(existingTemplate.getMethodStatement())
                    .ppeRequired(existingTemplate.getPpeRequired())
                    .frequencyDays(existingTemplate.getFrequencyDays())
                    .isActive(true)
                    .build();

            when(ramsTemplateRepository.findById(1L)).thenReturn(Optional.of(existingTemplate));
            when(ramsTemplateRepository.save(any(RAMSTemplate.class))).thenReturn(copy);

            RAMSTemplateResponse response = service.copyTemplate(1L, "Scaffolding RAMS v2");

            assertThat(response.getTitle()).isEqualTo("Scaffolding RAMS v2");
            assertThat(response.getDescription()).isEqualTo(existingTemplate.getDescription());
            assertThat(response.getTrade()).isEqualTo(existingTemplate.getTrade());
            assertThat(response.getRiskAssessment()).isEqualTo(existingTemplate.getRiskAssessment());
            assertThat(response.getMethodStatement()).isEqualTo(existingTemplate.getMethodStatement());
            assertThat(response.getPpeRequired()).isEqualTo(existingTemplate.getPpeRequired());
            assertThat(response.getFrequencyDays()).isEqualTo(existingTemplate.getFrequencyDays());
            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("the saved copy is always active regardless of the original's state")
        void copiedTemplateIsAlwaysActive() {
            RAMSTemplate inactive = RAMSTemplate.builder()
                    .id(3L)
                    .title("Old RAMS")
                    .trade("Electrical")
                    .frequencyDays(60)
                    .isActive(false)
                    .build();

            RAMSTemplate copy = RAMSTemplate.builder()
                    .id(4L)
                    .title("New RAMS Copy")
                    .trade("Electrical")
                    .frequencyDays(60)
                    .isActive(true)
                    .build();

            when(ramsTemplateRepository.findById(3L)).thenReturn(Optional.of(inactive));
            when(ramsTemplateRepository.save(any(RAMSTemplate.class))).thenReturn(copy);

            RAMSTemplateResponse response = service.copyTemplate(3L, "New RAMS Copy");

            ArgumentCaptor<RAMSTemplate> captor = ArgumentCaptor.forClass(RAMSTemplate.class);
            verify(ramsTemplateRepository).save(captor.capture());
            assertThat(captor.getValue().getIsActive()).isTrue();
            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("saved entity has the exact title supplied to copyTemplate")
        void savedCopyHasSuppliedTitle() {
            when(ramsTemplateRepository.findById(1L)).thenReturn(Optional.of(existingTemplate));
            when(ramsTemplateRepository.save(any(RAMSTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            service.copyTemplate(1L, "Brand New Title");

            ArgumentCaptor<RAMSTemplate> captor = ArgumentCaptor.forClass(RAMSTemplate.class);
            verify(ramsTemplateRepository).save(captor.capture());
            assertThat(captor.getValue().getTitle()).isEqualTo("Brand New Title");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when template is not found")
        void throwsResourceNotFoundWhenTemplateDoesNotExist() {
            when(ramsTemplateRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.copyTemplate(99L, "Any Title"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("RAMSTemplate");

            verify(ramsTemplateRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates template with all supplied fields")
        void createsTemplateWithAllFieldsFromRequest() {
            RAMSTemplateRequest request = new RAMSTemplateRequest();
            request.setTitle("Electrical RAMS");
            request.setDescription("Electrical works procedure");
            request.setTrade("Electrical");
            request.setRiskAssessment("Medium risk");
            request.setMethodStatement("Isolate supply before work");
            request.setPpeRequired("Insulated gloves, safety boots");
            request.setFrequencyDays(60);
            request.setIsActive(true);

            RAMSTemplate saved = RAMSTemplate.builder()
                    .id(5L)
                    .title("Electrical RAMS")
                    .description("Electrical works procedure")
                    .trade("Electrical")
                    .riskAssessment("Medium risk")
                    .methodStatement("Isolate supply before work")
                    .ppeRequired("Insulated gloves, safety boots")
                    .frequencyDays(60)
                    .isActive(true)
                    .build();

            when(ramsTemplateRepository.save(any(RAMSTemplate.class))).thenReturn(saved);

            RAMSTemplateResponse response = service.create(request);

            assertThat(response.getId()).isEqualTo(5L);
            assertThat(response.getTitle()).isEqualTo("Electrical RAMS");
            assertThat(response.getTrade()).isEqualTo("Electrical");
            assertThat(response.getFrequencyDays()).isEqualTo(60);
            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("defaults frequencyDays to 90 when not provided in the request")
        void defaultsFrequencyDaysToNinetyWhenNotProvided() {
            RAMSTemplateRequest request = new RAMSTemplateRequest();
            request.setTitle("Plumbing RAMS");
            request.setTrade("Plumbing");
            request.setFrequencyDays(null);
            request.setIsActive(null);

            when(ramsTemplateRepository.save(any(RAMSTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            service.create(request);

            ArgumentCaptor<RAMSTemplate> captor = ArgumentCaptor.forClass(RAMSTemplate.class);
            verify(ramsTemplateRepository).save(captor.capture());
            assertThat(captor.getValue().getFrequencyDays()).isEqualTo(90);
        }

        @Test
        @DisplayName("defaults isActive to true when not provided in the request")
        void defaultsIsActiveToTrueWhenNotProvided() {
            RAMSTemplateRequest request = new RAMSTemplateRequest();
            request.setTitle("Groundworks RAMS");
            request.setTrade("Groundworks");
            request.setFrequencyDays(null);
            request.setIsActive(null);

            when(ramsTemplateRepository.save(any(RAMSTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            service.create(request);

            ArgumentCaptor<RAMSTemplate> captor = ArgumentCaptor.forClass(RAMSTemplate.class);
            verify(ramsTemplateRepository).save(captor.capture());
            assertThat(captor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("persists the correct trade value")
        void persistsCorrectTradeValue() {
            RAMSTemplateRequest request = new RAMSTemplateRequest();
            request.setTitle("Roofing RAMS");
            request.setTrade("Roofing");

            when(ramsTemplateRepository.save(any(RAMSTemplate.class))).thenAnswer(inv -> inv.getArgument(0));

            service.create(request);

            ArgumentCaptor<RAMSTemplate> captor = ArgumentCaptor.forClass(RAMSTemplate.class);
            verify(ramsTemplateRepository).save(captor.capture());
            assertThat(captor.getValue().getTrade()).isEqualTo("Roofing");
        }
    }

    // ---------------------------------------------------------------------------
    // findByTrade
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findByTrade()")
    class FindByTrade {

        @Test
        @DisplayName("delegates to repository.findActiveByTrade() with the correct argument")
        void delegatesToRepositoryFindActiveByTrade() {
            List<RAMSTemplate> templates = List.of(existingTemplate);
            when(ramsTemplateRepository.findActiveByTrade("Scaffolding")).thenReturn(templates);

            List<RAMSTemplateResponse> result = service.findByTrade("Scaffolding");

            verify(ramsTemplateRepository).findActiveByTrade("Scaffolding");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTrade()).isEqualTo("Scaffolding");
        }

        @Test
        @DisplayName("returns an empty list when no templates match the trade")
        void returnsEmptyListWhenNoTemplatesMatchTrade() {
            when(ramsTemplateRepository.findActiveByTrade("NonExistentTrade")).thenReturn(List.of());

            List<RAMSTemplateResponse> result = service.findByTrade("NonExistentTrade");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("maps multiple results to response objects")
        void mapsEachTemplateToResponse() {
            RAMSTemplate t2 = RAMSTemplate.builder()
                    .id(6L)
                    .title("Scaffolding RAMS B")
                    .trade("Scaffolding")
                    .frequencyDays(90)
                    .isActive(true)
                    .build();

            when(ramsTemplateRepository.findActiveByTrade("Scaffolding"))
                    .thenReturn(List.of(existingTemplate, t2));

            List<RAMSTemplateResponse> result = service.findByTrade("Scaffolding");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(RAMSTemplateResponse::getTitle)
                    .containsExactly("Scaffolding RAMS", "Scaffolding RAMS B");
        }
    }

    // ---------------------------------------------------------------------------
    // findActive
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("findActive()")
    class FindActive {

        @Test
        @DisplayName("returns all templates from repository.findByIsActiveTrue()")
        void returnsOnlyActiveTemplates() {
            RAMSTemplate active1 = RAMSTemplate.builder()
                    .id(7L).title("Active RAMS 1").trade("Brickwork")
                    .frequencyDays(90).isActive(true).build();

            RAMSTemplate active2 = RAMSTemplate.builder()
                    .id(8L).title("Active RAMS 2").trade("Roofing")
                    .frequencyDays(120).isActive(true).build();

            when(ramsTemplateRepository.findByIsActiveTrue()).thenReturn(List.of(active1, active2));

            List<RAMSTemplateResponse> result = service.findActive();

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(RAMSTemplateResponse::getIsActive);
        }

        @Test
        @DisplayName("delegates to repository.findByIsActiveTrue()")
        void delegatesToRepositoryFindByIsActiveTrue() {
            when(ramsTemplateRepository.findByIsActiveTrue()).thenReturn(List.of());

            service.findActive();

            verify(ramsTemplateRepository).findByIsActiveTrue();
        }

        @Test
        @DisplayName("returns empty list when no active templates exist")
        void returnsEmptyListWhenNoActiveTemplates() {
            when(ramsTemplateRepository.findByIsActiveTrue()).thenReturn(List.of());

            List<RAMSTemplateResponse> result = service.findActive();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("maps each active template to a response with correct id and title")
        void mapsActiveTemplatesToResponses() {
            RAMSTemplate active = RAMSTemplate.builder()
                    .id(9L).title("Drainage RAMS").trade("Civil")
                    .frequencyDays(45).isActive(true).build();

            when(ramsTemplateRepository.findByIsActiveTrue()).thenReturn(List.of(active));

            List<RAMSTemplateResponse> result = service.findActive();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(9L);
            assertThat(result.get(0).getTitle()).isEqualTo("Drainage RAMS");
        }
    }
}
