package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.TenderStatus;
import com.crms.domain.tender.repository.TenderRepository;
import com.crms.dto.request.TenderRequest;
import com.crms.dto.response.ContractResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.TenderResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.TenderService;
import com.crms.util.PaginationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenderServiceImpl implements TenderService {
    
    private final TenderRepository tenderRepository;
    private final CompanyRepository companyRepository;
    private final SiteRepository siteRepository;
    private final ContractRepository contractRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<TenderResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        Page<Tender> tenderPage;
        if (params.containsKey("siteId") && params.get("siteId") != null) {
            tenderPage = tenderRepository.findBySiteId(Long.parseLong(params.get("siteId").toString()), pageable);
        } else if (params.containsKey("clientId") && params.get("clientId") != null) {
            tenderPage = tenderRepository.findByClientId(Long.parseLong(params.get("clientId").toString()), pageable);
        } else if (params.containsKey("status") && params.get("status") != null) {
            tenderPage = tenderRepository.findByStatus(TenderStatus.valueOf(params.get("status").toString()), pageable);
        } else {
            tenderPage = tenderRepository.findAll(pageable);
        }
        
        List<TenderResponse> content = tenderPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<TenderResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(tenderPage.getTotalElements())
                .totalPages(tenderPage.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public TenderResponse findById(Long id) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", id));
        return mapToResponse(tender);
    }
    
    @Override
    @Transactional
    public TenderResponse create(TenderRequest request) {
        Company client = companyRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
        
        Site site = null;
        if (request.getSiteId() != null) {
            site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));
        }
        
        Tender tender = Tender.builder()
                .tenderRef(request.getTenderRef())
                .title(request.getTitle())
                .description(request.getDescription())
                .client(client)
                .site(site)
                .status(request.getStatus() != null ? request.getStatus() : TenderStatus.LEAD)
                .contractForm(request.getContractForm())
                .measurementStandard(request.getMeasurementStandard())
                .valueRange(request.getValueRange())
                .winProbability(request.getWinProbability())
                .tenderOwner(request.getTenderOwner())
                .tenderIssuedDate(request.getTenderIssuedDate())
                .tenderReturnDate(request.getTenderReturnDate())
                .tenderValueSubmitted(request.getTenderValueSubmitted())
                .notes(request.getNotes())
                .build();
        
        tender = tenderRepository.save(tender);
        return mapToResponse(tender);
    }
    
    @Override
    @Transactional
    public TenderResponse update(Long id, TenderRequest request) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", id));
        
        if (request.getClientId() != null && (tender.getClient() == null || !request.getClientId().equals(tender.getClient().getId()))) {
            Company client = companyRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            tender.setClient(client);
        }
        
        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));
            tender.setSite(site);
        }
        
        tender.setTenderRef(request.getTenderRef());
        tender.setTitle(request.getTitle());
        tender.setDescription(request.getDescription());
        tender.setStatus(request.getStatus());
        tender.setContractForm(request.getContractForm());
        tender.setMeasurementStandard(request.getMeasurementStandard());
        tender.setValueRange(request.getValueRange());
        tender.setWinProbability(request.getWinProbability());
        tender.setTenderOwner(request.getTenderOwner());
        tender.setTenderIssuedDate(request.getTenderIssuedDate());
        tender.setTenderReturnDate(request.getTenderReturnDate());
        tender.setTenderValueSubmitted(request.getTenderValueSubmitted());
        tender.setNotes(request.getNotes());
        
        tender = tenderRepository.save(tender);
        return mapToResponse(tender);
    }
    
    @Override
    @Transactional
    public ContractResponse win(Long id) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", id));
        
        if (tender.getContract() != null) {
            throw new ValidationException("Tender already has a contract");
        }
        
        tender.setStatus(TenderStatus.AWARDED);
        tenderRepository.save(tender);
        
        Contract contract = Contract.builder()
                .contractRef("C-" + tender.getTenderRef())
                .title(tender.getTitle())
                .client(tender.getClient())
                .site(tender.getSite())
                .tender(tender)
                .contractForm(tender.getContractForm())
                .measurementStandard(tender.getMeasurementStandard())
                .contractValue(tender.getTenderValueSubmitted())
                .status(ContractStatus.ACTIVE)
                .build();
        
        contract = contractRepository.save(contract);
        tender.setContract(contract);
        tenderRepository.save(tender);
        
        return mapContractToResponse(contract);
    }
    
    @Override
    @Transactional
    public void lose(Long id, String reason) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", id));
        
        tender.setStatus(TenderStatus.LOST);
        tender.setNotes((tender.getNotes() != null ? tender.getNotes() + "\n" : "") + "Lost: " + reason);
        tenderRepository.save(tender);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Tender tender = tenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tender", id));
        
        if (tender.getContract() != null) {
            throw new ValidationException("Cannot delete tender with existing contract");
        }
        
        tenderRepository.delete(tender);
        log.info("Tender {} deleted", tender.getTenderRef());
    }
    
    private TenderResponse mapToResponse(Tender tender) {
        return TenderResponse.builder()
                .id(tender.getId())
                .tenderRef(tender.getTenderRef())
                .title(tender.getTitle())
                .description(tender.getDescription())
                .clientId(tender.getClient() != null ? tender.getClient().getId() : null)
                .clientName(tender.getClient() != null ? tender.getClient().getName() : null)
                .siteId(tender.getSite() != null ? tender.getSite().getId() : null)
                .siteName(tender.getSite() != null ? tender.getSite().getName() : null)
                .status(tender.getStatus() != null ? tender.getStatus().name() : null)
                .contractForm(tender.getContractForm() != null ? tender.getContractForm().name() : null)
                .measurementStandard(tender.getMeasurementStandard() != null ? tender.getMeasurementStandard().name() : null)
                .valueRange(tender.getValueRange())
                .winProbability(tender.getWinProbability())
                .tenderOwner(tender.getTenderOwner())
                .tenderIssuedDate(tender.getTenderIssuedDate())
                .tenderReturnDate(tender.getTenderReturnDate())
                .tenderValueSubmitted(tender.getTenderValueSubmitted())
                .lossReason(tender.getLossReason() != null ? tender.getLossReason().name() : null)
                .notes(tender.getNotes())
                .contractId(tender.getContract() != null ? tender.getContract().getId() : null)
                .build();
    }
    
    private ContractResponse mapContractToResponse(Contract contract) {
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
                .build();
    }
}