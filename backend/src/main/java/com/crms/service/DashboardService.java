package com.crms.service;

import com.crms.dto.response.DashboardStats;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardStats getStats();

    Map<String, Object> getDashboardStats();

    Map<String, Object> getKpis();

    List<Map<String, Object>> getActivityFeed(int limit);

    Map<String, Object> getExpiringItems(int days);

    Map<String, Object> getPipelineSummary();

    Map<String, Object> getContractSummary();

    Map<String, Object> getCashflowForecast(int monthsAhead);

    List<Map<String, Object>> getRetentionSchedule();

    Map<String, Object> getHealthSafetyStats(int months);

    Map<String, Object> getPlantUtilisation(int days);

    Map<String, Object> getCisSummary();

    Map<String, Object> getAdoptionStatus();

    Map<String, Object> getProcurementSummary();
}
