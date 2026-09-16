# ADR-003: control de concurrencia

Estado: aceptada e implementada.

`operational_request.version` conserva control optimista para escrituras ordinarias. La toma usa un único `UPDATE operational_request SET status='EN_ATENCION', assigned_actor_id=?, version=version+1 ... WHERE id=? AND status='REGISTRADA'`. El contexto JPA se limpia después del SQL nativo para no devolver estado obsoleto.

Exactamente un analista puede afectar una fila. Para un identificador existente, cero filas actualizadas significa conflicto y la API responde HTTP 409; un identificador inexistente responde 404 antes del intento. Esto evita el patrón inseguro leer-modificar-guardar, bloqueos largos y coordinación entre réplicas. Existe prueba unitaria del resultado ganador/perdedor; una prueba simultánea contra SQL Server con Testcontainers queda como limitación.
