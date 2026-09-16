# ADR-001: arquitectura hexagonal

Estado: aceptada.

`requests-service` separa dominio, aplicación y adaptadores. El dominio contiene reglas y modelos Java puros; aplicación define puertos y orquesta; infraestructura traduce HTTP, JPA, Kafka y OAuth2. Las dependencias apuntan hacia el dominio. Esto permite probar reglas sin Spring y reemplazar adaptadores sin alterar la lógica.

Se descartó una arquitectura por capas técnicas sin puertos porque facilita dependencias desde negocio hacia JPA/HTTP. También se descartó CQRS con almacenes separados en esta fase por complejidad prematura.
