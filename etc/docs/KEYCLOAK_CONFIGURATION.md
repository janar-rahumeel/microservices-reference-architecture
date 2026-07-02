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
  - Go to **Mappers → Add mapper → By configuration → Audience**
    - `Included Custom Audience = mra`

## 3. Enable Client Credentials (M2M)

- Go to **Clients → Create client (e.g. Type = `OpenID Connect`, ID = `mra-api`)**
- Enable: 
  - `Client authentication = ON`
  - `Service accounts roles = ✔`

This enables the `client_credentials` grant type

### Token Request Example

```bash
curl -X POST "http://localhost:8190/realms/mra/protocol/openid-connect/token" \
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

- Valid redirect URIs = http://localhost:4200/auth/callback
- Web origins = http://localhost:4200
- Go to **Users → Add user (e.g. `mra-demo`)**
  - `Required user actions = NONE`
  - `E-mail verified = OFF`
  - Go to **Credentials → Set password (e.g. `mra-demo`)**
    - `Temporary = OFF` 