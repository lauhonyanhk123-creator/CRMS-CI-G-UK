package com.crms.service.impl;

import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.material.entity.PurchaseOrder;
import com.crms.domain.material.entity.PurchaseRequisition;
import com.crms.domain.material.enums.PurchaseOrderStatus;
import com.crms.domain.material.enums.PurchaseRequisitionStatus;
import com.crms.domain.material.repository.DeliveryNoteRepository;
import com.crms.domain.material.repository.PurchaseOrderRepository;
import com.crms.domain.material.repository.PurchaseRequisitionRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.domain.user.entity.User;
import com.crms.domain.user.repository.UserRepository;
import com.crms.dto.request.PurchaseRequisitionRequest;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcurementServiceImpl unit tests")
class ProcurementServiceImplTest {

    @Mock
    private PurchaseRequisitionRepository purchaseRequisitionRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private DeliveryNoteRepository deliveryNoteRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProcurementServiceImpl procurementService;

    private Site testSite;
    private User testUser;
    private PurchaseRequisitionRequest validRequest;

    private final Long SITE_ID = 5L;
    private final Long REQUISITION_ID = 20L;

    @BeforeEach
    void setUp() {
        testSite = new Site();
        testSite.setId(SITE_ID);
        testSite.setName("Test Site");

        testUser = new User();

        validRequest = new PurchaseRequisitionRequest();
        validRequest.setSiteId(SITE_ID);
        validRequest.setRequestedById(new UUID(0L, 0L));
        validRequest.setNotes("Test requisition");
    }

    // -------------------------------------------------------------------------
    // createRequisition
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("createRequisition")
    class CreateRequisition {

