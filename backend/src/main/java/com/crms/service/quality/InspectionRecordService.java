package com.crms.service.quality;

import com.crms.dto.request.quality.InspectionRecordRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.InspectionRecordResponse;

import java.util.Map;

public interface InspectionRecordService {

    PageResponse<InspectionRecordResponse> findAll(Map<String, Object> params);

    InspectionRecordResponse findById(Long id);

    InspectionRecordResponse create(InspectionRecordRequest request);

    InspectionRecordResponse update(Long id, InspectionRecordRequest request);

    void delete(Long id);
}
