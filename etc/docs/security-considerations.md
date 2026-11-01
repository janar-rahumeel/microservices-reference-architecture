# Security Considerations

## 1. Purpose

This document describes the security principles and baseline security measures applied in the Microservices Reference Architecture (MRA), together with recommendations for deployments with varying security requirements.

Security is a trade-off between protection, operational complexity, performance and cost. The appropriate security model should reflect the system's criticality, data sensitivity, regulatory obligations and potential attack vectors.

The objective is to establish a secure baseline that can be extended according to deployment requirements, rather than enforce the same security model everywhere.

## 2. Current MRA Security Approach

MRA provides application-level authentication and authorization using OAuth2 and JWT access tokens, TLS-protected internal service-to-service communication, and support for deployment-level network isolation.

It does not mandate a specific external edge protection solution or infrastructure topology.

### 2.1. External Traffic and Edge Protection

MRA does not currently include Cloudflare or an equivalent edge protection service as an architectural component.

However, placing an edge security layer in front of publicly exposed infrastructure is a recommended deployment best practice.

Solutions such as Cloudflare or equivalent services can provide:

- Web Application Firewall (WAF)
- DDoS protection
- Rate limiting and traffic filtering
- Protection against common web attacks
- TLS termination and protection of external traffic

Origin infrastructure should ideally accept traffic only from trusted edge infrastructure, preventing direct access that bypasses these protections.

Edge protection provides an additional security boundary, not a replacement for application-level authentication and authorization.

### 2.2. API Gateway and OAuth2

The API Gateway acts as the primary resource server for externally exposed APIs.

MRA uses OAuth2-based authentication and authorization with JWT access tokens. OAuth2 provides standardized mechanisms for obtaining access tokens, while JWT allows receiving services to validate the identity and authorization context represented by those tokens.

MRA supports the following OAuth2 flows:

- **Authorization Code Flow with PKCE:** User authentication for user-facing applications.
- **Client Credentials Flow:** Machine-to-machine authentication.

The API Gateway is responsible for:

- OAuth2 access token validation
- Authentication and authorization
- Enforcing scopes, roles and permissions
- Controlling access to downstream APIs

Internal service communication is independently protected using TLS and application-level authentication, depending on the communication flow and endpoint requirements.

### 2.3. Network Isolation

Microservices may be deployed into Kubernetes namespaces with communication restricted through NetworkPolicies.

The intended approach is to:

- Restrict access to explicitly authorized workloads
- Limit database connectivity to services that require it
- Prevent unnecessary communication between application components
- Restrict access to internal endpoints at the ingress or gateway level

A Kubernetes namespace alone does not provide network isolation. Appropriate network policies and workload permissions are required.

Network isolation complements TLS and application-level authentication but does not replace them.

### 2.4. Internal API Protection

Internal endpoints follow the `/internal/**` URL convention.

MRA protects internal communication through:

- TLS encryption to protect data in transit
- HTTP Basic Authentication as an application-level authentication mechanism for internal endpoints
- Network-level restrictions to limit access to authorized workloads

Service credentials are cryptographically generated with high entropy. Receiving services store only the SHA-256 hash of each credential rather than its original value.

During authentication, the supplied credential is hashed and compared against the stored hash using a constant-time comparison.

This approach ensures that:

- Original service credentials are not stored by receiving services
- A configuration leak does not directly expose reusable credentials
- Compromising one service does not automatically expose the original credentials of other services
- No shared hashing secret needs to be distributed between services

SHA-256 is suitable for randomly generated, high-entropy machine-to-machine credentials. It must not be used for human-selected passwords, which require dedicated password hashing algorithms such as BCrypt or Argon2id.

Where possible, individual services should have dedicated credentials rather than share technical accounts. Credentials must be securely stored by calling services, transmitted over TLS and periodically rotated.

Basic Authentication provides authentication, not fine-grained authorization. Additional authorization rules are required when callers have different permissions.

OAuth2 with JWT access tokens is also supported for API authentication and authorization where applicable. The authentication mechanism depends on the communication flow and endpoint requirements.

TLS protects credentials and application data in transit, while credential hashing reduces the impact of credential storage compromise. Neither mechanism protects against a compromised service using credentials available to it.

### 2.5. Internal TLS and Mutual TLS

MRA uses TLS for internal microservice communication as part of its baseline security model.

For demonstration purposes, MRA currently uses a shared Root CA, server certificate and corresponding private key across backend services. This simplifies certificate generation and configuration but is **not recommended for production deployments**.

In production deployments:

- Each backend service/server must have its own dedicated server certificate and corresponding private key
- Server certificates may be signed by a shared trusted Certificate Authority (CA)
- Private keys must be unique to each service, securely stored and never shared
- Certificates must have appropriate validity periods and be renewed or rotated before expiration
- Compromising one service's private key should not compromise the server identity of other services

It is important to distinguish between TLS and mutual TLS (mTLS):

- **TLS:** Encrypts communication and allows the client to authenticate the server using its certificate
- **mTLS:** Additionally requires the client to present a certificate, allowing the receiving service to authenticate the calling workload at the transport layer

The current MRA TLS setup provides encrypted internal communication and server authentication, assuming proper certificate validation by clients.

mTLS can be introduced where stronger workload identity verification is required. It does not replace application-level authorization: receiving services must still enforce the permissions applicable to each caller.

## 3. Zero Trust

Zero Trust is based on the principle of **never trust implicitly, always verify explicitly**.

