# Backend Convention

This document defines backend code rules for the Algoga backend project.

## Absolute Rules

- Keep hexagonal architecture.
- Do not break dependency direction between layers.
- Follow the existing folder structure and naming style.
- When adding a new domain, start from the `example` domain pattern if it exists in the codebase.
- Do not use emoji in code or comments.
- Do not pollute diffs with unrelated formatting, import sorting, or whitespace-only changes.

## Tech Stack

- Java 17
- Spring Boot 3.5.14
- Spring Data JPA
- MySQL with `com.mysql:mysql-connector-j`
- MapStruct 1.5.5 for Domain <-> JPA Entity mapping
- Lombok with lombok-mapstruct-binding
- Spring Security + OAuth2 Client
- JWT with jjwt 0.12.6
- Redis, Redisson
- Bucket4j for rate limiting
- Resilience4j for circuit breaker behavior
- springdoc-openapi for Swagger
- Actuator + Micrometer/Prometheus
- Spring AI with OpenAI-compatible/Groq config and Ollama/vector related dependencies
- WebSocket
- Mail
- S3-compatible object storage
- PDF/document libraries such as PDFBox, Apache POI, and iText

## Runtime Ports

- Backend server port: `15000`
- Frontend local dev server port expected by backend config: `17000`
- Local frontend default URL: `http://localhost:17000`
- Local Swagger UI is expected at `http://localhost:15000/swagger-ui/index.html` when the app runs.

## Base Package

```text
com.kidmily.algoga_server
```

Each domain should live as an independent package under the base package, for example:

```text
admin
user
lms
payment
example
```

Shared behavior belongs under `global`.

## Current Top-Level Domain Packages

The repository currently contains these top-level packages under the base package:

```text
accommodation
admin
banner
benefit
blacklist
booking
calendar
chat
chatbot
community
course
example
flight
friend
global
inquiry
lms
notice
notification
packages
payment
refund
report
stats
user
```

## Standard Domain Structure

```text
{domain}/
  presentation/
    api/
      request/
      response/
    advice/
  application/
    usecase/
    service/
    command/
    result/
    port/
    event/
    scheduler/
    policy/
  domain/
    model/
    repository/
    event/
  infrastructure/
    persistence/
    mapper/
    redis/
    s3/
    document/
    event/
  exception/
  settings/
  config/
```

Create optional folders only when the feature needs them. Do not leave empty folders just to match the full template.

Required layers for normal domains:

- `presentation`
- `application`
- `domain`
- `infrastructure`
- `exception`

## Dependency Direction

```text
presentation  ->  application  ->  domain
                        ^
infrastructure  --------+
```

- Dependencies must point inward toward `domain`.
- `domain` must not reference Spring, JPA, MapStruct, or outer layers.
- `application` depends on domain models and ports, not infrastructure implementations.
- `infrastructure` implements ports defined by `application` or `domain`.

## Controller Rules

- Use `@RestController`, `@RequestMapping("/api/{plural}")`, and `@RequiredArgsConstructor`.
- Inject only use case interfaces, not service implementation classes.
- Use Swagger annotations such as `@Tag`, `@Operation`, and project error-code annotations.
- Receive request DTOs with `@Valid @RequestBody`.
- Convert request DTOs to command records immediately.
- Return `ResponseEntity<ApiResponse<T>>`.
- Keep business logic out of controllers.

## DTO Rules

- Use Java `record` for request and response DTOs.
- Use `@Schema` for descriptions and examples.
- Use `jakarta.validation` annotations for request validation.
- Do not put HTTP, Swagger, or framework concerns inside application command records.

## Use Case Rules

- Define inbound ports as interfaces under `application/usecase`.
- Methods receive command records and return IDs, result DTOs, or domain-level results as needed.

## Service Rules

- Use `@Service`, `@Transactional`, and `@RequiredArgsConstructor`.
- Implement use case interfaces.
- Use `@Transactional(readOnly = true)` for read-only methods.
- Typical flow:
  1. Validate input.
  2. Load or create domain model.
  3. Change state through domain methods.
  4. Save through repository ports.
- Throw business exceptions using `BusinessException(domainErrorCode)` for new code.

## Domain Model Rules

- Keep domain models as pure POJOs.
- Do not use JPA annotations in domain models.
- Use `@Getter` and `@NoArgsConstructor(access = AccessLevel.PROTECTED)` where the existing style does.
- Keep constructors private when creation must go through factories.
- Use static factories:
  - `create(...)` for new objects with invariant validation.
  - `reconstitute(...)` for restoring persisted data.
- Do not expose setters.
- Change state through meaningful methods such as `changeName` or `deactivate`.
- Keep invariant checks inside private validation methods.

## Repository Port Rules

- Define repository ports as interfaces.
- Use domain model types, not JPA entity types.
- Return `Optional`, `List`, or domain-specific results as needed.

## Persistence Rules

- JPA entity naming: `{Domain}JpaEntity`.
- Use `@Entity`, `@Table`, `@Getter`, and protected no-args constructors.
- Use `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` for generated primary keys unless existing code says otherwise.
- Define column constraints with `@Column`.
- Keep JPA entities separate from domain models.
- Spring Data repository naming: `SpringData{Domain}Repository`.
- Adapter naming: `{Domain}RepositoryAdapter`.
- Repository adapters implement domain/application repository ports.
- Use MapStruct mappers to convert between JPA entities and domain models.

## Mapper Rules

- Use `@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)`.
- `toJpaEntity(domain)` maps domain to JPA entity.
- `toDomain(entity)` maps JPA entity to domain.
- If the domain constructor is closed, call `reconstitute(...)` in a default mapper method.
- Handle null defensively where needed.

## Exception Rules

- Domain error code naming: `{Domain}ErrorCode`.
- Error code enums implement `BaseErrorCode`.
- Fields:
  - `HttpStatus status`
  - `String code`
  - `String message`
- Code format should use a domain abbreviation and number, such as `EX_001`.
- Use `BusinessException` with `BaseErrorCode` for new code unless a legacy domain requires its own exception type.
- Domain-specific exception advice belongs under `presentation/advice`.
- Limit advice scope to the domain controller package.

## Global Reuse

Check `global` before creating new infrastructure for:

- API response wrapper
- business exceptions
- error code contracts
- common exception advice
- Swagger error-code annotations
- mail
- S3
- events
- locks
- cache
- rate limiting
- circuit breaker behavior

## New Feature Checklist

1. Decide which domain owns the feature.
2. If a new domain is needed, follow the existing `example` pattern if present.
3. Model business rules in `domain/model` first.
4. Define repository ports.
5. Define use case interfaces.
6. Implement application services.
7. Add persistence entity, Spring Data repository, adapter, and mapper.
8. Add controller and request/response records.
9. Add or update error codes.
10. Add Swagger metadata.
11. Verify dependency direction and folder structure.
12. Run relevant tests or build commands.
