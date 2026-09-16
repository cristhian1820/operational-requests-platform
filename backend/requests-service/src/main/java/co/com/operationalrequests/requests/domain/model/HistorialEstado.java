package co.com.operationalrequests.requests.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record HistorialEstado(
        UUID id,
        EstadoSolicitud estadoAnterior,
        EstadoSolicitud estadoNuevo,
        UUID actorId,
        RolActor rolActor,
        Instant ocurridoEn) {
    public HistorialEstado {
        Objects.requireNonNull(id, "El id del historial es obligatorio");
        Objects.requireNonNull(estadoAnterior, "El estado anterior es obligatorio");
        Objects.requireNonNull(estadoNuevo, "El estado nuevo es obligatorio");
        Objects.requireNonNull(actorId, "El actor es obligatorio");
        Objects.requireNonNull(rolActor, "El rol del actor es obligatorio");
        Objects.requireNonNull(ocurridoEn, "La fecha de transición es obligatoria");
    }
}
