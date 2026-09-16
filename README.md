# Operational Requests Platform — Fase 1

Base técnica de una plataforma de solicitudes operacionales con dos microservicios Java 21, SQL Server, Kafka KRaft y Keycloak. La fase entrega cimientos compilables e infraestructura local; no entrega todavía casos de uso ni frontend funcional.

## Arquitectura y estructura

`requests-service` aplica arquitectura hexagonal: dominio Java puro, puertos de aplicación y adaptadores de infraestructura. `indicators-service` posee una base analítica independiente y queda preparado para consumir eventos en una fase posterior. Las decisiones y diagramas están en `docs/adr` y `docs/architecture`.

- `backend/`: reactor Maven y ambos servicios desplegables.
- `frontend/`: marcadores de posición del shell y microfrontend futuro.
- `infrastructure/`: import local de Keycloak y documentación de infraestructura.
- `docs/`: ADR, modelos, diagramas y contratos de eventos.
- `tests/karate/`: reservado para una fase posterior.

## Prerrequisitos

- Git.
- Java 21.
- Docker con Docker Compose.
- Al menos 6 GB de memoria disponibles para el stack local.

El Maven Wrapper descarga Maven 3.9.11 en su primer uso.

## Flujo completo desde la clonación

Use este único flujo: clonar, crear `.env`, compilar, validar Compose, levantar y comprobar. Revise las contraseñas ficticias del `.env` antes de usar el stack.

PowerShell:

```powershell
git clone <URL_DEL_REPOSITORIO>
Set-Location operational-requests-platform
Copy-Item .env.example .env
.\mvnw.cmd clean verify
docker compose config --quiet
docker compose up --build -d
docker compose ps
Invoke-RestMethod http://localhost:18081/actuator/health
Invoke-RestMethod http://localhost:8082/actuator/health
Invoke-RestMethod http://localhost:8080/realms/operational-requests/.well-known/openid-configuration
```

Bash:

```bash
git clone <URL_DEL_REPOSITORIO>
cd operational-requests-platform
cp .env.example .env
./mvnw clean verify
docker compose config --quiet
docker compose up --build -d
docker compose ps
curl --fail http://localhost:18081/actuator/health
curl --fail http://localhost:8082/actuator/health
curl --fail http://localhost:8080/realms/operational-requests/.well-known/openid-configuration
```

Para detener el stack sin eliminar los volúmenes:

```bash
docker compose down
```

Los módulos también pueden compilarse por separado desde la raíz:

```powershell
.\mvnw.cmd -pl backend/requests-service -am clean verify
.\mvnw.cmd -pl backend/indicators-service -am clean verify
```

## Variables de entorno

`.env` está excluido de Git. `.env.example` solo contiene valores locales ficticios. Define contraseñas de ambos SQL Server, administración local de Keycloak y puertos. Las aplicaciones reciben además las URL JDBC, Kafka e issuer OIDC desde Compose. Keycloak no interpola variables dentro de `realm-export.json`; por eso las claves temporales de usuarios de demostración están explícitas y documentadas como locales.

## Puertos y salud

| Servicio | Puerto predeterminado | Comprobación |
|---|---:|---|
| Keycloak | 8080 | `http://localhost:8080/realms/operational-requests/.well-known/openid-configuration` |
| requests-service | 18081 (host) → 8081 (contenedor) | `http://localhost:18081/actuator/health` |
| indicators-service | 8082 | `http://localhost:8082/actuator/health` |
| Kafka | 9092 | health check interno con CLI Kafka |
| SQL operacional | 1433 | health check interno con `sqlcmd` |
| SQL analítico | 1434 | health check interno con `sqlcmd` |

Kafka anuncia `kafka:29092` dentro de la red Docker y `localhost:9092` para clientes ejecutados en el host.

## Identidad local

Realm: `operational-requests`. Cliente SPA público: `operational-requests-web`, con Authorization Code Flow y PKCE S256, sin client secret. Usuarios locales:

| Usuario | Rol |
|---|---|
| `solicitante` | `SOLICITANTE` |
| `analista` | `ANALISTA` |
| `supervisor` | `SUPERVISOR` |

Sus contraseñas `LocalOnly_*` son ficticias, temporales y exclusivas del entorno local. La autorización por roles en endpoints pertenece a una fase posterior. En Fase 1 solo health, liveness y readiness son públicos; los demás endpoints requieren un JWT válido.

## Estado implementado

- Reactor Maven, Wrapper, Java 21 y dos aplicaciones Spring Boot.
- Dominio puro y reglas de transición de solicitudes con pruebas unitarias.
- Migraciones Flyway operacionales y analíticas.
- SQL Server independientes, Kafka KRaft y realm reproducible de Keycloak.
- Resource Server JWT, Actuator y OpenAPI base para `requests-service`.
- Dockerfiles multi-stage, Compose, contratos, ADR y diagramas.

## Limitaciones y siguientes fases

No existen todavía endpoints funcionales, adaptadores JPA, publicador Outbox, consumidores Kafka, autorización por roles, UI, pruebas Karate, Kubernetes ni CI. Tampoco hay topología productiva o alta disponibilidad. Las siguientes fases implementarán estos elementos y las pruebas de integración y contrato correspondientes.

## Versiones fijadas

Java 21, Maven 3.9.11, Spring Boot 3.5.16, springdoc 2.8.13, JaCoCo 0.8.13, Kafka 4.1.1, Keycloak 26.7.3 y SQL Server 2022 CU20.
