--liquibase formatted sql

--changeset janar.rahumeel:MRA-15-01-use-identity-id-as-user-id
--comment: The user id is the identity provider subject and the username lives in the identity provider only
ALTER TABLE "user"
    DROP COLUMN username,
    ALTER COLUMN id DROP DEFAULT;
