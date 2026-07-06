# Framework Upgrade Strategy (Major and Minor Versions)

## Purpose

To maintain system stability while still benefiting from improvements in frameworks and core libraries, **major and minor version upgrades of core frameworks** (e.g., Angular, Spring Boot) are handled intentionally through **roadmap tasks rather than automated dependency updates**.

This approach ensures upgrades are properly evaluated, coordinated, and tested before adoption, while also **supporting AI-assisted development**.

## Strategy

### 1. Disable Automatic Major and Minor Upgrades

Automated dependency tools should **not automatically upgrade major or minor versions of core frameworks**. These upgrades frequently introduce breaking changes, deprecations, or behavioral changes that require review and testing.

Dependency automation should therefore only allow:

* **Patch updates** (bug fixes and security fixes)

Major and minor upgrades should be performed only through **explicitly planned work**.

### 2. Plan Framework Upgrades via Roadmap Tasks

All **major and minor framework upgrades** must be introduced through **dedicated roadmap tasks**.

Framework upgrades often require:

* Code changes
* Configuration updates
* Dependency adjustments
* Testing updates

Because of this, upgrades must be **planned, prioritized, and scheduled** on the roadmap to ensure sufficient time for implementation and validation.

### 3. Consider Cross-Dependency Compatibility

Framework ecosystems typically include many **related dependencies that must stay compatible with each other**. For example, a framework upgrade may require aligned versions of:

* Core libraries
* Build plugins
* Tooling
* Testing frameworks
* Companion packages

Upgrading too early can lead to incompatibilities if the surrounding ecosystem has not yet fully adapted to the new version.

### 4. Consider Internal Shared Dependencies

In addition to external dependencies, many systems rely on **internal company libraries or shared modules**.

These may include:

* Shared backend libraries
* Common frontend components
* Internal SDKs
* Platform or infrastructure libraries

Framework upgrades can require **updates to these internal dependencies as well**. If the internal libraries are not yet compatible with the new framework version, upgrading too early can introduce integration issues.

Planning upgrades through roadmap tasks allows teams to **coordinate updates across services and shared libraries**.

### 5. Avoid Migrating to the Initial Release

The first release of a new **major or minor version** often contains issues that are discovered only after broader community adoption. In addition, ecosystem and internal dependencies may not yet be fully compatible.

Therefore, the recommended policy is:

* **Do not migrate to `x.y.0` or `x.0.0`.**
* Wait until **at least the 3rd or 4th patch release** before upgrading.

Example:

| Version       | Recommendation             |
| ------------- | -------------------------- |
| 3.2.0         | ❌ Do not upgrade           |
| 3.2.1         | ❌ Avoid                    |
| 3.2.2         | ⚠ Evaluate cautiously      |
| 3.2.3 / 3.2.4 | ✅ Preferred upgrade window |

Waiting several patch releases allows:

* early defects to be fixed
* ecosystem dependencies to catch up
* internal libraries to add compatibility
* migration guidance to stabilize

### 6. Recommended Upgrade Cadence

Based on the release cadence of major frameworks such as Angular and Spring Boot, it is recommended to **perform framework upgrades at least once per year**.

Typical release cadence:

* Angular publishes a **major release approximately every 6 months**
* Spring Boot publishes **minor releases approximately every 6 months**

Because of this cadence, it is not necessary to upgrade to every released version. Instead, upgrades may **intentionally skip versions** while still staying reasonably current.

Example upgrade paths:

| Framework   | Example Upgrade |
| ----------- | --------------- |
| Spring Boot | 3.2 → 3.4       |
| Spring Boot | 3.3 → 3.5       |
| Angular     | 16 → 18         |
| Angular     | 17 → 19         |

Skipping intermediate versions reduces upgrade overhead while still keeping systems within a modern and supported range.

To avoid difficult migrations, teams should also **avoid skipping too many framework generations**. As a general guideline, upgrades should **not skip more than two major framework versions**.

### 7. Runtime and Platform Alignment

Framework upgrades often require upgrading the underlying **runtime environments** and platform tooling.

Examples include:

* Java runtime upgrades required by Spring Boot versions
* Node.js upgrades required by Angular tooling
* Build tool or plugin upgrades

When planning framework upgrades, teams should also evaluate compatibility with:

* Java runtime versions
* Node.js versions
* build tooling
* CI/CD pipelines
* container base images

These runtime upgrades should ideally be **planned and executed together with framework upgrades** to ensure platform consistency.

### 8. Focus on Regularity and Alignment

The most important goal is **regular and predictable upgrades**, rather than upgrading to every available release.

Upgrades should be:

* **Performed at least once per year**
* **Coordinated across teams**
* **Aligned across services and applications within the company**

This alignment is important because framework upgrades often affect:

* shared internal libraries
* platform tooling
* build pipelines
* infrastructure components
* cross-service dependencies

Coordinated upgrades ensure that **internal shared libraries, services, and infrastructure dependencies remain compatible** and reduce fragmentation across the organization.

> In some organizations, a **“stable unless required” policy** is followed: framework versions are only upgraded when there is a compelling reason such as security fixes, compliance requirements, runtime updates, or new feature needs. In these cases, **no upgrades are applied simply to stay current**. This reinforces the principle that **planned, controlled upgrades** take priority over automatically chasing the latest version.

### 9. Platform Upgrade Windows

For larger organizations or multi-service environments, it is beneficial to establish **periodic platform upgrade windows**.

A platform upgrade window is a **planned timeframe during which framework upgrades are coordinated across multiple projects or teams**.

