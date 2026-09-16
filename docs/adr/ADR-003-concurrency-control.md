# ADR-003: control de concurrencia

Estado: aceptada.

`operational_request.version` soportará bloqueo optimista. Tomar una solicitud realizará una actualización condicional por `id`, estado `REGISTRADA` y versión esperada; exactamente un analista podrá cambiarla. Cero filas actualizadas significa conflicto concurrente y la API responderá HTTP 409, no 404. Esto evita bloqueos largos y funciona entre réplicas.
