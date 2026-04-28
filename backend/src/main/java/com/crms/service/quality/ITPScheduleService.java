package com.crms.service.quality;

import com.crms.dto.request.quality.ITPScheduleRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.ITPScheduleResponse;

import java.util.Map;

public interface ITPScheduleService {

    PageResponse<ITPScheduleResponse> findAll(Map<String, Object> params);

    ITPScheduleResponse findById(Long id);

    ITPScheduleResponse create(ITPScheduleRequest request);

    ITPScheduleResponse update(Long id, ITPScheduleRequest request);

    void delete(Long id);

    ITPScheduleResponse createFromTemplate(Long templateId, Long contractId);

    ITPScheduleResponse completeItem(Long scheduleId, Long itemId, String completedBy, String result);
}
