package com.crms.service;

import com.crms.dto.response.CVRItem;

import java.util.List;
import java.util.Map;

public interface ReportService {
    
    List<CVRItem> getCVR(Long contractId, String period);
    
    Object getCashflow(String from, String to);
    
    Object getRetention();
    
    Object getCISSummary(String taxMonth);
    
    Object getPlantUtilization(Map<String, Object> params);
    
    Object getTenderPipeline();
}