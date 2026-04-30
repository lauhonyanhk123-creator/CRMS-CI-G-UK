package com.crms.service.impl;

import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.entity.AdoptionStage;
import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.repository.AdoptionCaseRepository;
import com.crms.domain.adoption.repository.AdoptionStageRepository;
import com.crms.domain.adoption.repository.BondRepository;
import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.dto.response.*;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.AdoptionService;
import com.crms.util.PaginationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionCaseRepository adoptionCaseRepository;
    private final AdoptionStageRepository adoptionStageRepository;
    private final BondRepository bondRepository;
    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;

    @Override
    public PageResponse<AdoptionCaseResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));

        Page<AdoptionCase> casePage;
        if (params.containsKey("status") && params.get("status") != null) {
            AdoptionStatus status = AdoptionStatus.valueOf(params.get("status").toString());
            casePage = adoptionCaseRepository.findByStatus(status, pageable);
        } else if (params.containsKey("contractId") && params.get("contractId") != null) {
            Long contractId = Long.parseLong(params.get("contractId").toString());
            casePage = adoptionCaseRepository.findByContractId(contractId, pageable);
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
        return AdoptionCaseResponse.fromEntityWithDetails(adoptionCase);
    }

    @Override
    @Transactional
    public AdoptionCaseResponse create(Long contractId, Object request) {
        if (!(request instanceof Map)) {
            throw new ValidationException("Invalid request type");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> req = (Map<String, Object>) request;

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        Long clientId = Long.parseLong(req.get("clientId").toString());
        Company client = companyRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", clientId));

        Long laOrWaterId = Long.parseLong(req.get("localAuthorityOrWaterAuthorityId").toString());
        Company laOrWater = companyRepository.findById(laOrWaterId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", laOrWaterId));

        String adoptionTypeStr = req.get("adoptionType").toString();
        com.crms.domain.adoption.enums.AdoptionType adoptionType =
                com.crms.domain.adoption.enums.AdoptionType.valueOf(adoptionTypeStr);

        String caseRef = generateCaseRef(adoptionType);

        AdoptionCase adoptionCase = AdoptionCase.builder()
                .caseRef(caseRef)
                .adoptionType(adoptionType)
                .contract(contract)
                .client(client)
                .localAuthorityOrWaterAuthority(laOrWater)
                .status(AdoptionStatus.PRE_APP)
                .build();

        if (req.containsKey("designCheckFees")) {
            adoptionCase.setDesignCheckFees(new BigDecimal(req.get("designCheckFees").toString()));
        }
        if (req.containsKey("supervisionFees")) {
            adoptionCase.setSupervisionFees(new BigDecimal(req.get("supervisionFees").toString()));
        }
        if (req.containsKey("commutedSumTotal")) {
            adoptionCase.setCommutedSumTotal(new BigDecimal(req.get("commutedSumTotal").toString()));
        }
        if (req.containsKey("maintenancePeriodMonths")) {
            adoptionCase.setMaintenancePeriodMonths(Integer.parseInt(req.get("maintenancePeriodMonths").toString()));
        }

        adoptionCase = adoptionCaseRepository.save(adoptionCase);
        log.info("Created adoption case {} for contract {}", caseRef, contract.getContractRef());

        return AdoptionCaseResponse.fromEntity(adoptionCase);
    }

    @Override
    @Transactional
    public AdoptionStageCreateResponse addStage(Long id, Object stage) {
        if (!(stage instanceof Map)) {
            throw new ValidationException("Invalid stage type");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> stageData = (Map<String, Object>) stage;

        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));

        String stageName = stageData.get("stageName").toString();

        // Get the next stage order
        long stageCount = adoptionStageRepository.countByAdoptionCaseId(id);
        int nextOrder = (int) stageCount + 1;

        AdoptionStage adoptionStage = AdoptionStage.builder()
                .adoptionCase(adoptionCase)
                .stageName(stageName)
                .stageOrder(nextOrder)
                .status(com.crms.domain.adoption.enums.StageStatus.PENDING)
                .build();

        if (stageData.containsKey("description")) {
            adoptionStage.setDescription(stageData.get("description").toString());
        }
        if (stageData.containsKey("targetDate")) {
            adoptionStage.setPlannedDate(LocalDate.parse(stageData.get("targetDate").toString()));
        }

        adoptionStage = adoptionStageRepository.save(adoptionStage);

        log.info("Added stage {} to adoption case {}", stageName, adoptionCase.getCaseRef());

        return AdoptionStageCreateResponse.fromEntity(adoptionStage);
    }

    @Override
    @Transactional
    public BondReleaseResponse requestBondRelease(Long id) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));

        Bond bond = bondRepository.findByAdoptionCaseId(id)
                .orElseThrow(() -> new ValidationException("No bond found for this adoption case"));

        bond.setReleaseRequestedDate(LocalDate.now());
        bond.setReleaseRequested(true);
        bond.setStatus(com.crms.domain.adoption.enums.BondStatus.PENDING_RELEASE);
        bond = bondRepository.save(bond);

        log.info("Bond release requested for adoption case {}", adoptionCase.getCaseRef());

        return BondReleaseResponse.fromEntity(bond);
    }

    private String generateCaseRef(com.crms.domain.adoption.enums.AdoptionType adoptionType) {
        String prefix = adoptionType == com.crms.domain.adoption.enums.AdoptionType.SEWER_ADOPTION ? "SA" : "HA";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }
}
