package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.healthsafety.entity.RAMSDocument;
import com.crms.domain.healthsafety.repository.RAMSDocumentRepository;
import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.enums.CardType;
import com.crms.domain.operative.enums.OperativeStatus;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.OperativeRequest;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.OperativeService;
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
public class OperativeServiceImpl implements OperativeService {
    
    private final OperativeRepository operativeRepository;
    private final CompanyRepository companyRepository;
    private final CardRepository cardRepository;
    private final SiteRepository siteRepository;
    private final RAMSDocumentRepository ramsRepository;
    
    @Override
    public PageResponse<OperativeResponse> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        
        Page<Operative> operativePage;
        if (params.containsKey("status") && params.get("status") != null) {
            operativePage = operativeRepository.findByStatus(
                    OperativeStatus.valueOf(params.get("status").toString()), pageable);
        } else {
            operativePage = operativeRepository.findAll(pageable);
        }
        
        List<OperativeResponse> content = operativePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<OperativeResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(operativePage.getTotalElements())
                .totalPages(operativePage.getTotalPages())
                .build();
    }
    
    @Override
    public OperativeResponse findById(Long id) {
        Operative operative = operativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", id));
        return mapToResponse(operative);
    }
    
    @Override
    @Transactional
    public OperativeResponse create(OperativeRequest request) {
        Company employer = null;
        if (request.getEmployerId() != null) {
            employer = companyRepository.findById(request.getEmployerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getEmployerId()));
        }
        
        Operative operative = Operative.builder()
                .employeeRef(request.getEmployeeRef())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .nationality(request.getNationality())
                .niNumber(request.getNiNumber())
                .utr(request.getUtr())
                .rightToWorkExpiry(request.getRightToWorkExpiry())
                .rightToWorkDocType(request.getRightToWorkDocType())
                .passportNumber(request.getPassportNumber())
                .bankSortCode(request.getBankSortCode())
                .bankAccountNumber(request.getBankAccountNumber())
                .employmentStatus(request.getEmploymentStatus())
                .status(request.getStatus() != null ? request.getStatus() : OperativeStatus.ACTIVE)
                .employer(employer)
                .build();
        
        operative = operativeRepository.save(operative);
        log.info("Operative {} created", operative.getEmployeeRef());
        
        return mapToResponse(operative);
    }
    
    @Override
    @Transactional
    public OperativeResponse update(Long id, OperativeRequest request) {
        Operative operative = operativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", id));
        
        if (request.getEmployerId() != null && (operative.getEmployer() == null || 
                !request.getEmployerId().equals(operative.getEmployer().getId()))) {
            Company employer = companyRepository.findById(request.getEmployerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getEmployerId()));
            operative.setEmployer(employer);
        }
        
        operative.setEmployeeRef(request.getEmployeeRef());
        operative.setFirstName(request.getFirstName());
        operative.setLastName(request.getLastName());
        operative.setDateOfBirth(request.getDateOfBirth());
        operative.setGender(request.getGender());
        operative.setNationality(request.getNationality());
        operative.setNiNumber(request.getNiNumber());
        operative.setUtr(request.getUtr());
        operative.setRightToWorkExpiry(request.getRightToWorkExpiry());
        operative.setRightToWorkDocType(request.getRightToWorkDocType());
        operative.setPassportNumber(request.getPassportNumber());
        operative.setBankSortCode(request.getBankSortCode());
        operative.setBankAccountNumber(request.getBankAccountNumber());
        operative.setEmploymentStatus(request.getEmploymentStatus());
        operative.setStatus(request.getStatus());
        
        operative = operativeRepository.save(operative);
        return mapToResponse(operative);
    }
    
    @Override
    public SubbieGateStatus smartCheckCard(Long operativeId, Long cardId) {
        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId));
        
        // Stub for CSCS Smart Check API integration
        log.info("Smart checking card {} for operative {}", cardId, operativeId);
        
        boolean isValid = card.getExpiryDate() != null && 
                         card.getExpiryDate().isAfter(LocalDate.now());
        
        return SubbieGateStatus.builder()
                .operativeId(operativeId)
                .isHMRCVerified(operative.getHmrcVerified())
                .isCSCSValid(isValid && card.getCardType() == CardType.CSCS)
                .isRAMSValid(true) // Would check RAMS completion
                .isInductionValid(true) // Would check induction status
                .isPlantTicketValid(true) // Would check plant tickets
                .isGateOpen(isValid)
                .statusMessage(isValid ? "All checks passed" : "Card validation failed")
                .build();
    }
    
    @Override
    public SubbieGateStatus getSubbieGateStatus(Long operativeId) {
        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));
        
        boolean hasValidCard = operative.getCards().stream()
                .anyMatch(c -> c.getExpiryDate() != null && c.getExpiryDate().isAfter(LocalDate.now()));
        
        return SubbieGateStatus.builder()
                .operativeId(operativeId)
                .isHMRCVerified(operative.getHmrcVerified())
                .isCSCSValid(hasValidCard)
                .isRAMSValid(true)
                .isInductionValid(true)
                .isPlantTicketValid(true)
                .isGateOpen(hasValidCard)
                .statusMessage(hasValidCard ? "Gate Open" : "Card validation required")
                .build();
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Operative operative = operativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", id));
        operativeRepository.delete(operative);
        log.info("Operative {} deleted", operative.getEmployeeRef());
    }
    
    private OperativeResponse mapToResponse(Operative operative) {
        return OperativeResponse.builder()
                .id(operative.getId())
                .employeeRef(operative.getEmployeeRef())
                .firstName(operative.getFirstName())
                .lastName(operative.getLastName())
                .fullName(operative.getFullName())
                .dateOfBirth(operative.getDateOfBirth() != null ? operative.getDateOfBirth().toString() : null)
                .gender(operative.getGender())
                .nationality(operative.getNationality())
                .niNumber(operative.getNiNumber())
                .utr(operative.getUtr() != null ? operative.getUtr().toString() : null)
                .rightToWorkExpiry(operative.getRightToWorkExpiry() != null ? operative.getRightToWorkExpiry().toString() : null)
                .rightToWorkDocType(operative.getRightToWorkDocType())
                .employmentStatus(operative.getEmploymentStatus() != null ? operative.getEmploymentStatus().name() : null)
                .status(operative.getStatus() != null ? operative.getStatus().name() : null)
                .employerId(operative.getEmployer() != null ? operative.getEmployer().getId() : null)
                .employerName(operative.getEmployer() != null ? operative.getEmployer().getName() : null)
                .build();
    }
}