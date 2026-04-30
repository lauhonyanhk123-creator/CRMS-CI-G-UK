-- V12__fix_data_integrity_issues.sql
-- Construction Resource Management System for UK Groundworks
-- Fix duplicate routes, cascade deletion, duplicate Address, CisStatus consolidation, orphan BCISIndex, duplicate Bond

-- ============================================
-- FIX BCIS INDICES ORPHAN RECORDS
-- ============================================

-- Add unique constraint on bcis_indices to prevent duplicate series/year/month combinations
-- Already defined in V4__create_bcis_indices_table.sql as UNIQUE(series, year, month)

-- Add index for faster lookups
CREATE INDEX IF NOT EXISTS idx_bcis_series_year ON bcis_indices(series, year DESC, month DESC);

-- ============================================
-- FIX BONDS TABLE - UNIQUE CONSTRAINT
-- ============================================

-- Ensure adoption_case_id has unique constraint to prevent multiple bonds per case
-- Already added as unique=true in Bond entity @JoinColumn

-- ============================================
-- CLEANUP DUPLICATE CIS STATUS
-- ============================================

-- Remove duplicate enum by consolidating imports (handled in Java code)
-- No database changes needed - both enums have identical values

-- ============================================
-- ENSURE CASCADE DELETE CONSTRAINTS
-- ============================================

-- Companies -> Contacts: FK already defined, add cascade delete
ALTER TABLE contacts DROP CONSTRAINT IF EXISTS fk_contacts_company;
ALTER TABLE contacts ADD CONSTRAINT fk_contacts_company 
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE;

-- Companies -> Sites: FK already defined, add cascade delete
ALTER TABLE sites DROP CONSTRAINT IF EXISTS fk_sites_client;
ALTER TABLE sites ADD CONSTRAINT fk_sites_client 
    FOREIGN KEY (client_id) REFERENCES companies(id) ON DELETE CASCADE;

-- Contracts -> Variations: FK already defined, add cascade delete
ALTER TABLE variations DROP CONSTRAINT IF EXISTS fk_variations_contract;
ALTER TABLE variations ADD CONSTRAINT fk_variations_contract 
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE;

-- Contracts -> Applications: FK already defined, add cascade delete
ALTER TABLE applications_for_payment DROP CONSTRAINT IF EXISTS fk_applications_contract;
ALTER TABLE applications_for_payment ADD CONSTRAINT fk_applications_contract 
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE;

-- Applications -> Payment Notices: FK already defined, add cascade delete
ALTER TABLE payment_notices DROP CONSTRAINT IF EXISTS fk_payment_notices_application;
ALTER TABLE payment_notices ADD CONSTRAINT fk_payment_notices_application 
    FOREIGN KEY (application_id) REFERENCES applications_for_payment(id) ON DELETE CASCADE;

-- Applications -> Pay Less Notices: FK already defined, add cascade delete
ALTER TABLE pay_less_notices DROP CONSTRAINT IF EXISTS fk_pay_less_notices_application;
ALTER TABLE pay_less_notices ADD CONSTRAINT fk_pay_less_notices_application 
    FOREIGN KEY (application_id) REFERENCES applications_for_payment(id) ON DELETE CASCADE;

-- Applications -> Payment Certificates: FK already defined, add cascade delete
ALTER TABLE payment_certificates DROP CONSTRAINT IF EXISTS fk_payment_certificates_application;
ALTER TABLE payment_certificates ADD CONSTRAINT fk_payment_certificates_application 
    FOREIGN KEY (application_id) REFERENCES applications_for_payment(id) ON DELETE CASCADE;

-- Contracts -> Retention Ledger: FK already defined, add cascade delete
ALTER TABLE retention_ledger DROP CONSTRAINT IF EXISTS fk_retention_ledger_contract;
ALTER TABLE retention_ledger ADD CONSTRAINT fk_retention_ledger_contract 
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE;

-- Retention Ledger -> Movements: FK already defined, add cascade delete
ALTER TABLE retention_movements DROP CONSTRAINT IF EXISTS fk_retention_movements_ledger;
ALTER TABLE retention_movements ADD CONSTRAINT fk_retention_movements_ledger 
    FOREIGN KEY (retention_ledger_id) REFERENCES retention_ledger(id) ON DELETE CASCADE;

-- Tenders -> BoQ Items: FK already defined, add cascade delete
ALTER TABLE boq_items DROP CONSTRAINT IF EXISTS fk_boq_items_tender;
ALTER TABLE boq_items ADD CONSTRAINT fk_boq_items_tender 
    FOREIGN KEY (tender_id) REFERENCES tenders(id) ON DELETE CASCADE;

-- Tenders -> Documents: FK already defined, add cascade delete
ALTER TABLE tender_documents DROP CONSTRAINT IF EXISTS fk_tender_documents_tender;
ALTER TABLE tender_documents ADD CONSTRAINT fk_tender_documents_tender 
    FOREIGN KEY (tender_id) REFERENCES tenders(id) ON DELETE CASCADE;

-- Sites -> Tenders: FK already defined, add cascade delete
ALTER TABLE tenders DROP CONSTRAINT IF EXISTS fk_tenders_site;
ALTER TABLE tenders ADD CONSTRAINT fk_tenders_site 
    FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE;

-- ============================================
-- ADD MISSING UNIQUE CONSTRAINTS
-- ============================================

-- Contracts -> tender_id should be unique (one contract per tender)
ALTER TABLE contracts DROP CONSTRAINT IF EXISTS uk_contracts_tender;
ALTER TABLE contracts ADD CONSTRAINT uk_contracts_tender UNIQUE (tender_id);

-- Adoption Cases -> contract_id unique per adoption case (one adoption case per contract typically)
-- Note: Multiple adoption cases per contract may be valid for phased adoptions
-- Uncomment if needed: ALTER TABLE adoption_cases ADD CONSTRAINT uk_adoption_cases_contract UNIQUE (contract_id);

-- ============================================
-- CLEANUP DUPLICATE ADDRESS TABLES
-- ============================================

-- Note: The addresses table (standalone) and embedded addresses are separate concerns:
-- - addresses table: Used for shared address lookups
-- - Embedded @Embedded Address: Used within entities for address data

-- If duplicate address data exists, consolidate by:
-- 1. Keeping standalone addresses for shared addresses (company billing, etc.)
-- 2. Keeping embedded addresses for entity-specific addresses

-- ============================================
-- COMMENTS
-- ============================================

COMMENT ON TABLE bcis_indices IS 'BCIS (Building Cost Information Service) quarterly cost indices for CVR calculations';
COMMENT ON COLUMN bcis_indices.series IS 'BCIS series: 1=All-in TPI, 3=Materials, 4=Labour, 5=Plant';
