# ADR-002: publicación de eventos

Estado: aceptada e implementada parcialmente.

`RequestsApplicationService` está delimitado con `@Transactional`: guarda solicitud e historial y el adaptador JPA inserta el sobre completo en `outbox_event` con estado `PENDING`, todo en la misma transacción SQL Server. Se crean `SolicitudRegistrada`, `SolicitudTomada`, `SolicitudResuelta` y `SolicitudCerrada`. La devolución a atención no produce evento en esta versión porque no es uno de los cuatro contratos mínimos; sí queda en historial.

Publicar antes del commit podría emitir un hecho que después se revierte. Un proceso futuro —no incluido en Fase 2— reclamará pendientes, publicará usando `aggregateId` como clave y marcará `published_at`. Si falla entre publicar y marcar, repetirá: la entrega es al menos una vez. El consumidor deduplicará por `eventId` mediante `processed_event` único. El payload analítico excluye asunto, descripción y datos personales.
