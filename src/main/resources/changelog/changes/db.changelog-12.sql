--liquibase formatted sql

--changeset gonzalo:drop-unique-domain
ALTER TABLE vehicles DROP CONSTRAINT IF EXISTS vehicles_domain_key;

--changeset gonzalo:add-partial-unique-index
CREATE UNIQUE INDEX IF NOT EXISTS unique_active_domain
    ON vehicles (LOWER(domain))
    WHERE deleted_at IS NULL;