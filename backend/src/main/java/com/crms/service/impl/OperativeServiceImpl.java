package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.healthsafety.entity.RAMSDocument;
import com.crms.domain.healthsafety.repository.RAMSDocumentRepository;
import com.crms.domain.healthsafety.repository.RAMSSignOnRepository;
import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Induction;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.entity.Qualification;
import com.crms.domain.operative.enums.CardType;
import com.crms.domain.operative.enums.OperativeStatus;
import com.crms.domain.operative.enums.QualificationType;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.InductionRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.operative.repository.QualificationRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.OperativeRequest;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.OperativeService;
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
    private final RAMSSignOnRepository ramsSignOnRepository;
    private final InductionRepository inductionRepository;
    private final QualificationRepository qualificationRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<OperativeResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");
        
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public SubbieGateStatus smartCheckCard(Long operativeId, Long cardId) {
        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId));

        log.info("Smart checking card {} for operative {}", cardId, operativeId);

        // CSCS/CPCS card must be valid — this is the hard gate requirement
        boolean isCardValid = card.getExpiryDate() != null
                && card.getExpiryDate().isAfter(LocalDate.now())
                && (card.getCardType() == CardType.CSCS || card.getCardType() == CardType.CPCS);

        // RAMS: operative must have a currently-valid RAMS sign-on record
        boolean hasRams = !ramsSignOnRepository.findByOperativeId(operativeId).isEmpty()
                && ramsSignOnRepository.findByOperativeId(operativeId).stream()
                        .anyMatch(r -> r.isValid());

        // Induction: operative must have a valid induction record for any site
        boolean hasInduction = !inductionRepository.findByOperativeId(operativeId).isEmpty()
                && inductionRepository.findByOperativeId(operativeId).stream()
                        .anyMatch(Induction::isValid);

        // Plant ticket: operative must hold a valid plant operation qualification
        // Recognised types: NPORS, CPCS, CPCS_BLUE, CITY_AND_GUILDS
        boolean hasPlantTicket = qualificationRepository.findByOperativeId(operativeId).stream()
                .anyMatch(q -> q.isValid() && (q.getQualificationType() == QualificationType.NPORS
                        || q.getQualificationType() == QualificationType.CPCS
                        || q.getQualificationType() == QualificationType.CPCS_BLUE
                        || q.getQualificationType() == QualificationType.CITY_AND_GUILDS));

        // Gate opens only when CSCS/CPCS card is valid
        // RAMS, induction, and plant ticket generate advisory warnings but do not block entry
        boolean gateOpen = isCardValid;

        String statusMessage;
        if (!isCardValid) {
            statusMessage = "CSCS/CPCS card missing or expired — gate locked";
        } else if (!hasRams) {
            statusMessage = "RAMS sign-on required for this site";
        } else if (!hasInduction) {
            statusMessage = "Site induction required";
        } else if (!hasPlantTicket) {
            statusMessage = "Plant ticket recommended — contact supervisor";
        } else {
            statusMessage = "All checks passed — gate open";
        }

        return SubbieGateStatus.builder()
                .operativeId(operativeId)
                .isHMRCVerified(operative.getHmrcVerified())
                .isCSCSValid(isCardValid)
                .isRAMSValid(hasRams)
                .isInductionValid(hasInduction)
                .isPlantTicketValid(hasPlantTicket)
                .isGateOpen(gateOpen)
                .statusMessage(statusMessage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SubbieGateStatus getSubbieGateStatus(Long operativeId) {
        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));

        boolean hasValidCard = operative.getCards().stream()
                .anyMatch(c -> c.getExpiryDate() != null && c.getExpiryDate().isAfter(LocalDate.now())
                        && (c.getCardType() == CardType.CSCS || c.getCardType() == CardType.CPCS));

        boolean hasRams = ramsSignOnRepository.findByOperativeId(operativeId).stream()
                .anyMatch(RAMSSignOn::isValid);

        boolean hasInduction = inductionRepository.findByOperativeId(operativeId).stream()
                .anyMatch(Induction::isValid);

        boolean hasPlantTicket = qualificationRepository.findByOperativeId(operativeId).stream()
                .anyMatch(q -> q.isValid() && (q.getQualificationType() == QualificationType.NPORS
                        || q.getQualificationType() == QualificationType.CPCS
                        || q.getQualificationType() == QualificationType.CPCS_BLUE));

        String statusMessage;
        if (!hasValidCard) {
            statusMessage = "Card validation required — CSCS/CPCS card missing or expired";
        } else if (!hasRams) {
            statusMessage = "RAMS sign-on required";
        } else if (!hasInduction) {
            statusMessage = "Site induction required";
        } else if (!hasPlantTicket) {
            statusMessage = "Plant ticket recommended for plant operations";
        } else {
            statusMessage = "Gate Open — all checks passed";
        }

        return SubbieGateStatus.builder()
                .operativeId(operativeId)
                .isHMRCVerified(operative.getHmrcVerified())
                .isCSCSValid(hasValidCard)
                .isRAMSValid(hasRams)
                .isInductionValid(hasInduction)
                .isPlantTicketValid(hasPlantTicket)
                .isGateOpen(hasValidCard)
                .statusMessage(statusMessage)
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