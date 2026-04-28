package com.crms.service;

import com.crms.dto.request.PermitToDigRequest;
import com.crms.dto.response.PermitToDigResponse;
import com.crms.dto.response.PageResponse;

import java.util.Map;

public interface PermitToDigService {

    PageResponse<PermitToDigResponse> findAll(Map<String, Object> params);

    PermitToDigResponse findById(Long id);

    PermitToDigResponse create(PermitToDigRequest request);

    PermitToDigResponse update(Long id, PermitToDigRequest request);

    PermitToDigResponse submitForPrecheck(Long id);

    PermitToDigResponse precheck(Long id);

    PermitToDigResponse rejectPrecheck(Long id, String reason);

    PermitToDigResponse issue(Long id);

    PermitToDigResponse startWork(Long id);

    PermitToDigResponse complete(Long id);

    PermitToDigResponse cancel(Long id, String reason);

    PermitToDigResponse extend(Long id, java.time.LocalDate newEndDate);

    PermitToDigResponse findActiveBySiteId(Long siteId);
}