        @Test
        @DisplayName("throws ValidationException when passed a non-PurchaseRequisitionRequest object")
        void throwsWhenInvalidRequestType() {
            Object badRequest = "not-a-request";

            assertThatThrownBy(() -> procurementService.createRequisition(badRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Invalid request type");

            verifyNoInteractions(purchaseRequisitionRepository, siteRepository, userRepository);
        }

        @Test
        @DisplayName("throws ValidationException when passed null")
        void throwsWhenNull() {
            assertThatThrownBy(() -> procurementService.createRequisition(null))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Invalid request type");
        }

        @Test
        @DisplayName("creates requisition with DRAFT status when request is valid")
        void createsRequisitionWithDraftStatus() {
            when(siteRepository.findById(SITE_ID)).thenReturn(Optional.of(testSite));
            when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
            when(purchaseRequisitionRepository.save(any(PurchaseRequisition.class)))
                    .thenAnswer(inv -> {
                        PurchaseRequisition saved = inv.getArgument(0);
                        assertThat(saved.getStatus()).isEqualTo(PurchaseRequisitionStatus.DRAFT);
                        return saved;
                    });

            Object result = procurementService.createRequisition(validRequest);

            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            assertThat(resultMap.get("status")).isEqualTo(PurchaseRequisitionStatus.DRAFT.name());

            verify(purchaseRequisitionRepository).save(any(PurchaseRequisition.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when site does not exist")
        void throwsWhenSiteNotFound() {
            when(siteRepository.findById(SITE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> procurementService.createRequisition(validRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Site");

            verify(purchaseRequisitionRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // approveRequisition
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("approveRequisition")
    class ApproveRequisition {

        @Test
        @DisplayName("throws ResourceNotFoundException when requisition does not exist")
        void throwsWhenNotFound() {
            when(purchaseRequisitionRepository.findById(REQUISITION_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> procurementService.approveRequisition(REQUISITION_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("PurchaseRequisition");
        }

        @Test
        @DisplayName("throws ValidationException when requisition status is APPROVED")
        void throwsWhenAlreadyApproved() {
            PurchaseRequisition requisition = PurchaseRequisition.builder()
                    .id(REQUISITION_ID)
                    .requisitionRef("PR-111")
                    .status(PurchaseRequisitionStatus.APPROVED)
                    .build();

            when(purchaseRequisitionRepository.findById(REQUISITION_ID))
                    .thenReturn(Optional.of(requisition));

            assertThatThrownBy(() -> procurementService.approveRequisition(REQUISITION_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only DRAFT requisitions can be approved");

            verify(purchaseRequisitionRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ValidationException when requisition status is ORDERED / CONVERTED_TO_PO")
        void throwsWhenOrdered() {
            PurchaseRequisition requisition = PurchaseRequisition.builder()
                    .id(REQUISITION_ID)
                    .requisitionRef("PR-222")
                    .status(PurchaseRequisitionStatus.CONVERTED_TO_PO)
                    .build();

            when(purchaseRequisitionRepository.findById(REQUISITION_ID))
                    .thenReturn(Optional.of(requisition));

            assertThatThrownBy(() -> procurementService.approveRequisition(REQUISITION_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only DRAFT requisitions can be approved");
        }

        @Test
        @DisplayName("sets status to APPROVED when requisition is DRAFT")
        void approvesSuccessfullyWhenDraft() {
            PurchaseRequisition requisition = PurchaseRequisition.builder()
                    .id(REQUISITION_ID)
                    .requisitionRef("PR-333")
                    .status(PurchaseRequisitionStatus.DRAFT)
                    .build();

            when(purchaseRequisitionRepository.findById(REQUISITION_ID))
                    .thenReturn(Optional.of(requisition));
            when(purchaseRequisitionRepository.save(any(PurchaseRequisition.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Object result = procurementService.approveRequisition(REQUISITION_ID);

            assertThat(result).isNotNull();
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            assertThat(resultMap.get("status")).isEqualTo(PurchaseRequisitionStatus.APPROVED.name());

            verify(purchaseRequisitionRepository).save(any(PurchaseRequisition.class));
        }
    }

    // -------------------------------------------------------------------------
    // createPO
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("createPO")
    class CreatePO {

        @Test
        @DisplayName("throws ValidationException when requisition status is DRAFT")
        void throwsWhenDraft() {
            PurchaseRequisition requisition = PurchaseRequisition.builder()
                    .id(REQUISITION_ID)
                    .requisitionRef("PR-444")
                    .status(PurchaseRequisitionStatus.DRAFT)
                    .lines(new ArrayList<>())
                    .build();

            when(purchaseRequisitionRepository.findById(REQUISITION_ID))
                    .thenReturn(Optional.of(requisition));

            assertThatThrownBy(() -> procurementService.createPO(REQUISITION_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only APPROVED requisitions can be converted to purchase orders");

            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ValidationException when requisition is already CONVERTED_TO_PO")
        void throwsWhenAlreadyConverted() {
            PurchaseRequisition requisition = PurchaseRequisition.builder()
                    .id(REQUISITION_ID)
                    .requisitionRef("PR-555")
                    .status(PurchaseRequisitionStatus.CONVERTED_TO_PO)
                    .lines(new ArrayList<>())
                    .build();

            when(purchaseRequisitionRepository.findById(REQUISITION_ID))
                    .thenReturn(Optional.of(requisition));

            assertThatThrownBy(() -> procurementService.createPO(REQUISITION_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only APPROVED requisitions can be converted to purchase orders");
        }

        @Test
        @DisplayName("creates PurchaseOrder linked to the requisition when requisition is APPROVED")
        void createsPOSuccessfullyWhenApproved() {
            PurchaseRequisition requisition = PurchaseRequisition.builder()
                    .id(REQUISITION_ID)
                    .requisitionRef("PR-666")
                    .status(PurchaseRequisitionStatus.APPROVED)
                    .site(testSite)
                    .lines(new ArrayList<>())
                    .build();

            when(purchaseRequisitionRepository.findById(REQUISITION_ID))
                    .thenReturn(Optional.of(requisition));
            when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
                    .thenAnswer(inv -> {
                        PurchaseOrder po = inv.getArgument(0);
                        assertThat(po.getRequisition()).isSameAs(requisition);
                        return po;
                    });
            when(purchaseRequisitionRepository.save(any(PurchaseRequisition.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Object result = procurementService.createPO(REQUISITION_ID);

            assertThat(result).isNotNull();
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            assertThat(resultMap.get("requisitionId")).isEqualTo(REQUISITION_ID);

            verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
            // Requisition status should be updated to CONVERTED_TO_PO
            verify(purchaseRequisitionRepository).save(argThat(r ->
                    r.getStatus() == PurchaseRequisitionStatus.CONVERTED_TO_PO));
        }
    }
}
