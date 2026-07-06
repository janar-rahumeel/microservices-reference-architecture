# Local CA & TLS Certificates for Local Environment (Windows)

This guide explains how to create a local Certificate Authority (CA), issue TLS certificates for your applications, and trust the CA in Windows

The setup uses a single wildcard certificate for the following demo domains:

* `https://ui.mra.local`
* `https://api.mra.local`
* `https://kc.mra.local`

## 1. Install Smallstep

Install the required tools using Winget:

```bash
winget install Smallstep.step
winget install Smallstep.step-ca
```

## 2. Create a Local Certificate Authority

Initialize a new local CA:

```bash
step ca init
```

Recommended configuration:

| Setting  | Value                             |
| -------- |-----------------------------------|
| CA Name  | MRA Local CA                      |
| DNS Name | localhost                         |
| Port     | 9000                              |
| Password | changeit (or something memorable) |

## 3. Adjust Default TLS Certificate Expiration (Optional)

Step CA controls certificate lifetimes via `claims` in `ca.json` under `authority`. By default, the certificate lifetime is 24h, so longer-lived certificates require explicitly increasing `defaultTLSCertDuration` and `maxTLSCertDuration` in the CA configuration. 

More information: https://smallstep.com/docs/step-ca/basic-certificate-authority-operations/#adjust-certificate-lifetimes

## 4. Start the CA

Launch the CA server:

```bash
step-ca "$(step path)/config/ca.json"
```

Keep this process running while issuing certificates

## 5. Issue a Wildcard Certificate

Generate a wildcard certificate that can be used by all local services:

```bash
step ca certificate "*.mra.local" mra.crt mra.key \
  --san "*.mra.local" \
  --san "mra.local"
```

This creates:

* `mra.crt` – TLS certificate
* `mra.key` – Private key

Copy the generated files to `etc/certificate` directory

## 6. Configure Local DNS

Edit the Windows hosts file:

```text
C:\Windows\System32\drivers\etc\hosts
```

Add the following entries:

```text
127.0.0.1 ui.mra.local
127.0.0.1 api.mra.local
127.0.0.1 kc.mra.local
```

## 7. Trust the CA in Windows

Import the root CA so browsers and Windows trust certificates issued by your local CA

1. Press **Win + R**
2. Run:

```text
certmgr.msc
```

3. Navigate to:

```text
Trusted Root Certification Authorities
    └── Certificates
```

4. Import:

```text
root_ca.crt
```

After importing, browsers that use the Windows certificate store (such as Chrome and Edge) will trust certificates issued by this CA

# Security Notes

* Never commit private key (`*.key`) files to source control
* The root CA certificate (`root_ca.crt`) may be shared within your development team so everyone can trust the same local CA
* Each developer should generate or securely obtain the corresponding private keys used by local services
* This setup is intended for local environment only and should never be used in **production**