No user, device or workload should be trusted simply because it belongs to an internal network or operates within a trusted infrastructure zone.

Access requests should be evaluated based on identity, permissions and applicable security context.

Typical Zero Trust measures include:

- Unique identities for users and workloads
- Authentication of service-to-service communication
- Fine-grained authorization and least privilege
- Encrypted communication
- Mutual TLS where appropriate
- Short-lived credentials and automated rotation
- Independent identity verification rather than relying on network location
- Continuous monitoring and auditing

MRA already implements several of these principles through OAuth2, JWT validation, TLS-protected internal communication, application-level authentication and authorization, and support for network isolation.

Comprehensive Zero Trust introduces additional operational complexity, infrastructure requirements and performance considerations. Its implementation should therefore be proportional to actual security requirements.

## 4. Security Requirements Based on System Criticality

Security measures should be selected based on system criticality, data sensitivity, business impact, regulatory obligations and realistic threat scenarios.

The following measures build upon the MRA security baseline. Higher criticality levels introduce additional controls or stricter enforcement requirements.

| Criticality | Potential security measures |
|---|---|
| **Low** | HTTPS, TLS-protected internal communication, OAuth2, JWT validation, API Gateway authentication and authorization, network isolation, Basic Authentication with hashed service credentials for internal endpoints, secure secrets management and basic security monitoring |
| **Medium** | Dedicated service identities, fine-grained authorization, default-deny network policies, credential rotation and centralized audit logging |
| **High** | Comprehensive Zero Trust, mTLS, short-lived credentials, strict network segmentation, runtime protection, SIEM (Security Information and Event Management) integration, advanced security monitoring and formal incident response |

These are indicative measures rather than strict classifications. Actual requirements must be determined through a security and risk assessment.

GDPR applicability does not automatically require the same security architecture for every system. However, personal data processing must always meet applicable GDPR security obligations.

## 5. Internal Networks Are Not Necessarily Trusted

Attacks do not necessarily originate from the public Internet. This is particularly relevant in industries such as energy, where organizations may operate separate IT, OT and IoT networks.

For example:

1. An attacker compromises an IoT device connected to an operational network.
2. The compromised device is used to access other reachable systems.
3. Insufficient network segmentation or a vulnerable integration allows access to IT infrastructure.
4. The attacker attempts lateral movement towards application services, databases or other infrastructure.

In this scenario, external edge protection does not protect against the initial attack because it does not originate from the public Internet.

Similarly, Kubernetes NetworkPolicies cannot prevent all attacks originating from an already compromised but authorized workload.

TLS protects internal communication against network-level interception and tampering, but does not prevent compromised workloads from accessing services they are legitimately allowed to reach.

Consequently, internal network location must never be considered sufficient proof of trust.

For IT/OT environments, additional measures should include:

- Strict segmentation between IT, OT and IoT networks
- Explicitly controlled communication between security zones
- Dedicated integration gateways
- No unrestricted inbound connectivity from less trusted networks
- Authentication and authorization for every integration
- Monitoring of cross-network communication

## 6. Additional Security Considerations

Regardless of deployment criticality, the following principles should guide security decisions:

| Area | Consideration |
|---|---|
| Authentication | Use standardized mechanisms such as OAuth2 and JWT where applicable. Prefer dedicated identities for individual services |
| Authorization | Apply least-privilege access independently of network restrictions. Enforce scopes, roles and permissions at the appropriate application boundary |
| Transport | Use TLS for external and internal service-to-service communication. Consider mTLS for stronger workload identity verification |
| Certificates | Use dedicated server certificates and private keys for individual backend services. A trusted CA may be shared, but private keys must never be shared |
| Secrets | Use centralized secrets management and credential rotation. Receiving services should store credential hashes rather than original service credentials |
| Credential storage | Use cryptographically random, high-entropy credentials and SHA-256 hashes for machine-to-machine Basic Authentication. Never use this approach for human passwords |
| Kubernetes | Restrict service accounts, RBAC permissions and container privileges |
| Network | Apply explicit ingress and egress restrictions |
| Monitoring | Log authentication failures, authorization denials and suspicious activity |
| Supply chain | Secure dependencies, container images, build pipelines and deployment credentials |

## 7. Conclusion

MRA provides a layered security model combining OAuth2-based authentication and authorization, JWT access token validation, TLS-protected internal communication and support for deployment-level network isolation.

Its baseline includes:

- Authorization Code Flow with PKCE for user-facing applications
- Client Credentials Flow for machine-to-machine communication
- JWT access token validation and API Gateway authorization
- TLS-protected internal microservice communication
- Basic Authentication with hashed service credentials for internal endpoints
- Support for network isolation through deployment-level controls

The current MRA certificate configuration uses a shared Root CA, server certificate and private key across backend services for demonstration purposes only. Production deployments must use dedicated server certificates and private keys for each backend service, even when certificates are issued by the same trusted CA.

These measures reduce security risks but do not eliminate threats associated with compromised workloads, stolen credentials or unrestricted lateral movement.

Additional controls, such as mutual TLS, workload identity, stricter network segmentation and comprehensive Zero Trust, can be introduced according to deployment requirements.

The appropriate security model must be determined through an organization's risk assessment, system criticality, data classification, regulatory obligations and potential attack vectors.

In particular, organizations operating critical infrastructure or interconnected IT, OT and IoT networks must account for threats originating from internal networks, not only the public Internet.

**The objective is not to implement the most complex security model everywhere, but to ensure that security controls are proportionate to the risks and potential impact of a compromise.**