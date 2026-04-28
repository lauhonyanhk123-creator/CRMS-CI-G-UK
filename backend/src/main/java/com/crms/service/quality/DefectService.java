package com.crms.service.quality;

import com.crms.dto.request.quality.DefectRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.DefectResponse;

import java.util.Map;

public interface DefectService {

    PageResponse<DefectResponse> findAll(Map<String, Object> params);

    DefectResponse findById(Long id);

    DefectResponse create(DefectRequest request);

    DefectResponse update(Long id, DefectRequest request);

    void delete(Long id);

    DefectResponse updateStatus(Long id, String status);

    DefectResponse assignOperative(Long id, String operative);

    DefectResponse assignContractor(Long id, String contractor);
}
