# Public API Deprecation Policy

## Purpose

This policy defines the process for deprecating public APIs exposed to external consumers. Its goal is to ensure that API changes are communicated in a predictable and transparent manner while providing consumers with sufficient time to migrate before functionality is removed

Unlike internal APIs, public APIs are consumed by organizations operating on independent release cycles. Consumers may require weeks or months to implement, test, and deploy changes. A well-defined transition period minimizes disruption and allows API providers to evolve their services responsibly

## Scope

This policy applies to all public APIs exposed outside the organization, including APIs consumed by:

- Customers
- Business partners
- Third-party integrators
- Other organizations with contractual integrations

Internal APIs may follow a different lifecycle depending on organizational requirements

# Why API Deprecation Matters

Public API consumers often cannot upgrade immediately because of:

- independent development and release schedules
- contractual obligations
- regulatory or compliance processes
- extensive integration testing
- multiple production environments

Deprecating an API instead of removing it immediately allows consumers to:

- plan migration activities
- validate integrations against the replacement API
- deploy changes according to their own release processes
- avoid unexpected production outages

# Deprecation Lifecycle

A typical public API lifecycle is:

1. API is released
2. A replacement API becomes available (if applicable)
3. Existing API is officially marked as **deprecated**
4. Deprecation is communicated through all supported channels
5. A transition period is provided
6. The announced sunset date is reached
7. The deprecated API is removed

The transition period should be determined according to business requirements, contractual commitments, and consumer impact

Unless otherwise agreed, a transition period of **at least six months** is recommended for public APIs

# Communication

Deprecation should never rely on a single communication channel

Recommended communication methods include:

- OpenAPI / Swagger documentation
- HTTP deprecation headers
- Release notes
- Changelog
- Developer portal
- Customer communications
- Email notifications (where applicable)
- Support documentation

Different consumers rely on different sources of information. Providing consistent messaging across multiple channels increases the likelihood that consumers become aware of upcoming changes

# API Documentation

API documentation must accurately reflect the lifecycle state of every public endpoint

Deprecated endpoints should clearly indicate:

- that the endpoint is deprecated
- the recommended replacement
- migration guidance
- the planned sunset/removal date (when known)

Swagger/OpenAPI is an excellent technical communication channel because many client SDKs, IDEs, API gateways, and developer tools automatically surface deprecation metadata

Documentation should always be updated before or at the same time the deprecation is announced

# HTTP Deprecation Headers

Where appropriate, public APIs should expose standardized HTTP headers to communicate deprecation information at runtime

Recommended headers include:

- `Deprecation`
- `Sunset`
- `Link` (pointing to migration documentation or the successor API)

These headers allow automated tooling, API gateways, monitoring systems, and client applications to detect deprecated endpoints without requiring users to manually review documentation

Example:

```http
Deprecation: @1861920000
Sunset: Wed, 1 Jul 2029 00:00:00 GMT
Link: </api/v2/customers>; rel="successor-version"
```

# Spring Boot Support

Spring Boot provides built-in support for API deprecation through its integration with the Spring Framework and OpenAPI tooling

Common capabilities include:

- marking endpoints with Java's `@Deprecated` annotation
- exposing deprecated operations in generated OpenAPI specifications
- automatically displaying deprecated operations in Swagger UI
- supporting runtime communication through standardized HTTP deprecation headers
- enabling centralized deprecation handling across an application

Starting with Spring Boot 4.1, a dedicated API deprecation abstraction is available for declaring deprecation metadata, including:

- deprecation date
- sunset date
- links to migration documentation or successor APIs

This allows applications to implement standards-based API deprecation with minimal custom infrastructure while keeping implementation, runtime behavior, and generated documentation synchronized

# Best Practices

- Never silently remove a public API
- Always provide a supported replacement before announcing deprecation whenever possible
- Communicate deprecation through multiple channels
- Keep implementation and documentation synchronized
- Publish migration guidance alongside the deprecation announcement
- Announce sunset dates as early as possible
- Return HTTP deprecation headers for deprecated endpoints
- Monitor usage of deprecated APIs before removal
- Consider contractual obligations and consumer release cycles when defining transition periods
- Treat API deprecation as both a technical and communication process

# Versioning Considerations

Deprecation does not necessarily require introducing a new API version

Individual resources, operations, request fields, response fields, query parameters, or headers may be deprecated independently while remaining within the same API version

Breaking removals should occur only in accordance with the project's versioning strategy and published compatibility guarantees

# Summary

A successful public API deprecation process combines:

- technical implementation
- accurate documentation
- runtime communication
- clear migration guidance
- transparent timelines
- sufficient transition periods (typically at least six months)

A predictable and well-communicated deprecation policy enables API providers to evolve their services while giving consumers the confidence and time needed to migrate safely