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
import com.crms.dto.request.CardRequest;
import com.crms.dto.request.OperativeRequest;
import com.crms.dto.request.QualificationRequest;
import com.crms.dto.response.CardResponse;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.QualificationResponse;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        String search = params.getOrDefault("search", "").toString().trim();

        if (params.containsKey("status") && params.get("status") != null) {
            operativePage = operativeRepository.findByStatus(
                    OperativeStatus.valueOf(params.get("status").toString()), pageable);
        } else if (!search.isEmpty()) {
            operativePage = operativeRepository.searchByNameOrRef(search, pageable);
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
                .employmentStatus(request.getEmploymentStatus() != null ? request.getEmploymentStatus() : com.crms.domain.operative.enums.EmploymentStatus.PAYE)
                .status(request.getStatus() != null ? request.getStatus() : OperativeStatus.ACTIVE)
                .employer(employer)
                .hmrcVerified(request.getHmrcVerified() != null ? request.getHmrcVerified() : false)
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
        if (request.getStatus() != null) {
            operative.setStatus(request.getStatus());
        }

        operative = operativeRepository.save(operative);
        return mapToResponse(operative);
    }

    // Cards
    @Override
    @Transactional
    public CardResponse addCard(Long operativeId, CardRequest request) {
        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));

        Card card = Card.builder()
                .operative(operative)
                .cardType(request.getCardType())
                .scheme(request.getScheme())
                .cardNumber(request.getCardNumber())
                .expiryDate(request.getExpiryDate())
                .photoUrl(request.getPhotoUrl())
                .competencyRef(request.getCompetencyRef())
                .isVerified(false)
                .build();

        card = cardRepository.save(card);
        log.info("Card {} added to operative {}", request.getCardNumber(), operativeId);
        return mapToCardResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> getCards(Long operativeId) {
        operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));
        return cardRepository.findByOperativeId(operativeId).stream()
                .map(this::mapToCardResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardResponse deleteCard(Long operativeId, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId));
        if (!card.getOperative().getId().equals(operativeId)) {
            throw new ResourceNotFoundException("Card", cardId);
        }
        cardRepository.delete(card);
        log.info("Card {} deleted from operative {}", cardId, operativeId);
        return mapToCardResponse(card);
    }

    // Qualifications
    @Override
    @Transactional
    public QualificationResponse addQualification(Long operativeId, QualificationRequest request) {
        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));

        Qualification qual = Qualification.builder()
                .operative(operative)
                .qualificationType(request.getQualificationType())
                .level(request.getLevel())
                .awardingBody(request.getAwardingBody())
                .certificateNumber(request.getCertificateNumber())
                .achievedDate(request.getAchievedDate())
                .expiryDate(request.getExpiryDate())
                .notes(request.getNotes())
                .build();

        qual = qualificationRepository.save(qual);
        log.info("Qualification {} added to operative {}", request.getQualificationType(), operativeId);
        return mapToQualificationResponse(qual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualificationResponse> getQualifications(Long operativeId) {
        operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));
        return qualificationRepository.findByOperativeId(operativeId).stream()
                .map(this::mapToQualificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QualificationResponse deleteQualification(Long operativeId, Long qualificationId) {
        Qualification qual = qualificationRepository.findById(qualificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Qualification", qualificationId));
        if (!qual.getOperative().getId().equals(operativeId)) {
            throw new ResourceNotFoundException("Qualification", qualificationId);
        }
        qualificationRepository.delete(qual);
        log.info("Qualification {} deleted from operative {}", qualificationId, operativeId);
        return mapToQualificationResponse(qual);
    }

    // CSCS Smart Check
    @Override
    @Transactional(readOnly = true)
    public SubbieGateStatus smartCheckCard(Long operativeId, Long cardId) {
        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardId));

        log.info("Smart checking card {} for operative {}", cardId, operativeId);

        boolean isCardValid = card.getExpiryDate() != null
                && card.getExpiryDate().isAfter(LocalDate.now())
                && (card.getCardType() == CardType.CSCS || card.getCardType() == CardType.CPCS);

        boolean hasRams = !ramsSignOnRepository.findByOperativeId(operativeId).isEmpty()
                && ramsSignOnRepository.findByOperativeId(operativeId).stream()
                        .anyMatch(r -> r.isValid());

        boolean hasInduction = !inductionRepository.findByOperativeId(operativeId).isEmpty()
                && inductionRepository.findByOperativeId(operativeId).stream()
                        .anyMatch(Induction::isValid);

        boolean hasPlantTicket = qualificationRepository.findByOperativeId(operativeId).stream()
                .anyMatch(q -> q.isValid() && (q.getQualificationType() == QualificationType.NPORS
                        || q.getQualificationType() == QualificationType.CPCS
                        || q.getQualificationType() == QualificationType.CPCS_BLUE
                        || q.getQualificationType() == QualificationType.CITY_AND_GUILDS));

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
                .hmrcVerified(operative.getHmrcVerified())
                .build();
    }

    private CardResponse mapToCardResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .operativeId(card.getOperative().getId())
                .cardType(card.getCardType() != null ? card.getCardType().name() : null)
                .scheme(card.getScheme())
                .cardNumber(card.getCardNumber())
                .expiryDate(card.getExpiryDate() != null ? card.getExpiryDate().toString() : null)
                .photoUrl(card.getPhotoUrl())
                .isVerified(card.getIsVerified())
                .lastCheckedAt(card.getLastCheckedAt() != null ? card.getLastCheckedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .competencyRef(card.getCompetencyRef())
                .isValid(card.isValid())
                .isExpiringSoon(card.isExpiringSoon(30))
                .build();
    }

    private QualificationResponse mapToQualificationResponse(Qualification qual) {
        return QualificationResponse.builder()
                .id(qual.getId())
                .operativeId(qual.getOperative().getId())
                .qualificationType(qual.getQualificationType() != null ? qual.getQualificationType().name() : null)
                .level(qual.getLevel())
                .awardingBody(qual.getAwardingBody())
                .certificateNumber(qual.getCertificateNumber())
                .achievedDate(qual.getAchievedDate() != null ? qual.getAchievedDate().toString() : null)
                .expiryDate(qual.getExpiryDate() != null ? qual.getExpiryDate().toString() : null)
                .notes(qual.getNotes())
                .isValid(qual.isValid())
                .isExpiringSoon(qual.isExpiringSoon(30))
                .build();
    }
}