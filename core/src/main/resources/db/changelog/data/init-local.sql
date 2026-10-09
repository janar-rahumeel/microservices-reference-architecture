--changeset janar.rahumeel
--comment: Create mra-demo user, SUPER_ADMIN role and permissions

INSERT INTO "user" (id, username, enabled)
VALUES (uuidv7(),
        'mra-demo',
        true) ON CONFLICT (username) DO NOTHING;

INSERT INTO role (id, name, description)
VALUES (uuidv7(),
        'SUPER_ADMIN',
        'Super administrator with unrestricted administrative access') ON CONFLICT (name) DO NOTHING;

INSERT INTO permission (id, name, description)
VALUES (uuidv7(), 'CUSTOMER_READ', 'Read customers'),
       (uuidv7(), 'CUSTOMER_WRITE', 'Write customers') ON CONFLICT (name) DO NOTHING;

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id
FROM "user" u
         CROSS JOIN role r
WHERE u.username = 'mra-demo'
  AND r.name = 'SUPER_ADMIN' ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
         CROSS JOIN permission p
WHERE r.name = 'SUPER_ADMIN' ON CONFLICT (role_id, permission_id) DO NOTHING;