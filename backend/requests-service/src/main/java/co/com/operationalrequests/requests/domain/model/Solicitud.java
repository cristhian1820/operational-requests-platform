package co.com.operationalrequests.requests.domain.model;

import co.com.operationalrequests.requests.domain.exception.AccesoDominioNoPermitidoException;
import co.com.operationalrequests.requests.domain.exception.TransicionEstadoInvalidaException;

import java.time.Instant;
import java.util.*;

public final class Solicitud {
    private static final Map<EstadoSolicitud, Set<EstadoSolicitud>> TRANSICIONES = Map.of(
            EstadoSolicitud.REGISTRADA, Set.of(EstadoSolicitud.EN_ATENCION),
            EstadoSolicitud.EN_ATENCION, Set.of(EstadoSolicitud.RESUELTA),
            EstadoSolicitud.RESUELTA, Set.of(EstadoSolicitud.EN_ATENCION, EstadoSolicitud.CERRADA),
            EstadoSolicitud.CERRADA, Set.of());

    private final UUID id;
    private final String numero;
    private final String asunto;
    private final UUID categoriaId;
    private final UUID solicitanteId;
    private final Prioridad prioridad;
    private final String descripcion;
    private final Instant creadaEn;
    private final List<Observacion> observaciones;
    private final List<HistorialEstado> historial;
    private EstadoSolicitud estado;
    private UUID analistaAsignadoId;
    private Instant actualizadaEn;
    private final long version;

    private Solicitud(UUID id, String numero, String asunto, UUID categoriaId, UUID solicitanteId,
                      Prioridad prioridad, String descripcion, EstadoSolicitud estado, UUID analistaAsignadoId,
                      Instant creadaEn, Instant actualizadaEn, long version, List<Observacion> observaciones,
                      List<HistorialEstado> historial) {
        this.id = Objects.requireNonNull(id);
        this.numero = requerido(numero, "El número es obligatorio", 24);
        this.asunto = requerido(asunto, "El asunto es obligatorio", 200);
        this.categoriaId = Objects.requireNonNull(categoriaId);
        this.solicitanteId = Objects.requireNonNull(solicitanteId);
        this.prioridad = Objects.requireNonNull(prioridad);
        this.descripcion = requerido(descripcion, "La descripción es obligatoria", 2000);
        this.estado = Objects.requireNonNull(estado);
        this.analistaAsignadoId = analistaAsignadoId;
        this.creadaEn = Objects.requireNonNull(creadaEn);
        this.actualizadaEn = Objects.requireNonNull(actualizadaEn);
        this.version = version;
        this.observaciones = new ArrayList<>(observaciones);
        this.historial = new ArrayList<>(historial);
    }

    public static Solicitud crear(UUID id, String numero, String asunto, UUID categoriaId,
                                  UUID solicitanteId, Prioridad prioridad, String descripcion, Instant ahora) {
        var inicial = new HistorialEstado(UUID.randomUUID(), null, EstadoSolicitud.REGISTRADA,
                solicitanteId, RolActor.SOLICITANTE, null, ahora);
        return new Solicitud(id, numero, asunto, categoriaId, solicitanteId, prioridad, descripcion,
                EstadoSolicitud.REGISTRADA, null, ahora, ahora, 0, List.of(), List.of(inicial));
    }

    public static Solicitud reconstruir(UUID id, String numero, String asunto, UUID categoriaId,
                                        UUID solicitanteId, Prioridad prioridad, String descripcion, EstadoSolicitud estado,
                                        UUID analistaAsignadoId, Instant creadaEn, Instant actualizadaEn, long version,
                                        List<Observacion> observaciones, List<HistorialEstado> historial) {
        return new Solicitud(id, numero, asunto, categoriaId, solicitanteId, prioridad, descripcion,
                estado, analistaAsignadoId, creadaEn, actualizadaEn, version, observaciones, historial);
    }

