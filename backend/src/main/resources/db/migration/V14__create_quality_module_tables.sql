-- V14: Quality / ITP Module
-- Creates tables for ITP templates, schedules, inspection records, defects, and sign-offs.

-- ============================================================
-- ITP Templates
-- ============================================================
CREATE TABLE itp_templates (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255)    NOT NULL,
    description        TEXT,
    category           VARCHAR(255)    NOT NULL,
    trade_category     VARCHAR(255),
    version            INTEGER         NOT NULL DEFAULT 1,
    status             VARCHAR(50)     NOT NULL DEFAULT 'DRAFT',
    created_at         TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP,
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255),
    lock_version       BIGINT          DEFAULT 0
);

CREATE INDEX idx_itp_template_name     ON itp_templates (name);
CREATE INDEX idx_itp_template_category ON itp_templates (category);
CREATE INDEX idx_itp_template_status   ON itp_templates (status);

-- ============================================================
-- ITP Template Items
-- ============================================================
CREATE TABLE itp_template_items (
    id                 BIGSERIAL PRIMARY KEY,
    template_id        BIGINT          NOT NULL REFERENCES itp_templates(id) ON DELETE CASCADE,
    sequence           INTEGER         NOT NULL,
    description        TEXT            NOT NULL,
    inspection_type    VARCHAR(50)     NOT NULL,
    responsible_party  VARCHAR(255)    NOT NULL,
    notes              TEXT,
    frequency          VARCHAR(255),
    required_evidence  VARCHAR(255)
);

CREATE INDEX idx_itp_template_item_template ON itp_template_items (template_id);

-- ============================================================
-- ITP Schedules
-- ============================================================
CREATE TABLE itp_schedules (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(255)    NOT NULL,
    contract_id         UUID            NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    template_id         BIGINT          REFERENCES itp_templates(id) ON DELETE SET NULL,
    start_date          DATE,
    due_date            DATE,
    completed_date      DATE,
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    assigned_inspector  VARCHAR(255),
    sign_off_by         VARCHAR(255),
    sign_off_date       DATE,
    sign_off_signature  TEXT,
    notes               TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    lock_version        BIGINT          DEFAULT 0
);

CREATE INDEX idx_itp_schedule_contract  ON itp_schedules (contract_id);
CREATE INDEX idx_itp_schedule_status    ON itp_schedules (status);
CREATE INDEX idx_itp_schedule_due_date  ON itp_schedules (due_date);

-- ============================================================
-- ITP Schedule Items
-- ============================================================
CREATE TABLE itp_schedule_items (
    id                 BIGSERIAL PRIMARY KEY,
    schedule_id        BIGINT          NOT NULL REFERENCES itp_schedules(id) ON DELETE CASCADE,
    sequence           INTEGER         NOT NULL,
    description        TEXT            NOT NULL,
    inspection_type    VARCHAR(50)     NOT NULL,
    responsible_party  VARCHAR(255)    NOT NULL,
    due_date           DATE,
    frequency          VARCHAR(255),
    required_evidence  VARCHAR(255),
    status             VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    completed_date     DATE,
    completed_by       VARCHAR(255),
    result             TEXT,
    notes              TEXT
);

CREATE INDEX idx_itp_schedule_item_schedule ON itp_schedule_items (schedule_id);

-- ============================================================
-- Inspection Records
-- ============================================================
CREATE TABLE inspection_records (
    id                   BIGSERIAL PRIMARY KEY,
    schedule_item_id     BIGINT          NOT NULL REFERENCES itp_schedule_items(id) ON DELETE CASCADE,
    title                VARCHAR(255)    NOT NULL,
    inspector_name       VARCHAR(255)    NOT NULL,
    inspector_signature  TEXT,
    inspection_date      DATE            NOT NULL,
    inspection_time      TIMESTAMP,
    result               VARCHAR(50)     NOT NULL,
    notes                TEXT,
    findings             TEXT,
    non_conformance_ref  VARCHAR(255),
    created_at           TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP,
    created_by           VARCHAR(255),
    updated_by           VARCHAR(255),
    lock_version         BIGINT          DEFAULT 0
);

