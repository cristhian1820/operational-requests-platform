# Matriz formal de aceptación A1-A7

La matriz usa exactamente los criterios definidos para la prueba técnica. Karate se ejecuta contra los contenedores levantados y usa el cliente técnico local `operational-requests-acceptance`; el cliente SPA público conserva Authorization Code + PKCE.

| Criterio | Escenario | Prueba automatizada | Evidencia | Comando | Resultado |
|---|---|---|---|---|---|
| A1 | Crear una solicitud correctamente | `requests-api.feature`: creación, estado `REGISTRADA`, historial y consulta posterior | HTTP 201, `id`, `categoriaId`, `numero` y detalle | `./mvnw -Pacceptance -pl tests/karate -am test` | PASS: 4 escenarios Karate, 0 fallos |
| A2 | Dos analistas intentan asignar la misma solicitud | `requests-api.feature`: dos peticiones HTTP concurrentes mediante `ConcurrentAssignment` | Una respuesta 200 y una 409; detalle queda `EN_ATENCION` | `./mvnw -Pacceptance -pl tests/karate -am test` | PASS: 4 escenarios Karate, 0 fallos |
| A3 | SOLICITANTE ejecuta operación restringida | `requests-api.feature`: solicitante intenta asignar | HTTP 403 y `ACCESS_DENIED` | `./mvnw -Pacceptance -pl tests/karate -am test` | PASS: 4 escenarios Karate, 0 fallos |
| A4 | Transición no permitida | `requests-api.feature`: supervisor intenta `REGISTRADA -> CERRADA` | HTTP 422 y `INVALID_TRANSITION` | `./mvnw -Pacceptance -pl tests/karate -am test` | PASS: 4 escenarios Karate, 0 fallos |
| A5 | Consumir dos veces el mismo evento no duplica hechos ni indicadores | `AnalyticsServiceTest.duplicateEventDoesNotDuplicateFactOrProjection` | `processed_event`, hecho y proyección se escriben una sola vez | `./mvnw -pl backend/indicators-service -Dtest=AnalyticsServiceTest test` | Cubierto por prueba automatizada del servicio |
| A6 | Recargar directamente `/solicitudes/{id}` conserva y muestra detalle | `routing.test.tsx`, `detail-category.test.tsx` y validación manual documentada | El shell recupera sesión, consulta detalle y muestra nombre de categoría | `npm run test` desde `frontend/` + validación browser | Validado manualmente; sin E2E browser automatizado |
| A7 | Docker Compose inicia y deja servicios saludables | `tests/karate/verify-compose.ps1` | Configuración válida, ocho servicios principales healthy y endpoints HTTP 200 | `powershell -ExecutionPolicy Bypass -File tests/karate/verify-compose.ps1` | PASS: Compose config, health checks y HTTP 200 verificados |

## Alcance y limitaciones

- Las credenciales son exclusivamente locales y se leen desde variables `KARATE_*`.
- El cliente de aceptación tiene Direct Access Grants habilitado únicamente para pruebas no interactivas locales; el cliente SPA `operational-requests-web` mantiene PKCE y Direct Access Grants deshabilitado.
- A5 se valida con la prueba transaccional del consumidor, no mediante publicación manual frágil desde Karate.
- A6 conserva las pruebas frontend y la evidencia manual; no se afirma una prueba de navegador automatizada.
