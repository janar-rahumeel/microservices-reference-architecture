# Domain-Driven Design (DDD)

The goal of this structure is to keep business decisions explicit, testable, and resilient to framework changes. Business logic lives in the center, while transport, storage, and integration concerns remain at the edges.

The architecture follows an inward dependency rule:

```text
adapter → application → domain
```

This ensures that the domain remains independent of frameworks, databases, and external systems.

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

I keep domain boundaries strict, but avoid over-engineering simple cases. Not every rule requires a dedicated service or abstraction. However, all core business decisions must remain inside the domain layer.

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

A use case follows this sequence:

1. Receive input intent
2. Convert primitives into domain types
3. Load aggregates via outbound ports
4. Execute domain behavior
5. Persist changes via ports
6. Trigger side effects (events, notifications, integrations)

Application services should not depend on each other in a way that creates cycles. Shared orchestration logic should only be extracted when duplication becomes meaningful.

### Tradeoff

For simplicity, I may avoid explicit command/query objects for every endpoint. However, the conceptual separation remains important and should guide future refactoring when complexity increases.

## 3) Adapter Layer (Interface Adapters + Infrastructure)

The adapter layer is responsible for interacting with the outside world. It contains all framework-specific and integration-specific code. This layer translates external formats into application-friendly input and maps application output back to external systems.

Adapters depend inward on the application layer via ports.

### Inbound Adapters (Driving Adapters)

These initiate application workflows:

- REST Controllers
- Message Consumers
- Scheduled Jobs

### Outbound Adapters (Driven Adapters)

These implement application-defined ports:

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

## 5) Why This Structure Helps AI-Assisted Development

Clear separation of responsibilities improves predictability in code generation and refactoring.

### Benefits

- Reduces ambiguity about where logic belongs
- Prevents business rules from leaking into adapters/controllers
- Makes changes localized and easier to reason about
- Improves consistency in generated code structure

### Practical Impact

- Domain becomes stable and reusable context
- Application layer becomes predictable orchestration space
- Adapter layer becomes replaceable infrastructure boundary

This reduces cognitive load and improves correctness in automated or AI-assisted development workflows.

### Tradeoff

Strict layering introduces some boilerplate (mappers, ports, DTOs). This is acceptable when it improves long-term maintainability and boundary clarity. Simplification is allowed only when it does not violate dependency direction or domain integrity.