Benefits include:

* synchronized upgrades across services
* reduced version fragmentation
* easier maintenance of shared libraries
* better alignment with infrastructure and platform tooling

For example, the organization may define **one upgrade window per year** where major or minor framework upgrades are evaluated and implemented.

### 10. Modernize Frontend Code During Framework Upgrades

For frontend frameworks in particular, version upgrades provide a good opportunity to **modernize existing code and adopt new framework capabilities**.

Many projects focus only on upgrading the framework version while keeping legacy patterns in place. Over time this leads to outdated architecture and prevents teams from benefiting from improvements introduced by newer framework versions.

When upgrading frontend frameworks, teams should consider:

* migrating to recommended framework patterns
* replacing deprecated APIs
* adopting new architectural features
* simplifying legacy workarounds

Combining **version upgrades with gradual code modernization** helps prevent long-term technical debt and allows teams to fully benefit from the evolution of the framework.

### 11. Security Exception Policy

While major and minor upgrades are normally scheduled through roadmap planning, **security vulnerabilities may require faster upgrades**.

If a critical security issue is fixed only in a newer major or minor version, teams may need to **upgrade earlier than the planned upgrade window**.

In such cases:

* the upgrade should still be reviewed and tested
* the security risk should be evaluated
* the upgrade may be prioritized outside the normal roadmap schedule

Security considerations should always take precedence over strict upgrade policies.

### 12. Framework End-of-Life Awareness

Framework versions eventually reach **end-of-life (EOL)** when they no longer receive bug fixes or security updates.

Running production systems on unsupported framework versions introduces security and operational risks.

Teams should therefore:

* monitor framework support timelines
* plan upgrades **before the current version reaches EOL**
* ensure services remain within supported framework versions

Upgrade planning should consider vendor support policies and lifecycle timelines.

### 13. Perform Upgrade Assessment or Spike Tasks

Before implementing a major or minor framework upgrade, it is often beneficial to perform a **short technical assessment (spike)**.

This task typically includes:

* reviewing official migration guides
* identifying breaking changes
* verifying runtime requirements
* checking compatibility of major dependencies
* estimating upgrade effort

The output of the spike helps teams **plan realistic roadmap tasks and reduce implementation risk**.

### 14. Security-Constrained Environments

In organizations with **strict security governance** (for example financial institutions or highly regulated environments), release processes may be subject to additional restrictions such as:

* mandatory security reviews
* restricted production release windows
* compliance validation
* change management approvals
* automated **vulnerability scanning in CI/CD pipelines** that may block merging or deployment if known issues are detected

These constraints may **delay or temporarily block the deployment of framework upgrades**, even though the framework version is already released by the vendor.

The upgrade strategy should be **adapted accordingly**:

* upgrade cadence may be slower due to governance and security validation
* upgrade work should still be **planned and tracked through roadmap tasks**
* migration preparation tasks should be scheduled in advance to ensure readiness when release windows and security scans allow deployment

Even when upgrades cannot be released immediately, maintaining **active upgrade planning and migration preparation** helps prevent large version gaps and reduces future upgrade risks.

### 15. Execute Upgrades in a Controlled Scope

Major and minor upgrades should:

* Be implemented in a **dedicated branch**
* Be validated through **automated tests and CI pipelines**
* Include regression and integration testing
* Be carefully reviewed before merging

If the framework is used across multiple services or modules, upgrades should ideally be **coordinated to maintain version consistency**.

### 16. Developer Experience and Resource Availability

Staying on outdated framework versions for too long can **reduce developer motivation** and make it **harder to find or retain talent**, especially for frontend or widely used frameworks.

Regularly scheduled upgrades ensure that the team works with **modern tooling, APIs, and best practices**, making the project more attractive to current and future developers.

This also reduces the ramp-up time for new hires and ensures the team is familiar with supported frameworks.

### 17. AI-Assisted Development Considerations

AI-assisted development tools (e.g., Claude, Copilot, ChatGPT) can **significantly speed up development** and refactoring. However:

* AI suggestions are **more accurate and reliable** when the codebase uses **modern, supported framework versions**.
* Outdated frameworks may result in AI-generated code that uses **unsupported APIs** or **deprecated patterns**, requiring additional review.
* Consistent, up-to-date frameworks across projects improve **AI-assisted productivity** and reduce cognitive overhead for developers.

Integrating framework upgrades with AI-assisted workflows ensures teams can **fully leverage modern AI tools** while maintaining stability.

## Benefits of This Approach

* Reduces risk from breaking or behavioral changes
* Prevents disruptive automated upgrades
* Allows time for ecosystem and internal dependencies to stabilize
* Ensures upgrades are properly tested and coordinated
* Enables teams to plan migration work effectively
* Maintains alignment between framework versions, runtimes, and internal libraries
* Encourages gradual modernization of application architecture
* Supports proactive security and lifecycle management
* Integrates security validation and CI/CD pipeline constraints into upgrade planning
* Maintains developer motivation and improves resource availability
* Enhances AI-assisted development effectiveness

## Summary

**Major and minor framework upgrades are strategic changes**, not routine dependency updates. By planning them through roadmap tasks, considering both external and internal dependencies, waiting for several patch releases before adoption, coordinating runtime upgrades, modernizing code where appropriate, integrating security validation steps, maintaining developer motivation, leveraging AI-assisted development, and performing upgrades regularly (at least once per year), the organization can safely introduce new framework versions while minimizing operational risk.
