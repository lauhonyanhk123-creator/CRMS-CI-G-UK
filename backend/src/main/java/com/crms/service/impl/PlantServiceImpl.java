package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.enums.CardType;
import com.crms.domain.operative.repository.CardRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.plant.entity.LOLERExamination;
import com.crms.domain.plant.entity.PlantAllocation;
import com.crms.domain.plant.entity.PlantItem;
import com.crms.domain.plant.entity.PUWERInspection;
import com.crms.domain.plant.enums.AllocationStatus;
import com.crms.domain.plant.enums.PlantStatus;
import com.crms.domain.plant.repository.LOLERExaminationRepository;
import com.crms.domain.plant.repository.PlantAllocationRepository;
import com.crms.domain.plant.repository.PlantItemRepository;
import com.crms.domain.plant.repository.PUWERInspectionRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.LOLERRequest;
import com.crms.dto.request.PlantAllocationRequest;
import com.crms.dto.request.PlantItemRequest;
import com.crms.dto.request.PUWERRequest;
import com.crms.dto.response.LOLERResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.PlantGanttItem;
import com.crms.dto.response.PlantItemResponse;
import com.crms.dto.response.PUWERResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.PlantService;
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
public class PlantServiceImpl implements PlantService {

    private final PlantItemRepository plantRepository;
    private final CompanyRepository companyRepository;
    private final LOLERExaminationRepository lolerRepository;
    private final PUWERInspectionRepository puwerRepository;
    private final PlantAllocationRepository allocationRepository;
    private final OperativeRepository operativeRepository;
    private final SiteRepository siteRepository;
    private final CardRepository cardRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PlantItemResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

        Page<PlantItem> plantPage;
        String search = params.getOrDefault("search", "").toString().trim();

        if (params.containsKey("status") && params.get("status") != null) {
            PlantStatus status = PlantStatus.valueOf(params.get("status").toString().toUpperCase());
            plantPage = plantRepository.findByStatus(status, pageable);
        } else if (params.containsKey("category") && params.get("category") != null) {
            plantPage = plantRepository.findByCategory(
                    com.crms.domain.plant.enums.PlantCategory.valueOf(params.get("category").toString().toUpperCase()), pageable);
        } else if (!search.isEmpty()) {
            plantPage = plantRepository.searchByRefOrDescription(search, pageable);
        } else {
            plantPage = plantRepository.findAll(pageable);
        }

        List<PlantItemResponse> content = plantPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<PlantItemResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(plantPage.getTotalElements())
                .totalPages(plantPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PlantItemResponse findById(Long id) {
        PlantItem plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", id));
        return mapToResponse(plant);
    }

    @Override
    @Transactional
    public PlantItemResponse create(PlantItemRequest request) {
        Company supplier = null;
        if (request.getSupplierId() != null) {
            supplier = companyRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getSupplierId()));
        }

        PlantItem plant = PlantItem.builder()
                .plantRef(request.getPlantRef())
                .serialNumber(request.getSerialNumber())
                .description(request.getDescription())
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .category(request.getCategory())
                .weight(request.getWeight())
                .hireStatus(request.getHireStatus())
                .supplier(supplier)
                .telematicsId(request.getTelematicsId())
                .quickHitchType(request.getQuickHitchType())
                .status(request.getStatus() != null ? request.getStatus() : PlantStatus.AVAILABLE)
                .dailyHireRate(request.getDailyHireRate())
                .notes(request.getNotes())
                .build();

        plant = plantRepository.save(plant);
        log.info("Plant item {} created", plant.getPlantRef());

