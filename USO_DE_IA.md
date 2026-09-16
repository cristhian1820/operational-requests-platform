# Uso de IA

Se utilizó OpenAI Codex para inspeccionar el repositorio e implementar código de dominio, casos de uso, adaptadores JPA/REST, seguridad, migraciones, pruebas y documentación de la Fase 2, además de ejecutar verificaciones técnicas.

La IA no sustituye la revisión del candidato. Deben revisarse manualmente las reglas del dominio, amenazas, permisos OAuth2, SQL y planes de ejecución, compatibilidad de imágenes, licencias, accesibilidad y decisiones productivas. El candidato conserva la responsabilidad de arquitectura, seguridad, calidad, operación y de explicar cada decisión.

Se ejecutaron Maven `clean verify`, validación y construcción Compose, arranque con SQL Server real, migraciones Flyway, health checks, discovery OIDC, respuesta 401 sin JWT y acceso público local a OpenAPI. El flujo Authorization Code interactivo no se automatizó porque no existe frontend y no se habilitó Resource Owner Password Grant. Los resultados exactos —incluidos los defectos encontrados y corregidos durante el arranque— se informan en la entrega de la fase.