    public void tomar(UUID analistaId, Instant ahora) {
        transicionar(EstadoSolicitud.EN_ATENCION, analistaId, RolActor.ANALISTA, null, ahora);
        analistaAsignadoId = analistaId;
    }

    public void agregarObservacion(UUID actorId, RolActor rol, String texto, Instant ahora) {
        if (rol != RolActor.ANALISTA || !actorId.equals(analistaAsignadoId)) {
            throw new AccesoDominioNoPermitidoException("Solo el analista asignado puede agregar observaciones");
        }
        observaciones.add(new Observacion(UUID.randomUUID(), actorId, rol, texto, ahora));
        actualizadaEn = ahora;
    }

    public void resolver(UUID analistaId, String observacion, Instant ahora) {
        if (!analistaId.equals(analistaAsignadoId)) {
            throw new AccesoDominioNoPermitidoException("Solo el analista asignado puede resolver la solicitud");
        }
        if (observacion != null && !observacion.isBlank()) {
            agregarObservacion(analistaId, RolActor.ANALISTA, observacion, ahora);
        }
        transicionar(EstadoSolicitud.RESUELTA, analistaId, RolActor.ANALISTA, null, ahora);
    }

    public void devolver(UUID supervisorId, String motivo, Instant ahora) {
        transicionar(EstadoSolicitud.EN_ATENCION, supervisorId, RolActor.SUPERVISOR,
                requerido(motivo, "El motivo es obligatorio", 500), ahora);
    }

    public void cerrar(UUID supervisorId, String motivo, Instant ahora) {
        transicionar(EstadoSolicitud.CERRADA, supervisorId, RolActor.SUPERVISOR,
                requerido(motivo, "El motivo es obligatorio", 500), ahora);
    }

    /**
     * Aplica la maquina de estados sin asociarla a un permiso HTTP concreto. Los casos de uso
     * siguen siendo responsables de decidir qué rol puede solicitar cada transición.
     */
    public void cambiarEstado(EstadoSolicitud destino, UUID actorId, RolActor rol, String motivo, Instant ahora) {
        transicionar(destino, actorId, rol, motivo, ahora);
    }

    private void transicionar(EstadoSolicitud destino, UUID actorId, RolActor rol, String motivo, Instant ahora) {
        Objects.requireNonNull(destino);
        Objects.requireNonNull(actorId);
        Objects.requireNonNull(rol);
        Objects.requireNonNull(ahora);
        if (!TRANSICIONES.getOrDefault(estado, Set.of()).contains(destino)) {
            throw new TransicionEstadoInvalidaException(estado, destino);
        }
        EstadoSolicitud anterior = estado;
        estado = destino;
        actualizadaEn = ahora;
        historial.add(new HistorialEstado(UUID.randomUUID(), anterior, destino, actorId, rol, motivo, ahora));
    }

    private static String requerido(String valor, String mensaje, int maximo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensaje);
        String limpio = valor.trim();
        if (limpio.length() > maximo) throw new IllegalArgumentException("Longitud máxima excedida: " + maximo);
        return limpio;
    }

    public UUID id() {
        return id;
    }

    public String numero() {
        return numero;
    }

    public String asunto() {
        return asunto;
    }

    public UUID categoriaId() {
        return categoriaId;
    }

    public UUID solicitanteId() {
        return solicitanteId;
    }

    public Prioridad prioridad() {
        return prioridad;
    }

    public String descripcion() {
        return descripcion;
    }

    public EstadoSolicitud estado() {
        return estado;
    }

    public UUID analistaAsignadoId() {
        return analistaAsignadoId;
    }

    public Instant creadaEn() {
        return creadaEn;
    }

    public Instant actualizadaEn() {
        return actualizadaEn;
    }

    public long version() {
        return version;
    }

    public List<Observacion> observaciones() {
        return List.copyOf(observaciones);
    }

    public List<HistorialEstado> historial() {
        return List.copyOf(historial);
    }
}
