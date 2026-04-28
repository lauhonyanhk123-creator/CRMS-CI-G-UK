package com.crms.service;

import com.crms.dto.request.WipReportRequest;
import com.crms.dto.response.WipReportResponse;
import com.crms.dto.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface WipJournalService {

    WipReportResponse generateWipReport(Long contractId, LocalDate reportDate);

    List<WipReportResponse> generateAllWipReports(LocalDate reportDate);

    WipReportResponse getWipReportById(Long reportId);

    PageResponse<WipReportResponse> getWipReportHistory(Long contractId, int page, int size);

    List<WipReportResponse> getWipReportsByDate(LocalDate reportDate);

    WipReportResponse postToJournal(Long reportId);

    WipReportResponse reverseJournal(Long reportId);

    void deleteDraftReport(Long reportId);
}
