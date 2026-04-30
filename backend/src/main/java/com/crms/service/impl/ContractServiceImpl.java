package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.RetentionLedger;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.RetentionLedgerRepository;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.dto.request.ContractRequest;
import com.crms.dto.response.ContractResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.RetentionLedgerResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.ContractService;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    
    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;
    private final SiteRepository siteRepository;
    private final RetentionLedgerRepository retentionLedgerRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContractResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        Page<Contract> contractPage;
        if (params.containsKey("siteId") && params.get("siteId") != null) {
            contractPage = contractRepository.findBySiteId(Long.parseLong(params.get("siteId").toString()), pageable);
        } else if (params.containsKey("status") && params.get("status") != null) {
            contractPage = contractRepository.findByStatus(ContractStatus.valueOf(params.get("status").toString()), pageable);
        } else if (params.containsKey("clientId") && params.get("clientId") != null) {
            contractPage = contractRepository.findByClientId(Long.parseLong(params.get("clientId").toString()), pageable);
        } else {
            contractPage = contractRepository.findAll(pageable);
        }
        
        List<ContractResponse> content = contractPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<ContractResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(contractPage.getTotalElements())
                .totalPages(contractPage.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public ContractResponse findById(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
        return mapToResponse(contract);
    }
    
    @Override
    @Transactional
    public ContractResponse create(ContractRequest request) {
        Company client = companyRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
        
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));
        
        Contract contract = Contract.builder()
                .contractRef(request.getContractRef())
                .title(request.getTitle())
                .client(client)
                .site(site)
                .contractForm(request.getContractForm())
                .measurementStandard(request.getMeasurementStandard())
                .contractValue(request.getContractValue())
                .retentionPercent(request.getRetentionPercent() != null ? request.getRetentionPercent() : new BigDecimal("5.0"))
                .retentionReductionPercent(request.getRetentionReductionPercent() != null ? request.getRetentionReductionPercent() : new BigDecimal("2.5"))
                .practicalCompletionDefectsPeriodMonths(request.getPracticalCompletionDefectsPeriodMonths() != null ? request.getPracticalCompletionDefectsPeriodMonths() : 12)
                .paymentTermsDays(request.getPaymentTermsDays() != null ? request.getPaymentTermsDays() : 30)
                .finalDateForPaymentOffsetDays(request.getFinalDateForPaymentOffsetDays() != null ? request.getFinalDateForPaymentOffsetDays() : 14)
                .payLessNoticePrescribedPeriodDays(request.getPayLessNoticePrescribedPeriodDays() != null ? request.getPayLessNoticePrescribedPeriodDays() : 7)
                .bondPercent(request.getBondPercent())
                .bondValue(request.getBondValue())
                .bondRef(request.getBondRef())
                .contractDocuments(request.getContractDocuments())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .defectsEndDate(request.getDefectsEndDate())
                .nec4Options(request.getNec4Options())
                .nec4PricingMechanism(request.getNec4PricingMechanism())
                .status(request.getStatus() != null ? request.getStatus() : ContractStatus.DRAFT)
                .build();
        
        contract = contractRepository.save(contract);
        
        // Create retention ledger for the contract
        RetentionLedger retentionLedger = RetentionLedger.builder()
                .contract(contract)
                .build();
        retentionLedgerRepository.save(retentionLedger);
        
        return mapToResponse(contract);
    }
    
    @Override
    @Transactional
    public ContractResponse update(Long id, ContractRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
        
        if (request.getClientId() != null && (contract.getClient() == null || !request.getClientId().equals(contract.getClient().getId()))) {
            Company client = companyRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            contract.setClient(client);
        }
        
        if (request.getSiteId() != null && (contract.getSite() == null || !request.getSiteId().equals(contract.getSite().getId()))) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));
            contract.setSite(site);
        }
        
        contract.setContractRef(request.getContractRef());
        contract.setTitle(request.getTitle());
        contract.setContractForm(request.getContractForm());
        contract.setMeasurementStandard(request.getMeasurementStandard());
        contract.setContractValue(request.getContractValue());
        contract.setRetentionPercent(request.getRetentionPercent());
        contract.setRetentionReductionPercent(request.getRetentionReductionPercent());
        contract.setPracticalCompletionDefectsPeriodMonths(request.getPracticalCompletionDefectsPeriodMonths());
        contract.setPaymentTermsDays(request.getPaymentTermsDays());
        contract.setFinalDateForPaymentOffsetDays(request.getFinalDateForPaymentOffsetDays());
        contract.setPayLessNoticePrescribedPeriodDays(request.getPayLessNoticePrescribedPeriodDays());
        contract.setBondPercent(request.getBondPercent());
        contract.setBondValue(request.getBondValue());
        contract.setBondRef(request.getBondRef());
        contract.setContractDocuments(request.getContractDocuments());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setDefectsEndDate(request.getDefectsEndDate());
        contract.setNec4Options(request.getNec4Options());
        contract.setNec4PricingMechanism(request.getNec4PricingMechanism());
        contract.setStatus(request.getStatus());
        
        contract = contractRepository.save(contract);
        return mapToResponse(contract);
    }
    
    @Override
    @Transactional(readOnly = true)
    public RetentionLedgerResponse getRetentionLedger(Long id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", id));
        
        RetentionLedger ledger = contract.getRetentionLedger();
        if (ledger == null) {
            ledger = RetentionLedger.builder()
                    .contract(contract)
                    .build();
            ledger = retentionLedgerRepository.save(ledger);
        }
        
        List<?> movements = ledger.getMovements() != null ? ledger.getMovements() : List.of();
        
        return RetentionLedgerResponse.builder()
                .id(ledger.getId())
                .contractId(contract.getId())
                .contractRef(contract.getContractRef())
                .totalRetention(ledger.getTotalRetention())
                .totalReleased(ledger.getTotalReleased())
                .currentRetention(ledger.getCurrentRetention())
                .movements(movements.stream().map(m -> (Object) m).collect(Collectors.toList()))
                .build();
    }
    
    private ContractResponse mapToResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .contractRef(contract.getContractRef())
                .title(contract.getTitle())
                .clientId(contract.getClient() != null ? contract.getClient().getId() : null)
                .clientName(contract.getClient() != null ? contract.getClient().getName() : null)
                .siteId(contract.getSite() != null ? contract.getSite().getId() : null)
                .siteName(contract.getSite() != null ? contract.getSite().getName() : null)
                .tenderId(contract.getTender() != null ? contract.getTender().getId() : null)
                .contractForm(contract.getContractForm() != null ? contract.getContractForm().name() : null)
                .measurementStandard(contract.getMeasurementStandard() != null ? contract.getMeasurementStandard().name() : null)
                .contractValue(contract.getContractValue())
                .retentionPercent(contract.getRetentionPercent())
                .retentionReductionPercent(contract.getRetentionReductionPercent())
                .practicalCompletionDefectsPeriodMonths(contract.getPracticalCompletionDefectsPeriodMonths())
                .paymentTermsDays(contract.getPaymentTermsDays())
                .finalDateForPaymentOffsetDays(contract.getFinalDateForPaymentOffsetDays())
                .bondPercent(contract.getBondPercent())
                .bondValue(contract.getBondValue())
                .bondRef(contract.getBondRef())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .defectsEndDate(contract.getDefectsEndDate())
                .status(contract.getStatus() != null ? contract.getStatus().name() : null)
                .nec4Options(contract.getNec4Options())
                .nec4PricingMechanism(contract.getNec4PricingMechanism())
                .build();
    }
}