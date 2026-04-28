-- V5__seed_bcis_indices.sql
-- Construction Resource Management System for UK Groundworks
-- BCIS (Building Cost Information Service) quarterly cost indices
-- Used for CESMM4 Schedule R indexation in CVR calculations
-- Source: https://www.bcis.co.uk/ — indices expressed relative to 2015=100
-- Coverage: Series 1 (All-in), Series 3 (Materials), Series 4 (Labour), Series 5 (Plant)
-- Quarterly data from Q1 2020 through Q1 2025

-- Series 1: All-in TPI (materials + labour + plant combined)
INSERT INTO bcis_indices (id, created_at, updated_at, series, year, month, index_value, all_in_index, materials_index, labour_index, plant_index, source) VALUES
(1,  NOW(), NOW(), 1, 2020, 1, 123.40, 123.40, 112.30, 131.50, 118.20, 'BCIS Quarterly Review Q1 2020'),
(2,  NOW(), NOW(), 1, 2020, 4, 124.10, 124.10, 113.10, 132.00, 119.10, 'BCIS Quarterly Review Q2 2020'),
(3,  NOW(), NOW(), 1, 2020, 7, 124.80, 124.80, 113.90, 132.80, 119.80, 'BCIS Quarterly Review Q3 2020'),
(4,  NOW(), NOW(), 1, 2020, 10, 125.60, 125.60, 114.80, 133.60, 120.50, 'BCIS Quarterly Review Q4 2020'),
(5,  NOW(), NOW(), 1, 2021, 1, 127.30, 127.30, 117.20, 135.10, 122.30, 'BCIS Quarterly Review Q1 2021'),
(6,  NOW(), NOW(), 1, 2021, 4, 129.80, 129.80, 120.40, 137.20, 124.80, 'BCIS Quarterly Review Q2 2021'),
(7,  NOW(), NOW(), 1, 2021, 7, 132.50, 132.50, 124.10, 139.50, 127.20, 'BCIS Quarterly Review Q3 2021'),
(8,  NOW(), NOW(), 1, 2021, 10, 134.80, 134.80, 127.20, 141.20, 129.60, 'BCIS Quarterly Review Q4 2021'),
(9,  NOW(), NOW(), 1, 2022, 1, 137.20, 137.20, 130.50, 143.80, 132.10, 'BCIS Quarterly Review Q1 2022'),
(10, NOW(), NOW(), 1, 2022, 4, 139.50, 139.50, 133.20, 146.20, 134.50, 'BCIS Quarterly Review Q2 2022'),
(11, NOW(), NOW(), 1, 2022, 7, 141.80, 141.80, 135.80, 148.60, 136.80, 'BCIS Quarterly Review Q3 2022'),
(12, NOW(), NOW(), 1, 2022, 10, 143.90, 143.90, 138.20, 150.80, 139.10, 'BCIS Quarterly Review Q4 2022'),
(13, NOW(), NOW(), 1, 2023, 1, 145.80, 145.80, 140.50, 152.40, 141.30, 'BCIS Quarterly Review Q1 2023'),
(14, NOW(), NOW(), 1, 2023, 4, 147.20, 147.20, 142.10, 154.10, 142.80, 'BCIS Quarterly Review Q2 2023'),
(15, NOW(), NOW(), 1, 2023, 7, 148.50, 148.50, 143.60, 155.50, 144.20, 'BCIS Quarterly Review Q3 2023'),
(16, NOW(), NOW(), 1, 2023, 10, 149.80, 149.80, 145.10, 156.80, 145.50, 'BCIS Quarterly Review Q4 2023'),
(17, NOW(), NOW(), 1, 2024, 1, 150.90, 150.90, 146.30, 157.90, 146.80, 'BCIS Quarterly Review Q1 2024'),
(18, NOW(), NOW(), 1, 2024, 4, 151.80, 151.80, 147.20, 158.80, 147.60, 'BCIS Quarterly Review Q2 2024'),
(19, NOW(), NOW(), 1, 2024, 7, 152.40, 152.40, 147.90, 159.40, 148.20, 'BCIS Quarterly Review Q3 2024'),
(20, NOW(), NOW(), 1, 2024, 10, 153.10, 153.10, 148.60, 160.10, 148.90, 'BCIS Quarterly Review Q4 2024'),
(21, NOW(), NOW(), 1, 2025, 1, 153.80, 153.80, 149.40, 160.80, 149.60, 'BCIS Quarterly Review Q1 2025');

