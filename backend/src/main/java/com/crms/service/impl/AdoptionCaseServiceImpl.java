package com.crms.service.impl;

import com.crms.domain.adoption.entity.*;
import com.crms.domain.adoption.enums.*;
import com.crms.domain.adoption.repository.*;
import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.dto.request.*;
import com.crms.dto.response.*;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.AdoptionCaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdoptionCaseServiceImpl implements AdoptionCaseService {
    
    private final AdoptionCaseRepository adoptionCaseRepository;
    private final AdoptionStageRepository adoptionStageRepository;
    private final SnaggingItemRepository snaggingItemRepository;
    private final BondRepository bondRepository;
    private final CommutedSumMovementRepository commutedSumMovementRepository;
    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;
    
    @Override
    public PageResponse<AdoptionCaseResponse> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "createdAt";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        Page<AdoptionCase> casePage;
        if (params.containsKey("contractId") && params.get("contractId") != null) {
            List<AdoptionCase> cases = adoptionCaseRepository.findByContractId(
                    Long.parseLong(params.get("contractId").toString()));
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), cases.size());
            List<AdoptionCase> pageContent = start < cases.size() ? cases.subList(start, end) : List.of();
            casePage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, cases.size());
        } else if (params.containsKey("status") && params.get("status") != null) {
            casePage = adoptionCaseRepository.findByStatus(AdoptionStatus.valueOf(params.get("status").toString()), pageable);
        } else if (params.containsKey("adoptionType") && params.get("adoptionType") != null) {
            casePage = adoptionCaseRepository.findByAdoptionType(AdoptionType.valueOf(params.get("adoptionType").toString()), pageable);
        } else {
            casePage = adoptionCaseRepository.findAll(pageable);
        }
        
        List<AdoptionCaseResponse> content = casePage.getContent().stream()
                .map(AdoptionCaseResponse::fromEntity)
                .collect(Collectors.toList());
        
        return PageResponse.<AdoptionCaseResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(casePage.getTotalElements())
                .totalPages(casePage.getTotalPages())
                .build();
    }
    
    @Override
    public AdoptionCaseResponse findById(Long id) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));
        return AdoptionCaseResponse.fromEntity(adoptionCase);
    }
    
    @Override
    public AdoptionCaseResponse findByCaseRef(String caseRef) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findByCaseRef(caseRef)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase with ref: " + caseRef));
        return AdoptionCaseResponse.fromEntity(adoptionCase);
    }
    
    @Override
    @Transactional
    public AdoptionCaseResponse create(AdoptionCaseRequest request) {
        if (adoptionCaseRepository.existsByCaseRef(request.getCaseRef())) {
            throw new ValidationException("Adoption case reference already exists: " + request.getCaseRef());
        }
        
        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract", request.getContractId()));
        
        Company client = companyRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
        
        Company localAuthority = companyRepository.findById(request.getLocalAuthorityOrWaterAuthorityId())
                .orElseThrow(() -> new ResourceNotFoundException("Local Authority/Water Authority", request.getLocalAuthorityOrWaterAuthorityId()));
        
        // Calculate maintenance end date if commencement date and period are provided
        LocalDate maintenanceEndDate = request.getMaintenanceEndDate();
        if (request.getCommencementDate() != null && request.getMaintenancePeriodMonths() != null && maintenanceEndDate == null) {
            maintenanceEndDate = request.getCommencementDate().plusMonths(request.getMaintenancePeriodMonths());
        }
        
        AdoptionCase adoptionCase = AdoptionCase.builder()
                .caseRef(request.getCaseRef())
                .adoptionType(request.getAdoptionType())
                .contract(contract)
                .client(client)
                .localAuthorityOrWaterAuthority(localAuthority)
                .technicalApprovalRef(request.getTechnicalApprovalRef())
                .designCheckFees(request.getDesignCheckFees())
                .supervisionFees(request.getSupervisionFees())
                .commutedSumTotal(request.getCommutedSumTotal())
                .commutedSumPaid(java.math.BigDecimal.ZERO)
                .maintenancePeriodMonths(request.getMaintenancePeriodMonths())
                .commencementDate(request.getCommencementDate())
                .maintenanceEndDate(maintenanceEndDate)
                .status(request.getStatus() != null ? request.getStatus() : AdoptionStatus.PRE_APP)
                .build();
        
        adoptionCase = adoptionCaseRepository.save(adoptionCase);
        log.info("Created adoption case {} of type {}", adoptionCase.getCaseRef(), adoptionCase.getAdoptionType());
        
        return AdoptionCaseResponse.fromEntity(adoptionCase);
    }
    
    @Override
    @Transactional
    public AdoptionCaseResponse update(Long id, AdoptionCaseRequest request) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));
        
        if (request.getTechnicalApprovalRef() != null) {
            adoptionCase.setTechnicalApprovalRef(request.getTechnicalApprovalRef());
        }
        if (request.getDesignCheckFees() != null) {
            adoptionCase.setDesignCheckFees(request.getDesignCheckFees());
        }
        if (request.getSupervisionFees() != null) {
            adoptionCase.setSupervisionFees(request.getSupervisionFees());
        }
        if (request.getCommutedSumTotal() != null) {
            adoptionCase.setCommutedSumTotal(request.getCommutedSumTotal());
        }
        if (request.getMaintenancePeriodMonths() != null) {
            adoptionCase.setMaintenancePeriodMonths(request.getMaintenancePeriodMonths());
        }
        if (request.getCommencementDate() != null) {
            adoptionCase.setCommencementDate(request.getCommencementDate());
            if (adoptionCase.getMaintenancePeriodMonths() != null) {
                adoptionCase.setMaintenanceEndDate(
                        request.getCommencementDate().plusMonths(adoptionCase.getMaintenancePeriodMonths()));
            }
        }
        if (request.getMaintenanceEndDate() != null) {
            adoptionCase.setMaintenanceEndDate(request.getMaintenanceEndDate());
        }
        if (request.getStatus() != null) {
            validateStatusTransition(adoptionCase.getStatus(), request.getStatus());
            adoptionCase.setStatus(request.getStatus());
        }
        
        adoptionCase = adoptionCaseRepository.save(adoptionCase);
        return AdoptionCaseResponse.fromEntity(adoptionCase);
    }
    
    @Override
    @Transactional
    public AdoptionCaseResponse updateStatus(Long id, AdoptionStatus status) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));
        
        validateStatusTransition(adoptionCase.getStatus(), status);
        adoptionCase.setStatus(status);
        
        // Auto-update maintenance end date based on period when entering MAINTENANCE status
        if (status == AdoptionStatus.MAINTENANCE && adoptionCase.getCommencementDate() != null 
                && adoptionCase.getMaintenancePeriodMonths() != null) {
            adoptionCase.setMaintenanceEndDate(
                    adoptionCase.getCommencementDate().plusMonths(adoptionCase.getMaintenancePeriodMonths()));
        }
        
        adoptionCase = adoptionCaseRepository.save(adoptionCase);
        log.info("Adoption case {} status updated to {}", adoptionCase.getCaseRef(), status);
        
        return AdoptionCaseResponse.fromEntity(adoptionCase);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));
        
        if (adoptionCase.getStatus() != AdoptionStatus.PRE_APP && 
            adoptionCase.getStatus() != AdoptionStatus.APPLICATION) {
            throw new ValidationException("Cannot delete adoption case in status: " + adoptionCase.getStatus());
        }
        
        adoptionCaseRepository.delete(adoptionCase);
        log.info("Deleted adoption case {}", id);
    }
    
    // Stage operations
    @Override
    @Transactional
    public AdoptionStageResponse addStage(Long adoptionCaseId, AdoptionStageRequest request) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(adoptionCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", adoptionCaseId));
        
        // Validate stage order uniqueness
        adoptionStageRepository.findByAdoptionCaseIdAndStageOrder(adoptionCaseId, request.getStageOrder())
                .ifPresent(s -> { throw new ValidationException("Stage order already exists: " + request.getStageOrder()); });
        
        AdoptionStage stage = AdoptionStage.builder()
                .adoptionCase(adoptionCase)
                .stageName(request.getStageName())
                .stageOrder(request.getStageOrder())
                .plannedDate(request.getPlannedDate())
                .actualDate(request.getActualDate())
                .status(request.getStatus() != null ? request.getStatus() : StageStatus.PENDING)
                .notes(request.getNotes())
                .build();
        
        stage = adoptionStageRepository.save(stage);
        log.info("Added stage '{}' to adoption case {}", stage.getStageName(), adoptionCase.getCaseRef());
        
        return AdoptionStageResponse.fromEntity(stage);
    }
    
    @Override
    @Transactional
    public AdoptionStageResponse updateStage(Long stageId, AdoptionStageRequest request) {
        AdoptionStage stage = adoptionStageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionStage", stageId));
        
        if (request.getStageName() != null) {
            stage.setStageName(request.getStageName());
        }
        if (request.getStageOrder() != null) {
            stage.setStageOrder(request.getStageOrder());
        }
        if (request.getPlannedDate() != null) {
            stage.setPlannedDate(request.getPlannedDate());
        }
        if (request.getActualDate() != null) {
            stage.setActualDate(request.getActualDate());
        }
        if (request.getStatus() != null) {
            stage.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            stage.setNotes(request.getNotes());
        }
        
        stage = adoptionStageRepository.save(stage);
        return AdoptionStageResponse.fromEntity(stage);
    }
    
    @Override
    @Transactional
    public AdoptionStageResponse completeStage(Long stageId) {
        AdoptionStage stage = adoptionStageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionStage", stageId));
        
        if (stage.getStatus() == StageStatus.COMPLETED) {
            throw new ValidationException("Stage is already completed");
        }
        
        stage.setStatus(StageStatus.COMPLETED);
        stage.setActualDate(LocalDate.now());
        
        stage = adoptionStageRepository.save(stage);
        log.info("Stage '{}' completed for adoption case {}", stage.getStageName(), 
                stage.getAdoptionCase().getCaseRef());
        
        return AdoptionStageResponse.fromEntity(stage);
    }
    
    // Snagging operations
    @Override
    @Transactional
    public SnaggingItemResponse addSnaggingItem(SnaggingItemRequest request) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(request.getAdoptionCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", request.getAdoptionCaseId()));
        
        SnaggingItem item = SnaggingItem.builder()
                .adoptionCase(adoptionCase)
                .description(request.getDescription())
                .location(request.getLocation())
                .priority(request.getPriority() != null ? request.getPriority() : SnaggingItemPriority.MEDIUM)
                .identifiedDate(request.getIdentifiedDate() != null ? request.getIdentifiedDate() : LocalDate.now())
                .targetCompletionDate(request.getTargetCompletionDate())
                .actualCompletionDate(request.getActualCompletionDate())
                .status(request.getStatus() != null ? request.getStatus() : SnaggingItemStatus.OPEN)
                .notes(request.getNotes())
                .assignedTo(request.getAssignedTo())
                .build();
        
        item = snaggingItemRepository.save(item);
        log.info("Added snagging item to adoption case {}", adoptionCase.getCaseRef());
        
        return SnaggingItemResponse.fromEntity(item);
    }
    
    @Override
    @Transactional
    public SnaggingItemResponse updateSnaggingItem(Long id, SnaggingItemRequest request) {
        SnaggingItem item = snaggingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SnaggingItem", id));
        
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            item.setLocation(request.getLocation());
        }
        if (request.getPriority() != null) {
            item.setPriority(request.getPriority());
        }
        if (request.getTargetCompletionDate() != null) {
            item.setTargetCompletionDate(request.getTargetCompletionDate());
        }
        if (request.getActualCompletionDate() != null) {
            item.setActualCompletionDate(request.getActualCompletionDate());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }
        if (request.getAssignedTo() != null) {
            item.setAssignedTo(request.getAssignedTo());
        }
        
        item = snaggingItemRepository.save(item);
        return SnaggingItemResponse.fromEntity(item);
    }
    
    @Override
    @Transactional
    public SnaggingItemResponse completeSnaggingItem(Long id) {
        SnaggingItem item = snaggingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SnaggingItem", id));
        
        if (item.getStatus() == SnaggingItemStatus.COMPLETED) {
            throw new ValidationException("Snagging item is already completed");
        }
        
        item.setStatus(SnaggingItemStatus.COMPLETED);
        item.setActualCompletionDate(LocalDate.now());
        
        item = snaggingItemRepository.save(item);
        log.info("Snagging item completed for adoption case {}", item.getAdoptionCase().getCaseRef());
        
        return SnaggingItemResponse.fromEntity(item);
    }
    
    @Override
    @Transactional
    public SnaggingItemResponse verifySnaggingItem(Long id, String verifiedBy) {
        SnaggingItem item = snaggingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SnaggingItem", id));
        
        if (item.getStatus() != SnaggingItemStatus.COMPLETED) {
            throw new ValidationException("Snagging item must be completed before verification");
        }
        
        item.setStatus(SnaggingItemStatus.VERIFIED);
        item.setVerifiedDate(LocalDate.now());
        item.setVerifiedBy(verifiedBy);
        
        item = snaggingItemRepository.save(item);
        return SnaggingItemResponse.fromEntity(item);
    }
    
    @Override
    public PageResponse<SnaggingItemResponse> getSnaggingItems(Long adoptionCaseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "priority"));
        Page<SnaggingItem> itemPage = snaggingItemRepository.findByAdoptionCaseId(adoptionCaseId, pageable);
        
        List<SnaggingItemResponse> content = itemPage.getContent().stream()
                .map(SnaggingItemResponse::fromEntity)
                .collect(Collectors.toList());
        
        return PageResponse.<SnaggingItemResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .build();
    }
    
    // Bond operations
    @Override
    @Transactional
    public BondResponse createBond(Long adoptionCaseId, BondRequest request) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(adoptionCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", adoptionCaseId));
        
        if (bondRepository.existsByBondNumber(request.getBondNumber())) {
            throw new ValidationException("Bond number already exists: " + request.getBondNumber());
        }
        
        Company issuingSurety = companyRepository.findById(request.getIssuingSuretyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.getIssuingSuretyId()));
        
        Bond bond = Bond.builder()
                .bondNumber(request.getBondNumber())
                .bondType(request.getBondType())
                .issuingSurety(issuingSurety)
                .bondValue(request.getBondValue())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .releaseConditions(request.getReleaseConditions())
                .status(BondStatus.ACTIVE)
                .adoptionCase(adoptionCase)
                .build();
        
        bond = bondRepository.save(bond);
        log.info("Created bond {} for adoption case {}", bond.getBondNumber(), adoptionCase.getCaseRef());
        
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    @Transactional
    public BondResponse releaseBond(Long adoptionCaseId) {
        Bond bond = bondRepository.findByAdoptionCaseId(adoptionCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Bond for adoption case: " + adoptionCaseId));
        
        if (bond.getStatus() == BondStatus.RELEASED) {
            throw new ValidationException("Bond is already released");
        }
        
        bond.setStatus(BondStatus.RELEASED);
        bond.setReleaseDate(LocalDate.now());
        
        bond = bondRepository.save(bond);
        log.info("Bond {} released for adoption case {}", bond.getBondNumber(), 
                bond.getAdoptionCase().getCaseRef());
        
        return BondResponse.fromEntity(bond);
    }
    
    // Commuted sum operations
    @Override
    @Transactional
    public CommutedSumMovementResponse addCommutedSumMovement(Long adoptionCaseId, CommutedSumMovementRequest request) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(adoptionCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", adoptionCaseId));
        
        CommutedSumMovement movement = CommutedSumMovement.builder()
                .adoptionCase(adoptionCase)
                .movementDate(request.getMovementDate())
                .type(request.getType())
                .amount(request.getAmount())
                .reason(request.getReason())
                .documentRef(request.getDocumentRef())
                .build();
        
        movement = commutedSumMovementRepository.save(movement);
        
        // Update adoption case commuted sum paid
        if (request.getType() == CommutedSumType.INITIAL || request.getType() == CommutedSumType.ADJUSTMENT) {
            java.math.BigDecimal currentPaid = adoptionCase.getCommutedSumPaid() != null ? 
                    adoptionCase.getCommutedSumPaid() : java.math.BigDecimal.ZERO;
            adoptionCase.setCommutedSumPaid(currentPaid.add(request.getAmount()));
            adoptionCaseRepository.save(adoptionCase);
        } else if (request.getType() == CommutedSumType.RELEASED) {
            // Handle release - reduce paid amount
            java.math.BigDecimal currentPaid = adoptionCase.getCommutedSumPaid() != null ? 
                    adoptionCase.getCommutedSumPaid() : java.math.BigDecimal.ZERO;
            adoptionCase.setCommutedSumPaid(currentPaid.subtract(request.getAmount()));
            adoptionCaseRepository.save(adoptionCase);
        }
        
        log.info("Added commuted sum movement of {} to adoption case {}", request.getAmount(), adoptionCase.getCaseRef());
        
        return CommutedSumMovementResponse.fromEntity(movement);
    }
    
    @Override
    public AdoptionCaseResponse getWithDetails(Long id) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));
        return AdoptionCaseResponse.fromEntityWithDetails(adoptionCase);
    }
    
    private void validateStatusTransition(AdoptionStatus current, AdoptionStatus target) {
        // Define valid status transitions
        boolean valid = switch (current) {
            case PRE_APP -> target == AdoptionStatus.APPLICATION;
            case APPLICATION -> target == AdoptionStatus.DESIGN || target == AdoptionStatus.PRE_APP;
            case DESIGN -> target == AdoptionStatus.TECHNICAL_ACCEPTANCE || target == AdoptionStatus.APPLICATION;
            case TECHNICAL_ACCEPTANCE -> target == AdoptionStatus.CONSTRUCTION || target == AdoptionStatus.DESIGN;
            case CONSTRUCTION -> target == AdoptionStatus.MAINTENANCE || target == AdoptionStatus.TECHNICAL_ACCEPTANCE;
            case MAINTENANCE -> target == AdoptionStatus.ADOPTION || target == AdoptionStatus.CONSTRUCTION;
            case ADOPTION -> target == AdoptionStatus.COMPLETED || target == AdoptionStatus.MAINTENANCE;
            case COMPLETED -> false; // Terminal state
        };
        
        if (!valid) {
            throw new ValidationException("Invalid status transition from " + current + " to " + target);
        }
    }
}
