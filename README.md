# 🏦 Secure Banking System

A production-level **microservices-based banking backend** built with Java 17, Spring Boot, Spring Cloud Gateway, Spring Security (JWT), Apache Kafka, and MySQL.

## Architecture

```
                    ┌──────────────────────────┐
                    │   API Gateway (:8080)     │
                    │  JWT Validation + Routing │
                    └─────┬──┬──┬──┬───────────┘
                          │  │  │  │
              ┌───────────┘  │  │  └───────────┐
              ▼              ▼  ▼              ▼
        ┌──────────┐  ┌─────────┐  ┌──────────────┐
        │ Auth Svc  │  │User Svc │  │ Account Svc  │
        │  :8081    │  │ :8082   │  │   :8083      │
        └──────────┘  └─────────┘  └──────┬───────┘
                                          │ REST
                                   ┌──────┴───────┐
                                   │ Transaction  │
                                   │  Svc :8084   │
                                   └──────┬───────┘
                                          │ Kafka Events
                                   ┌──────┴───────┐
                                   │ Notification │
                                   │  Svc :8085   │
                                   └──────────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| **API Gateway** | 8080 | Routes requests, validates JWT tokens |
| **Auth Service** | 8081 | User registration, login, JWT token generation |
| **User Service** | 8082 | User profile management (KYC, personal details) |
| **Account Service** | 8083 | Bank account operations (create, balance, status) |
| **Transaction Service** | 8084 | Deposit, withdraw, transfer + Kafka producer |
| **Notification Service** | 8085 | Kafka consumer, logs & stores notifications |

## Quick Start

### Prerequisites
- Java 17+, Maven, MySQL 8.0+, Apache Kafka

### 1. Create Databases
1. Ensure PostgreSQL is running on port 5432.
2. Open pgAdmin or psql and run:
```sql
CREATE DATABASE banking_auth_db;
CREATE DATABASE banking_user_db;
CREATE DATABASE banking_account_db;
CREATE DATABASE banking_transaction_db;
CREATE DATABASE banking_notification_db;
```
3. Default credentials in `application.yml` are `postgres`/`your password`. Update if yours differ.

### 2. Start Kafka via Docker
```bash
docker-compose up -d
```

### 3. Build & Run
```bash
mvn clean install -DskipTests

# Run each service in separate terminals
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd account-service && mvn spring-boot:run
cd transaction-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### 4. Test
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@bank.com","password":"Pass123","fullName":"John Doe"}'

# Login (get JWT token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"Pass123"}'
```

## Key Features
- ✅ JWT Authentication with Role-Based Access (USER/ADMIN)
- ✅ API Gateway with centralized JWT validation
- ✅ Event-driven notifications via Kafka
- ✅ Paginated & sorted transaction history
- ✅ Compensating transactions for money transfers
- ✅ Global exception handling per service
- ✅ Swagger/OpenAPI documentation
- ✅ Spring Cache for read optimization
- ✅ DTO validation with Bean Validation
- ✅ Database-per-service pattern

## Swagger UI
Each service has Swagger at: `http://localhost:{port}/swagger-ui.html`

## Tech Stack
Java 17 • Spring Boot 3.2 • Spring Cloud Gateway • Spring Security • Spring Data JPA • MySQL • Apache Kafka • Lombok • SpringDoc OpenAPI
