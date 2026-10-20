--changeset janar.rahumeel
--comment: Create mra-demo user

-- id is the Keycloak subject of mra-demo (see etc/docker/config/keycloak/mra-realm.json)
INSERT INTO "user" (id)
VALUES ('0f7b8c8c-5d4a-4870-83f9-ace270edaff2') ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT '0f7b8c8c-5d4a-4870-83f9-ace270edaff2'::uuid, r.id
FROM role r
WHERE r.name = 'SUPER_ADMIN' ON CONFLICT (user_id, role_id) DO NOTHING;
