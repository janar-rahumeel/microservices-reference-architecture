# Microservices Reference Architecture (Work in Progress)

Reference implementation of a production-ready microservices architecture demonstrating best practices, event-driven communication, service integration patterns, and observability. Services are implemented with Spring Boot and communicate via RabbitMQ

## Local Environment Setup

This repository includes a complete local environment setup guide covering TLS, certificates, identity, and service configuration

### 🔐 Security & TLS

[Local TLS Setup](etc/local-tls-setup.md) – Step CA setup, local certificate authority, and HTTPS configuration for all services

### 🔑 Identity & Authentication

Start Keycloak infrastructure:

```bash
make deploy-keycloak-db
make deploy-keycloak
```

URL: https://kc.mra.local

See [Keycloak Setup](etc/keycloak-configuration.md) for OAuth2/OIDC configuration

### 🧩 Start Applications

#### UI

```bash
make start-ui
```

URL: https://ui.mra.local

#### API Gateway

```bash
make start-gateway
```

URL: https://api.mra.local

#### Core Microservice

```bash
make start-core
```

No public URL (accessible through the API Gateway)