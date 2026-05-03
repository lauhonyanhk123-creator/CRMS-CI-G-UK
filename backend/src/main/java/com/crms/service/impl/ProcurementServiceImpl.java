package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.material.entity.DeliveryNote;
import com.crms.domain.material.entity.PurchaseOrder;
import com.crms.domain.material.entity.PurchaseRequisition;
import com.crms.domain.material.enums.DeliveryStatus;
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
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.ProcurementService;
import com.crms.util.PaginationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcurementServiceImpl implements ProcurementService {

    private final PurchaseRequisitionRepository requisitionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;

    @Override
    public PageResponse<?> findRequisitions(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

        Page<PurchaseRequisition> requisitionPage;
        if (params.containsKey("status") && params.get("status") != null) {
            PurchaseRequisitionStatus status = PurchaseRequisitionStatus.valueOf(params.get("status").toString());
            requisitionPage = requisitionRepository.findAll(pageable);
            // Filter by status in memory for simplicity
            List<PurchaseRequisition> filtered = requisitionPage.getContent().stream()
                    .filter(r -> r.getStatus() == status)
                    .collect(Collectors.toList());
            requisitionPage = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        } else if (params.containsKey("siteId") && params.get("siteId") != null) {
            requisitionPage = requisitionRepository.findAll(pageable);
            Long siteId = Long.parseLong(params.get("siteId").toString());
            List<PurchaseRequisition> filtered = requisitionPage.getContent().stream()
                    .filter(r -> r.getSite() != null && r.getSite().getId().equals(siteId))
                    .collect(Collectors.toList());
            requisitionPage = new org.springframework.data.domain.PageImpl<>(filtered, pageable, filtered.size());
        } else {
            requisitionPage = requisitionRepository.findAll(pageable);
        }

        List<Map<String, Object>> content = requisitionPage.getContent().stream()
                .map(this::mapRequisitionToMap)
                .collect(Collectors.toList());

        return PageResponse.builder()
                .content(new java.util.ArrayList<>(content))
                .page(page)
                .size(size)
                .totalElements(requisitionPage.getTotalElements())
                .totalPages(requisitionPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public Object createRequisition(Object request) {
        if (!(request instanceof PurchaseRequisitionRequest)) {
            throw new ValidationException("Invalid request type");
        }
        PurchaseRequisitionRequest req = (PurchaseRequisitionRequest) request;

        Site site = siteRepository.findById(req.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", req.getSiteId()));

        User requestedBy = userRepository.findById(new java.util.UUID(0L, req.getRequestedById()))
                .orElseThrow(() -> new ResourceNotFoundException("User", req.getRequestedById()));

        String requisitionRef = generateRequisitionRef();

        PurchaseRequisition requisition = PurchaseRequisition.builder()
                .requisitionRef(requisitionRef)
                .site(site)
                .requestedBy(requestedBy)
                .requiredDate(req.getRequiredDate())
                .notes(req.getNotes())
                .status(PurchaseRequisitionStatus.DRAFT)
                .build();

        if (req.getContractId() != null) {
            Contract contract = contractRepository.findById(req.getContractId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contract", req.getContractId()));
            requisition.setContract(contract);
        }

        requisition = requisitionRepository.save(requisition);
        log.info("Created purchase requisition {}", requisitionRef);

        return mapRequisitionToMap(requisition);
    }

    @Override
    @Transactional
    public Object approveRequisition(Long id) {
        PurchaseRequisition requisition = requisitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequisition", id));

        if (requisition.getStatus() != PurchaseRequisitionStatus.DRAFT) {
            throw new ValidationException("Only DRAFT requisitions can be approved");
        }

        requisition.setStatus(PurchaseRequisitionStatus.APPROVED);
        requisition = requisitionRepository.save(requisition);

        log.info("Approved purchase requisition {}", requisition.getRequisitionRef());
        return mapRequisitionToMap(requisition);
    }

    @Override
    @Transactional
    public Object createPO(Long requisitionId) {
        PurchaseRequisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseRequisition", requisitionId));

        if (requisition.getStatus() != PurchaseRequisitionStatus.APPROVED) {
            throw new ValidationException("Only APPROVED requisitions can be converted to purchase orders");
        }

        String poRef = generatePurchaseOrderRef();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .purchaseOrderRef(poRef)
                .site(requisition.getSite())
                .supplier(requisition.getLines().isEmpty() ? null : requisition.getLines().get(0).getSupplier())
                .requisition(requisition)
                .orderDate(LocalDate.now())
                .status(PurchaseOrderStatus.DRAFT)
                .build();

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        requisition.setStatus(PurchaseRequisitionStatus.CONVERTED_TO_PO);
        requisitionRepository.save(requisition);

        log.info("Created purchase order {} from requisition {}", poRef, requisition.getRequisitionRef());

        return mapPurchaseOrderToMap(purchaseOrder);
    }

    @Override
    public PageResponse<?> getDeliveryNotes(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

        Page<DeliveryNote> deliveryNotePage;
        if (params.containsKey("status") && params.get("status") != null) {
            DeliveryStatus status = DeliveryStatus.valueOf(params.get("status").toString());
            List<DeliveryNote> notes = deliveryNoteRepository.findByStatus(status);
            deliveryNotePage = new org.springframework.data.domain.PageImpl<>(notes, pageable, notes.size());
        } else if (params.containsKey("orderId") && params.get("orderId") != null) {
            List<DeliveryNote> notes = deliveryNoteRepository.findByOrderId(Long.parseLong(params.get("orderId").toString()));
            deliveryNotePage = new org.springframework.data.domain.PageImpl<>(notes, pageable, notes.size());
        } else {
            deliveryNotePage = deliveryNoteRepository.findAll(pageable);
        }

        List<Map<String, Object>> content = deliveryNotePage.getContent().stream()
                .map(this::mapDeliveryNoteToMap)
                .collect(Collectors.toList());

        return PageResponse.builder()
                .content(new java.util.ArrayList<>(content))
                .page(page)
                .size(size)
                .totalElements(deliveryNotePage.getTotalElements())
                .totalPages(deliveryNotePage.getTotalPages())
                .build();
    }

    private String generateRequisitionRef() {
        String prefix = "PR";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private String generatePurchaseOrderRef() {
        String prefix = "PO";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private Map<String, Object> mapRequisitionToMap(PurchaseRequisition requisition) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", requisition.getId());
        map.put("requisitionRef", requisition.getRequisitionRef());
        map.put("siteId", requisition.getSite() != null ? requisition.getSite().getId() : null);
        map.put("siteName", requisition.getSite() != null ? requisition.getSite().getName() : null);
        map.put("contractId", requisition.getContract() != null ? requisition.getContract().getId() : null);
        map.put("requestedById", requisition.getRequestedBy() != null ? requisition.getRequestedBy().getId() : null);
        map.put("status", requisition.getStatus() != null ? requisition.getStatus().name() : null);
        map.put("requiredDate", requisition.getRequiredDate());
        map.put("notes", requisition.getNotes());
        map.put("createdAt", requisition.getCreatedAt());
        return map;
    }

    private Map<String, Object> mapPurchaseOrderToMap(PurchaseOrder order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("purchaseOrderRef", order.getPurchaseOrderRef());
        map.put("orderNumber", order.getOrderNumber());
        map.put("siteId", order.getSite() != null ? order.getSite().getId() : null);
        map.put("siteName", order.getSite() != null ? order.getSite().getName() : null);
        map.put("supplierId", order.getSupplier() != null ? order.getSupplier().getId() : null);
        map.put("supplierName", order.getSupplier() != null ? order.getSupplier().getName() : null);
        map.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        map.put("orderDate", order.getOrderDate());
        map.put("deliveryDate", order.getDeliveryDate());
        map.put("netValue", order.getNetValue());
        map.put("vatValue", order.getVatValue());
        map.put("totalValue", order.getTotalValue());
        map.put("requisitionId", order.getRequisition() != null ? order.getRequisition().getId() : null);
        map.put("createdAt", order.getCreatedAt());
        return map;
    }

    private Map<String, Object> mapDeliveryNoteToMap(DeliveryNote note) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", note.getId());
        map.put("deliveryNoteRef", note.getDeliveryNoteRef());
        map.put("orderId", note.getOrder() != null ? note.getOrder().getId() : null);
        map.put("siteId", note.getSite() != null ? note.getSite().getId() : null);
        map.put("siteName", note.getSite() != null ? note.getSite().getName() : null);
        map.put("supplierId", note.getSupplier() != null ? note.getSupplier().getId() : null);
        map.put("supplierName", note.getSupplier() != null ? note.getSupplier().getName() : null);
        map.put("deliveryDate", note.getDeliveryDate());
        map.put("deliveryTime", note.getDeliveryTime());
        map.put("deliveredBy", note.getDeliveredBy());
        map.put("vehicleReg", note.getVehicleReg());
        map.put("status", note.getStatus() != null ? note.getStatus().name() : null);
        map.put("notes", note.getNotes());
        map.put("createdAt", note.getCreatedAt());
        return map;
    }
}
