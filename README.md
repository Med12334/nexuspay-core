# NexusPay Core — Distributed Payment & Ledger Settlement Engine

NexusPay Core is an event-driven financial transaction engine and settlement platform. It demonstrates a hybrid cloud-native and mainframe-ledger architecture, bridging high-throughput modern account services with an immutable core banking ledger.

## Tech Stack
* **Backend:** Java 17, Spring Boot 3.3, Spring Cloud Gateway, Spring Data JPA, Spring for Apache Kafka
* **Frontend:** Angular 18 (Standalone Components, Signals, RxJS, OnPush Change Detection)
* **Messaging & Streaming:** Apache Kafka (KRaft mode)
* **Databases:** PostgreSQL 16 (Operational Store & Transactional Outbox), IBM DB2 (Core Settlement Ledger)

---

## System Architecture

    +-------------------------------------------------------------------------+
    |                     ANGULAR 18 OPERATIONS CONSOLE                       |
    |         (RxJS Live Streams, Signals, OnPush, HTTP Interceptors)         |
    +------------------------------------+------------------------------------+
                                         | HTTP / REST (Port 8000)
                                         v
    +-------------------------------------------------------------------------+
    |                   SPRING CLOUD API GATEWAY (api-gateway)                |
    +------+--------------------+--------------------+--------------------+---+
           |                    |                    |                    |
           v (8081)             v (8082)             v (8083)             v (8084)
    +--------------+    +----------------+   +----------------+   +---------------+
    |account-service|   |legacy-db2-bridge|  |tx-orchestrator |   | fraud-engine  |
    | (PostgreSQL  |    |   (IBM DB2     |   | (Kafka Saga    |   | (Real-Time    |
    |  + Outbox)   |    |  Core Ledger)  |   |  Coordinator)  |   |  AML Rules)   |
    +------+-------+    +--------+-------+   +--------+-------+   +-------+-------+
           |                     |                    |                   |
           +---------------------+--------------------+-------------------+
                                         |
                                         v
                 +-----------------------------------------------+
                 |        APACHE KAFKA EVENT STREAMING BUS       |
                 | Topics: tx-initiated | tx-validated           |
                 |         tx-fraud-flagged | tx-completed       |
                 +-----------------------------------------------+

---

## Architectural Highlights

* **Transactional Outbox Pattern (`account-service`):** Prevents dual-write inconsistencies between PostgreSQL and Apache Kafka. Balance mutations and outbox events are committed within a single local ACID transaction, with a scheduled relay publishing pending events to Kafka.
* **Concurrency Control:** Uses JPA optimistic locking (`@Version`) on account entities to prevent lost updates and double-spending under concurrent transfer requests.
* **Mainframe Ledger Settlement (`legacy-db2-bridge`):** Consumes validated payment events and books immutable settlement records into IBM DB2 (`LEGACY_CORE_LEDGER`) with idempotency checks (`TX_REFERENCE_CODE`). Includes a `local` Spring profile using H2 in `MODE=DB2` for lightweight development.
* **Choreographed Saga & Risk Evaluation (`transaction-orchestrator` & `fraud-engine`):** Coordinates multi-step asynchronous transaction lifecycles across Kafka topics while evaluating real-time velocity and AML threshold rules.

---

## Getting Started

### 1. Start Infrastructure (Docker Compose)
    cd infra
    docker-compose up -d

### 2. Build Backend Microservices
    cd backend
    mvn clean package -DskipTests

### 3. Run Frontend Console
    cd frontend
    npm install
    npm start
