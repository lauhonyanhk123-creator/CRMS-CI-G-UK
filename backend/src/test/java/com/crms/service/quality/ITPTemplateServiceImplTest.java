package com.crms.service.quality;

import com.crms.domain.quality.entity.ITPTemplate;
import com.crms.domain.quality.entity.ITPTemplateItem;
import com.crms.domain.quality.enums.InspectionType;
import com.crms.domain.quality.enums.TemplateStatus;
import com.crms.domain.quality.repository.ITPTemplateRepository;
import com.crms.dto.request.quality.ITPTemplateRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.ITPTemplateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ITPTemplateServiceImpl}.
 *
 * Covers: copyTemplate (happy path, status reset, item copy, not-found guard),
 * create (DRAFT default, explicit status, items attached), getDistinctCategories
 * (delegation), findAll (status/category/tradeCategory filters, pagination defaults).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ITPTemplateServiceImplTest {

    // -------------------------------------------------------------------------
    //  Mocks
    // -------------------------------------------------------------------------

    @Mock
    private ITPTemplateRepository repository;

    @InjectMocks
    private ITPTemplateServiceImpl service;

    // -------------------------------------------------------------------------
    //  Shared fixtures
    // -------------------------------------------------------------------------

    private ITPTemplate activeTemplate;
    private ITPTemplateItem item1;

    @BeforeEach
    void setUpSharedFixtures() {
        activeTemplate = ITPTemplate.builder()
                .id(1L)
                .name("Concrete Works ITP")
                .description("Standard concrete inspection template")
                .category("Structural")
                .tradeCategory("Civils")
                .status(TemplateStatus.ACTIVE)
                .version(2)
                .build();

        item1 = ITPTemplateItem.builder()
                .id(100L)
                .sequence(1)
                .description("Check reinforcement placement")
                .inspectionType(InspectionType.WITNESS)
                .responsibleParty("Site Engineer")
                .notes("Verify rebar spacing per drawings")
                .frequency("Every pour")
                .requiredEvidence("Signed inspection sheet")
                .build();
        item1.setTemplate(activeTemplate);
        activeTemplate.getItems().add(item1);
    }

    // =========================================================================
    //  copyTemplate()
    // =========================================================================

    @Nested
    @DisplayName("copyTemplate()")
    class CopyTemplateTests {

        @Test
        @DisplayName("creates copy with name appended with ' (Copy)'")
        void copyTemplate_appendsCopySuffix() {
            when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> {
                ITPTemplate t = inv.getArgument(0);
                t.setId(99L);
                return t;
            });

            ITPTemplateResponse result = service.copyTemplate(1L);

            assertThat(result.getName()).isEqualTo("Concrete Works ITP (Copy)");
        }

        @Test
        @DisplayName("resets status to DRAFT regardless of original status")
        void copyTemplate_resetsStatusToDraft() {
            assertThat(activeTemplate.getStatus()).isEqualTo(TemplateStatus.ACTIVE);

            when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.copyTemplate(1L);

            assertThat(captor.getValue().getStatus()).isEqualTo(TemplateStatus.DRAFT);
        }

        @Test
        @DisplayName("resets version to 1 on the copy")
        void copyTemplate_resetsVersionToOne() {
            when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.copyTemplate(1L);

            assertThat(captor.getValue().getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("copies all items from the original with identical field values")
        void copyTemplate_copiesAllItems() {
            when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.copyTemplate(1L);

            ITPTemplate saved = captor.getValue();
            assertThat(saved.getItems()).hasSize(1);
            ITPTemplateItem copiedItem = saved.getItems().get(0);
            assertThat(copiedItem.getDescription()).isEqualTo("Check reinforcement placement");
            assertThat(copiedItem.getInspectionType()).isEqualTo(InspectionType.WITNESS);
            assertThat(copiedItem.getResponsibleParty()).isEqualTo("Site Engineer");
            assertThat(copiedItem.getSequence()).isEqualTo(1);
            assertThat(copiedItem.getFrequency()).isEqualTo("Every pour");
            assertThat(copiedItem.getRequiredEvidence()).isEqualTo("Signed inspection sheet");
        }

        @Test
        @DisplayName("preserves category and tradeCategory from original")
        void copyTemplate_preservesCategoryFields() {
            when(repository.findById(1L)).thenReturn(Optional.of(activeTemplate));
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.copyTemplate(1L);

            ITPTemplate saved = captor.getValue();
            assertThat(saved.getCategory()).isEqualTo("Structural");
            assertThat(saved.getTradeCategory()).isEqualTo("Civils");
        }

        @Test
        @DisplayName("throws RuntimeException when template not found")
        void copyTemplate_notFound_throwsRuntimeException() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.copyTemplate(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("does not copy items when original has none")
        void copyTemplate_noItems_resultHasNoItems() {
            ITPTemplate emptyTemplate = ITPTemplate.builder()
                    .id(2L)
                    .name("Empty Template")
                    .category("Finishing")
                    .status(TemplateStatus.ACTIVE)
                    .version(1)
                    .build();

            when(repository.findById(2L)).thenReturn(Optional.of(emptyTemplate));
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.copyTemplate(2L);

            assertThat(captor.getValue().getItems()).isEmpty();
        }
    }

    // =========================================================================
    //  create()
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        private ITPTemplateRequest.ITPItemRequest buildItemRequest() {
            ITPTemplateRequest.ITPItemRequest itemReq = new ITPTemplateRequest.ITPItemRequest();
            itemReq.setSequence(1);
            itemReq.setDescription("Inspect formwork alignment");
            itemReq.setInspectionType(InspectionType.HOLD);
            itemReq.setResponsibleParty("Project Manager");
            itemReq.setNotes("Check plumb and level");
            itemReq.setFrequency("Before each pour");
            itemReq.setRequiredEvidence("Checklist photo");
            return itemReq;
        }

        private ITPTemplateRequest buildRequest(TemplateStatus status) {
            ITPTemplateRequest req = new ITPTemplateRequest();
            req.setName("Formwork ITP");
            req.setDescription("Formwork inspection template");
            req.setCategory("Structural");
            req.setTradeCategory("Civils");
            req.setStatus(status);
            req.setItems(List.of(buildItemRequest()));
            return req;
        }

        @Test
        @DisplayName("persists template with DRAFT status when no status supplied")
        void create_noStatusInRequest_defaultsToDraft() {
            ITPTemplateRequest req = buildRequest(null);
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> {
                ITPTemplate t = inv.getArgument(0);
                t.setId(10L);
                return t;
            });

            ITPTemplateResponse result = service.create(req);

            assertThat(result.getStatus()).isEqualTo(TemplateStatus.DRAFT);
            assertThat(captor.getValue().getStatus()).isEqualTo(TemplateStatus.DRAFT);
        }

        @Test
        @DisplayName("uses provided status when explicitly set in request")
        void create_withExplicitStatus_usesProvidedStatus() {
            ITPTemplateRequest req = buildRequest(TemplateStatus.ACTIVE);
            when(repository.save(any(ITPTemplate.class))).thenAnswer(inv -> {
                ITPTemplate t = inv.getArgument(0);
                t.setId(10L);
                return t;
            });

            ITPTemplateResponse result = service.create(req);

            assertThat(result.getStatus()).isEqualTo(TemplateStatus.ACTIVE);
        }

        @Test
        @DisplayName("attaches all provided items to the template before saving")
        void create_attachesAllItems() {
            ITPTemplateRequest req = buildRequest(null);
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> {
                ITPTemplate t = inv.getArgument(0);
                t.setId(10L);
                return t;
            });

            ITPTemplateResponse result = service.create(req);

            ITPTemplate saved = captor.getValue();
            assertThat(saved.getItems()).hasSize(1);
            assertThat(saved.getItems().get(0).getDescription()).isEqualTo("Inspect formwork alignment");
            assertThat(saved.getItems().get(0).getInspectionType()).isEqualTo(InspectionType.HOLD);
            assertThat(result.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("sets version to 1 on new template")
        void create_setsVersionToOne() {
            ITPTemplateRequest req = buildRequest(null);
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.create(req);

            assertThat(captor.getValue().getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty items list when request items is null")
        void create_nullItems_savesWithNoItems() {
            ITPTemplateRequest req = buildRequest(null);
            req.setItems(null);
            ArgumentCaptor<ITPTemplate> captor = ArgumentCaptor.forClass(ITPTemplate.class);
            when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.create(req);

            assertThat(captor.getValue().getItems()).isEmpty();
        }
    }

    // =========================================================================
    //  getDistinctCategories()
    // =========================================================================

    @Nested
    @DisplayName("getDistinctCategories()")
    class GetDistinctCategoriesTests {

        @Test
        @DisplayName("delegates to repository.findDistinctCategories and returns result")
        void getDistinctCategories_delegatesToRepository() {
            List<String> categories = List.of("Civils", "Finishing", "Structural");
            when(repository.findDistinctCategories()).thenReturn(categories);

            List<String> result = service.getDistinctCategories();

            assertThat(result).containsExactly("Civils", "Finishing", "Structural");
            verify(repository).findDistinctCategories();
        }

        @Test
        @DisplayName("returns empty list when no templates exist")
        void getDistinctCategories_noTemplates_returnsEmpty() {
            when(repository.findDistinctCategories()).thenReturn(Collections.emptyList());

            List<String> result = service.getDistinctCategories();

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    //  findAll()
    // =========================================================================

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("passes null status/category/tradeCategory when params map is empty")
        void findAll_noParams_passesNullFilters() {
            Page<ITPTemplate> page = new PageImpl<>(List.of(activeTemplate));
            when(repository.findByFilters(isNull(), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<ITPTemplateResponse> result = service.findAll(Map.of());

            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByFilters(isNull(), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("passes parsed TemplateStatus when status param is provided")
        void findAll_withStatusParam_passesStatusToRepository() {
            Page<ITPTemplate> page = new PageImpl<>(List.of(activeTemplate));
            when(repository.findByFilters(eq(TemplateStatus.ACTIVE), isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<ITPTemplateResponse> result = service.findAll(Map.of("status", "ACTIVE"));

            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByFilters(eq(TemplateStatus.ACTIVE), isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("passes category string when category param is provided")
        void findAll_withCategoryParam_passesCategoryToRepository() {
            Page<ITPTemplate> page = new PageImpl<>(List.of(activeTemplate));
            when(repository.findByFilters(isNull(), eq("Structural"), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<ITPTemplateResponse> result = service.findAll(Map.of("category", "Structural"));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCategory()).isEqualTo("Structural");
        }

        @Test
        @DisplayName("passes tradeCategory string when tradeCategory param is provided")
        void findAll_withTradeCategoryParam_passesTradeCategoryToRepository() {
            Page<ITPTemplate> page = new PageImpl<>(List.of(activeTemplate));
            when(repository.findByFilters(isNull(), isNull(), eq("Civils"), any(Pageable.class)))
                    .thenReturn(page);

            PageResponse<ITPTemplateResponse> result = service.findAll(Map.of("tradeCategory", "Civils"));

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("uses default page=0 and size=20 when not specified")
        void findAll_noPageParams_usesDefaults() {
            Page<ITPTemplate> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
            when(repository.findByFilters(any(), any(), any(), any(Pageable.class))).thenReturn(page);

            PageResponse<ITPTemplateResponse> result = service.findAll(Map.of());

            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("returns empty page when no templates match filters")
        void findAll_noMatches_returnsEmptyPage() {
            Page<ITPTemplate> emptyPage = new PageImpl<>(Collections.emptyList());
            when(repository.findByFilters(any(), any(), any(), any(Pageable.class))).thenReturn(emptyPage);

            PageResponse<ITPTemplateResponse> result = service.findAll(Map.of("status", "ARCHIVED"));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("maps template items into response correctly")
        void findAll_mapsItemsIntoResponse() {
            Page<ITPTemplate> page = new PageImpl<>(List.of(activeTemplate));
            when(repository.findByFilters(any(), any(), any(), any(Pageable.class))).thenReturn(page);

            PageResponse<ITPTemplateResponse> result = service.findAll(Map.of());

            ITPTemplateResponse response = result.getContent().get(0);
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getDescription()).isEqualTo("Check reinforcement placement");
            assertThat(response.getItems().get(0).getInspectionType()).isEqualTo(InspectionType.WITNESS);
        }
    }
}
