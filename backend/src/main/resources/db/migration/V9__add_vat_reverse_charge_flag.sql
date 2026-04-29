-- V9__add_vat_reverse_charge_flag.sql
-- Construction Resource Management System for UK Groundworks
-- Add VAT reverse charge flag to applications_for_payment table

-- ============================================
-- ADD REVERSE CHARGE COLUMN
-- ============================================

ALTER TABLE applications_for_payment
ADD COLUMN reverse_charge BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN applications_for_payment.reverse_charge IS 'VAT reverse charge applies when subcontractor is VAT registered and contract value exceeds threshold (£85,000)';

-- ============================================
-- ADD INDEX FOR QUERY OPTIMIZATION
-- ============================================

CREATE INDEX idx_afp_reverse_charge ON applications_for_payment(reverse_charge) WHERE reverse_charge = TRUE;

-- ============================================
-- UPDATE EXISTING RECORDS
-- ============================================

-- Set reverse_charge to FALSE for existing records by default
-- In production, you may want to run a separate script to calculate correct values
-- based on historical contract/subcontractor data

UPDATE applications_for_payment SET reverse_charge = FALSE WHERE reverse_charge IS NULL;

-- ============================================
-- VERIFY CONSTRAINT
-- ============================================

-- Ensure the NOT NULL constraint is enforced
ALTER TABLE applications_for_payment
ALTER COLUMN reverse_charge SET NOT NULL;
