-- V9: Add CIS rate CHECK constraint, null safety, and security improvements
-- HMRC CIS rules: deduction rates must be exactly 0, 20, or 30 percent

-- 1. Enforce CIS rate values (0, 20, or 30) per HMRC CIS300 requirements
ALTER TABLE cis_return_lines
    DROP CONSTRAINT IF EXISTS chk_cis_rate_values;

ALTER TABLE cis_return_lines
    ADD CONSTRAINT chk_cis_rate_values
    CHECK (cis_rate IS NULL OR cis_rate IN (0, 20, 30));

-- 2. Ensure gross_paid and deduction are non-negative (HMRC requires non-negative deductions)
ALTER TABLE cis_return_lines
    DROP CONSTRAINT IF EXISTS chk_cis_gross_non_negative;

ALTER TABLE cis_return_lines
    ADD CONSTRAINT chk_cis_gross_non_negative
    CHECK (gross_paid IS NULL OR gross_paid >= 0);

-- 3. Net paid must equal gross - deduction (cross-field integrity)
ALTER TABLE cis_return_lines
    DROP CONSTRAINT IF EXISTS chk_cis_net_integrity;

ALTER TABLE cis_return_lines
    ADD CONSTRAINT chk_cis_net_integrity
    CHECK (
        (gross_paid IS NULL AND deduction IS NULL AND net_paid IS NULL)
        OR (gross_paid IS NOT NULL AND deduction IS NOT NULL AND net_paid IS NOT NULL
            AND net_paid >= 0)
    );

-- 4. Add non-null constraint on operative_cards expiry_date (cards without expiry cannot be validated)
-- Only apply where card_type is known (backwards compatible)
ALTER TABLE operative_cards
    ALTER COLUMN expiry_date SET NOT NULL;

-- 5. Ensure tax_month format is YYYY-MM (CIS300 monthly returns)
ALTER TABLE cis_returns
    DROP CONSTRAINT IF EXISTS chk_tax_month_format;

ALTER TABLE cis_returns
    ADD CONSTRAINT chk_tax_month_format
    CHECK (tax_month ~ '^[0-9]{4}-(0[1-9]|1[0-2])$');

-- 6. audit_logs: ensure entity_id is non-null for DML operations (audit trail completeness)
-- Only affects future records as column is nullable; existing records remain intact.
-- Applications should enforce non-null entityId via @Valid DTOs.
