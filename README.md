##📄 URL Shortener Service
A lightweight, production-style URL Shortener service built with Java, Spring Boot, JPA, and an H2 in-memory database.
This service follows clean, layered architecture principles and demonstrates URL creation, redirect resolution, expiration management, and link analytics.

### 🧱 1. System Overview
The system provides:
- Short URL creation (custom alias or auto-generated)
- Redirect resolution using `/r/{shortCode}`
- Expiration rules 
- Basic analytics:
  - click count 
  - last accessed timestamp 
  - status (ACTIVE / EXPIRED)
- Input validation 
- Structured error handling

### 🏗 2. Architecture & Design Rationale
This project follows a 3-layered architecture:
```
API Layer (UrlActivity)
↓
Service Layer (UrlManager)
↓
Repository Layer (UrlRepository)
↓
Database (H2)
```

Why this architecture? (Senior-level justification)

| Layer                | Responsibility                                                   | Notes                                         |
| -------------------- | ---------------------------------------------------------------- | --------------------------------------------- |
| **API Layer**        | HTTP mapping, serialization, error translation                   | No business logic. Returns proper HTTP codes. |
| **Service Layer**    | Core business rules, validation, expiration logic, stats updates | Pure logic. Testable without web/db.          |
| **Repository Layer** | Data persistence, uniqueness constraints                         | Implementation hidden behind an interface.    |

This separation ensures:
- Good unit testability (service tests without Spring)
- Clear ownership boundaries 
- Easy migration to different datastores 
- Low coupling between HTTP and domain logic

### 📊 3. Domain Model

#### URL Entity

| Field            | Type          | Description                                    |
| ---------------- | ------------- | ---------------------------------------------- |
| `id`             | Long          | Auto-generated primary key                     |
| `targetUrl`      | String        | The full URL to redirect to                    |
| `customAlias`    | String        | Unique short code (generated or user-provided) |
| `createdAt`      | LocalDateTime | Timestamp of link creation                     |
| `expiresAt`      | LocalDateTime | Optional expiration date                       |
| `clickCount`     | int           | Number of successful redirects                 |
| `lastAccessedAt` | LocalDateTime | Timestamp of last redirect                     |
| `status`         | derived       | ACTIVE or EXPIRED                              |

Domain Behavior
Encapsulated inside the entity:
- isExpired()
- incrementClicks()
- touchLastAccessedTime()

This keeps business rules close to the data they govern.

### 🗄 4. Data Model & Constraints
Table: url
```
id                BIGINT AUTO_INCREMENT PRIMARY KEY
custom_alias      VARCHAR(255) UNIQUE NOT NULL
target_url        VARCHAR(2000) NOT NULL
created_at        TIMESTAMP NOT NULL
expires_at        TIMESTAMP NULL
click_count       INT DEFAULT 0
last_accessed_at  TIMESTAMP NULL

```

Key Constraints
- custom_alias is unique
- target_url is required and max ~2k chars 
- expiration must be >= creation time 
- click count is non-negative


### 🔐 5. Validation Rules

| Field              | Rules                                                           |
| ------------------ | --------------------------------------------------------------- |
| `targetUrl`        | Must be a valid URL (`new URL().toURI()`)                       |
| `customAlias`      | Optional; alphanumeric + `_` `-`; max length 50; must be unique |
| `expiresAt`        | Optional; must not be in the past                               |
| Default expiration | 1 year from creation if omitted                                 |

Input violating these constraints results in a 400 error.


### ⚠ 6. Error Model

All errors follow a consistent structure:
```
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable explanation"
  }
}
```

Error Codes Returned:

| Scenario             | HTTP | Code             |
| -------------------- | ---- | ---------------- |
| Invalid input        | 400  | `INVALID_INPUT`  |
| Alias already exists | 409  | `ALIAS_CONFLICT` |
| Short code not found | 404  | `NOT_FOUND`      |
| URL expired          | 410  | `EXPIRED_URL`    |
| Internal error       | 500  | `INTERNAL_ERROR` |

### ⚙️ 7. Concurrency & Uniqueness
In this implementation:
- H2 + JPA @Column(unique = true) ensures alias uniqueness. 
- Conflicts trigger DataIntegrityViolationException, mapped to 409.

In real systems:
- Distributed services rely on DB uniqueness constraints, not in-memory checks. 
- UUID substring generation gives extremely low collision probability, but a retry loop would be added in scaled systems.

### 🔁 8. Redirect Logic
Redirect flow:
- Look up alias 
- If not found → 404 
- If expired → 410 
- Update:
  - clickCount++ 
  - lastAccessedAt = now 
  - Persist changes 
  - Return HTTP 302 redirect 
Only successful redirects increment clicks.

### 📈 9. API Documentation
#### ✳️ Create Short URL
POST /links
Request
```
{
  "targetUrl": "https://google.com",
  "customAlias": "mario-long",
  "expiresAt": "2025-12-01T23:59:59"
}
```
Successful Response (201)
```
{
  "id": 1,
  "targetUrl": "https://google.com",
  "shortCode": "mario-long",
  "shortUrl": "http://localhost:8080/r/mario-long",
  "createdAt": "2025-11-29T12:05:12",
  "expiresAt": "2025-12-01T23:59:59"
}
```
Error Examples
- 409 ALIAS_CONFLICT
```
{"error": {"code": "ALIAS_CONFLICT", "message": "customAlias already exists"}}
```

#### ✳️ Redirect Short URL
GET /r/{shortCode}
- Redirects (302) → target URL 
- 404 → unknown alias 
- 410 → expired

#### ✳️ Get URL Details
GET /links/{shortCode}
Response
```
{
  "shortCode": "mario-long",
  "shortUrl": "http://localhost:8080/r/mario-long",
  "targetUrl": "https://google.com",
  "createdAt": "2025-11-29T12:05:12",
  "expiresAt": "2025-12-01T23:59:59",
  "clickCount": 42,
  "lastAccessedAt": "2025-11-29T21:01:10",
  "status": "ACTIVE"
}
```

### 🧪 10. Testing Strategy
Unit Tests
- Service-layer tests 
- Validation logic 
- Expiration logic 
- Stats increment behavior

Integration Tests:
- Full controller endpoints using Spring Boot Test 
- Alias conflict handling 
- Redirect behavior
All tests run via:
```mvn test```

###🚀 11. Running the Application
Clone & run
```
git clone https://github.com/YOUR_USER/url-shortener-service.git
cd url-shortener-service
mvn spring-boot:run
```

H2 Console
```
http://localhost:8080/h2-console
```

JDBC URL: jdbc:h2:mem:testdb

### 🔮 12. Future Enhancements (Technical Roadmap)
Production-grade improvements
- Switch to PostgreSQL / DynamoDB 
- Add caching layer (Redis) for hot URLs 
- Base62 short code generation (much shorter links)
- Rate limiting to prevent abuse 
- Async click logging for performance 
- Background cleanup job for expired URLs 
- User accounts / per-user link management 
- Analytics dashboards 
- Service SLOs + Observability (Prometheus/Grafana)

Horizontal scaling
- Stateless Spring Boot nodes behind a load balancer 
- Use Redis or SQL for persistent short-code lookup 
- Shard short codes by prefix for massive scale