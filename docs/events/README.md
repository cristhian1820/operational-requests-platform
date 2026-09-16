# Contrato `operational-requests.events.v1`

El valor es JSON y `aggregateId` es la key Kafka para ordenar eventos de una solicitud. El sobre compatible v1 es:

```json
{"eventId":"9f247210-e250-48d6-9c17-132c9d4450b2","occurredAt":"2026-09-16T14:30:00Z","aggregateId":"2eb194f2-f94c-4d3e-94aa-535295432098","type":"SolicitudRegistrada","version":1,"correlationId":"d21b4ff2-ab4d-4b36-907c-cf7ae5758b40","payload":{"requestId":"2eb194f2-f94c-4d3e-94aa-535295432098","readableId":"SOL-2026-000001","categoryId":"11111111-1111-1111-1111-111111111111","status":"REGISTRADA","priority":"MEDIA","actorRole":"SOLICITANTE","occurredAt":"2026-09-16T14:30:00Z"}}
```

Tipos v1: `SolicitudRegistrada`, `SolicitudTomada`, `SolicitudResuelta`, `SolicitudCerrada`. El payload mínimo sigue siendo compatible con Fase 2: requestId, readableId, categoryId, status, priority, actorRole y occurredAt. No contiene asunto, descripción, identidad, correo ni JWT.

Headers: `eventId`, `eventType`, `eventVersion`, `correlationId`. Versión distinta de 1, tipo desconocido o payload inválido se reintentan de forma limitada y terminan en `operational-requests.events.v1.dlt` sin modificar indicadores. La devolución RESUELTA → EN_ATENCION sigue registrada operacionalmente pero no emite evento v1.
