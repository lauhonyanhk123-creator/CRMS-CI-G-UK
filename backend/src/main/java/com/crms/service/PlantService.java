package com.crms.service;

import com.crms.dto.request.LOLERRequest;
import com.crms.dto.request.PlantAllocationRequest;
import com.crms.dto.request.PUWERRequest;
import com.crms.dto.request.PlantItemRequest;
import com.crms.dto.response.LOLERResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.PlantGanttItem;
import com.crms.dto.response.PlantItemResponse;
import com.crms.dto.response.PUWERResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PlantService {

    PageResponse<PlantItemResponse> findAll(Map<String, Object> params);

    PlantItemResponse findById(Long id);

    PlantItemResponse create(PlantItemRequest request);

    PlantItemResponse update(Long id, PlantItemRequest request);

    // Safety inspections
    LOLERResponse addLOLER(Long id, LOLERRequest request);

    List<LOLERResponse> getLOLERHistory(Long plantId);

    PUWERResponse addPUWER(Long id, PUWERRequest request);

    List<PUWERResponse> getPUWERHistory(Long plantId);

    // Allocations
    PlantItemResponse addAllocation(Long plantId, PlantAllocationRequest request);

    List<PlantItemResponse> getAllocations(Long plantId);

    // Gantt
    List<PlantGanttItem> getPlantGantt(LocalDate from, LocalDate to);

    void delete(Long id);
}