# Operational Requests Platform — Fase 4

Plataforma Java 21 y React 19 para solicitudes operacionales. Incluye API operacional, Outbox/Kafka, indicadores idempotentes, autenticación Keycloak y frontend federado.

## Arquitectura frontend

- `frontend/shell`: host Rspack/Module Federation en `http://localhost:4200`. Es dueño de Keycloak, sesión descriptiva Redux, layout, navegación e indicadores.
- `frontend/requests-mfe`: remoto en `http://localhost:4201`, expone `requestsMfe/Routes` y también funciona standalone.
- `frontend/shared`: contratos Zod, cliente HTTP, tipos y slice de sesión. No persiste tokens.

React, React DOM, Router, MUI, Emotion, Redux Toolkit, React Redux y Keycloak se comparten como singleton con versiones exactas. El navegador descarga `remoteEntry.js` desde localhost; las variables son build args, por lo que cambiar URLs requiere reconstruir las imágenes.

## Arranque completo

Requisitos: Java 21, Docker Compose, Node 22+ y npm 11+; se recomiendan 8 GB libres.

PowerShell:

```powershell
Copy-Item .env.example .env
Set-Location frontend
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
npm run build-storybook
Set-Location ..
.\mvnw.cmd clean verify
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
```

Bash:

```bash
cp .env.example .env
cd frontend
npm ci
npm run lint && npm run typecheck && npm run test && npm run build && npm run build-storybook
cd ..
./mvnw clean verify
docker compose --env-file .env config --quiet
docker compose --env-file .env up --build -d
docker compose --env-file .env ps
```

Detener conservando volúmenes: `docker compose --env-file .env down`.

## Desarrollo frontend sin Docker

Con infraestructura y APIs disponibles, en terminales separadas desde `frontend/`:

```text
npm run dev -w requests-mfe
npm run dev -w shell
npm run storybook
```

El remoto debe iniciarse antes que el shell. Storybook usa el puerto 6006 y no necesita backend.

## Sesión y seguridad

El shell inicializa Keycloak con `check-sso`: la portada pública explica cómo iniciar sesión y una sesión SSO existente se restaura sin forzar redirección. Login usa Authorization Code + PKCE S256. Tokens y refresh tokens permanecen únicamente dentro de `keycloak-js`; Redux solo guarda `sub`, `preferred_username` y roles. Antes de cada petición se renueva el token con 30 segundos de margen. Logout vuelve al origen permitido.

La recarga directa de `/solicitudes/:id` conserva la URL, recupera la sesión SSO y vuelve a consultar el detalle mostrando loading (criterio A6). Las acciones visibles responden al rol, pero backend sigue siendo la autoridad. El cliente diferencia 400/401/403/404/409/422/500 y valida respuestas críticas con Zod (A7).

Usuarios exclusivamente locales: `solicitante`, `analista`, `supervisor`. Las contraseñas temporales están marcadas `LocalOnly_*` en el realm demo; no son credenciales productivas.

## Navegación por rol

- SOLICITANTE: bandeja propia, creación y consulta.
- ANALISTA: bandeja global, tomar, observar y resolver solicitudes asignadas; indicadores.
- SUPERVISOR: bandeja global, devolver/cerrar resueltas; indicadores.

Rutas: `/`, `/solicitudes`, `/solicitudes/nueva`, `/solicitudes/:id`, `/indicadores`, `/sin-autorizacion` y 404.

## Puertos y salud

| Componente | Puerto | Comprobación |
|---|---:|---|
| Shell | 4200 | `/` |
| Requests MFE | 4201 | `/remoteEntry.js` |
| Keycloak | 8080 | realm discovery |
| requests-service | 18081 | `/actuator/health`, `/v3/api-docs` |
| indicators-service | 8082 | `/actuator/health`, `/v3/api-docs` |
| Kafka host/interno | 9092 / 29092 | CLI Kafka |
| SQL operacional | 1433 | Flyway V4 |
| SQL analítico | 1434 | Flyway V3 |

## Pruebas de aceptación A1–A7

Con el stack levantado, ejecuta la suite Karate contra contenedores reales:

```powershell
.\mvnw.cmd -Pacceptance -pl tests/karate -am test
powershell -ExecutionPolicy Bypass -File tests/karate/verify-compose.ps1
```

El reporte HTML queda en `tests/karate/target/karate-reports/karate-summary.html`. Las credenciales del cliente técnico local se configuran con variables `KARATE_*`; el cliente SPA conserva PKCE y Direct Access Grants deshabilitado. Consulta la [matriz formal A1–A7](docs/acceptance-criteria.md).

## Helm

El chart de Kubernetes está en `infrastructure/helm/operational-requests` y despliega los cuatro componentes propios. SQL Server, Kafka y Keycloak son dependencias externas. Consulta `infrastructure/helm/README.md` para `helm lint`, `helm template`, Secret externo, instalación, actualización y desinstalación.

## Estado y limitaciones

Implementados: Fases 1–4 y pruebas de aceptación Karate de la Fase 5 (A1-A4 y comprobación reproducible A7), con matriz formal A1-A7. Pendientes: Helm, GitLab CI y automatización de navegador para A6. No existe configuración runtime externa: las URLs públicas quedan incorporadas al build. El login interactivo requiere navegador. Los bundles MUI/Storybook conservan advertencias de tamaño que deberán optimizarse si las métricas productivas lo justifican.
