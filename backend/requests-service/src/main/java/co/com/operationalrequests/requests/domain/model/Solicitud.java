package co.com.operationalrequests.requests.domain.model;

import co.com.operationalrequests.requests.domain.exception.TransicionEstadoInvalidaException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Solicitud {
    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES = Map.of(
            EstadoSolicitud.REGISTRADA, Set.of(EstadoSolicitud.EN_ATENCION),
            EstadoSolicitud.EN_ATENCION, Set.of(EstadoSolicitud.RESUELTA),
            EstadoSolicitud.RESUELTA, Set.of(EstadoSolicitud.EN_ATENCION, EstadoSolicitud.CERRADA),
            EstadoSolicitud.CERRADA, Set.of());

    private final UUID id;
    private final String numero;
    private final Categoria categoria;
    private final UUID solicitanteId;
    private final Prioridad prioridad;
    private final String descripcion;
    private final Instant creadaEn;
    private final List<HistorialEstado> historial = new ArrayList<>();
    private EstadoSolicitud estado;

    public Solicitud(
            UUID id,
            String numero,
            Categoria categoria,
            UUID solicitanteId,
            Prioridad prioridad,
            String descripcion,
            Instant creadaEn) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.numero = textoObligatorio(numero, "El número de solicitud es obligatorio");
        this.categoria = Objects.requireNonNull(categoria, "La categoría es obligatoria");
        this.solicitanteId = Objects.requireNonNull(solicitanteId, "El solicitante es obligatorio");
        this.prioridad = Objects.requireNonNull(prioridad, "La prioridad es obligatoria");
        this.descripcion = textoObligatorio(descripcion, "La descripción es obligatoria");
        this.creadaEn = Objects.requireNonNull(creadaEn, "La fecha de creación es obligatoria");
        this.estado = EstadoSolicitud.REGISTRADA;
    }

    public void cambiarEstado(EstadoSolicitud nuevo, UUID actorId, RolActor rol, Instant ocurridoEn) {
        Objects.requireNonNull(nuevo, "El estado destino es obligatorio");
        Objects.requireNonNull(actorId, "El actor es obligatorio");
        Objects.requireNonNull(rol, "El rol del actor es obligatorio");
        Objects.requireNonNull(ocurridoEn, "La fecha de transición es obligatoria");

        if (!TRANSICIONES.getOrDefault(estado, Set.of()).contains(nuevo)) {
            throw new TransicionEstadoInvalidaException(estado, nuevo);
        }

        EstadoSolicitud anterior = estado;
        estado = nuevo;
        historial.add(new HistorialEstado(UUID.randomUUID(), anterior, nuevo, actorId, rol, ocurridoEn));
    }

    private static String textoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    public UUID id() { return id; }
    public String numero() { return numero; }
    public EstadoSolicitud estado() { return estado; }
    public List<HistorialEstado> historial() { return List.copyOf(historial); }
}
