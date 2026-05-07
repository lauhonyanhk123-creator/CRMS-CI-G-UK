-- Add TOTP (Time-based One-Time Password) 2FA columns to users table
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS totp_secret VARCHAR(64),
    ADD COLUMN IF NOT EXISTS totp_enabled BOOLEAN NOT NULL DEFAULT FALSE;
