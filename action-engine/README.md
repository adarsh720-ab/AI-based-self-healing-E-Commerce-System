# Action Engine

Self-healing action engine for the AI-powered e-commerce platform.

## What it does

Consumes `anomaly-events` from Kafka, applies a Redis-based cooldown guard to prevent
action storms, then routes by severity to one of three recovery actions.

```
anomaly-events (Kafka)
        ↓
ActionEngineConsumer
        ↓
CooldownGuard  ──── skip if in cooldown (Redis TTL 5 min)
        ↓
SeverityRouter
   ├── CRITICAL → RestartAction   (Docker stub → real restart later)
   ├── WARNING  → ScaleUpAction   (Kubernetes/Compose stub)
   └── LOW      → LogAndMonitorAction
        ↓
ActionDispatcher → action_logs table (PostgreSQL)
```

## Port

`8092`

## REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/actions` | All action logs |
| GET | `/api/actions/service/{serviceId}` | Logs for a specific service |
| GET | `/api/actions/stats` | Counts per action type (last 24 h) |
| DELETE | `/api/actions/cooldown/{serviceId}/{anomalyType}` | Manually clear cooldown |
| GET | `/actuator/health` | Health probe |

## Configuration

Key properties in `application.yml`:

```yaml
action:
  cooldown:
    ttl-seconds: 300      # 5 min between repeat actions per service
  docker:
    stub-mode: true       # set false when Docker socket is wired
```

## Database setup

Before starting, create the database:

```sql
CREATE DATABASE action_db;
```

JPA `ddl-auto: update` creates the `action_logs` table automatically on first boot.

## Docker Compose snippet

```yaml
action-engine:
  build:
    context: ./action-engine
    dockerfile: Dockerfile
  container_name: ecommerce-action-engine
  ports:
    - "8092:8092"
  environment:
    SPRING_DATASOURCE_URL: jdbc:postgresql://ecommerce-postgres:5432/action_db
    SPRING_DATASOURCE_USERNAME: enter your username
    SPRING_DATASOURCE_PASSWORD: enter your password
    SPRING_DATA_REDIS_HOST: ecommerce-redis
    SPRING_KAFKA_BOOTSTRAP_SERVERS: ecommerce-kafka:9092
  depends_on:
    - ecommerce-postgres
    - ecommerce-redis
    - ecommerce-kafka
  networks:
    - ecommerce-network
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8092/actuator/health/liveness"]
    interval: 30s
    timeout: 10s
    retries: 3
```

## Activating real Docker restart

1. Set `action.docker.stub-mode=false` in `application.yml`
2. Mount Docker socket in `docker-compose.yml`:
   ```yaml
   volumes:
     - /var/run/docker.sock:/var/run/docker.sock
   ```
3. Add `docker-java` dependency to `pom.xml`
4. Replace the stub block in `RestartAction.java` with real Docker client call

## Package structure

```
com.ecommerce.action/
├── ActionEngineApplication.java
├── config/
│   ├── KafkaConsumerConfig.java
│   └── RedisConfig.java
├── consumer/
│   └── ActionEngineConsumer.java
├── controller/
│   └── ActionLogController.java
├── event/
│   └── AnomalyEvent.java
├── service/
│   ├── CooldownGuard.java
│   ├── SeverityRouter.java
│   └── ActionDispatcher.java
├── action/
│   ├── RestartAction.java
│   ├── ScaleUpAction.java
│   └── LogAndMonitorAction.java
├── model/
│   └── ActionLog.java
└── repository/
    └── ActionLogRepository.java
```
