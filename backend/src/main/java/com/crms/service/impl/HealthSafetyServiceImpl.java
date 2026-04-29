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
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.HealthSafetyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
    public Object createF10(Long contractId, Object request) {
        if (!(request instanceof Map)) {
            throw new IllegalArgumentException("Request must be a Map");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> req = (Map<String, Object>) request;

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        String notificationNumber = "F10-" + System.currentTimeMillis();

        F10Notification f10 = F10Notification.builder()
                .contract(contract)
                .notificationNumber(notificationNumber)
                .moreThan30Days(Boolean.parseBoolean(req.getOrDefault("moreThan30Days", "false").toString()))
                .moreThan500PersonDays(Boolean.parseBoolean(req.getOrDefault("moreThan500PersonDays", "false").toString()))
                .constructionStartDate(req.containsKey("constructionStartDate") ?
                        LocalDate.parse(req.get("constructionStartDate").toString()) : LocalDate.now())
                .constructionEndDate(req.containsKey("constructionEndDate") ?
                        LocalDate.parse(req.get("constructionEndDate").toString()) : null)
                .isActive(true)
                .hdfAcknowledged(false)
                .build();

        f10 = f10NotificationRepository.save(f10);
        log.info("Created F10 notification {} for contract {}", notificationNumber, contract.getContractRef());

        Map<String, Object> result = new HashMap<>();
        result.put("id", f10.getId());
        result.put("notificationNumber", f10.getNotificationNumber());
        result.put("contractId", contractId);
        result.put("status", "DRAFT");
        return result;
    }

    @Override
    @Transactional
    public Object createCPP(Long contractId, Object request) {
        if (!(request instanceof Map)) {
            throw new IllegalArgumentException("Request must be a Map");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> req = (Map<String, Object>) request;

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        String planRef = "CPP-" + System.currentTimeMillis();

        ConstructionPhasePlan cpp = ConstructionPhasePlan.builder()
                .contract(contract)
                .planRef(planRef)
                .version(1)
                .status(CPPStatus.DRAFT)
                .build();

        if (req.containsKey("description")) {
            cpp.setDescription(req.get("description").toString());
        }
        if (req.containsKey("startDate")) {
            cpp.setStartDate(LocalDate.parse(req.get("startDate").toString()));
        }
        if (req.containsKey("endDate")) {
            cpp.setEndDate(LocalDate.parse(req.get("endDate").toString()));
        }

        cpp = cppRepository.save(cpp);
        log.info("Created CPP {} for contract {}", planRef, contract.getContractRef());

        Map<String, Object> result = new HashMap<>();
        result.put("id", cpp.getId());
        result.put("planRef", cpp.getPlanRef());
        result.put("contractId", contractId);
        result.put("status", cpp.getStatus().name());
        return result;
    }

    @Override
    @Transactional
    public Object createRAMS(Long contractId, Object request) {
        if (!(request instanceof Map)) {
            throw new IllegalArgumentException("Request must be a Map");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> req = (Map<String, Object>) request;

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));

        String ramsRef = "RAMS-" + System.currentTimeMillis();

        RAMSDocument rams = RAMSDocument.builder()
                .contract(contract)
                .ramsRef(ramsRef)
                .version(1)
                .status(RamsStatus.DRAFT)
                .build();

        if (req.containsKey("title")) {
            rams.setTitle(req.get("title").toString());
        }
        if (req.containsKey("description")) {
            rams.setDescription(req.get("description").toString());
        }
        if (req.containsKey("validUntil")) {
            rams.setValidUntil(LocalDate.parse(req.get("validUntil").toString()));
        }

        rams = ramsRepository.save(rams);
        log.info("Created RAMS {} for contract {}", ramsRef, contract.getContractRef());

        Map<String, Object> result = new HashMap<>();
        result.put("id", rams.getId());
        result.put("ramsRef", rams.getRamsRef());
        result.put("contractId", contractId);
        result.put("status", rams.getStatus().name());
        return result;
    }

    @Override
    @Transactional
    public Object signRAMS(Long ramsId, Long operativeId, Long siteId) {
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

        Map<String, Object> result = new HashMap<>();
        result.put("id", signOn.getId());
        result.put("ramsId", ramsId);
        result.put("operativeId", operativeId);
        result.put("siteId", siteId);
        result.put("signedAt", signOn.getSignedAt());
        return result;
    }

    @Override
    @Transactional
    public Object createPermit(Object request) {
        if (!(request instanceof Map)) {
            throw new IllegalArgumentException("Request must be a Map");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> req = (Map<String, Object>) request;

        String permitType = req.getOrDefault("permitType", "PERMIT_TO_DIG").toString();

        if ("PERMIT_TO_DIG".equals(permitType)) {
            return createPermitToDig(req);
        }

        throw new IllegalArgumentException("Unknown permit type: " + permitType);
    }

    @Transactional
    private Object createPermitToDig(Map<String, Object> req) {
        Long siteId = Long.parseLong(req.get("siteId").toString());
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site", siteId));

        String permitNumber = "PTD-" + System.currentTimeMillis();

        PermitToDig permit = PermitToDig.builder()
                .site(site)
                .permitNumber(permitNumber)
                .worksDescription(req.getOrDefault("worksDescription", "").toString())
                .startDate(req.containsKey("startDate") ?
                        LocalDate.parse(req.get("startDate").toString()) : LocalDate.now())
                .endDate(req.containsKey("endDate") ?
                        LocalDate.parse(req.get("endDate").toString()) : null)
                .status(PermitStatus.DRAFT)
                .build();

        permit = permitToDigRepository.save(permit);

        Map<String, Object> result = new HashMap<>();
        result.put("id", permit.getId());
        result.put("permitNumber", permit.getPermitNumber());
        result.put("siteId", siteId);
        result.put("status", permit.getStatus().name());
        return result;
    }

    @Override
    @Transactional
    public Object approvePermit(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.ISSUED);
        permit.setIssuedDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);

        log.info("Approved permit {} with status {}", permit.getPermitNumber(), permit.getStatus());

        Map<String, Object> result = new HashMap<>();
        result.put("id", permit.getId());
        result.put("permitNumber", permit.getPermitNumber());
        result.put("status", permit.getStatus().name());
        result.put("issuedDate", permit.getIssuedDate());
        return result;
    }

    @Override
    @Transactional
    public Object createIncident(Object request) {
        if (!(request instanceof Map)) {
            throw new IllegalArgumentException("Request must be a Map");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> req = (Map<String, Object>) request;

        Long siteId = Long.parseLong(req.get("siteId").toString());
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site", siteId));

        String reportNumber = "INC-" + System.currentTimeMillis();

        IncidentReport incident = IncidentReport.builder()
                .site(site)
                .reportNumber(reportNumber)
                .incidentDate(req.containsKey("incidentDate") ?
                        LocalDate.parse(req.get("incidentDate").toString()) : LocalDate.now())
                .description(req.getOrDefault("description", "").toString())
                .type(req.containsKey("type") ?
                        com.crms.domain.healthsafety.enums.IncidentType.valueOf(req.get("type").toString()) : null)
                .severity(req.containsKey("severity") ?
                        com.crms.domain.healthsafety.enums.IncidentSeverity.valueOf(req.get("severity").toString()) : null)
                .status(IncidentStatus.DRAFT)
                .build();

        if (req.containsKey("locationDescription")) {
            incident.setLocationDescription(req.get("locationDescription").toString());
        }
        if (req.containsKey("operativeId")) {
            Long operativeId = Long.parseLong(req.get("operativeId").toString());
            Operative operative = operativeRepository.findById(operativeId).orElse(null);
            incident.setOperative(operative);
        }

        incident = incidentReportRepository.save(incident);

        Map<String, Object> result = new HashMap<>();
        result.put("id", incident.getId());
        result.put("reportNumber", incident.getReportNumber());
        result.put("siteId", siteId);
        result.put("status", incident.getStatus().name());
        return result;
    }
}
