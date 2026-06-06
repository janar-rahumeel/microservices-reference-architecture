## Configure Client Credentials Flow (Keycloak)

> ⚠️ **Demo vs PRODUCTION note**
>
> This setup uses the **Client Credentials Flow**, which is the recommended approach for M2M (machine-to-machine) access.
>
> For **demo purposes**, configuration is intentionally simplified (single client, shared secret).
>
> In **PRODUCTION environments**, you should:
> - Use **one client per integration**
> - Rotate secrets or use private-key JWT / mTLS instead of static secrets
> - Apply strict scope/route separation per client
> - Avoid shared credentials across multiple external parties

## 1. Enable Client Credentials on Client

- Go to **Clients → your-client (e.g. `mra-api`)**
- Enable:
  - `Service Accounts Enabled = ON`

This automatically enables:
- `client_credentials` grant type

## 2. Configure Client Credentials

- Ensure:
  - Correct `client_id`
  - Valid `client_secret`

## 3. Token Request Example

```bash
curl -X POST "http://localhost:8190/realms/mra/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=mra-api" \
  -d "client_secret=mra-api-secret"