# Pruebas Karate y aceptación

La suite usa Karate 1.4.1 y se ejecuta contra los servicios reales. Requiere Docker Compose levantado, Java 21 y Maven Wrapper.

PowerShell:

```powershell
Copy-Item .env.example .env
docker compose --env-file .env up --build -d
.\mvnw.cmd -Pacceptance -pl tests/karate -am test
powershell -ExecutionPolicy Bypass -File tests/karate/verify-compose.ps1
```

Bash:

```bash
cp .env.example .env
docker compose --env-file .env up --build -d
./mvnw -Pacceptance -pl tests/karate -am test
```

Los tokens se obtienen desde Keycloak con el cliente técnico local `operational-requests-acceptance`. Sus usuarios y contraseñas se configuran mediante `KARATE_*`; nunca se usan credenciales productivas. El cliente SPA público mantiene PKCE y Direct Access Grants deshabilitado.

Reporte HTML: `tests/karate/target/karate-reports/karate-summary.html`.
