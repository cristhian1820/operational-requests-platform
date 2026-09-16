# Operational Requests Platform — Fase 3

Plataforma Java 21 con API operacional, Outbox transaccional, Kafka KRaft e indicadores idempotentes sobre SQL Server.

## Arranque local

Requisitos: Java 21, Docker Compose y al menos 6 GB libres.

PowerShell:

```powershell
Copy-Item .env.example .env
.\mvnw.cmd clean verify
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
```

Bash:

```bash
cp .env.example .env
./mvnw clean verify
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
```

Detener conservando volúmenes: `docker compose --env-file .env down`.

## Flujo de eventos

`requests-service` confirma primero negocio y Outbox. El scheduler reclama lotes pequeños atómicamente, publica en `operational-requests.events.v1` con `aggregateId` como key y marca `PUBLISHED` después del ack. Fallos usan backoff lineal, máximo configurable y recuperación de locks vencidos. La entrega es al menos una vez.

`indicators-service` valida el sobre, inserta `processed_event` bajo restricción única y actualiza hecho, dimensiones y `request_current_state` en una transacción. Duplicados no cambian indicadores. Tras dos reintentos, mensajes inválidos van a `.dlt`.

Variables: `KAFKA_EVENTS_TOPIC`, `KAFKA_EVENTS_DLT_TOPIC`, `OUTBOX_BATCH_SIZE`, `OUTBOX_POLL_INTERVAL_MS`, `OUTBOX_MAX_ATTEMPTS`, `OUTBOX_BACKOFF_SECONDS`, `OUTBOX_LOCK_TIMEOUT_SECONDS` y `KAFKA_CONSUMER_CONCURRENCY`. Local usa tres particiones/réplica 1; producción requiere replicación y capacidad acordes al tráfico.

## APIs

- requests-service: `http://localhost:18081`, Swagger `/swagger-ui.html`.
- indicators-service: `http://localhost:8082`, Swagger `/swagger-ui.html`.
- `GET /api/v1/indicadores/resumen`: ANALISTA o SUPERVISOR.
- `GET /api/v1/indicadores/tendencia?desde=YYYY-MM-DD&hasta=YYYY-MM-DD`: ANALISTA o SUPERVISOR, UTC, máximo 366 días.

Se permite ANALISTA porque necesita visibilidad operacional global; SOLICITANTE recibe 403 y SUPERVISOR conserva acceso. OpenAPI es público solo localmente; datos funcionales requieren JWT. Ejemplos: `docs/http/requests-service.http` y `docs/http/indicators-service.http`.

## Puertos y salud

| Componente | Puerto | Comprobación |
|---|---:|---|
| Keycloak | 8080 | realm discovery |
| requests-service | 18081 | `/actuator/health` |
| indicators-service | 8082 | `/actuator/health` |
| Kafka host/interno | 9092 / 29092 | CLI Kafka |
| SQL operacional | 1433 | Flyway V4 |
| SQL analítico | 1434 | Flyway V3 |

## Estado y limitaciones

Implementados: casos operacionales de Fase 2, Outbox multiinstancia, publicación Kafka, DLT, consumo idempotente, modelo actual/histórico, resumen y tendencia. Pendientes: frontend, Karate, Helm y CI. No hay exactamente-una-vez distribuido; la seguridad proviene de Outbox + idempotencia. Las pruebas unitarias no sustituyen pruebas de carga ni una topología Kafka productiva.
