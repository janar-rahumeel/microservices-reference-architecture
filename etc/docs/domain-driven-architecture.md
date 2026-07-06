# Domain-Driven Design (DDD)

The goal of this structure is to keep business decisions explicit, testable, and resilient to framework changes. Business logic lives in the center, while transport, storage, and integration concerns remain at the edges.

The architecture follows an inward dependency rule:

```text
adapter → application → domain
```

The codebase is organized first by **bounded contexts (business capabilities)** and then by architectural layers. This ensures that business functionality remains cohesive while dependencies always point toward the domain model.

## 1) Domain Layer (Business Core)

The domain layer contains the core business model and rules. It is completely independent of frameworks, transport mechanisms, and persistence technologies. The code here should express business concepts directly rather than technical implementation details.

### Responsibilities

- **Entities**: Objects with identity and lifecycle
- **Value Objects**: Immutable concepts defined by attributes
- **Aggregates**: Consistency boundaries enforcing invariants
- **Domain Services**: Business logic that does not belong to a single entity
- **Domain Events**: Business-relevant state changes
- **Invariants and Domain Errors**: Explicit business rules and violations

### Key Principle

Domain objects must protect their invariants. Invalid states should be impossible or at least difficult to construct. Validation should happen as close to the domain model as possible, and business errors should be explicit and meaningful.

### Tradeoff

The domain boundaries are kept strict, but not over-engineered. Not every rule requires a dedicated service or abstraction. However, all core business decisions must remain inside the domain layer.

## 2) Application Layer (Use Cases)

The application layer orchestrates business workflows. It translates user intent into domain operations and coordinates interactions with external systems via ports.

It does not implement business rules—it executes them.

### Responsibilities

- **Application Services / Use Cases**
  - Orchestrate workflows
  - Define transaction boundaries

- **Commands and Queries**
  - Separate write and read models when needed (CQS/CQRS)

- **Ports (Interfaces)**
  - Define required external capabilities (persistence, messaging, email, HTTP clients, etc.)

### Typical Flow

1. Receive input intent
2. Convert primitives into domain types
3. Load aggregates via outbound ports
4. Execute domain behavior
5. Persist changes via ports
6. Trigger side effects (events, notifications, integrations)

Application services should not depend on each other in a way that creates cycles. Shared orchestration logic is extracted only when duplication becomes meaningful.

### Tradeoff

For simplicity, explicit command/query objects may be omitted for every endpoint. However, the conceptual separation remains important and guides future refactoring as complexity grows.

## 3) Adapter Layer (Interface Adapters + Infrastructure)

The adapter layer is responsible for interacting with the outside world. It contains all framework-specific and integration-specific code. This layer translates external formats into application-friendly input and maps application output back to external systems.

Adapters depend inward on the application layer via ports.

### Inbound Adapters (Driving Adapters)

- REST Controllers
- Message Consumers
- Scheduled Jobs

### Outbound Adapters (Driven Adapters)

- Database Repositories (JPA, JDBC, etc.)
- HTTP Clients
- Messaging Publishers
- External API Integrations
- File Storage

### Supporting Components

- DTOs (transport contracts)
- Mappers (domain ↔ external models)
- Framework configuration (Spring, DI, security, etc.)

### Key Principle

Adapters translate—not decide. They must not contain business rules. Any business decision belongs in the application or domain layers.

For legacy or external systems, adapters may act as an **anti-corruption layer**, isolating the domain from external model leakage.

## 4) Dependency Direction

All dependencies point inward:

```text
adapter → application → domain
```

### Meaning

- **Domain**: defines business rules and invariants
- **Application**: defines use cases and orchestration
- **Adapter**: implements communication and integration

### Consequences

- Domain has no framework dependencies
- Application depends only on domain and ports
- Adapters implement ports and depend on application contracts

### Testing Strategy

- Domain → pure unit tests
- Application → use case tests with mocked ports
- Adapters → integration tests against real systems where needed

### Tradeoff

Some pragmatic shortcuts may be used in adapters for delivery speed, but they must never leak into domain logic. Any technical debt must remain isolated and explicit.

## 5) Package Structure (Bounded Contexts)

The codebase is organized by **bounded contexts (business capabilities)** rather than technical layers. Each bounded context encapsulates a cohesive business domain and contains its own domain, application, and adapter packages.

A bounded context represents a distinct business capability with its own terminology, rules, and lifecycle. Package names should reflect the business domain rather than technical concerns e.g. `customer`, `agreement`, or `meteringpoint`. Each context owns its business model and implementation details, minimizing coupling with other contexts. Communication between contexts should occur through well-defined application interfaces or domain events, avoiding direct access to another context's internal implementation.


Example:

```text
ee.geckosolutions.mra.core
└── context
    ├── customer      // Customer Management
    │   ├── domain
    │   ├── application
    │   └── adapter
    ├── agreement     // Agreement Management
    │   ├── domain
    │   ├── application
    │   └── adapter
    └── meteringpoint // Metering Point Management
        ├── domain
        ├── application
        └── adapter
```

Within each context, the same inward dependency rule applies:

```text
adapter → application → domain
```

### Benefits

- Groups code by business capability rather than technical concern
- Keeps related domain, application, and adapter code together
- Reduces coupling between unrelated business areas
- Enables contexts to evolve independently
- Simplifies future extraction into separate modules or microservices
- Improves navigation, discoverability, and AI-assisted code generation

### Tradeoff

Small systems may start with fewer contexts. New contexts should only be introduced when they represent a clear business capability, avoiding premature fragmentation.

## 6) Why This Structure Helps AI-Assisted Development

Clear separation of responsibilities improves predictability in code generation and refactoring.

### Benefits

- Reduces ambiguity about where logic belongs
- Prevents business rules from leaking into adapters/controllers
- Makes changes localized and easier to reason about
- Improves consistency in generated code structure

### Practical Impact

- Domain becomes a stable and reusable core model
- Application layer becomes predictable orchestration space
- Adapter layer becomes replaceable infrastructure boundary

This reduces cognitive load and improves correctness in automated or AI-assisted development workflows.

## 7) Tradeoffs Summary

Strict layering introduces additional structure (ports, DTOs, mappers). This is acceptable when it improves long-term maintainability and boundary clarity.

Simplification is allowed only when it does not violate:

- Dependency direction
- Domain integrity
- Context boundaries
