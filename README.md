# Stock Management — Backend API

A multi-tenant SaaS REST API for stock and inventory management, built with Spring Boot 3 and Java 21.

---

## Table of Contents

- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Database](#database)
- [Security](#security)
- [API Reference](#api-reference)
- [Running Locally](#running-locally)
- [CI/CD](#cicd)

---

## Technologies

| Category | Technology | Version |
| --- | --- | --- |
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.9 |
| Build Tool | Maven | (wrapper included) |
| Database | PostgreSQL | 17.5 |
| ORM | Spring Data JPA / Hibernate | — |
| Migrations | Flyway | 1.19.0 |
| Security | Spring Security + JJWT (RSA-256) | 0.12.3 |
| API Docs | SpringDoc OpenAPI / Swagger UI | 2.6.0 |
| Validation | Spring Validation | — |
| Code Generation | Lombok | — |
| Monitoring | Spring Boot Actuator | — |
| Testing | JUnit 5, Mockito, TestContainers | — |
| Test Database | H2 (in-memory) | — |
| Containerization | Docker / Docker Compose | — |

---

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/kte/backend/
│   │   │   ├── BackendApplication.java          # Entry point
│   │   │   ├── common/
│   │   │   │   └── PageReponse.java             # Generic pagination wrapper
│   │   │   ├── config/
│   │   │   │   ├── BeansConfigs.java            # AuthenticationManager, BCrypt bean
│   │   │   │   ├── CacheConfig.java
│   │   │   │   ├── JpaAuditingConfig.java       # Auditor provider
│   │   │   │   ├── SecurityConfig.java          # JWT filter chain, CORS, route protection
│   │   │   │   ├── SwaggerConfig.java           # OpenAPI customization
│   │   │   │   └── tenantConfig/
│   │   │   │       ├── CurrentTenantIdentifierResolverImpl.java
│   │   │   │       ├── MultiTenantConnectionProviderImpl.java
│   │   │   │       ├── TenantContext.java       # ThreadLocal tenant/schema holder
│   │   │   │       ├── TenantHibernateFilter.java
│   │   │   │       └── TenantSchemaResolver.java
│   │   │   ├── controllers/
│   │   │   │   ├── AuthenticationController.java
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CategorieController.java
│   │   │   │   ├── StockMvtController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── TenantController.java
│   │   │   │   └── uicontrollers/              # Controller interface contracts
│   │   │   ├── dto/
│   │   │   │   ├── ErrorDto.java
│   │   │   │   ├── requests/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── RegisterTenantRequest.java
│   │   │   │   │   ├── ProductRequest.java
│   │   │   │   │   ├── CategoryRequest.java
│   │   │   │   │   ├── StockMvtRequest.java
│   │   │   │   │   └── UserRequest.java
│   │   │   │   └── responses/
│   │   │   │       ├── LoginReponse.java
│   │   │   │       ├── ProductResponse.java
│   │   │   │       ├── CategoryResponse.java
│   │   │   │       ├── StockMvtResponse.java
│   │   │   │       ├── UserResponse.java
│   │   │   │       └── TenantResponse.java
│   │   │   ├── entities/
│   │   │   │   ├── AbstractEntity.java         # UUID id, timestamps, soft-delete flag
│   │   │   │   ├── User.java                   # Implements UserDetails
│   │   │   │   ├── Product.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── StockMvt.java
│   │   │   │   └── Tenant.java
│   │   │   ├── enums/
│   │   │   │   ├── UserRole.java               # PLATFORM_ADMIN, COMPAGNY_ADMIN, USER, SALES_OPERATOR
│   │   │   │   ├── TenantStatus.java           # PENDING, ACTIVE, SUSPENDED, INACTIVE
│   │   │   │   └── TypeMvt.java                # IN, OUT
│   │   │   ├── exceptions/
│   │   │   │   ├── DuplicateEntityException.java
│   │   │   │   ├── InvalidRequestException.java
│   │   │   │   ├── TenantProvisioningException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   └── handler/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── mappers/
│   │   │   │   ├── ProductMapper.java
│   │   │   │   ├── CategoryMapper.java
│   │   │   │   ├── StockMvtMapper.java
│   │   │   │   ├── UserMapper.java
│   │   │   │   └── TenantMapper.java
│   │   │   ├── properties/
│   │   │   │   └── JwtProperties.java
│   │   │   ├── repositories/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── StockMvtRepository.java
│   │   │   │   └── TenantRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenService.java        # RSA-256 token generation & validation
│   │   │   │   └── JwtAuthentificationFilter.java # JWT request interceptor
│   │   │   ├── services/
│   │   │   │   ├── CrudServices.java           # Generic CRUD interface
│   │   │   │   ├── auth/
│   │   │   │   │   ├── AuthenticationService.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   └── impl/
│   │   │   │   │       ├── AuthenticationServiceImpl.java
│   │   │   │   │       └── UserServiceImpl.java
│   │   │   │   ├── catalog/
│   │   │   │   │   ├── CategoryService.java
│   │   │   │   │   ├── ProductService.java
│   │   │   │   │   └── impl/
│   │   │   │   │       ├── CategoryServiceImpl.java
│   │   │   │   │       └── ProductServiceImpl.java
│   │   │   │   ├── stock/
│   │   │   │   │   ├── StockMvtService.java
│   │   │   │   │   └── impl/
│   │   │   │   │       └── StockMvtServiceImpl.java
│   │   │   │   └── tenant/
│   │   │   │       ├── ProvisioningService.java
│   │   │   │       ├── TenantService.java
│   │   │   │       └── impl/
│   │   │   │           ├── ProvisioningServiceImpl.java
│   │   │   │           └── TenantServiceImpl.java
│   │   │   └── utils/
│   │   │       └── NameUtils.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── certs/
│   │       │   ├── private_key.pem
│   │       │   └── public_key.pem
│   │       └── db/migration/
│   │           ├── common/
│   │           │   └── V1__init_Common_Tables.sql
│   │           └── tenant/
│   │               └── V1__Init_DB_FOR_Tenant.sql
│   └── test/
├── .github/workflows/
│   ├── ci.yml
│   └── backend_workflow.yml
├── docker-compose.yml
├── pom.xml
├── mvnw
└── mvnw.cmd
```

---

## Architecture Overview

The application follows a **layered architecture** with clear separation of concerns:

```
HTTP Request
     │
     ▼
JwtAuthentificationFilter   ← extracts JWT, sets TenantContext + SecurityContext
     │
     ▼
Controller                  ← input validation, delegates to service
     │
     ▼
Service (interface + impl)  ← business logic
     │
     ▼
Repository (Spring Data JPA) ← data access, tenant-scoped queries
     │
     ▼
PostgreSQL (schema per tenant)
```

**Multi-tenancy strategy:** schema-per-tenant. The JWT claims include a `tenantId` which is extracted by the filter and stored in a `ThreadLocal` (`TenantContext`). Hibernate's `MultiTenantConnectionProvider` routes queries to the correct PostgreSQL schema. Flyway applies tenant-specific migrations when a new tenant is provisioned.

---

## Database

### Common schema (`public`)

| Table | Description |
| --- | --- |
| `tenants` | Tenant registry with company info and lifecycle status |
| `users` | Platform and company user accounts (linked to a tenant) |

### Tenant schema (one per tenant)

| Table | Description |
| --- | --- |
| `categories` | Product categories |
| `products` | Products with reference, price, alert threshold |
| `stock_mvts` | Stock movement records (IN / OUT) |

**Soft delete** is enabled on all entities via a `deleted` boolean column. All base entities inherit UUID primary keys, `created_at`, and `updated_at` timestamps from `AbstractEntity`.

**Connection pool:** HikariCP — max 10 connections, min idle 5.

---

## Security

| Mechanism | Details |
| --- | --- |
| Authentication | JWT (RS256 — RSA asymmetric keys) |
| Token lifetime | ~24 hours (86 800 000 ms, configurable) |
| Password encoding | BCrypt |
| Authorization | Role-based (`@PreAuthorize`), method-level |
| Sessions | Stateless (no server-side session) |
| CSRF | Disabled (stateless API) |
| CORS | Enabled — all origins, standard HTTP methods |

### Roles

| Role | Scope |
| --- | --- |
| `ROLE_PLATFORM_ADMIN` | Full platform access |
| `ROLE_COMPAGNY_ADMIN` | Manage users and data within own tenant |
| `ROLE_SALES_OPERATOR` | Operational access within own tenant |
| `ROLE_USER` | Read-only or limited access |

### Public endpoints (no token required)

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/tenants/approve/{id}`
- `PATCH /api/v1/tenants/activate/{id}`
- Swagger UI and API docs

---

## API Reference

**Base URL:** `http://localhost:8080/api/v1`  
**Content-Type:** `application/json`  
**Authorization:** `Bearer <JWT>` (all protected routes)

All list endpoints return a paginated response:
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

---

### Authentication

#### `POST /auth/login`

Login with username and password. Returns a JWT access token.

**Request body**
```json
{
  "username": "john.doe",
  "password": "secret123"
}
```

**Response `200`**
```json
{
  "accessToken": "<JWT>",
  "tokenType": "Bearer"
}
```

---

#### `POST /auth/register`

Register a new company (tenant). Creates the tenant record and its admin user. The tenant starts in `PENDING` status and must be approved.

**Request body**
```json
{
  "compagnyName": "Acme Corp",
  "compagnyCode": "ACME",
  "email": "contact@acme.com",
  "adminFullName": "Jane Doe",
  "adminEmail": "jane@acme.com",
  "adminUserName": "jane.doe",
  "adminPassword": "securepass"
}
```

**Response `201`**

---

### Products

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/products` | JWT | List products (paginated) |
| `GET` | `/products/{id}` | JWT | Get product by ID |
| `POST` | `/products` | JWT | Create product |
| `PUT` | `/products/{id}` | JWT | Update product |
| `DELETE` | `/products/{id}` | JWT | Soft-delete product |

**Query params (list):** `page` (default `0`), `size` (default `10`)

**ProductRequest**
```json
{
  "name": "Wireless Mouse",
  "reference": "PROD-001",
  "description": "Ergonomic wireless mouse with USB receiver",
  "alertThreshold": 10,
  "price": 29.99,
  "categoryId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```
> Fields: `name` (2–150 chars), `reference` (2–50 chars, unique), `description` (max 1000 chars), `alertThreshold` and `price` must be positive.

**ProductResponse**
```json
{
  "name": "string",
  "reference": "string",
  "description": "string",
  "alertThreshold": 10,
  "price": 29.99,
  "categoryName": "string",
  "availableQuantity": 150
}
```

---

### Categories

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/categories` | JWT | List categories (paginated) |
| `GET` | `/categories/{id}` | JWT | Get category by ID |
| `POST` | `/categories` | JWT | Create category |
| `PUT` | `/categories/{id}` | JWT | Update category |
| `DELETE` | `/categories/{id}` | JWT | Soft-delete category |

**CategoryRequest**
```json
{
  "name": "string (2–100 chars, required)",
  "description": "string (max 500 chars, optional)"
}
```

**CategoryResponse**
```json
{
  "name": "string",
  "description": "string",
  "nbProducts": 12
}
```

---

### Stock Movements

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/stock-mvts` | JWT | List movements (paginated) |
| `GET` | `/stock-mvts/{id}` | JWT | Get movement by ID |
| `POST` | `/stock-mvts` | JWT | Record a movement |
| `PUT` | `/stock-mvts/{id}` | JWT | Update a movement |
| `DELETE` | `/stock-mvts/{id}` | JWT | Soft-delete a movement |

**StockMvtRequest**
```json
{
  "quantity": "integer > 0 (required)",
  "typeMvt": "IN | OUT (required)",
  "mvtDate": "ISO-8601 datetime, past or present (required)",
  "comment": "string (optional)",
  "productId": "UUID (required)"
}
```

**StockMvtResponse**
```json
{
  "quantity": 50,
  "typeMvt": "IN",
  "mvtDate": "2025-06-01T10:00:00",
  "comment": "Initial stock"
}
```

---

### Users

| Method | Path | Required Role | Description |
| --- | --- | --- | --- |
| `GET` | `/users` | COMPAGNY_ADMIN, PLATFORM_ADMIN | List users (paginated) |
| `GET` | `/users/{id}` | COMPAGNY_ADMIN, PLATFORM_ADMIN | Get user by ID |
| `POST` | `/users` | COMPAGNY_ADMIN | Create user |
| `PUT` | `/users/{id}` | COMPAGNY_ADMIN | Update user |
| `DELETE` | `/users/{id}` | COMPAGNY_ADMIN | Soft-delete user |
| `PUT` | `/users/{id}/enable` | COMPAGNY_ADMIN | Enable user account |
| `PUT` | `/users/{id}/disable` | COMPAGNY_ADMIN | Disable user account |

**UserRequest**
```json
{
  "username": "string (required)",
  "email": "string (required)",
  "password": "string (min 8 chars, required)",
  "firstName": "string (required)",
  "lastName": "string (required)",
  "role": "ROLE_COMPAGNY_ADMIN | ROLE_USER | ROLE_SALES_OPERATOR (required)"
}
```

**UserResponse**
```json
{
  "id": "UUID",
  "username": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "ROLE_USER"
}
```

---

### Tenants

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/tenants` | JWT | List tenants (paginated) |
| `POST` | `/tenants/approve/{id}` | Public | Approve a pending tenant |
| `PATCH` | `/tenants/activate/{id}` | Public | Activate a tenant |
| `PATCH` | `/tenants/deactivate/{id}` | JWT | Deactivate a tenant |
| `PATCH` | `/tenants/suspend/{id}` | JWT | Suspend a tenant |

**TenantResponse**
```json
{
  "tenantId": "UUID",
  "compagnyName": "Acme Corp",
  "compagnyCode": "ACME",
  "email": "contact@acme.com",
  "adminFullName": "Jane Doe",
  "adminEmail": "jane@acme.com",
  "adminUserName": "jane.doe",
  "createdAt": "2025-01-15T09:00:00",
  "status": "ACTIVE"
}
```

**Tenant lifecycle:** `PENDING` → (approve) → `INACTIVE` → (activate) → `ACTIVE` ↔ `SUSPENDED`

---

### Error Responses

All errors follow the same structure:

```json
{
  "timestamp": "2025-06-12T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": ["name: must not be blank"]
}
```

| HTTP Status | Trigger |
| --- | --- |
| `400` | Invalid request body / validation failure |
| `401` | Missing or invalid JWT |
| `403` | Insufficient role permissions |
| `404` | Entity not found |
| `409` | Duplicate entity (unique constraint) |
| `500` | Tenant provisioning failure |

---

## Running Locally

### Prerequisites

- Java 21+
- Docker & Docker Compose

### 1. Start the database

```bash
docker-compose up -d
```

### 2. Configure environment

Copy `.env` and adjust if needed:

```
SPRING_PROFILES_ACTIVE=dev
DB_HOST=localhost
DB_PORT=5432
DB_NAME=saas-app-db
DB_USER=postgres
DB_PASSWORD=postgres
JWT_ACCESS_TOKEN_EXPIRATION=86800000
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

### 4. Access Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### 5. Run tests

```bash
./mvnw test
```

---

## CI/CD

GitHub Actions runs on every push and pull request:

| Step | Command |
| --- | --- |
| Build (skip tests) | `./mvnw -B -DskipTests package` |
| Run all tests | `./mvnw -B test` |
| Upload reports | Surefire XML reports uploaded as artifacts |

**Java version:** Temurin 21  
**JVM options:** `-Xmx2g -XX:+TieredCompilation`
