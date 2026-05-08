-- V17: Create tables for entities that were defined in JPA but had no migration
-- Affected: cdm_register, commuted_sums, cost_transactions, journal_entries,
--           snagging_items, wip_reports

-- ─── CDM Register (Health & Safety / CDM 2015) ──────────────────────────────
CREATE TABLE cdm_register (
    id                              BIGSERIAL PRIMARY KEY,
    notification_number             VARCHAR(255) NOT NULL UNIQUE,
    project_name                    VARCHAR(255) NOT NULL,
    project_address                 TEXT,
    project_description             TEXT,
    client_id                       BIGINT NOT NULL REFERENCES companies(id),
    site_id                         BIGINT REFERENCES sites(id),
    principal_designer_name         VARCHAR(255),
    principal_designer_email        VARCHAR(255),
    principal_designer_phone        VARCHAR(100),
    principal_contractor_name       VARCHAR(255),
    principal_contractor_email      VARCHAR(255),
    principal_contractor_phone      VARCHAR(100),
    notification_date               DATE,
    construction_start_date         DATE,
    construction_end_date           DATE,
    is_notifiable                   BOOLEAN NOT NULL DEFAULT FALSE,
    more_than_30_days               BOOLEAN,
    more_than_500_person_days       BOOLEAN,
    hse_notification_ref            VARCHAR(255),
    date_preconstruction_info_shared DATE,
    construction_phase_plan_date    DATE,
    health_safety_file_ref          VARCHAR(255),
    health_safety_file_created_date TIMESTAMP WITH TIME ZONE,
    health_safety_file_completed_date TIMESTAMP WITH TIME ZONE,
    is_active                       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cdm_register_project    ON cdm_register (site_id);
CREATE INDEX idx_cdm_register_client     ON cdm_register (client_id);
CREATE INDEX idx_cdm_notification_number ON cdm_register (notification_number);

-- ─── Commuted Sums (Adoption) ────────────────────────────────────────────────
CREATE TABLE commuted_sums (
    id                 BIGSERIAL PRIMARY KEY,
    adoption_case_id   BIGINT NOT NULL REFERENCES adoption_cases(id),
    commuted_sum_type  VARCHAR(100) NOT NULL,
    total_amount       NUMERIC(12,2) NOT NULL,
    paid_amount        NUMERIC(12,2) NOT NULL DEFAULT 0,
    released_amount    NUMERIC(12,2) NOT NULL DEFAULT 0,
    description        TEXT,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cs_case ON commuted_sums (adoption_case_id);
CREATE INDEX idx_cs_type ON commuted_sums (commuted_sum_type);

-- ─── Snagging Items (Adoption) ───────────────────────────────────────────────
CREATE TABLE snagging_items (
    id                      BIGSERIAL PRIMARY KEY,
    adoption_case_id        BIGINT NOT NULL REFERENCES adoption_cases(id),
    description             VARCHAR(1000) NOT NULL,
    location                VARCHAR(255),
    priority                VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    identified_date         DATE NOT NULL,
    target_completion_date  DATE,
    actual_completion_date  DATE,
    status                  VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    notes                   TEXT,
    assigned_to             VARCHAR(255),
    verified_date           DATE,
    verified_by             VARCHAR(255),
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_snagging_case     ON snagging_items (adoption_case_id);
CREATE INDEX idx_snagging_status   ON snagging_items (status);
CREATE INDEX idx_snagging_priority ON snagging_items (priority);

-- ─── WIP Reports (Financial) ─────────────────────────────────────────────────
CREATE TABLE wip_reports (
    id                BIGSERIAL PRIMARY KEY,
    contract_id       BIGINT NOT NULL REFERENCES contracts(id),
    report_date       DATE NOT NULL,
    period_start      DATE NOT NULL,
    period_end        DATE NOT NULL,
    certified_value   NUMERIC(14,2) NOT NULL DEFAULT 0,
    cost_incurred     NUMERIC(14,2) NOT NULL DEFAULT 0,
    wip_value         NUMERIC(14,2) NOT NULL DEFAULT 0,
    under_recovery    NUMERIC(14,2) NOT NULL DEFAULT 0,
    over_recovery     NUMERIC(14,2) NOT NULL DEFAULT 0,
    status            VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    journal_reference VARCHAR(255),
    notes             TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wip_contract    ON wip_reports (contract_id);
CREATE INDEX idx_wip_report_date ON wip_reports (report_date);
CREATE INDEX idx_wip_status      ON wip_reports (status);

-- ─── Journal Entries (Financial) ─────────────────────────────────────────────
CREATE TABLE journal_entries (
    id                  BIGSERIAL PRIMARY KEY,
    wip_report_id       BIGINT REFERENCES wip_reports(id),
    journal_reference   VARCHAR(255) NOT NULL,
    journal_type        VARCHAR(100) NOT NULL,
    transaction_date    DATE NOT NULL,
    description         VARCHAR(500),
    debit_account_code  VARCHAR(20),
    credit_account_code VARCHAR(20),
    debit_amount        NUMERIC(14,2),
    credit_amount       NUMERIC(14,2),
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_je_journal_ref     ON journal_entries (journal_reference);
CREATE INDEX idx_je_transaction_date ON journal_entries (transaction_date);
CREATE INDEX idx_je_wip_report      ON journal_entries (wip_report_id);

-- ─── Cost Transactions (Financial) ───────────────────────────────────────────
CREATE TABLE cost_transactions (
    id               BIGSERIAL PRIMARY KEY,
    contract_id      BIGINT NOT NULL REFERENCES contracts(id),
    transaction_date DATE NOT NULL,
    description      VARCHAR(500),
    category         VARCHAR(100),
    amount           NUMERIC(14,2) NOT NULL,
    vendor_supplier  VARCHAR(255),
    reference        VARCHAR(255),
    cost_type        VARCHAR(50) NOT NULL DEFAULT 'DIRECT',
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ct_contract         ON cost_transactions (contract_id);
CREATE INDEX idx_ct_transaction_date ON cost_transactions (transaction_date);
CREATE INDEX idx_ct_category         ON cost_transactions (category);
