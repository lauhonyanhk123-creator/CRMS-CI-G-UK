package com.crms.service.impl;

import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.entity.AdoptionStage;
import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.enums.BondType;
import com.crms.domain.adoption.repository.AdoptionCaseRepository;
import com.crms.domain.adoption.repository.AdoptionStageRepository;
import com.crms.domain.adoption.repository.BondRepository;
import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.AdoptionService;
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
import java.util.HashMap;
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
    public PageResponse<?> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "createdAt";

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

        List<Map<String, Object>> content = casePage.getContent().stream()
                .map(this::mapCaseToMap)
                .collect(Collectors.toList());

        return PageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(casePage.getTotalElements())
                .totalPages(casePage.getTotalPages())
                .build();
    }

    @Override
    public Object findById(Long id) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));
        return mapCaseToMap(adoptionCase);
    }

    @Override
    @Transactional
    public Object create(Long contractId, Object request) {
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

        return mapCaseToMap(adoptionCase);
    }

    @Override
    @Transactional
    public Object addStage(Long id, Object stage) {
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
            adoptionStage.setTargetDate(LocalDate.parse(stageData.get("targetDate").toString()));
        }

        adoptionStage = adoptionStageRepository.save(adoptionStage);

        log.info("Added stage {} to adoption case {}", stageName, adoptionCase.getCaseRef());

        Map<String, Object> result = new HashMap<>();
        result.put("id", adoptionStage.getId());
        result.put("caseId", adoptionCase.getId());
        result.put("stageName", adoptionStage.getStageName());
        result.put("description", adoptionStage.getDescription());
        result.put("status", adoptionStage.getStatus() != null ? adoptionStage.getStatus().name() : null);
        result.put("targetDate", adoptionStage.getTargetDate());
        result.put("completedDate", adoptionStage.getCompletedDate());

        return result;
    }

    @Override
    @Transactional
    public Object requestBondRelease(Long id) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", id));

        Bond bond = bondRepository.findByAdoptionCaseId(id)
                .orElseThrow(() -> new ValidationException("No bond found for this adoption case"));

        bond.setReleaseRequestedDate(LocalDate.now());
        bond.setReleaseRequested(true);
        bond.setStatus(com.crms.domain.adoption.enums.BondStatus.PENDING_RELEASE);
        bond = bondRepository.save(bond);

        log.info("Bond release requested for adoption case {}", adoptionCase.getCaseRef());

        Map<String, Object> result = new HashMap<>();
        result.put("id", bond.getId());
        result.put("caseId", adoptionCase.getId());
        result.put("bondRef", bond.getBondRef());
        result.put("releaseRequested", bond.getReleaseRequested());
        result.put("releaseRequestedDate", bond.getReleaseRequestedDate());
        result.put("status", bond.getStatus().name());

        return result;
    }

    private String generateCaseRef(com.crms.domain.adoption.enums.AdoptionType adoptionType) {
        String prefix = adoptionType == com.crms.domain.adoption.enums.AdoptionType.SEWER_ADOPTION ? "SA" : "HA";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private Map<String, Object> mapCaseToMap(AdoptionCase adoptionCase) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", adoptionCase.getId());
        map.put("caseRef", adoptionCase.getCaseRef());
        map.put("adoptionType", adoptionCase.getAdoptionType() != null ? adoptionCase.getAdoptionType().name() : null);
        map.put("contractId", adoptionCase.getContract() != null ? adoptionCase.getContract().getId() : null);
        map.put("contractRef", adoptionCase.getContract() != null ? adoptionCase.getContract().getContractRef() : null);
        map.put("clientId", adoptionCase.getClient() != null ? adoptionCase.getClient().getId() : null);
        map.put("clientName", adoptionCase.getClient() != null ? adoptionCase.getClient().getName() : null);
        map.put("localAuthorityId", adoptionCase.getLocalAuthorityOrWaterAuthority() != null ? adoptionCase.getLocalAuthorityOrWaterAuthority().getId() : null);
        map.put("localAuthorityName", adoptionCase.getLocalAuthorityOrWaterAuthority() != null ? adoptionCase.getLocalAuthorityOrWaterAuthority().getName() : null);
        map.put("technicalApprovalRef", adoptionCase.getTechnicalApprovalRef());
        map.put("designCheckFees", adoptionCase.getDesignCheckFees());
        map.put("supervisionFees", adoptionCase.getSupervisionFees());
        map.put("commutedSumTotal", adoptionCase.getCommutedSumTotal());
        map.put("commutedSumPaid", adoptionCase.getCommutedSumPaid());
        map.put("commutedSumOutstanding", adoptionCase.getCommutedSumOutstanding());
        map.put("maintenancePeriodMonths", adoptionCase.getMaintenancePeriodMonths());
        map.put("commencementDate", adoptionCase.getCommencementDate());
        map.put("maintenanceEndDate", adoptionCase.getMaintenanceEndDate());
        map.put("status", adoptionCase.getStatus() != null ? adoptionCase.getStatus().name() : null);
        map.put("createdAt", adoptionCase.getCreatedAt());
        map.put("updatedAt", adoptionCase.getUpdatedAt());
        return map;
    }
}
