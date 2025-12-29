-- Initialize Order Tracking Database
-- This script runs automatically when PostgreSQL container starts

-- Create database (already created by POSTGRES_DB env var)
-- CREATE DATABASE order_tracking_db;

-- Connect to the database
\c order_tracking_db;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE order_tracking_db TO postgres;

-- Create indexes for better performance (tables will be created by Hibernate)
-- These will be created after the application starts and creates tables

-- Log initialization
SELECT 'Order Tracking Database initialized successfully' as status;