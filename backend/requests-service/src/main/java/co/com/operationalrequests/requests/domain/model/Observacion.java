package co.com.operationalrequests.requests.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Observacion(UUID id, UUID actorId, RolActor rolActor, String texto, Instant creadaEn) {
    public Observacion {
        Objects.requireNonNull(id, "El id de observación es obligatorio");
        Objects.requireNonNull(actorId, "El actor es obligatorio");
        Objects.requireNonNull(rolActor, "El rol del actor es obligatorio");
        Objects.requireNonNull(creadaEn, "La fecha de creación es obligatoria");
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("La observación es obligatoria");
        }
        texto = texto.trim();
    }
}
