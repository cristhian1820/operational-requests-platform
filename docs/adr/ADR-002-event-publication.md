# ADR-002: publicación Outbox y entrega al menos una vez

Estado: aceptada e implementada.

La transacción operacional inserta negocio, historial y `outbox_event=PENDING`; nunca llama Kafka. Un scheduler reclama como máximo un lote mediante `UPDLOCK, READPAST, ROWLOCK`, cambia filas a `PROCESSING` y confirma esa transacción antes de esperar la respuesta del broker. El record usa `aggregateId` como key y añade headers `eventId`, `eventType`, `eventVersion` y `correlationId`.

Con confirmación Kafka se marca `PUBLISHED`. Un fallo incrementa `attempts`, limpia el lock y vuelve a `PENDING` con backoff lineal; al alcanzar el máximo pasa a `FAILED`. Un `PROCESSING` cuyo `locked_at` expiró vuelve a ser reclamable. Se registra solo id/tipo/intento, nunca payload.

Si Kafka confirma y el proceso cae antes del update, el evento será republicado. Por ello la garantía real es **al menos una vez**, no exactamente una vez. El consumidor inserta primero `processed_event` bajo PK única y, en la misma transacción, actualiza dimensiones, hecho y estado actual. Una violación de PK significa duplicado exitosamente ignorado. Eventos inválidos se reintentan dos veces y luego van al topic DLT versionado.

Local usa tres particiones y réplica 1 por existir un broker. Producción debe dimensionar particiones según throughput y usar réplica mínima 3 con `min.insync.replicas` apropiado.
