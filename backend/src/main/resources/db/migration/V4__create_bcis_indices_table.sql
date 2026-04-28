-- V4__create_bcis_indices_table.sql
-- Construction Resource Management System for UK Groundworks
-- Create BCIS indices table before seeding data

-- ============================================
-- BCIS INDICES TABLE
-- ============================================

CREATE TABLE bcis_indices (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    series INTEGER NOT NULL, -- 1=All-in TPI, 3=Materials, 4=Labour, 5=Plant
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    index_value DECIMAL(6,2) NOT NULL,
    all_in_index DECIMAL(6,2), -- CESMM4 Schedule R all-in index
    materials_index DECIMAL(6,2), -- Materials component
    labour_index DECIMAL(6,2), -- Labour component
    plant_index DECIMAL(6,2), -- Plant component
    source VARCHAR(255), -- BCIS publication source
    UNIQUE(series, year, month)
);

-- Indexes for BCIS lookups
CREATE INDEX idx_bcis_series_year ON bcis_indices(series, year DESC, month DESC);
CREATE INDEX idx_bcis_year_month ON bcis_indices(year, month);

COMMENT ON TABLE bcis_indices IS 'BCIS (Building Cost Information Service) quarterly cost indices for CVR calculations';
COMMENT ON COLUMN bcis_indices.series IS 'BCIS series: 1=All-in TPI, 3=Materials, 4=Labour, 5=Plant';
COMMENT ON COLUMN bcis_indices.all_in_index IS 'CESMM4 Schedule R all-in index (2015=100)';
