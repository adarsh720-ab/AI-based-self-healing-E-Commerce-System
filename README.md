# AI-Powered Self-Healing E-Commerce Platform

## Package Structure (Applied to Every Service)

```
com.ecommerce.{service}/
├── entity/                      ← JPA entities (@Entity classes)
│   └── dtos/
│       ├── request/             ← Request DTOs (@Valid, @NotBlank etc.)
│       └── response/            ← Response DTOs (returned to client)
├── mapper/                      ← MapStruct interfaces (entity ↔ DTO)
├── repository/                  ← JpaRepository interfaces
├── service/                     ← Service INTERFACES (contracts)
│   └── impl/                    ← Service IMPLEMENTATIONS (@Service)
└── controller/                  ← REST controllers (@RestController)
```

## Project Structure

```
ecommerce-platform/
├── pom.xml                      ← Parent pom (all versions declared here)
├── docker-compose.yml           ← Local infra: Postgres, Kafka, Redis, ES
├── scripts/init-db.sql          ← Creates all 8 databases on first run
│
├── commons/                     ← Shared library (plain JAR, not runnable)
│   └── src/.../commons/
│       ├── security/            → JwtUtil.java + JwtAuthFilter.java
│       ├── util/                → KafkaLogInterceptor.java (AOP)
│       ├── event/               → All Kafka event POJOs
│       ├── feign/               → InventoryClient + UserServiceClient
│       └── exception/           → All exceptions + GlobalExceptionHandler
│
├── auth-service/    port 8089   ← Issues + validates JWT tokens
├── user-service/    port 8081   ← User profiles, internal endpoints
├── product-service/ port 8082   ← Product catalog + Elasticsearch
├── inventory-service/ port 8085 ← Stock management + Redis
├── order-service/   port 8083   ← Orders + Kafka publishing
├── payment-service/ port 8084   ← Stripe + Kafka consumer
├── notification-service/ port 8086 ← Email/SMS Kafka consumers only
├── delivery-service/ port 8087  ← Shipment tracking + Kafka
└── api-gateway/     port 8080   ← Spring Cloud Gateway
```

---

## How to Run

### Prerequisites
- Java 21
- Maven 3.9+
- Docker + Docker Compose

### Step 1 — Start infrastructure
```bash
docker compose up -d
```
Wait ~30 seconds for Kafka and Elasticsearch to fully start.
Check everything is running:
```bash
docker compose ps
```

### Step 2 — Build commons first (all services depend on it)
```bash
# From the root ecommerce-platform/ folder:
mvn clean install -pl commons
```

### Step 3 — Start user-service FIRST
auth-service calls user-service via Feign on startup.
user-service must be running before auth-service starts.
```bash
mvn spring-boot:run -pl user-service
```
Wait for: `Started UserServiceApplication on port 8081`

### Step 4 — Start auth-service
```bash
mvn spring-boot:run -pl auth-service
```
Wait for: `Started AuthServiceApplication on port 8089`

### Step 5 — Start remaining services (any order)
```bash
mvn spring-boot:run -pl product-service
mvn spring-boot:run -pl inventory-service
mvn spring-boot:run -pl order-service
mvn spring-boot:run -pl payment-service
mvn spring-boot:run -pl notification-service
mvn spring-boot:run -pl delivery-service
```

### Step 6 — Start API Gateway last
```bash
mvn spring-boot:run -pl api-gateway
```

### Build everything at once (optional)
```bash
mvn clean install -DskipTests
```

---

## Test with Postman

### 1. Register
```
POST http://localhost:8089/api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "9999999999"
}
```

### 2. Login
```
POST http://localhost:8089/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```
Copy the `accessToken` from the response.

### 3. Get profile (protected)
```
GET http://localhost:8081/api/users/me
Authorization: Bearer <accessToken>
```

### 4. Create product
```
POST http://localhost:8082/api/products
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "name": "iPhone 15",
  "description": "Latest Apple phone",
  "price": 79999.00,
  "category": "Electronics",
  "sellerId": "<any-uuid>",
  "stockQuantity": 100
}
```

### 5. Place order
```
POST http://localhost:8083/api/orders
Authorization: Bearer <accessToken>
Content-Type: application/json

{
  "addressId": "<any-uuid>",
  "totalAmount": 79999.00,
  "items": [
    {
      "productId": "<product-id>",
      "quantity": 1,
      "unitPrice": 79999.00
    }
  ]
}
```

### 6. Logout
```
POST http://localhost:8089/api/auth/logout
Authorization: Bearer <accessToken>
```

---

## What Changed in the Restructure

### Package changes per service

| Before | After |
|--------|-------|
| `service/AuthService.java` (concrete class) | `service/AuthService.java` (interface) + `service/impl/AuthServiceImpl.java` |
| `dto/UserDtos.java` (flat) | `entity/dtos/request/` + `entity/dtos/response/` (split) |
| `service/UserService.java` (concrete) | `service/UserService.java` (interface) + `service/impl/UserServiceImpl.java` |
| `dto/ProductRequest.java` (flat) | `entity/dtos/request/CreateProductRequest.java` |
| Controllers importing from `dto.*` | Controllers importing from `entity.dtos.request.*` and `entity.dtos.response.*` |
| `@Mapper` without componentModel | `@Mapper(componentModel = "spring")` for Spring DI |

### Logic — unchanged
All business logic, Kafka flows, security config, JWT validation, Redis usage, and database queries are identical to the original. Only package locations and import paths changed.

---

## Service Ports

| Service              | Port |
|----------------------|------|
| auth-service         | 8089 |
| user-service         | 8081 |
| product-service      | 8082 |
| order-service        | 8083 |
| payment-service      | 8084 |
| inventory-service    | 8085 |
| notification-service | 8086 |
| delivery-service     | 8087 |
| api-gateway          | 8080 |
| Kafka UI             | 9090 |
| PostgreSQL           | 5432 |
| Redis                | 6379 |
| Kafka                | 9092 |
| Elasticsearch        | 9200 |

## Environment Variables (.env file — never commit)
```
DB_HOST=localhost
DB_USER=postgres
DB_PASS=postgres
KAFKA_BOOTSTRAP=localhost:9092
REDIS_HOST=localhost
JWT_SECRET=mySecretKey123456789012345678901234567890AbcDef
STRIPE_API_KEY=sk_test_your_key_here
SENDGRID_API_KEY=SG.your_key_here
TWILIO_ACCOUNT_SID=ACyour_sid
TWILIO_AUTH_TOKEN=your_token
```
