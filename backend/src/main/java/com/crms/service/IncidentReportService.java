package com.crms.service;

import com.crms.dto.request.IncidentReportRequest;
import com.crms.dto.response.IncidentReportResponse;
import com.crms.dto.response.PageResponse;

import java.util.Map;

public interface IncidentReportService {

    PageResponse<IncidentReportResponse> findAll(Map<String, Object> params);

    IncidentReportResponse findById(Long id);

    IncidentReportResponse create(IncidentReportRequest request);

    IncidentReportResponse update(Long id, IncidentReportRequest request);

    IncidentReportResponse submit(Long id);

    IncidentReportResponse investigate(Long id, String investigationOutcome);

    IncidentReportResponse close(Long id);

    IncidentReportResponse submitRIDDOR(Long id, String hseRef);

    IncidentReportResponse submitMOR(Long id, String conditions, String restrictions);

    IncidentReportResponse signMOR(Long id, String signedBy);

    IncidentReportResponse verifyMOR(Long id, String verifiedBy);

    PageResponse<IncidentReportResponse> findBySiteId(Long siteId);

    PageResponse<IncidentReportResponse> findRIDDORReportable();
}
