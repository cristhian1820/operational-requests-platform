# ADR-002: publicación de eventos

Estado: aceptada.

La escritura operacional y `outbox_event` ocurren en la misma transacción. Publicar antes del commit podría emitir un hecho que después se revierte. Un proceso futuro leerá pendientes, publicará y marcará; si falla entre ambos pasos repetirá, por lo que la entrega es al menos una vez. Cada consumidor deduplica por `eventId` mediante `processed_event` único.
