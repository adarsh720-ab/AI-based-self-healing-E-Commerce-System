# Architecture Overview

This document explains the runtime architecture of the `Self-Healing Ecommerce Platform` and how business and self-healing flows move across services.

## 1) System Context

The platform is split into independent Spring Boot microservices behind `api-gateway`, backed by Kafka and stateful stores.

- Public entry point: `api-gateway`
- Domain services: `auth`, `user`, `product`, `inventory`, `order`, `payment`, `delivery`, `notification`
- Self-healing services: `ml-service`, `ai-service`, `action-engine`
- Data and infra: PostgreSQL, Redis, Kafka, Elasticsearch

## 2) High-Level Components

```mermaid
flowchart LR
    Client[Client App / Frontend] --> Gateway[api-gateway]

    Gateway --> Auth[auth-service]
    Gateway --> User[user-service]
    Gateway --> Product[product-service]
    Gateway --> Inventory[inventory-service]
    Gateway --> Order[order-service]
    Gateway --> Payment[payment-service]
    Gateway --> Delivery[delivery-service]

    Order <--> Kafka[(Kafka)]
    Payment <--> Kafka
    Delivery <--> Kafka
    Notification[notification-service] <--> Kafka

    ML[ml-service] <--> Kafka
    AI[ai-service] <--> Kafka
    Action[action-engine] <--> Kafka

    Order --> Postgres[(PostgreSQL)]
    Payment --> Postgres
    Delivery --> Postgres
    Auth --> Postgres
    User --> Postgres
    Product --> Postgres
    Inventory --> Postgres
    AI --> Postgres

    Gateway --> Redis[(Redis)]
    Action --> Redis
```

## 3) Kafka Topics Used

Defined/used by services and `commons` topic configuration:

- `service-logs`
- `anomaly-events`
- `order-events`
- `payment-events`
- `delivery-events`

## 4) Order Lifecycle Sequence

This is the event-driven order-to-delivery flow implemented in `order-service`, `payment-service`, and `delivery-service`.

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant G as API Gateway
    participant O as order-service
    participant I as inventory-service
    participant K as Kafka
    participant P as payment-service
    participant D as delivery-service
    participant N as notification-service

    C->>G: POST /api/orders
    G->>O: Forward create order request
    O->>I: Reserve stock (per item)
    I-->>O: Reservation result
    O->>O: Persist PENDING order
    O->>K: Publish ORDER_PLACED (order-events)

    K-->>P: Consume ORDER_PLACED
    P->>P: Create PENDING payment

    Note over C,P: Payment confirmation webhook arrives later
    C->>G: POST /api/payments/webhook/stripe
    G->>P: Forward webhook payload
    P->>P: Mark payment SUCCESS
    P->>K: Publish PAYMENT_SUCCESS (payment-events)

    K-->>O: Consume PAYMENT_SUCCESS
    O->>O: Update order status to CONFIRMED

    K-->>D: Consume PAYMENT_SUCCESS
    D->>D: Create delivery with tracking code

    K-->>N: Consume order/payment/delivery events
    N->>N: Send notifications (email/SMS/log)
```

## 5) Self-Healing Sequence

This is the anomaly detection and action loop implemented by `ml-service`, `ai-service`, and `action-engine`.

```mermaid
sequenceDiagram
    autonumber
    participant S as Services (logs/events)
    participant K as Kafka
    participant M as ml-service
    participant AI as ai-service
    participant OL as Ollama (Qwen)
    participant DB as ai-service incident DB
    participant AE as action-engine

    S->>K: Publish service telemetry (service-logs)
    K-->>M: Consume service-logs
    M->>M: Score with isolation-forest model
    alt anomaly detected
        M->>K: Publish anomaly-events
    else normal traffic
        M->>M: Continue monitoring
    end

    K-->>AI: Consume anomaly-events
    AI->>OL: RootCauseAnalyser.analyse(event)
    OL-->>AI: rootCause + suggestedFix + severity
    AI->>DB: Save incident record (OPEN)

    K-->>AE: Consume anomaly-events
    AE->>AE: CooldownGuard check
    alt severity = CRITICAL
        AE->>AE: RestartAction
    else severity = WARNING
        AE->>AE: ScaleUpAction
    else severity = LOW/unknown
        AE->>AE: LogAndMonitorAction
    end
```

## 6) Failure Domain Notes

- A failure in one business service does not require a full platform restart.
- Kafka decouples producers and consumers, reducing direct synchronous dependency chains.
- `ai-service` requires reachable Ollama endpoint; if unavailable, anomaly analysis degrades.
- `action-engine` uses cooldown logic to reduce repeated remediation loops.

## 7) Deployment Views

- Local: `docker-compose.yml`
- Kubernetes: `k8s-deploy/k8s/` manifests
- Image build scripts: `k8s-deploy/build-images.ps1` and `k8s-deploy/build-images.sh`

## 8) Suggested Next Docs

- `docs/RUNBOOK.md`: incident response and operator commands
- `docs/ADR/`: architecture decision records for key tradeoffs
- `docs/API.md`: full request/response contracts by service

