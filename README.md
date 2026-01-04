# Distributed Transaction Intelligence Platform

A production-grade, event-driven distributed transaction orchestration system
built using Kafka, Spring Boot, Redis, and PostgreSQL.

This project demonstrates how to handle distributed transactions using the
Saga pattern without relying on two-phase commit (2PC), while ensuring
reliability, idempotency, retries, and failure recovery.

---

## 🧠 Problem Statement

In distributed systems, a single business transaction often spans multiple
services (e.g., order creation, inventory reservation, payment processing).
Traditional ACID transactions do not work across microservices.

This platform solves the problem by:
- Using **event-driven sagas**
- Handling **partial failures**
- Providing **compensation logic**
- Guaranteeing **eventual consistency**

---

## 🏗️ Architecture Overview

### Core Services
- **Transaction Service**
  - REST API entry point
  - Publishes initial transaction events

- **Inventory Service**
  - Reserves and releases inventory
  - Idempotent Kafka consumer

- **Payment Service**
  - Processes payments
  - Simulates business and transient failures
  - Uses retries and DLQ

- **Orchestrator Service**
  - Central saga coordinator
  - Maintains saga state machine
  - Triggers next steps or compensation

---

## 🔄 Event Flow (Saga Lifecycle)

1. Client creates transaction (`/transactions`)
2. Transaction Service publishes `CREATED`
3. Inventory Service publishes `INVENTORY_RESERVED`
4. Payment Service publishes:
   - `PAYMENT_COMPLETED` (success)
   - `PAYMENT_FAILED` (failure)
5. Orchestrator updates saga state:
   - Completes transaction
   - Or triggers compensation (`INVENTORY_RELEASED`)

---

## 🔐 Reliability Guarantees

### ✅ Idempotency
- Redis-backed idempotency keys
- Prevents duplicate event processing
- Safe for retries and replays

### 🔁 Retries
- Kafka retry topics using `@RetryableTopic`
- Exponential backoff

### ☠ Dead Letter Queue (DLQ)
- Messages that fail after retries are routed to:
  - `transaction-events-DLT`
- Enables debugging and replay

---

## 💾 Data & Caching Strategy

### Redis
- Idempotency tracking
- Fast saga state cache
- Shared across instances

### PostgreSQL
- Durable saga state persistence
- Crash recovery
- Auditing and debugging

---

## 🧪 Running the System Locally

### Prerequisites
- Java 21
- Maven
- Docker & Docker Compose

### Start Infrastructure
```bash
docker-compose up -d
