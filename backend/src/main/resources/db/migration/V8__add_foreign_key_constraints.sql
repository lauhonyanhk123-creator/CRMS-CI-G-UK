-- V8__add_foreign_key_constraints.sql
-- Construction Resource Management System for UK Groundworks
-- Add missing database-level foreign key constraints for referential integrity

-- ============================================
-- ADD MISSING FOREIGN KEYS
-- ============================================

-- BoQ Standard Items: category foreign key is already defined, ensure constraint exists
-- Note: Added in V6__add_boq_categories.sql

-- BoQ Categories: self-referencing parent_id constraint
ALTER TABLE boq_categories 
ADD CONSTRAINT fk_boq_categories_parent 
FOREIGN KEY (parent_id) REFERENCES boq_categories(id) ON DELETE SET NULL;

-- BoQ Categories: created_by and updated_by constraints
ALTER TABLE boq_categories 
ADD CONSTRAINT fk_boq_categories_created_by 
FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE boq_categories 
ADD CONSTRAINT fk_boq_categories_updated_by 
FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- BoQ Standard Items: created_by and updated_by constraints
ALTER TABLE boq_standard_items 
ADD CONSTRAINT fk_boq_standard_items_created_by 
FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE boq_standard_items 
ADD CONSTRAINT fk_boq_standard_items_updated_by 
FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- ============================================
-- ADD MISSING INDEXES FOR FK LOOKUPS
-- ============================================

-- BoQ categories parent lookup
CREATE INDEX idx_boq_categories_created_by ON boq_categories(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX idx_boq_categories_updated_by ON boq_categories(updated_by) WHERE updated_by IS NOT NULL;

-- BoQ standard items created_by/updated_by lookups
CREATE INDEX idx_boq_standard_items_created_by ON boq_standard_items(created_by) WHERE created_by IS NOT NULL;
CREATE INDEX idx_boq_standard_items_updated_by ON boq_standard_items(updated_by) WHERE updated_by IS NOT NULL;

-- ============================================
-- ADD SEQUENCES FOR REFERENCE NUMBERS
-- ============================================

-- Create sequence for BoQ standard items if not using UUID
-- (already using UUID as PK in V6)

-- ============================================
-- ADD DEFAULT VALUES FOR NEW COLUMNS
-- ============================================

-- Ensure default values for boq_categories
ALTER TABLE boq_categories 
ALTER COLUMN is_active SET DEFAULT TRUE;

ALTER TABLE boq_categories 
ALTER COLUMN sort_order SET DEFAULT 0;

-- Ensure default values for boq_standard_items  
ALTER TABLE boq_standard_items 
ALTER COLUMN is_active SET DEFAULT TRUE;

-- ============================================
-- ADD CHECK CONSTRAINTS
-- ============================================

-- BoQ categories: ensure sort_order is non-negative
ALTER TABLE boq_categories 
ADD CONSTRAINT chk_boq_categories_sort_order 
CHECK (sort_order >= 0);

-- BoQ standard items: ensure costs are non-negative
ALTER TABLE boq_standard_items 
ADD CONSTRAINT chk_boq_standard_items_material_cost 
CHECK (material_cost >= 0 OR material_cost IS NULL);

ALTER TABLE boq_standard_items 
ADD CONSTRAINT chk_boq_standard_items_labour_cost 
CHECK (labour_cost >= 0 OR labour_cost IS NULL);

ALTER TABLE boq_standard_items 
ADD CONSTRAINT chk_boq_standard_items_plant_cost 
CHECK (plant_cost >= 0 OR plant_cost IS NULL);

ALTER TABLE boq_standard_items 
ADD CONSTRAINT chk_boq_standard_items_total_cost 
CHECK (total_cost >= 0 OR total_cost IS NULL);

ALTER TABLE boq_standard_items 
ADD CONSTRAINT chk_boq_standard_items_composite_rate 
CHECK (composite_rate >= 0 OR composite_rate IS NULL);

-- ============================================
-- ADD COMMENTS FOR DOCUMENTATION
-- ============================================

COMMENT ON TABLE boq_categories IS 'Bill of Quantities categories for organising standard items by work type';
COMMENT ON TABLE boq_standard_items IS 'Library of standard BoQ items with default costs for UK groundwork contractors';
COMMENT ON COLUMN boq_categories.measurement_standard IS 'Measurement standard: CESMM4 (Civil Engineering), NRM2 (New Rules of Measurement), SMM7';
COMMENT ON COLUMN boq_categories.cesmm4_class IS 'CESMM4 class letter for cross-referencing';
COMMENT ON COLUMN boq_standard_items.work_category IS 'Trade category: Excavation, Drainage, Concrete, etc.';
COMMENT ON COLUMN boq_standard_items.nrm2_code IS 'NRM2 work section code for SMM7 style measurement';
COMMENT ON COLUMN boq_standard_items.composite_rate IS 'All-in rate combining material, labour and plant if applicable';
