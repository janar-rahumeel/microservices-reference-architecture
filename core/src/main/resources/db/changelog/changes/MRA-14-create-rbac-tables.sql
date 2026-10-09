--liquibase formatted sql

--changeset janar.rahumeel:MRA-14-01-create-user-table
--comment: Create user table
CREATE TABLE "user"
(
    id         UUID         NOT NULL PRIMARY KEY DEFAULT uuidv7(),
    username   TEXT         NOT NULL UNIQUE,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

--changeset janar.rahumeel:MRA-14-02-create-role-table
--comment: Create role table
CREATE TABLE role
(
    id          UUID         NOT NULL PRIMARY KEY DEFAULT uuidv7(),
    name        TEXT         NOT NULL UNIQUE,
    description TEXT,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

--changeset janar.rahumeel:MRA-14-03-create-user-role-table
--comment: Create user-role relationship table
CREATE TABLE user_role
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE INDEX idx_user_role_role_id ON user_role (role_id);

--changeset janar.rahumeel:MRA-14-04-create-permission-table
--comment: Create permission table
CREATE TABLE permission
(
    id          UUID        PRIMARY KEY DEFAULT uuidv7(),
    name        TEXT        NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

--changeset janar.rahumeel:MRA-14-05-create-role-permission-table
--comment: Create role-permission relationship table
CREATE TABLE role_permission
(
    role_id       UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id) REFERENCES permission (id)
);

CREATE INDEX idx_role_permission_permission_id ON role_permission (permission_id);

--changeset janar.rahumeel:MRA-14-06-add-customer-audit-columns
--comment: Add audit columns to customer table
ALTER TABLE customer
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

