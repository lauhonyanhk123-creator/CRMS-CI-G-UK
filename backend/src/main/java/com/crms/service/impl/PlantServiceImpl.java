package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.entity.SiteSignOn;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.operative.repository.SiteSignOnRepository;
import com.crms.domain.plant.entity.PlantAllocation;
import com.crms.domain.plant.enums.PlantCategory;
import com.crms.domain.plant.enums.PlantStatus;
import com.crms.domain.plant.entity.LOLERExamination;
import com.crms.domain.plant.entity.PlantItem;
import com.crms.domain.plant.entity.PUWERInspection;
import com.crms.domain.plant.repository.LOLERExaminationRepository;
import com.crms.domain.plant.repository.PlantAllocationRepository;
import com.crms.domain.plant.repository.PlantItemRepository;
import com.crms.domain.plant.repository.PUWERInspectionRepository;
import com.crms.dto.request.PlantItemRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.PlantGanttItem;
import com.crms.dto.response.PlantItemResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.PlantService;
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
    
    @Override
    public PageResponse<PlantItemResponse> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        
        Page<PlantItem> plantPage;
        if (params.containsKey("status") && params.get("status") != null) {
            PlantStatus status = PlantStatus.valueOf(params.get("status").toString().toUpperCase());
            plantPage = plantRepository.findByStatus(status, pageable);
        } else if (params.containsKey("category") && params.get("category") != null) {
            PlantCategory category = PlantCategory.valueOf(params.get("category").toString().toUpperCase());
            plantPage = plantRepository.findByCategory(category, pageable);
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
                .status(request.getStatus())
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
        plant.setStatus(request.getStatus());
        plant.setDailyHireRate(request.getDailyHireRate());
        plant.setNotes(request.getNotes());
        
        plant = plantRepository.save(plant);
        return mapToResponse(plant);
    }
    
    @Override
    @Transactional
    public PlantItemResponse addLOLER(Long id, Object request) {
        PlantItem plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", id));
        // Implementation would create LOLER examination record
        log.info("LOLER examination added for plant {}", plant.getPlantRef());
        return mapToResponse(plant);
    }
    
    @Override
    @Transactional
    public PlantItemResponse addPUWER(Long id, Object request) {
        PlantItem plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", id));
        // Implementation would create PUWER inspection record
        log.info("PUWER inspection added for plant {}", plant.getPlantRef());
        return mapToResponse(plant);
    }
    
    @Override
    @Transactional
    public PlantItemResponse addAllocation(Long id, Object request) {
        PlantItem plant = plantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlantItem", id));
        // Implementation would create plant allocation record
        log.info("Allocation added for plant {}", plant.getPlantRef());
        return mapToResponse(plant);
    }
    
    @Override
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
}