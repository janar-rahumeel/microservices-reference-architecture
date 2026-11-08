# Container Image Optimization

The container image build employs several optimization techniques to reduce image size, improve build and deployment efficiency, minimize the runtime attack surface and simplify the production execution environment.

The following mechanisms address different aspects of container image optimization:

- **Spring Boot layered packaging** – improves Docker layer caching and reduces the amount of data rebuilt and distributed when application code changes
- **Distroless runtime images** – minimize the operating system components included in the production image, reducing the attack surface and runtime footprint
- **Custom Java runtime using jlink** – includes only the Java platform modules required by the application, excluding unnecessary runtime components and development tools

## Spring Boot Layered Packaging

Spring Boot executable JARs can be extracted into separate layers based on component type and expected change frequency.

| Layer | Contents | Change frequency |
|---|---|---|
| `dependencies` | Released third-party dependencies | Low |
| `spring-boot-loader` | Spring Boot launcher | Low |
| `snapshot-dependencies` | Snapshot dependencies | Medium |
| `application` | Application classes and resources | High |

Each layer is copied independently into the container image, allowing Docker to reuse unchanged layers across builds.

**Benefits:**
- Improved build cache utilization
- Reduced incremental build time
- Reduced image push and pull overhead when only application code changes
- Independent caching of application and dependency components

Layering does not inherently reduce the total image size. Its primary purpose is to improve incremental build and distribution efficiency.

## Distroless Runtime Images

Distroless images provide a minimal runtime environment containing only the operating system components required to execute the application.

They exclude general-purpose utilities, package managers and interactive shells that are unnecessary for production application execution.

**Benefits:**
- Reduced runtime attack surface
- Smaller container image footprint
- Non-root execution
- Reduced number of unnecessary operating system components requiring maintenance

**Operational considerations:**
- No interactive shell or standard administrative utilities
- Troubleshooting relies on application logs, health endpoints, metrics and tracing
- Dedicated debugging containers may be required for runtime inspection

## Custom Java Runtime Using jlink

The JDK provides the `jlink` utility for generating custom Java runtimes containing only explicitly selected Java platform modules and their required dependencies.

The custom runtime is generated during the image build and copied into the final runtime image. This ensures that the complete JDK and its development tools are excluded from the production image.

**Benefits:**
- Reduced Java runtime footprint
- Exclusion of unnecessary Java modules and development tools
- Explicit control over runtime module composition
- Reduced amount of runtime content distributed with the application
- Potentially more than 30% reduction in total container image size compared with using a standard Java runtime image, depending on the application, selected modules and base image

**Operational considerations:**
- The selected module set must cover all application and transitive dependency requirements
- Runtime compatibility must be verified through application startup and integration tests
- Java security updates require regenerating the custom runtime
- Module selection and runtime maintenance introduce additional build complexity

## Optimization Trade-offs

| Optimization | Primary objective | Main consideration |
|---|---|---|
| Spring Boot layering | Build and deployment efficiency | Depends on dependency stability and change frequency |
| Distroless | Runtime security and footprint | Reduced debugging capabilities |
| jlink | Java runtime footprint | Additional runtime maintenance and validation |

An alternative to maintaining a custom Java runtime is using a Distroless image that already includes a Java runtime. This simplifies the build process and removes the need for custom module selection, at the cost of reduced control over runtime composition.

## Measuring Effectiveness

The effectiveness of these optimizations should be evaluated using measurable results.

Relevant metrics include:

- Compressed and uncompressed image size
- Individual image layer sizes
- Full and incremental build duration
- Image push and pull duration
- Application startup time
- Runtime vulnerability findings

The objective is to balance image size, build efficiency, runtime security and maintainability rather than optimize any single metric independently.