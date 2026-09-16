# Operational Requests Platform — Fase 2

API operacional de solicitudes sobre Java 21, Spring Boot, SQL Server, Keycloak y arquitectura hexagonal. `requests-service` implementa el ciclo operacional; `indicators-service` conserva los cimientos analíticos de Fase 1.

## Ejecución local

Requisitos: Git, Java 21, Docker con Compose y al menos 6 GB libres. El Wrapper usa Maven 3.9.11.

PowerShell:

```powershell
git clone <URL_DEL_REPOSITORIO>
Set-Location operational-requests-platform
Copy-Item .env.example .env
.\mvnw.cmd clean verify
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
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
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
curl --fail http://localhost:18081/actuator/health
curl --fail http://localhost:8082/actuator/health
curl --fail http://localhost:8080/realms/operational-requests/.well-known/openid-configuration
```

Para detener sin borrar volúmenes: `docker compose --env-file .env down`. `.env` está ignorado; `.env.example` contiene exclusivamente credenciales ficticias locales.

## API operacional

Base: `http://localhost:18081/api/v1`. Todos los endpoints requieren Bearer JWT. Swagger UI y OpenAPI son públicos **solo para desarrollo local** en `/swagger-ui.html` y `/v3/api-docs`; la API sigue protegida.

| Método y ruta | Rol |
|---|---|
| `GET /categorias` | cualquier rol operacional |
| `POST /solicitudes` | SOLICITANTE |
| `GET /solicitudes?page=0&size=20&estado=&categoriaId=&prioridad=` | todos; SOLICITANTE solo propias |
| `GET /solicitudes/{id}` | todos; SOLICITANTE solo propia |
| `POST /solicitudes/{id}/asignaciones` | ANALISTA |
| `POST /solicitudes/{id}/observaciones` | ANALISTA asignado |
| `POST /solicitudes/{id}/transiciones` | ANALISTA resuelve; SUPERVISOR devuelve o cierra |

Creación: `{"asunto":"Falla de acceso","descripcion":"No es posible ingresar","categoriaId":"11111111-1111-1111-1111-111111111111","prioridad":"MEDIA"}`.

Transición: `{"estadoDestino":"RESUELTA","observacion":"Servicio restaurado"}` o, para supervisor, `{"estadoDestino":"CERRADA","motivo":"Validación completada"}`. Cada respuesta devuelve `X-Correlation-Id`; se acepta ese header solo como UUID y en otro caso se genera uno. Los errores usan `ProblemDetail` con `errorCode`, correlación y fecha. La colección completa está en `docs/http/requests-service.http` y usa tokens ficticios.

## Identidad y permisos

Realm `operational-requests`; SPA pública `operational-requests-web`, Authorization Code + PKCE S256, sin secreto ni Direct Access Grants. Usuarios locales: `solicitante`/`SOLICITANTE`, `analista`/`ANALISTA` y `supervisor`/`SUPERVISOR`. Sus contraseñas `LocalOnly_*` son demostrativas, no productivas; Keycloak no interpola variables en un realm exportado.

El API toma `sub` como UUID estable, `preferred_username` solo como descriptor y `realm_access.roles` como autoridades `ROLE_*`. No persiste correo ni nombres. El supervisor no hereda acciones de analista.

## Persistencia y eventos

Flyway V3 añade asunto, motivo de transición, índices y la secuencia SQL Server `request_number_seq`, usada para números `SOL-año-000001` sin `MAX+1`. La toma usa un `UPDATE ... WHERE status='REGISTRADA'` atómico: cero filas produce HTTP 409. Negocio, historial y Outbox `PENDING` se escriben en una sola transacción; no se publica directamente a Kafka en esta fase.

## Puertos

| Servicio | Host | Salud/documentación |
|---|---:|---|
| Keycloak | 8080 | discovery del realm |
| requests-service | 18081 | `/actuator/health`, `/swagger-ui.html` |
| indicators-service | 8082 | `/actuator/health` |
| Kafka | 9092 | interno `kafka:29092` |
| SQL operacional | 1433 | base `operational_requests` |
| SQL analítico | 1434 | base `operational_indicators` |

## Estado y limitaciones

Implementado: categorías, alta, listado filtrado/paginado, detalle, asignación, observaciones, resolución, devolución, cierre, RBAC, errores, OpenAPI y Outbox transaccional. `requests-service` separa dominio Java puro, puertos/casos de uso y adaptadores REST/JPA/seguridad/Outbox.

Pendiente: publicador Outbox, consumidor de indicadores, frontend, pruebas Karate/Testcontainers SQL Server, Helm y CI. `frontend/`, `infrastructure/helm/` y `tests/karate/` siguen reservados. Las pruebas unitarias de concurrencia no sustituyen una ejecución simultánea real contra SQL Server.

Versiones fijadas: Java 21, Maven 3.9.11, Spring Boot 3.5.16, springdoc 2.8.13, JaCoCo 0.8.13, Kafka 4.1.1, Keycloak 26.7.3 y SQL Server 2022 CU20.
