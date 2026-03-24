# 🏦 Secure Banking System (Full-Stack)

A production-level **full-stack microservices banking application** built with a Modern React Frontend, Java 17, Spring Boot, Spring Cloud Gateway, Spring Security (JWT), Apache Kafka, and PostgreSQL.

## Architecture

```
                    ┌─────────────────────────┐
                    │    React Frontend       │
                    │  (Vite + TailwindCSS)   │
                    └────────────┬────────────┘
                                 │ REST / JWT
                    ┌────────────▼────────────┐
                    │   API Gateway (:8080)   │
                    │   JWT Route Filtering   │
                    └─────┬──┬──┬──┬──────────┘
                          │  │  │  │
              ┌───────────┘  │  │  └───────────┐
              ▼              ▼  ▼              ▼
        ┌──────────┐  ┌─────────┐  ┌──────────────┐
        │ Auth Svc │  │User Svc │  │ Account Svc  │
        │  :8081   │  │ :8082   │  │   :8083      │
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

## Features

### 🎨 Frontend (Modern Fintech UI)
- ✅ **Glassmorphism Aesthetic**: Sleek frosted glass cards, glow effects, and abstract animated backgrounds.
- ✅ **GPay/Paytm Style Layout**: Gorgeous circular quick-action buttons for Deposits, Withdrawals, and Transfers.
- ✅ **Dynamic Modals**: Intercepts actions securely with beautifully animated modal overlays.
- ✅ **Real-Time Animated Success States**: Smooth 'Checkmark' animations upon transaction completion.
- ✅ **State Management & JWT Guarding**: Persistent login checking and API route interception.

### ⚙️ Backend (Microservices)
- ✅ **Event-driven architecture** using Apache Kafka for real-time notifications.
- ✅ **API Gateway** acting as a single entry point with centralized JWT routing verification.
- ✅ **Database-per-service pattern**: 5 independent PostgreSQL databases ensuring high isolation.
- ✅ **Compensating transactions**: Handles cross-service transaction rollbacks if partial failures occur.

## Quick Start

### Prerequisites
- Java 17+, Maven
- PostgreSQL 14+
- Docker Desktop (for Kafka & Zookeeper)
- Node.js 18+ (for Frontend)

### 1. Database Setup
Ensure PostgreSQL is running on port 5432 with credentials `postgres` (or update the `application.yml` files).  
Create the 5 distinct databases:
```sql
CREATE DATABASE banking_auth_db;
CREATE DATABASE banking_user_db;
CREATE DATABASE banking_account_db;
CREATE DATABASE banking_transaction_db;
CREATE DATABASE banking_notification_db;
```

### 2. Start Kafka (Docker)
```bash
cd backend
docker-compose up -d
```

### 3. Run Backend Microservices
Open a terminal in the `backend` directory:
```bash
# Windows users can start all 6 services with a single script
.\start-all.bat
```
*(Alternatively, you can manually run `mvn spring-boot:run` inside each of the 6 service directories).*

### 4. Run the React Frontend
Open a new terminal in the `frontend` directory:
```bash
cd frontend
npm install
npm run dev
```

### 5. Access the App
Open your browser and navigate to:
**http://localhost:5173**

Register a new account, create your KYC profile, open a savings account, and try out the gorgeous new deposit and transfer features!

## Tech Stack
- **Frontend**: React, TypeScript, Vite, Tailwind CSS, Lucide Icons, React Router, Axios
- **Backend**: Java 17, Spring Boot 3.2, Spring Cloud Gateway, Spring Security
- **Data & Messaging**: PostgreSQL, Apache Kafka
- **Tools**: Docker, Swagger/OpenAPI
