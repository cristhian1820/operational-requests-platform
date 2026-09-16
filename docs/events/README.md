# Contrato de eventos v1

Todos los eventos usan el sobre siguiente. `aggregateId` (UUID de la solicitud) es la clave de partición Kafka para conservar orden por agregado. `version` empieza en `1` y solo aumenta ante cambios incompatibles, manteniendo consumidores tolerantes a campos nuevos.

```json
{"eventId":"9f247210-e250-48d6-9c17-132c9d4450b2","occurredAt":"2026-09-16T14:30:00Z","aggregateId":"2eb194f2-f94c-4d3e-94aa-535295432098","type":"SolicitudRegistrada","version":1,"correlationId":"d21b4ff2-ab4d-4b36-907c-cf7ae5758b40","payload":{"requestNumber":"SOL-2026-000001","categoryId":"11111111-1111-1111-1111-111111111111","priority":"MEDIA","actorRole":"SOLICITANTE"}}
```

Tipos y payloads:

- `SolicitudRegistrada`: `requestNumber`, `categoryId`, `priority`, `actorRole`.
- `SolicitudTomada`: `fromStatus`, `toStatus`, `actorRole`.
- `SolicitudResuelta`: `fromStatus`, `toStatus`, `actorRole`.
- `SolicitudCerrada`: `fromStatus`, `toStatus`, `actorRole`.

Ejemplos de transición (cada objeto es JSON válido):

```json
{"eventId":"a6c79401-4bc7-4e9b-8240-83430631372f","occurredAt":"2026-09-16T14:35:00Z","aggregateId":"2eb194f2-f94c-4d3e-94aa-535295432098","type":"SolicitudTomada","version":1,"correlationId":"d21b4ff2-ab4d-4b36-907c-cf7ae5758b40","payload":{"fromStatus":"REGISTRADA","toStatus":"EN_ATENCION","actorRole":"ANALISTA"}}
```
```json
{"eventId":"b59851bf-d75a-4eb6-97a0-13544577d990","occurredAt":"2026-09-16T15:20:00Z","aggregateId":"2eb194f2-f94c-4d3e-94aa-535295432098","type":"SolicitudResuelta","version":1,"correlationId":"d21b4ff2-ab4d-4b36-907c-cf7ae5758b40","payload":{"fromStatus":"EN_ATENCION","toStatus":"RESUELTA","actorRole":"ANALISTA"}}
```
```json
{"eventId":"456f55c1-7004-437c-a16a-b0dcc08b431a","occurredAt":"2026-09-16T16:00:00Z","aggregateId":"2eb194f2-f94c-4d3e-94aa-535295432098","type":"SolicitudCerrada","version":1,"correlationId":"d21b4ff2-ab4d-4b36-907c-cf7ae5758b40","payload":{"fromStatus":"RESUELTA","toStatus":"CERRADA","actorRole":"SOLICITANTE"}}
```

La transacción de negocio inserta el evento en `outbox_event`; un publicador posterior lo envía y marca como publicado. Nunca se publica antes del commit. Los reintentos conservan `eventId`. Kafka y el publicador ofrecen semántica al menos una vez, por lo que el consumidor primero registra `eventId` en `processed_event`; la restricción única convierte duplicados en no-op.
