package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.healthsafety.entity.*;
import com.crms.domain.healthsafety.enums.*;
import com.crms.domain.healthsafety.repository.*;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.*;
import com.crms.dto.response.*;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.HealthSafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthSafetyServiceImpl implements HealthSafetyService {

    private final ContractRepository contractRepository;
    private final SiteRepository siteRepository;
    private final OperativeRepository operativeRepository;
    private final F10NotificationRepository f10NotificationRepository;
    private final ConstructionPhasePlanRepository cppRepository;
    private final RAMSDocumentRepository ramsRepository;
    private final RAMSSignOnRepository ramsSignOnRepository;
    private final PermitToDigRepository permitToDigRepository;
    private final IncidentReportRepository incidentReportRepository;

    @Override
    @Transactional
    public F10NotificationResponse createF10(Long contractId, F10CreateRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        String notificationNumber = "F10-" + System.currentTimeMillis();

        F10Notification f10 = F10Notification.builder()
                .contract(contract)
                .notificationNumber(notificationNumber)
                .moreThan30Days(request.getMoreThan30Days() != null ? request.getMoreThan30Days() : false)
                .moreThan500PersonDays(request.getMoreThan500PersonDays() != null ? request.getMoreThan500PersonDays() : false)
                .constructionStartDate(request.getStartDate())
                .constructionEndDate(request.getExpectedCompletionDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .hdfAcknowledged(false)
                .build();

        f10 = f10NotificationRepository.save(f10);
        log.info("Created F10 notification {} for contract {}", notificationNumber, contract.getContractRef());

        return F10NotificationResponse.fromEntity(f10);
    }

    @Override
    @Transactional
    public ConstructionPhasePlanResponse createCPP(Long contractId, CPPCreateRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        String planRef = request.getDocumentRef() != null ? request.getDocumentRef() : "CPP-" + System.currentTimeMillis();

        ConstructionPhasePlan cpp = ConstructionPhasePlan.builder()
                .contract(contract)
                .planRef(planRef)
                .title(request.getTitle())
                .description(request.getScopeOfWork())
                .version(request.getVersion() != null ? request.getVersion() : "1")
                .status(CppStatus.DRAFT)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        cpp = cppRepository.save(cpp);
        log.info("Created CPP {} for contract {}", planRef, contract.getContractRef());

        return ConstructionPhasePlanResponse.fromEntity(cpp);
    }

    @Override
    @Transactional
    public RAMSDocumentResponse createRAMS(Long contractId, RAMSCreateRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        String ramsRef = request.getDocumentRef() != null ? request.getDocumentRef() : "RAMS-" + System.currentTimeMillis();

        RAMSDocument rams = RAMSDocument.builder()
                .contract(contract)
                .ramsRef(ramsRef)
                .title(request.getTitle())
                .description(request.getScopeOfWork())
                .version(request.getVersion() != null ? request.getVersion() : "1")
                .status(RamsStatus.DRAFT)
                .validUntil(request.getExpiryDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        rams = ramsRepository.save(rams);
        log.info("Created RAMS {} for contract {}", ramsRef, contract.getContractRef());

        return RAMSDocumentResponse.fromEntity(rams);
    }

    @Override
    @Transactional
    public RAMSSignOnResponse signRAMS(Long ramsId, Long operativeId, Long siteId) {
        RAMSDocument rams = ramsRepository.findById(ramsId)
                .orElseThrow(() -> new ResourceNotFoundException("RAMSDocument", ramsId));

        Operative operative = operativeRepository.findById(operativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site", siteId));

        RAMSSignOn signOn = RAMSSignOn.builder()
                .rams(rams)
                .operative(operative)
                .site(site)
                .signedAt(java.time.LocalDateTime.now())
                .build();

        signOn = ramsSignOnRepository.save(signOn);

        log.info("Operative {} signed RAMS {} at site {}", operative.getId(), rams.getRamsRef(), siteId);

        return RAMSSignOnResponse.fromEntity(signOn);
    }

    @Override
    @Transactional
    public PermitToDigResponse createPermit(PermitToDigCreateRequest request) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        String permitNumber = request.getPermitNumber() != null ? request.getPermitNumber() : "PTD-" + System.currentTimeMillis();

        PermitToDig permit = PermitToDig.builder()
                .site(site)
                .permitNumber(permitNumber)
                .locationDescription(request.getLocationDescription())
                .worksDescription(request.getNatureOfExcavation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(PermitStatus.DRAFT)
                .build();

        permit = permitToDigRepository.save(permit);
        log.info("Created permit {} for site {}", permitNumber, site.getSiteName());

        return PermitToDigResponse.fromEntity(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse approvePermit(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.ISSUED);
        permit.setIssuedDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);

        log.info("Approved permit {} with status {}", permit.getPermitNumber(), permit.getStatus());

        return PermitToDigResponse.fromEntity(permit);
    }

    @Override
    @Transactional
    public IncidentReportResponse createIncident(IncidentCreateRequest request) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        String reportNumber = "INC-" + System.currentTimeMillis();

        IncidentReport incident = IncidentReport.builder()
                .site(site)
                .reportNumber(reportNumber)
                .incidentDate(request.getDateTimeOfIncident())
                .description(request.getDescription())
                .type(request.getIncidentType())
                .severity(request.getSeverity() != null ? Severity.valueOf(request.getSeverity()) : Severity.MINOR)
                .status(IncidentStatus.DRAFT)
                .locationDescription(request.getLocation())
                .ridDORNotifiable(request.getRidDORNotifiable())
                .build();

        incident = incidentReportRepository.save(incident);

        return IncidentReportResponse.fromEntity(incident);
    }
}
