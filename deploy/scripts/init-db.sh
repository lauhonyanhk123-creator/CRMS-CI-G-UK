-- CRMS CI G UK - Database Initialization Script
-- This script runs on first database initialization

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create application user with appropriate permissions
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'crms_app') THEN
        CREATE USER crms_app WITH PASSWORD 'change_me';
        GRANT CONNECT ON DATABASE crms TO crms_app;
    END IF;
END
$$;

-- Grant schema permissions
GRANT USAGE ON SCHEMA public TO crms_app;
GRANT ALL PRIVILEGES ON SCHEMA public TO crms_app;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO crms_app;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO crms_app;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO crms_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO crms_app;

-- Create indexes for performance (optional optimization)
-- These will be created by Flyway migrations as well
