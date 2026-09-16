# Contrato de eventos v1

El Outbox persiste el sobre JSON completo. `aggregateId` —UUID de solicitud— será la clave de partición Kafka para preservar orden por agregado. `version` empieza en `1`; campos aditivos no lo incrementan, cambios incompatibles sí.

```json
{"eventId":"9f247210-e250-48d6-9c17-132c9d4450b2","occurredAt":"2026-09-16T14:30:00Z","aggregateId":"2eb194f2-f94c-4d3e-94aa-535295432098","type":"SolicitudRegistrada","version":1,"correlationId":"d21b4ff2-ab4d-4b36-907c-cf7ae5758b40","payload":{"requestId":"2eb194f2-f94c-4d3e-94aa-535295432098","readableId":"SOL-2026-000001","categoryId":"11111111-1111-1111-1111-111111111111","status":"REGISTRADA","priority":"MEDIA","actorRole":"SOLICITANTE","occurredAt":"2026-09-16T14:30:00Z"}}
```

Los tipos implementados son `SolicitudRegistrada`, `SolicitudTomada`, `SolicitudResuelta` y `SolicitudCerrada`. Todos llevan `requestId`, `readableId`, `categoryId`, `status`, `priority`, `actorRole` y `occurredAt`; `SolicitudTomada` añade `fromStatus`. No contienen asunto, descripción, correo ni nombre. La devolución de RESUELTA a EN_ATENCION queda en historial y no emite evento v1.

Ejemplo de transición:

```json
{"eventId":"456f55c1-7004-437c-a16a-b0dcc08b431a","occurredAt":"2026-09-16T16:00:00Z","aggregateId":"2eb194f2-f94c-4d3e-94aa-535295432098","type":"SolicitudCerrada","version":1,"correlationId":"d21b4ff2-ab4d-4b36-907c-cf7ae5758b40","payload":{"requestId":"2eb194f2-f94c-4d3e-94aa-535295432098","readableId":"SOL-2026-000001","categoryId":"11111111-1111-1111-1111-111111111111","status":"CERRADA","priority":"MEDIA","actorRole":"SUPERVISOR","occurredAt":"2026-09-16T16:00:00Z"}}
```

Solicitud, historial y Outbox `PENDING` confirman juntos. El publicador futuro reutilizará el mismo `eventId` en cada reintento. Como la entrega será al menos una vez, el consumidor deberá insertar primero `eventId` en `processed_event`; su restricción única convierte una repetición en no-op.
