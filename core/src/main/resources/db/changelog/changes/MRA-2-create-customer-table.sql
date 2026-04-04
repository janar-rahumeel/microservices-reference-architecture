--liquibase formatted sql

--changeset janar.rahumeel:MRA-2-01-create-customer-table
--comment: Create customer table
CREATE TABLE customer
(
    id UUID PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    first_name VARCHAR(128),
    last_name VARCHAR(128),
    personal_identification_code VARCHAR(64),
    name VARCHAR(256),
    registration_code VARCHAR(64)
);

ALTER TABLE customer
    ADD CONSTRAINT uq_customer_personal_identification_code UNIQUE (personal_identification_code);

ALTER TABLE customer
    ADD CONSTRAINT uq_customer_registration_code UNIQUE (registration_code);

ALTER TABLE customer
    ADD CONSTRAINT chk_customer_fields_by_type CHECK (
        (type = 'PERSON'
            AND first_name IS NOT NULL
            AND last_name IS NOT NULL
            AND personal_identification_code IS NOT NULL
            AND name IS NULL
            AND registration_code IS NULL)
        OR (type = 'LEGAL_ENTITY'
            AND name IS NOT NULL
            AND registration_code IS NOT NULL
            AND first_name IS NULL
            AND last_name IS NULL
            AND personal_identification_code IS NULL)
    );
