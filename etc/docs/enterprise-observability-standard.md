# Enterprise Observability Standard

## 1. Purpose

This document defines the standard for structured observability across enterprise applications and services.

The standard combines:

* **Elastic Common Schema (ECS)** for structured log fields and Elasticsearch/Kibana integration ([elastic.co](https://www.elastic.co/docs/reference/ecs))
* **OpenTelemetry (OTel) Semantic Conventions** for vendor-neutral observability semantics across logs, traces, metrics, and other telemetry ([opentelemetry.io](https://opentelemetry.io/docs/concepts/semantic-conventions))

The objective is to provide a consistent observability experience across different products and services. Common concepts should be represented using established standards, while organization- or product-specific information can be added through a defined custom-field policy

The standard follows three principles:

1. Use ECS and OTel conventions whenever an appropriate standard concept exists
2. Do not duplicate or redefine existing standard concepts
3. Use a documented and consistently applied policy for custom information

# 2. Benefits

## 2.1 Consistent observability across products

All products should use the same representation for common concepts such as:

```text
service.name
service.version
trace.id
span.id
event.*
error.*
http.*
```

This makes logs and telemetry easier to understand regardless of which product or microservice produced them

OTel provides semantic conventions that establish common names and meanings across codebases, libraries, platforms, and different telemetry signals

## 2.2 Common experience across different technologies

Products may use different programming languages, frameworks, and infrastructure, but common observability concepts should remain recognizable

For example:

```text
service.name
service.version
trace.id
span.id
http.request.method
http.response.status_code
```

should have the same meaning across products

This allows developers and operations teams to move between products without having to learn a completely different observability model for each one

## 2.3 Easier log and trace correlation

Standard trace and span fields allow logs to be correlated with distributed traces:

```json
{
  "service.name": "customer",
  "trace.id": "4bf92f3577b22da1a3ce329d0e0e4457",
  "span.id": "bb111ebf93e58c25"
}
```

The same trace can therefore be followed across multiple services

## 2.4 Reusable dashboards and alerts

Standard fields allow dashboards, searches, and alerts to be reused across products

For example:

```text
service.name : "customer"
```

or:

```text
log.level : "ERROR"
```

can be used without knowing how an individual application implemented its logging

## 2.5 Reduced custom conventions

Without a common standard, different applications may represent the same concept differently:

```text
requestId
request_id
request.id
correlationId
```

Using ECS and OTel conventions reduces these differences and provides a common vocabulary

# 3. ECS and OTel

ECS and Otel are complementary rather than competing standards

**OTel** provides vendor-neutral semantic conventions and telemetry models for logs, traces, metrics, events, and resources

**ECS** provides a common field schema particularly suited to Elasticsearch and Kibana

The enterprise standard should therefore align with both wherever practical

The goal is not to create a separate enterprise-specific observability model, but to build on established standards:

```text
                  Enterprise Observability
                           │
             ┌─────────────┴─────────────┐
             │                           │
           ECS                           OTel
             │                           │
             └─────────────┬─────────────┘
                           │
                    Common semantics
                           │
             ┌─────────────┼─────────────┐
             │             │             │
           Logs         Traces        Metrics
```

For example, service identity is represented consistently through concepts such as `service.name`, `service.version`, and `service.instance.id`. OTel defines these as service resource attributes ([opentelemetry.io](https://opentelemetry.io/docs/specs/semconv/resource/service))

# 4. Use Standards First

When introducing a new observability field:

* Check ECS and OTel Semantic Conventions first
* Use an existing standard field when its defined meaning matches the information being recorded
* Do not create custom aliases for existing standard concepts
* Do not use a standard field merely because its name appears convenient; its semantic meaning must also match

# 5. Custom Fields

Custom fields are expected for organization- or application-specific information not covered by ECS or OTel. They should follow a documented policy and:

* Avoid duplicating standard fields
* Use stable, recognizable names
* Follow consistent naming and nesting conventions
* Consider potential conflicts with future standard fields

An organization-specific root namespace is the recommended approach because it provides a clear ownership boundary and reduces the risk of conflicts with future ECS fields. For example, a reference project may use `mra.*`, while a real organization should select a namespace appropriate to its own policy

The organizational root namespace is recommended, not mandatory. Each organization should define its own approach based on its systems, governance, and product structure. Possible approaches include a single namespace such as `<organization>.*`, separate product namespaces, or another documented convention. Regardless of the approach, it should be stable, recognizable, and consistently applied

Custom fields should be grouped by meaningful domain or product concepts. Related attributes should be placed under logical objects, while generic observability concepts should remain in their standard fields. For example, a selected namespace might contain `mra.customer.*` and `mra.processing.*`

# 6. Standard Field Semantics and Naming

Do not create custom equivalents of existing standard concepts, such as `mra.service.name`, `mra.trace.id`, `mra.error.message`, or `mra.http.request.method`. Use the corresponding ECS or OTel fields instead. The custom namespace is for additional information, not an alternative representation of standard observability data

Standard ECS and OTel fields must retain their defined meaning. Use a standard field only when the recorded information matches its documented semantics. Organization-specific information that does not fit an existing standard concept should be placed under the organization's custom-field policy

Custom fields should generally follow the same naming principles:

* Lowercase field names
* Underscores between words where applicable
* Meaningful and descriptive names
* Logical nesting for related concepts
* Consistent terminology across products
* Avoid unnecessary abbreviations, duplicate names, and names that resemble standard fields while having different meanings

# 7. Example

A typical application event combines standard observability fields with organization-specific information:

```text
Standard
├── @timestamp
├── log.*
├── service.*
├── trace.*
├── span.*
└── event.*

Custom
└── <organization>.*
    ├── domain-specific data
    └── application-specific data
```

Standard fields provide common observability semantics, while the custom namespace contains product- and domain-specific information

# 8. `labels` for Simple Custom Values

ECS `labels` can be used for simple additional keyword values that do not require a structured domain model. Use a structured custom namespace instead when the information represents a meaningful domain concept or contains related attributes. For example, simple deployment metadata may use `labels`, while a domain object with multiple related attributes should use the organization's structured custom-field policy

# 9. Future Standard Evolution

ECS and OTel conventions evolve over time. A custom field introduced today may eventually have an appropriate standard equivalent. When this happens, the application should migrate to the standard representation and update dependent dashboards, alerts, queries, and integrations before removing the custom field

Keeping custom fields clearly separated from standard fields makes such migrations easier

# 10. Decision Process

When introducing a new observability field:

1. Check ECS for an appropriate concept
2. Check OTel Semantic Conventions
3. Use the standard concept if its semantics match
4. If no suitable standard exists, apply the organization's custom-field policy
5. Prefer the organization's root namespace where that policy defines one
6. Group related custom fields under meaningful domain or product objects
7. Do not duplicate or override standard concepts

The key rule is:

> **Use ECS and OTel Semantic Conventions for common observability concepts. Use the organization's defined custom-field policy for information that is not covered by those standards**

An organizational root namespace is the recommended approach for custom fields because it provides a clear ownership boundary and reduces the risk of conflicts with future ECS/OTel fields. However, this is a recommendation rather than a fixed ECS/OTel requirement. Each organization should define and govern its own custom-field policy

For a reference project, `mra.*` can be used as the custom namespace. In a real enterprise implementation, it should be replaced with the namespace selected by the organization

The overall objective is to make observability across different products mostly consistent and familiar, while retaining enough flexibility to represent product- and domain-specific information
