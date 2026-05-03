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

    // Compatibility overloads for legacy Map-based callers/tests.
    public java.util.Map<String, Object> createCPP(Long contractId, java.util.Map<String, Object> request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        ConstructionPhasePlan cpp = ConstructionPhasePlan.builder()
                .contract(contract)
                .planRef("CPP-" + System.currentTimeMillis())
                .version("1")
                .description((String) request.getOrDefault("description", request.get("projectDescription")))
                .status(CppStatus.DRAFT)
                .build();
        cpp = cppRepository.save(cpp);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", cpp.getId());
        result.put("planRef", cpp.getPlanRef());
        result.put("status", cpp.getStatus() != null ? cpp.getStatus().name() : null);
        return result;
    }

    public Object createCPP(Long contractId, Object request) {
        if (request instanceof java.util.Map<?, ?> map) {
            @SuppressWarnings("unchecked") java.util.Map<String, Object> typed = (java.util.Map<String, Object>) map;
            return createCPP(contractId, typed);
        }
        throw new IllegalArgumentException("Unsupported CPP request type");
    }

    public java.util.Map<String, Object> createRAMS(Long contractId, java.util.Map<String, Object> request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        RAMSDocument rams = RAMSDocument.builder()
                .contract(contract)
                .ramsRef("RAMS-" + System.currentTimeMillis())
                .title((String) request.getOrDefault("title", "RAMS"))
                .description((String) request.get("description"))
                .version("1")
                .status(RamsStatus.DRAFT)
                .isActive(true)
                .build();
        rams = ramsRepository.save(rams);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", rams.getId());
        result.put("ramsRef", rams.getRamsRef());
        result.put("status", rams.getStatus() != null ? rams.getStatus().name() : null);
        return result;
    }

    public Object createRAMS(Long contractId, Object request) {
        if (request instanceof java.util.Map<?, ?> map) {
            @SuppressWarnings("unchecked") java.util.Map<String, Object> typed = (java.util.Map<String, Object>) map;
            return createRAMS(contractId, typed);
        }
        throw new IllegalArgumentException("Unsupported RAMS request type");
    }

    public java.util.Map<String, Object> createPermit(java.util.Map<String, Object> request) {
        Object permitType = request.get("permitType");
        if (permitType != null && !"PERMIT_TO_DIG".equals(String.valueOf(permitType))) {
            throw new IllegalArgumentException("Unsupported permit type");
        }
        Long siteId = Long.valueOf(String.valueOf(request.get("siteId")));
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site", siteId));
        PermitToDig permit = PermitToDig.builder()
                .site(site)
                .permitNumber("PTD-" + System.currentTimeMillis())
                .worksDescription((String) request.getOrDefault("description", request.get("worksDescription")))
                .status(PermitStatus.DRAFT)
                .build();
        permit = permitToDigRepository.save(permit);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", permit.getId());
        result.put("permitNumber", permit.getPermitNumber());
        result.put("status", permit.getStatus() != null ? permit.getStatus().name() : null);
        return result;
    }

    public Object createPermit(Object request) {
        if (request instanceof java.util.Map<?, ?> map) {
            @SuppressWarnings("unchecked") java.util.Map<String, Object> typed = (java.util.Map<String, Object>) map;
            return createPermit(typed);
        }
        throw new IllegalArgumentException("Unsupported permit request type");
    }

    public java.util.Map<String, Object> createIncident(java.util.Map<String, Object> request) {
        Long siteId = Long.valueOf(String.valueOf(request.get("siteId")));
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site", siteId));
        Operative operative = null;
        if (request.get("operativeId") != null) {
            Long operativeId = Long.valueOf(String.valueOf(request.get("operativeId")));
            operative = operativeRepository.findById(operativeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Operative", operativeId));
        }
        IncidentReport incident = IncidentReport.builder()
                .site(site)
                .operative(operative)
                .reportNumber("INC-" + System.currentTimeMillis())
                .description((String) request.get("description"))
                .type(request.get("type") != null ? IncidentType.valueOf(String.valueOf(request.get("type"))) : IncidentType.NEAR_MISS)
                .severity(request.get("severity") != null ? Severity.valueOf(String.valueOf(request.get("severity"))) : Severity.MINOR)
                .status(IncidentStatus.DRAFT)
                .build();
        incident = incidentReportRepository.save(incident);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", incident.getId());
        result.put("reportNumber", incident.getReportNumber());
        result.put("status", incident.getStatus() != null ? incident.getStatus().name() : null);
        return result;
    }

    public Object createIncident(Object request) {
        if (request instanceof java.util.Map<?, ?> map) {
            @SuppressWarnings("unchecked") java.util.Map<String, Object> typed = (java.util.Map<String, Object>) map;
            return createIncident(typed);
        }
        throw new IllegalArgumentException("Unsupported incident request type");
    }


    public java.util.Map<String, Object> createF10(Long contractId, java.util.Map<String, Object> request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        F10Notification f10 = F10Notification.builder()
                .contract(contract)
                .notificationNumber("F10-" + System.currentTimeMillis())
                .moreThan30Days(Boolean.parseBoolean(String.valueOf(request.getOrDefault("moreThan30Days", "false"))))
                .moreThan500PersonDays(Boolean.parseBoolean(String.valueOf(request.getOrDefault("moreThan500PersonDays", "false"))))
                .isActive(true)
                .hdfAcknowledged(false)
                .build();
        f10 = f10NotificationRepository.save(f10);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", f10.getId());
        result.put("notificationNumber", f10.getNotificationNumber());
        result.put("status", "DRAFT");
        return result;
    }

    public Object createF10(Long contractId, Object request) {
        if (request instanceof java.util.Map<?, ?> map) {
            @SuppressWarnings("unchecked") java.util.Map<String, Object> typed = (java.util.Map<String, Object>) map;
            return createF10(contractId, typed);
        }
        throw new IllegalArgumentException("Unsupported F10 request type");
    }

}