CREATE INDEX idx_inspection_schedule_item ON inspection_records (schedule_item_id);
CREATE INDEX idx_inspection_result        ON inspection_records (result);
CREATE INDEX idx_inspection_date          ON inspection_records (inspection_date);

-- ============================================================
-- Inspection Attachments
-- ============================================================
CREATE TABLE inspection_attachments (
    id                    BIGSERIAL PRIMARY KEY,
    inspection_record_id  BIGINT          NOT NULL REFERENCES inspection_records(id) ON DELETE CASCADE,
    filename              VARCHAR(255)    NOT NULL,
    file_type             VARCHAR(100)    NOT NULL,
    file_path             VARCHAR(1000)   NOT NULL,
    file_size             BIGINT,
    description           TEXT,
    uploaded_by           VARCHAR(255)
);

CREATE INDEX idx_inspection_attachment_record ON inspection_attachments (inspection_record_id);

-- ============================================================
-- Defects
-- ============================================================
CREATE TABLE defects (
    id                   BIGSERIAL PRIMARY KEY,
    title                VARCHAR(255)    NOT NULL,
    contract_id          UUID            NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    description          TEXT            NOT NULL,
    location             VARCHAR(500)    NOT NULL,
    priority             VARCHAR(50)     NOT NULL DEFAULT 'MEDIUM',
    status               VARCHAR(50)     NOT NULL DEFAULT 'OPEN',
    identified_date      DATE,
    due_date             DATE,
    resolved_date        DATE,
    assigned_operative   VARCHAR(255),
    assigned_contractor  VARCHAR(255),
    notes                TEXT,
    root_cause           TEXT,
    resolution_details   TEXT,
    reinspection_required BOOLEAN        NOT NULL DEFAULT FALSE,
    reinspection_date    DATE,
    nc_reference         VARCHAR(255),
    created_at           TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP,
    created_by           VARCHAR(255),
    updated_by           VARCHAR(255),
    lock_version         BIGINT          DEFAULT 0
);

CREATE INDEX idx_defect_contract  ON defects (contract_id);
CREATE INDEX idx_defect_status    ON defects (status);
CREATE INDEX idx_defect_priority  ON defects (priority);
CREATE INDEX idx_defect_location  ON defects (location);

-- ============================================================
-- Defect Photos
-- ============================================================
CREATE TABLE defect_photos (
    id           BIGSERIAL PRIMARY KEY,
    defect_id    BIGINT          NOT NULL REFERENCES defects(id) ON DELETE CASCADE,
    filename     VARCHAR(255)    NOT NULL,
    file_path    VARCHAR(1000)   NOT NULL,
    file_size    BIGINT,
    description  TEXT,
    uploaded_by  VARCHAR(255),
    taken_date   VARCHAR(100)
);

CREATE INDEX idx_defect_photo_defect ON defect_photos (defect_id);

-- ============================================================
-- Sign-offs (NHBC / LABC / Local Authority)
-- ============================================================
CREATE TABLE sign_offs (
    id                    BIGSERIAL PRIMARY KEY,
    contract_id           UUID            NOT NULL REFERENCES contracts(id) ON DELETE CASCADE,
    building_control_type VARCHAR(50)     NOT NULL,
    inspection_type       VARCHAR(255)    NOT NULL,
    reference_number      VARCHAR(255),
    inspector_name        VARCHAR(255),
    inspector_email       VARCHAR(255),
    inspector_phone       VARCHAR(100),
    inspection_date       DATE            NOT NULL,
    next_inspection_date  DATE,
    result                VARCHAR(50)     NOT NULL,
    conditions_or_notes   TEXT,
    report_path           VARCHAR(1000),
    report_number         VARCHAR(255),
    sign_off_signature    TEXT,
    sign_off_date         DATE,
    notes                 TEXT,
    created_at            TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(255),
    updated_by            VARCHAR(255),
    lock_version          BIGINT          DEFAULT 0
);

CREATE INDEX idx_signoff_contract  ON sign_offs (contract_id);
CREATE INDEX idx_signoff_type      ON sign_offs (building_control_type);
CREATE INDEX idx_signoff_result    ON sign_offs (result);
CREATE INDEX idx_signoff_date      ON sign_offs (inspection_date);