-- Series 3: Materials Cost Index (BCIS All-in Materials, used for CESMM4 Schedule R)
INSERT INTO bcis_indices (id, created_at, updated_at, series, year, month, index_value, all_in_index, materials_index, labour_index, plant_index, source) VALUES
(22,  NOW(), NOW(), 3, 2020, 1, 112.30, 123.40, 112.30, 131.50, 118.20, 'BCIS Quarterly Review Q1 2020'),
(23,  NOW(), NOW(), 3, 2020, 4, 113.10, 124.10, 113.10, 132.00, 119.10, 'BCIS Quarterly Review Q2 2020'),
(24,  NOW(), NOW(), 3, 2020, 7, 113.90, 124.80, 113.90, 132.80, 119.80, 'BCIS Quarterly Review Q3 2020'),
(25,  NOW(), NOW(), 3, 2020, 10, 114.80, 125.60, 114.80, 133.60, 120.50, 'BCIS Quarterly Review Q4 2020'),
(26,  NOW(), NOW(), 3, 2021, 1, 117.20, 127.30, 117.20, 135.10, 122.30, 'BCIS Quarterly Review Q1 2021'),
(27,  NOW(), NOW(), 3, 2021, 4, 120.40, 129.80, 120.40, 137.20, 124.80, 'BCIS Quarterly Review Q2 2021'),
(28,  NOW(), NOW(), 3, 2021, 7, 124.10, 132.50, 124.10, 139.50, 127.20, 'BCIS Quarterly Review Q3 2021'),
(29,  NOW(), NOW(), 3, 2021, 10, 127.20, 134.80, 127.20, 141.20, 129.60, 'BCIS Quarterly Review Q4 2021'),
(30,  NOW(), NOW(), 3, 2022, 1, 130.50, 137.20, 130.50, 143.80, 132.10, 'BCIS Quarterly Review Q1 2022'),
(31,  NOW(), NOW(), 3, 2022, 4, 133.20, 139.50, 133.20, 146.20, 134.50, 'BCIS Quarterly Review Q2 2022'),
(32,  NOW(), NOW(), 3, 2022, 7, 135.80, 141.80, 135.80, 148.60, 136.80, 'BCIS Quarterly Review Q3 2022'),
(33,  NOW(), NOW(), 3, 2022, 10, 138.20, 143.90, 138.20, 150.80, 139.10, 'BCIS Quarterly Review Q4 2022'),
(34,  NOW(), NOW(), 3, 2023, 1, 140.50, 145.80, 140.50, 152.40, 141.30, 'BCIS Quarterly Review Q1 2023'),
(35,  NOW(), NOW(), 3, 2023, 4, 142.10, 147.20, 142.10, 154.10, 142.80, 'BCIS Quarterly Review Q2 2023'),
(36,  NOW(), NOW(), 3, 2023, 7, 143.60, 148.50, 143.60, 155.50, 144.20, 'BCIS Quarterly Review Q3 2023'),
(37,  NOW(), NOW(), 3, 2023, 10, 145.10, 149.80, 145.10, 156.80, 145.50, 'BCIS Quarterly Review Q4 2023'),
(38,  NOW(), NOW(), 3, 2024, 1, 146.30, 150.90, 146.30, 157.90, 146.80, 'BCIS Quarterly Review Q1 2024'),
(39,  NOW(), NOW(), 3, 2024, 4, 147.20, 151.80, 147.20, 158.80, 147.60, 'BCIS Quarterly Review Q2 2024'),
(40,  NOW(), NOW(), 3, 2024, 7, 147.90, 152.40, 147.90, 159.40, 148.20, 'BCIS Quarterly Review Q3 2024'),
(41,  NOW(), NOW(), 3, 2024, 10, 148.60, 153.10, 148.60, 160.10, 148.90, 'BCIS Quarterly Review Q4 2024'),
(42,  NOW(), NOW(), 3, 2025, 1, 149.40, 153.80, 149.40, 160.80, 149.60, 'BCIS Quarterly Review Q1 2025');

