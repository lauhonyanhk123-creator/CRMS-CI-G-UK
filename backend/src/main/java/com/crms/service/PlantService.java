package com.crms.service;

import com.crms.dto.request.PlantItemRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.PlantGanttItem;
import com.crms.dto.response.PlantItemResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PlantService {
    
    PageResponse<PlantItemResponse> findAll(Map<String, Object> params);
    
    PlantItemResponse findById(Long id);
    
    PlantItemResponse create(PlantItemRequest request);
    
    PlantItemResponse update(Long id, PlantItemRequest request);
    
    PlantItemResponse addLOLER(Long id, Object request);
    
    PlantItemResponse addPUWER(Long id, Object request);
    
    PlantItemResponse addAllocation(Long id, Object request);
    
    List<PlantGanttItem> getPlantGantt(LocalDate from, LocalDate to);
}