        return mapToResponse(plant);
    }

    @Override
    @Transactional
    public PlantItemResponse update(Long id, PlantItemRequest request) {
        PlantItem plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", id));

        if (request.getSupplierId() != null) {
            Company supplier = companyRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getSupplierId()));
            plant.setSupplier(supplier);
        }

        plant.setPlantRef(request.getPlantRef());
        plant.setSerialNumber(request.getSerialNumber());
        plant.setDescription(request.getDescription());
        plant.setMake(request.getMake());
        plant.setModel(request.getModel());
        plant.setYear(request.getYear());
        plant.setCategory(request.getCategory());
        plant.setWeight(request.getWeight());
        plant.setHireStatus(request.getHireStatus());
        plant.setTelematicsId(request.getTelematicsId());
        plant.setQuickHitchType(request.getQuickHitchType());
        if (request.getStatus() != null) {
            plant.setStatus(request.getStatus());
        }
        plant.setDailyHireRate(request.getDailyHireRate());
        plant.setNotes(request.getNotes());

        plant = plantRepository.save(plant);
        return mapToResponse(plant);
    }

    // LOLER Examinations
    @Override
    @Transactional
    public LOLERResponse addLOLER(Long plantId, LOLERRequest request) {
        PlantItem plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", plantId));

        LOLERExamination loler = LOLERExamination.builder()
                .plant(plant)
                .examinationDate(request.getExaminationDate())
                .nextDueDate(request.getNextDueDate())
                .examiner(request.getExaminer())
                .examinerCompany(request.getExaminerCompany())
                .result(request.getResult())
                .reportRef(request.getReportRef())
                .notes(request.getNotes())
                .documentRef(request.getDocumentRef())
                .build();

        loler = lolerRepository.save(loler);
        log.info("LOLER examination {} recorded for plant {}", loler.getId(), plantId);
        return mapToLOLERResponse(loler);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LOLERResponse> getLOLERHistory(Long plantId) {
        plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", plantId));
        return lolerRepository.findByPlantIdOrderByExaminationDateDesc(plantId).stream()
                .map(this::mapToLOLERResponse)
                .collect(Collectors.toList());
    }

    // PUWER Inspections
    @Override
    @Transactional
    public PUWERResponse addPUWER(Long plantId, PUWERRequest request) {
        PlantItem plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", plantId));

        PUWERInspection puwer = PUWERInspection.builder()
                .plant(plant)
                .inspectionDate(request.getInspectionDate())
                .nextDueDate(request.getNextDueDate())
                .inspector(request.getInspector())
                .inspectorCompany(request.getInspectorCompany())
                .result(request.getResult())
                .reportRef(request.getReportRef())
                .notes(request.getNotes())
                .documentRef(request.getDocumentRef())
                .build();

        puwer = puwerRepository.save(puwer);
        log.info("PUWER inspection {} recorded for plant {}", puwer.getId(), plantId);
        return mapToPUWERResponse(puwer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PUWERResponse> getPUWERHistory(Long plantId) {
        plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", plantId));
        return puwerRepository.findByPlantIdOrderByInspectionDateDesc(plantId).stream()
                .map(this::mapToPUWERResponse)
                .collect(Collectors.toList());
    }

    // Allocations
    @Override
    @Transactional
    public PlantItemResponse addAllocation(Long plantId, PlantAllocationRequest request) {
        PlantItem plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", plantId));

        Operative operative = operativeRepository.findById(request.getOperativeId())
                .orElseThrow(() -> new ResourceNotFoundException("Operative", request.getOperativeId()));

        validateOperativeCscCard(operative, plantId);

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        // Check for overlapping active allocations
        List<PlantAllocation> overlapping = allocationRepository.findByPlantIdAndDateRange(
                plantId, request.getStartDate(),
                request.getEndDate() != null ? request.getEndDate() : request.getStartDate());
        boolean hasOverlap = overlapping.stream()
                .anyMatch(a -> a.getStatus() == AllocationStatus.ACTIVE || a.getStatus() == AllocationStatus.ALLOCATED);
        if (hasOverlap) {
            log.warn("Plant {} already has active allocation during requested period", plantId);
        }

        PlantAllocation allocation = PlantAllocation.builder()
                .plant(plant)
                .operative(operative)
                .site(site)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : AllocationStatus.ALLOCATED)
                .build();

        allocation = allocationRepository.save(allocation);
        log.info("Plant {} allocated to operative {} on site {} from {}",
                plantId, operative.getId(), site.getId(), request.getStartDate());

        return mapToResponse(plant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantItemResponse> getAllocations(Long plantId) {
        plantRepository.findById(plantId)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", plantId));
        return allocationRepository.findByPlantId(plantId).stream()
                .map(a -> PlantItemResponse.builder()
                        .id(plantId)
                        .build())
                .collect(Collectors.toList());
    }

    private void validateOperativeCscCard(Operative operative, Long plantId) {
        List<Card> cards = cardRepository.findByOperativeId(operative.getId());
        boolean hasValidCard = cards.stream()
                .anyMatch(c -> c.getExpiryDate() != null
                        && c.getExpiryDate().isAfter(LocalDate.now())
                        && (c.getCardType() == CardType.CSCS || c.getCardType() == CardType.CPCS));

        if (!hasValidCard) {
            log.warn("Plant allocation blocked: operative {} has no valid CSCS/CPCS card", operative.getId());
            throw new IllegalArgumentException(
                    "Operative " + operative.getId() + " has no valid CSCS or CPCS card. " +
                    "Plant cannot be allocated without valid card verification.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantGanttItem> getPlantGantt(LocalDate from, LocalDate to) {
        List<PlantItem> plants = plantRepository.findAll();

        return plants.stream()
                .map(plant -> {
                    List<PlantAllocation> allocations = allocationRepository.findByPlantIdAndDateRange(
                            plant.getId(), from, to);

                    List<PlantGanttItem.AllocationPeriod> allocationPeriods = allocations.stream()
                            .map(a -> PlantGanttItem.AllocationPeriod.builder()
                                    .operativeId(a.getOperative() != null ? a.getOperative().getId() : null)
                                    .siteId(a.getSite() != null ? a.getSite().getId() : null)
                                    .siteName(a.getSite() != null ? a.getSite().getName() : null)
                                    .startDate(a.getStartDate())
                                    .endDate(a.getEndDate())
                                    .status(a.getStatus() != null ? a.getStatus().name() : null)
                                    .build())
                            .collect(Collectors.toList());

                    return PlantGanttItem.builder()
                            .plantId(plant.getId())
                            .plantRef(plant.getPlantRef())
                            .description(plant.getDescription())
                            .category(plant.getCategory() != null ? plant.getCategory().name() : null)
                            .status(plant.getStatus() != null ? plant.getStatus().name() : null)
                            .allocations(allocationPeriods)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PlantItem plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", id));
        plantRepository.delete(plant);
        log.info("Plant item {} deleted", plant.getPlantRef());
    }

    private PlantItemResponse mapToResponse(PlantItem plant) {
        return PlantItemResponse.builder()
                .id(plant.getId())
                .plantRef(plant.getPlantRef())
                .serialNumber(plant.getSerialNumber())
                .description(plant.getDescription())
                .make(plant.getMake())
                .model(plant.getModel())
                .year(plant.getYear())
                .category(plant.getCategory() != null ? plant.getCategory().name() : null)
                .weight(plant.getWeight())
                .hireStatus(plant.getHireStatus() != null ? plant.getHireStatus().name() : null)
                .supplierId(plant.getSupplier() != null ? plant.getSupplier().getId() : null)
                .supplierName(plant.getSupplier() != null ? plant.getSupplier().getName() : null)
                .telematicsId(plant.getTelematicsId())
                .quickHitchType(plant.getQuickHitchType())
                .status(plant.getStatus() != null ? plant.getStatus().name() : null)
                .dailyHireRate(plant.getDailyHireRate())
                .notes(plant.getNotes())
                .build();
    }

    private LOLERResponse mapToLOLERResponse(LOLERExamination loler) {
        return LOLERResponse.builder()
                .id(loler.getId())
                .plantId(loler.getPlant().getId())
                .examinationDate(loler.getExaminationDate() != null ? loler.getExaminationDate().toString() : null)
                .nextDueDate(loler.getNextDueDate() != null ? loler.getNextDueDate().toString() : null)
                .examiner(loler.getExaminer())
                .examinerCompany(loler.getExaminerCompany())
                .result(loler.getResult() != null ? loler.getResult().name() : null)
                .reportRef(loler.getReportRef())
                .notes(loler.getNotes())
                .documentRef(loler.getDocumentRef())
                .isDue(loler.isDue())
                .isDueSoon(loler.isDueSoon(30))
                .build();
    }

    private PUWERResponse mapToPUWERResponse(PUWERInspection puwer) {
        return PUWERResponse.builder()
                .id(puwer.getId())
                .plantId(puwer.getPlant().getId())
                .inspectionDate(puwer.getInspectionDate() != null ? puwer.getInspectionDate().toString() : null)
                .nextDueDate(puwer.getNextDueDate() != null ? puwer.getNextDueDate().toString() : null)
                .inspector(puwer.getInspector())
                .inspectorCompany(puwer.getInspectorCompany())
                .result(puwer.getResult() != null ? puwer.getResult().name() : null)
                .reportRef(puwer.getReportRef())
                .notes(puwer.getNotes())
                .documentRef(puwer.getDocumentRef())
                .isDue(puwer.isDue())
                .isDueSoon(puwer.isDueSoon(30))
                .build();
    }

    public Object addAllocation(Long plantId, Object request) {
        if (request instanceof PlantAllocationRequest typed) {
            return addAllocation(plantId, typed);
        }
        throw new IllegalArgumentException("Invalid allocation request type");
    }

}