# Microservices Reference Architecture (Work in Progress)

Reference implementation of a production-ready microservices
architecture demonstrating best practices, event-driven communication,
service integration patterns, and observability. Services are
implemented with Spring Boot and communicate via RabbitMQ

## Local Environment Setup

This repository includes a complete local environment setup guide
covering TLS, certificates, identity, messaging, observability, and
service configuration

### 🔐 Security & TLS

[Local TLS Setup](etc/local-tls-setup.md) - Step CA setup, local
certificate authority, and HTTPS configuration for all services

### 🔑 Identity & Authentication

Start Keycloak infrastructure:

``` bash
make deploy-keycloak-db
make deploy-keycloak
```

URL: https://kc.mra.local:9443 (credentials mra:mra123)

See [Keycloak Setup](etc/keycloak-configuration.md) for OAuth2/OIDC configuration

### 🐇 Messaging (RabbitMQ)

Start RabbitMQ:

``` bash
make deploy-rabbitmq
```

Management UI: http://localhost:15672

RabbitMQ provides asynchronous communication between microservices.
Services publish and consume domain events using exchanges and routing
keys, enabling reliable, loosely coupled, event-driven workflows

### 📈 Observability

Start the observability stack:

``` bash
make deploy-grafana 
make deploy-elastic-agent
```

#### Grafana

URL: http://localhost:9445 (credentials mra:mra123)

Grafana provides dashboards for application metrics, distributed traces, and infrastructure monitoring

#### Prometheus

URL: http://localhost:9444

Prometheus scrapes metrics exposed by Spring Boot Actuator endpoints, Blackbox Exporter and
stores them for visualization and alerting in Grafana

#### Blackbox Exporter

Blackbox Exporter performs external endpoint and service health probes and exposes the probe results as Prometheus metrics

#### Tempo

Tempo receives distributed traces from Spring Boot applications via
OpenTelemetry, enabling end-to-end tracing across services

#### Kibana

URL: http://localhost:9446 (credentials mra:mra123)

Kibana provides log exploration, search, and visualization for logs stored in Elasticsearch

#### Elasticsearch

Elasticsearch stores structured application logs in ECS format and
provides the backend for log search and analysis

#### Elastic Agent

Elastic Agent collects Spring Boot ECS-structured application logs and forwards them to Elasticsearch

### 🧩 Start Applications

#### UI

``` bash
make start-ui
```

URL: https://ui.mra.local:4200

#### API Gateway

``` bash
make start-gateway
```

URL: https://api.mra.local:8443

Swagger UI: https://api.mra.local:8443/swagger-ui/index.html

#### Core Microservice

``` bash
make start-core
```

No public URL (accessible through the API Gateway)

#### Worker Microservice

``` bash
make start-worker
```

No public URL

## Architecture

The architecture is maintained as **Architecture as Code** using LikeC4

``` bash
make start-likec4
```

Viewer URL: http://localhost:5173

The diagrams include product, operational platform, RabbitMQ, and runtime dependency views

The LikeC4 model is defined in `/etc/c4/architecture.c4`

