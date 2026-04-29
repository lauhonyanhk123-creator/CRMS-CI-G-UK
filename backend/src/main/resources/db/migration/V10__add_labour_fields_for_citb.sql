-- Add labour_value column to contracts table for CITB Levy calculation fallback
ALTER TABLE contracts ADD COLUMN labour_value DECIMAL(14, 2);

-- Add hourly_rate column to operatives table for wage calculation
ALTER TABLE operatives ADD COLUMN hourly_rate DECIMAL(8, 2) DEFAULT 0.00;

COMMENT ON COLUMN contracts.labour_value IS 'Contract estimated labour value for CITB Levy fallback calculation';
COMMENT ON COLUMN operatives.hourly_rate IS 'Operative hourly rate for timesheet wage calculation';
