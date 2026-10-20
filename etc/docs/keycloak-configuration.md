 ## Configure OAuth2 Flows

> ⚠️ **Demo vs PRODUCTION note**
>
> This setup includes:
> - Client Credentials Flow (M2M)
> - PKCE Authorization Code Flow (Angular UI)
>
> For **demo purposes**, configuration is intentionally simplified
>
> For **production**, you should:
> - Use one client per integration
> - Avoid sharing secrets across services
> - Prefer PKCE for SPAs (no client secret)
> - Use private-key JWT or mTLS for M2M where possible

## 1. Configure a New Realm

- Go to **Manage realms → Create realm (e.g. `mra`)**
  - `Enabled = ON`

## 2. Configure Custom Audience Scope

- Go to **Client scopes → Create client scope (e.g. `audience`)**
  - `Type = Default`
  - Go to **Mappers → Add mapper → Configure a new mapper → Audience**
    - `Included Custom Audience = mra`

## 3. Enable Client Credentials (M2M)

- Go to **Clients → Create client (e.g. Type = `OpenID Connect`, ID = `mra-api`)**
- Enable: 
  - `Client authentication = ON`
  - `Service accounts roles = ✔`

This enables the `client_credentials` grant type

- Go to **Credentials**
  - `Client Secret = mra-api-secret`

### Token Request Example

```bash
curl -X POST "https://kc.mra.local:9443/realms/mra/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=mra-api" \
  -d "client_secret=mra-api-secret"
```

## 4. Enable Authorization Code (Angular UI)

- Go to **Clients → Create client (e.g. Type = `OpenID Connect`, ID = `mra-ui`)**
- Enable:
  - `Standard flow = ✔`
  - `Require PKCE = ✔`
  - `PKCE Method = S256`

- Valid redirect URIs = https://ui.mra.local:8443/auth/callback
- Web origins = https://ui.mra.local:8443
- Go to **Users → Add user (e.g. `mra-demo`)**
  - `Required user actions = NONE`
  - `E-mail verified = OFF`
  - Go to **Credentials → Set password (e.g. `mra-demo`)**
    - `Temporary = OFF` 

## 5. Service Account for User Provisioning (core service)

The `core` service creates users in Keycloak (`POST /internal/api/v1/users`) through the Admin REST API using a
dedicated service account.

- Go to **Clients → Create client (e.g. Type = `OpenID Connect`, ID = `mra-core`)**
- Enable:
  - `Client authentication = ON`
  - `Service accounts roles = ✔`
- Go to **Credentials**
  - `Client Secret = mra-core-secret`
- Go to **Service accounts roles → Assign role → Filter by clients → `realm-management`**
  - Assign `manage-users` (least privilege; `view-users` is not required)

Configure `core` accordingly:

```properties
application.keycloak.base-url=http://localhost:8100
application.keycloak.realm=mra
application.keycloak.service-account.client-id=mra-core
application.keycloak.service-account.client-secret=mra-core-secret
application.keycloak.required-actions[0]=UPDATE_PASSWORD
```

Users are created without credentials and with the configured required actions. Keycloak must be configured with
`Duplicate emails = OFF` (default) so that duplicate e-mails are rejected with `409`.
