--liquibase formatted sql

--changeset janar.rahumeel:MRA-15-01-ceate-mra-demo-user
--comment: Create mra-demo user

INSERT INTO "user" (id)
VALUES ('0f7b8c8c-5d4a-4870-83f9-ace270edaff2') ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT '0f7b8c8c-5d4a-4870-83f9-ace270edaff2'::uuid, r.id
FROM role r
WHERE r.name = 'SUPER_ADMIN' ON CONFLICT (user_id, role_id) DO NOTHING;

--changeset janar.rahumeel:MRA-18-01-ceate-mra-api-technical-user
--comment: Create mra-api technical user

INSERT INTO "user" (id)
VALUES ('2405215e-da70-4fcf-8c78-b45b7dafd705') ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT '2405215e-da70-4fcf-8c78-b45b7dafd705'::uuid, r.id
FROM role r
WHERE r.name = 'SUPER_ADMIN' ON CONFLICT (user_id, role_id) DO NOTHING;
