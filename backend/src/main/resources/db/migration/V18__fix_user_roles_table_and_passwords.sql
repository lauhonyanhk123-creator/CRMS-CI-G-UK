-- V18: Fix user_roles table (missing from all prior migrations) and re-hash passwords
--
-- Problems fixed:
-- 1. The User entity maps roles via @ElementCollection to a 'user_roles' collection
--    table, but no migration ever created this table.  Hibernate ddl-auto=validate
--    refuses to start when a mapped table is absent.
-- 2. The V1 seed inserted BCrypt password hashes ($2a$...) but PasswordEncoderConfig
--    uses Argon2PasswordEncoder.  BCrypt hashes always fail Argon2 matches(), so
--    every seeded account was unloggable.
-- 3. The V1 users.role column is NOT NULL, which blocks JPA inserts because the
--    entity writes to user_roles instead of users.role.

-- ─── 1. Create user_roles collection table ───────────────────────────────────
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, role)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);

-- ─── 2. Populate user_roles from the legacy users.role column ────────────────
INSERT INTO user_roles (user_id, role)
SELECT id, role
FROM   users
WHERE  role IS NOT NULL
ON CONFLICT DO NOTHING;

-- Give the admin user the IT_ADMIN role too (matches DataInitializer behaviour)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_IT_ADMIN'
FROM   users
WHERE  username = 'admin'
ON CONFLICT DO NOTHING;

-- ─── 3. Make users.role nullable so JPA inserts (which use user_roles) work ──
ALTER TABLE users ALTER COLUMN role DROP NOT NULL;

-- ─── 4. Re-hash seeded passwords: BCrypt → Argon2id ─────────────────────────
--   Parameters: saltLen=16, hashLen=32, parallelism=4, memory=65536, iterations=3
--   (matches PasswordEncoderConfig exactly)
--   admin gets mustChangePassword=true; all others remain false.
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$0roXwjinNEZIqTWG0DrHmA$/KGWZUIASHE5D7LF7A4edHkMwDByleVnSFzaM9BnhTU', must_change_password = true  WHERE username = 'admin';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$GuM8Z8x5rxUipBQCYOz9vw$DN6mBDmDO+ysrFKNQJ50xs/2ipDnjfdBSUN7oybQ4QE', must_change_password = false WHERE username = 'ops_director';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$TEnpvVdKaS3lfC/FOIew9g$sSKyStugSRvZnRV0NaLH9d6fafaYW4SxPQTmEnkWfz0', must_change_password = false WHERE username = 'contracts_mgr';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$qhWC0NpbK0XIOSdECEFozQ$x2Tpc9QPqbqimYACmdKQe2NMbSUftrR8LJJqdZ/TfVI', must_change_password = false WHERE username = 'qs';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$1ZqzNiakFIKw1vrfu/fe+w$JOALRBcDp9+3EYKdO9yuxitRuxeVFflU/AcWmkDNwa0', must_change_password = false WHERE username = 'site_agent';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$sHaOEcKY8z6HkFKqFSJEiA$kBjk1VqIUlNuo2aGCW4SljuqfPDn5rpCD/nLU5ZGPfs', must_change_password = false WHERE username = 'engineer';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$pzSGUOr937s3JmTsvVeqNQ$AGFoNGEFbSTDRJuQarfKNo41zrLBRGc/S8TYM2a+/7I', must_change_password = false WHERE username = 'plant_mgr';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$H2NMiTHm3Ntba02pNWYMYQ$7sLjwUjtMVlEJ6WE+fRXyGx2AgTzISxl022HsyOrWMQ', must_change_password = false WHERE username = 'buyer';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$i7G2lnLuPeccg1AqZcx5Dw$Gt9IU5AIdbH0eOdP5Tujzkx5qd4TBQf1jgKaoc3XCPY', must_change_password = false WHERE username = 'finance';
UPDATE users SET password = '$argon2id$v=19$m=65536,t=3,p=4$8P7fe8/Zm/OeUwqB8L73ng$cD9VVioAr79O8w2j/u2utvtiacxcWA4fenzJYuQv5K4',  must_change_password = false WHERE username = 'estimator';