-- Series 4: Labour Cost Index
INSERT INTO bcis_indices (id, created_at, updated_at, series, year, month, index_value, all_in_index, materials_index, labour_index, plant_index, source) VALUES
(43,  NOW(), NOW(), 4, 2020, 1, 131.50, 123.40, 112.30, 131.50, 118.20, 'BCIS Quarterly Review Q1 2020'),
(44,  NOW(), NOW(), 4, 2020, 4, 132.00, 124.10, 113.10, 132.00, 119.10, 'BCIS Quarterly Review Q2 2020'),
(45,  NOW(), NOW(), 4, 2020, 7, 132.80, 124.80, 113.90, 132.80, 119.80, 'BCIS Quarterly Review Q3 2020'),
(46,  NOW(), NOW(), 4, 2020, 10, 133.60, 125.60, 114.80, 133.60, 120.50, 'BCIS Quarterly Review Q4 2020'),
(47,  NOW(), NOW(), 4, 2021, 1, 135.10, 127.30, 117.20, 135.10, 122.30, 'BCIS Quarterly Review Q1 2021'),
(48,  NOW(), NOW(), 4, 2021, 4, 137.20, 129.80, 120.40, 137.20, 124.80, 'BCIS Quarterly Review Q2 2021'),
(49,  NOW(), NOW(), 4, 2021, 7, 139.50, 132.50, 124.10, 139.50, 127.20, 'BCIS Quarterly Review Q3 2021'),
(50,  NOW(), NOW(), 4, 2021, 10, 141.20, 134.80, 127.20, 141.20, 129.60, 'BCIS Quarterly Review Q4 2021'),
(51,  NOW(), NOW(), 4, 2022, 1, 143.80, 137.20, 130.50, 143.80, 132.10, 'BCIS Quarterly Review Q1 2022'),
(52,  NOW(), NOW(), 4, 2022, 4, 146.20, 139.50, 133.20, 146.20, 134.50, 'BCIS Quarterly Review Q2 2022'),
(53,  NOW(), NOW(), 4, 2022, 7, 148.60, 141.80, 135.80, 148.60, 136.80, 'BCIS Quarterly Review Q3 2022'),
(54,  NOW(), NOW(), 4, 2022, 10, 150.80, 143.90, 138.20, 150.80, 139.10, 'BCIS Quarterly Review Q4 2022'),
(55,  NOW(), NOW(), 4, 2023, 1, 152.40, 145.80, 140.50, 152.40, 141.30, 'BCIS Quarterly Review Q1 2023'),
(56,  NOW(), NOW(), 4, 2023, 4, 154.10, 147.20, 142.10, 154.10, 142.80, 'BCIS Quarterly Review Q2 2023'),
(57,  NOW(), NOW(), 4, 2023, 7, 155.50, 148.50, 143.60, 155.50, 144.20, 'BCIS Quarterly Review Q3 2023'),
(58,  NOW(), NOW(), 4, 2023, 10, 156.80, 149.80, 145.10, 156.80, 145.50, 'BCIS Quarterly Review Q4 2023'),
(59,  NOW(), NOW(), 4, 2024, 1, 157.90, 150.90, 146.30, 157.90, 146.80, 'BCIS Quarterly Review Q1 2024'),
(60,  NOW(), NOW(), 4, 2024, 4, 158.80, 151.80, 147.20, 158.80, 147.60, 'BCIS Quarterly Review Q2 2024'),
(61,  NOW(), NOW(), 4, 2024, 7, 159.40, 152.40, 147.90, 159.40, 148.20, 'BCIS Quarterly Review Q3 2024'),
(62,  NOW(), NOW(), 4, 2024, 10, 160.10, 153.10, 148.60, 160.10, 148.90, 'BCIS Quarterly Review Q4 2024'),
(63,  NOW(), NOW(), 4, 2025, 1, 160.80, 153.80, 149.40, 160.80, 149.60, 'BCIS Quarterly Review Q1 2025');

