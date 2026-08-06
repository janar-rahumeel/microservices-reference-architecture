# Architecture as Code

This repository uses [LikeC4](https://likec4.dev/) to define and visualize the **Microservices Reference Architecture (MRA)** as code.

## Overview

The architecture is split into two main areas:

* **MRA Product** — the example microservices-based application.
* **Operational Platform** — shared infrastructure required to run and operate the application.

The MRA product contains:

* Angular frontend
* API Gateway
* Core Service
* Asynchronous Worker Service
* External integrator
* Indicative additional services

The operational platform contains:

* **Identity** — Keycloak
* **Messaging** — RabbitMQ, exchanges and queues
* **Database** — PostgreSQL databases
* **Observability** — Elasticsearch, Kibana, Prometheus, Blackbox Exporter, Grafana and Tempo

## Architecture Model

The architecture is described using LikeC4's declarative model:

```text
Product
├── MRA
│   ├── Frontend
│   ├── API Gateway
│   ├── Core Service
│   ├── Worker Service
│   └── Additional Services
│
└── Operational Platform
    ├── Identity
    ├── Messaging
    ├── Database
    └── Observability
```

Relationships between application services and platform capabilities are explicitly modeled, including authentication, database access, messaging, logging, metrics, health monitoring and distributed tracing

## Views

The model provides several views for different architectural perspectives:

| View                     | Purpose                                            |
| ------------------------ | -------------------------------------------------- |
| **Landscape**            | High-level MRA and platform landscape              |
| **MRA Product**          | Internal structure of the MRA application          |
| **Operational Platform** | Shared infrastructure and platform services        |
| **RabbitMQ**             | Messaging topology, including exchanges and queues |
| **Runtime Dependencies** | Application-to-platform runtime dependencies       |

## Indicative Components

Elements tagged `#phantom` represent **indicative/extensible components** rather than concrete implementations. They demonstrate where additional services, databases or queues can be added without making the reference architecture unnecessarily large

## Why Architecture as Code?

Keeping the architecture in source code provides:

* **Version control** — architectural changes are tracked alongside code changes
* **Consistency** — diagrams are generated from the same model
* **Reviewability** — architecture changes can be reviewed through Git merge requests
* **Maintainability** — diagrams do not need to be maintained manually
* **Traceability** — architecture evolves together with the implementation

The LikeC4 model is therefore treated as a **living architectural specification**, rather than a static diagram
