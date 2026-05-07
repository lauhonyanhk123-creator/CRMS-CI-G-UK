package com.crms.service.quality;

import com.crms.dto.request.quality.ITPTemplateRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.ITPTemplateResponse;

import java.util.List;
import java.util.Map;

public interface ITPTemplateService {

    PageResponse<ITPTemplateResponse> findAll(Map<String, Object> params);

    ITPTemplateResponse findById(Long id);

    ITPTemplateResponse create(ITPTemplateRequest request);

    ITPTemplateResponse update(Long id, ITPTemplateRequest request);

    void delete(Long id);

    ITPTemplateResponse copyTemplate(Long id);

    List<String> getDistinctCategories();
}