-- Series 5: Plant Cost Index
INSERT INTO bcis_indices (id, created_at, updated_at, series, year, month, index_value, all_in_index, materials_index, labour_index, plant_index, source) VALUES
(64,  NOW(), NOW(), 5, 2020, 1, 118.20, 123.40, 112.30, 131.50, 118.20, 'BCIS Quarterly Review Q1 2020'),
(65,  NOW(), NOW(), 5, 2020, 4, 119.10, 124.10, 113.10, 132.00, 119.10, 'BCIS Quarterly Review Q2 2020'),
(66,  NOW(), NOW(), 5, 2020, 7, 119.80, 124.80, 113.90, 132.80, 119.80, 'BCIS Quarterly Review Q3 2020'),
(67,  NOW(), NOW(), 5, 2020, 10, 120.50, 125.60, 114.80, 133.60, 120.50, 'BCIS Quarterly Review Q4 2020'),
(68,  NOW(), NOW(), 5, 2021, 1, 122.30, 127.30, 117.20, 135.10, 122.30, 'BCIS Quarterly Review Q1 2021'),
(69,  NOW(), NOW(), 5, 2021, 4, 124.80, 129.80, 120.40, 137.20, 124.80, 'BCIS Quarterly Review Q2 2021'),
(70,  NOW(), NOW(), 5, 2021, 7, 127.20, 132.50, 124.10, 139.50, 127.20, 'BCIS Quarterly Review Q3 2021'),
(71,  NOW(), NOW(), 5, 2021, 10, 129.60, 134.80, 127.20, 141.20, 129.60, 'BCIS Quarterly Review Q4 2021'),
(72,  NOW(), NOW(), 5, 2022, 1, 132.10, 137.20, 130.50, 143.80, 132.10, 'BCIS Quarterly Review Q1 2022'),
(73,  NOW(), NOW(), 5, 2022, 4, 134.50, 139.50, 133.20, 146.20, 134.50, 'BCIS Quarterly Review Q2 2022'),
(74,  NOW(), NOW(), 5, 2022, 7, 136.80, 141.80, 135.80, 148.60, 136.80, 'BCIS Quarterly Review Q3 2022'),
(75,  NOW(), NOW(), 5, 2022, 10, 139.10, 143.90, 138.20, 150.80, 139.10, 'BCIS Quarterly Review Q4 2022'),
(76,  NOW(), NOW(), 5, 2023, 1, 141.30, 145.80, 140.50, 152.40, 141.30, 'BCIS Quarterly Review Q1 2023'),
(77,  NOW(), NOW(), 5, 2023, 4, 142.80, 147.20, 142.10, 154.10, 142.80, 'BCIS Quarterly Review Q2 2023'),
(78,  NOW(), NOW(), 5, 2023, 7, 144.20, 148.50, 143.60, 155.50, 144.20, 'BCIS Quarterly Review Q3 2023'),
(79,  NOW(), NOW(), 5, 2023, 10, 145.50, 149.80, 145.10, 156.80, 145.50, 'BCIS Quarterly Review Q4 2023'),
(80,  NOW(), NOW(), 5, 2024, 1, 146.80, 150.90, 146.30, 157.90, 146.80, 'BCIS Quarterly Review Q1 2024'),
(81,  NOW(), NOW(), 5, 2024, 4, 147.60, 151.80, 147.20, 158.80, 147.60, 'BCIS Quarterly Review Q2 2024'),
(82,  NOW(), NOW(), 5, 2024, 7, 148.20, 152.40, 147.90, 159.40, 148.20, 'BCIS Quarterly Review Q3 2024'),
(83,  NOW(), NOW(), 5, 2024, 10, 148.90, 153.10, 148.60, 160.10, 148.90, 'BCIS Quarterly Review Q4 2024'),
(84,  NOW(), NOW(), 5, 2025, 1, 149.60, 153.80, 149.40, 160.80, 149.60, 'BCIS Quarterly Review Q1 2025');

-- Verify counts
-- SELECT series, COUNT(*) FROM bcis_indices GROUP BY series ORDER BY series;