package com.crms.integration.companieshouse;

import com.crms.integration.dto.CompanySearchResult;
import com.crms.integration.dto.CompanySearchResult.ChargeDto;
import com.crms.integration.dto.CompanySearchResult.OfficerDto;

import java.util.List;

/**
 * Companies House API Service interface.
 * Provides methods for searching companies and retrieving company data.
 */
public interface CompaniesHouseService {

    /**
     * Search for companies by name or company number.
     *
     * @param query Search query (company name or number)
     * @return List of matching companies
     */
    List<CompanySearchResult> searchCompanies(String query);

    /**
     * Get detailed company profile by company number.
     *
     * @param companyNumber The Companies House company number
     * @return Company profile details
     */
    CompanySearchResult getCompanyProfile(String companyNumber);

    /**
     * Get company officers (directors, secretaries, etc).
     *
     * @param companyNumber The Companies House company number
     * @return List of officers
     */
    List<OfficerDto> getCompanyOfficers(String companyNumber);

    /**
     * Get company charges (mortgages and debentures).
     *
     * @param companyNumber The Companies House company number
     * @return List of charges
     */
    List<ChargeDto> getCompanyCharges(String companyNumber);

    /**
     * Check company insolvency status.
     *
     * @param companyNumber The Companies House company number
     * @return Insolvency data if any
     */
    CompanySearchResult getCompanyInsolvency(String companyNumber);

    /**
     * Check if demo mode is active.
     *
     * @return true if using mock data
     */
    boolean isDemoMode();
}
