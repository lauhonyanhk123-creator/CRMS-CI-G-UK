-- V19: Deferred performance indexes for tables created in V14 and V17.
-- V11 could not reference these tables because they did not yet exist.

-- ─── V14 tables ─────────────────────────────────────────────────────────────

-- ITP Schedules
CREATE INDEX IF NOT EXISTS idx_itp_schedules_contract_status ON itp_schedules(contract_id, status);
CREATE INDEX IF NOT EXISTS idx_itp_schedules_due_date ON itp_schedules(due_date);

-- Inspection Records (schedule_item_id is the correct FK column)
CREATE INDEX IF NOT EXISTS idx_inspection_records_schedule_item ON inspection_records(schedule_item_id);
CREATE INDEX IF NOT EXISTS idx_inspection_records_result ON inspection_records(result);

-- Defects
CREATE INDEX IF NOT EXISTS idx_defects_contract_status ON defects(contract_id, status);
CREATE INDEX IF NOT EXISTS idx_defects_priority_status ON defects(priority, status);

-- Sign-offs (contract_id is the FK in V14, not itp_schedule_id)
CREATE INDEX IF NOT EXISTS idx_sign_offs_contract_status ON sign_offs(contract_id, result);

-- ─── V17 tables ─────────────────────────────────────────────────────────────

-- Commuted Sums (no status column in this table)
CREATE INDEX IF NOT EXISTS idx_commuted_sums_case ON commuted_sums(adoption_case_id);
CREATE INDEX IF NOT EXISTS idx_commuted_sums_type ON commuted_sums(commuted_sum_type);

-- Snagging Items
CREATE INDEX IF NOT EXISTS idx_snagging_items_case_status ON snagging_items(adoption_case_id, status);
CREATE INDEX IF NOT EXISTS idx_snagging_items_priority_status ON snagging_items(priority, status);

-- WIP Reports
CREATE INDEX IF NOT EXISTS idx_wip_reports_contract_date ON wip_reports(contract_id, report_date);
CREATE INDEX IF NOT EXISTS idx_wip_reports_status ON wip_reports(status);

-- Cost Transactions
CREATE INDEX IF NOT EXISTS idx_cost_transactions_contract_date ON cost_transactions(contract_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_cost_transactions_category ON cost_transactions(category);

-- CDM Register (no contract_id or status column — uses site_id and client_id)
CREATE INDEX IF NOT EXISTS idx_cdm_register_site ON cdm_register(site_id);
CREATE INDEX IF NOT EXISTS idx_cdm_register_client ON cdm_register(client_id);
CREATE INDEX IF NOT EXISTS idx_cdm_register_active ON cdm_register(is_active);
