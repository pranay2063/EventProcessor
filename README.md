# Alert Processor

Alert Processor is a project to build a centralized system that accepts issues, signals, and alarms, processes them, and routes notifications to upstream systems such as Jira, PagerDuty, and email.

> **Project status:** The current code is an early event-processing prototype. Jira, PagerDuty, and email delivery, alert normalization, routing rules, and the target pipeline below are planned; they are not implemented yet.

## Target architecture

The diagram shows the intended direction of the project. Components marked **planned** are future work.

```mermaid
flowchart LR
    subgraph Sources[Alert sources]
        MON[Monitoring systems]
        APP[Applications and services]
        OPS[Operators]
    end

    subgraph Intake[Alert Processor]
        API[Alert intake API]
        VALIDATE[Validate and normalize alert<br/>(planned)]
        QUEUE[(RabbitMQ)]
        PROCESS[Deduplicate, classify, enrich<br/>(planned)]
        RULES[Apply routing rules<br/>(planned)]
    end

    subgraph Destinations[Notification destinations]
        JIRA[Jira adapter<br/>(planned)]
        PD[PagerDuty adapter<br/>(planned)]
        EMAIL[Email adapter<br/>(planned)]
    end

    MON --> API
    APP --> API
    OPS --> API
    API --> VALIDATE --> QUEUE --> PROCESS --> RULES
    RULES --> JIRA
    RULES --> PD
    RULES --> EMAIL
```

## Current implementation

The current application is a Spring Boot API that accepts email-shaped event records, publishes them to RabbitMQ, and stores consumed records in memory. It is a starting point for the alert intake and asynchronous processing work, not a complete alert notification system.

| Capability | Current state |
| --- | --- |
| `POST /events` accepts `senderEmail`, `receiverEmail`, and `message`; the service assigns an ID | Implemented |
| RabbitMQ topic exchange `events`, routing key `eventkey`, and durable quorum queue `event` | Implemented |
| Consumer processes queued events with prefetch 1 and manual acknowledgement | Implemented |
| `GET /events?email=...` reads records grouped by sender email | Implemented |
| Persistent storage, alert-specific schema, deduplication, classification, enrichment, and routing rules | Planned |
| Jira, PagerDuty, and email notification adapters | Planned |

The existing event API is a prototype interface and may change as the canonical alert model is designed. `receiverEmail` is currently stored as event data: the application does not send email through SMTP or an email provider.

## Run locally

Requirements: Docker with Docker Compose. Java 17 is needed to run the app outside Compose.

```bash
docker compose up --build
```

Compose starts RabbitMQ and then the application after RabbitMQ passes its health check. The consumer declares the `events` exchange, `event` queue, and `eventkey` binding automatically.

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- RabbitMQ management: <http://localhost:15672/> (`guest` / `guest` for local default credentials)
- App API: <http://localhost:8080/events>

Stop the services with:

```bash
docker compose down
```

## Current API example

Submit a prototype event record:

```bash
curl -X POST http://localhost:8080/events \
  -H 'Content-Type: application/json' \
  -d '{"senderEmail":"sender@example.com","receiverEmail":"recipient@example.com","message":"Example signal"}'
```

Query events by sender email. The consumer runs asynchronously, so allow a short moment after a successful `POST` before querying.

```bash
curl 'http://localhost:8080/events?email=sender%40example.com'
```

## Data storage

Consumed events are held in an in-memory map keyed by `senderEmail`. They are available only while the application process is running and are cleared when it restarts. RabbitMQ provides the queue between publishing and processing; this project does not yet use a persistent event database.

## Roadmap

- [ ] Define a canonical alert schema and supported intake sources
- [ ] Add durable storage and idempotent alert processing
- [ ] Add severity, classification, enrichment, and configurable routing rules
- [ ] Build Jira, PagerDuty, and email adapters
- [ ] Track delivery status and add retries and failure handling
