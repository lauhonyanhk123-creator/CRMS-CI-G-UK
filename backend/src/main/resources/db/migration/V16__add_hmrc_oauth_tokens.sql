-- HMRC OAuth2 authorisation code flow tokens
-- Stores access + refresh tokens for the contractor's HMRC MTD CIS authorisation
CREATE TABLE hmrc_oauth_tokens (
    id                BIGSERIAL PRIMARY KEY,
    contractor_utr    VARCHAR(10)  NOT NULL,
    access_token      TEXT         NOT NULL,
    refresh_token     TEXT,
    token_type        VARCHAR(32)  NOT NULL DEFAULT 'Bearer',
    expires_in        BIGINT       NOT NULL,
    issued_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    scope             TEXT,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_hmrc_oauth_tokens_utr UNIQUE (contractor_utr)
);

CREATE INDEX idx_hmrc_oauth_tokens_utr ON hmrc_oauth_tokens (contractor_utr);
