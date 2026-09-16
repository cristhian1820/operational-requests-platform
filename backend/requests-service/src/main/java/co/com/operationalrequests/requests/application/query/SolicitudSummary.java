package co.com.operationalrequests.requests.application.query;
import co.com.operationalrequests.requests.domain.model.EstadoSolicitud;
import co.com.operationalrequests.requests.domain.model.Prioridad;
import java.time.Instant;
import java.util.UUID;
public record SolicitudSummary(UUID id, String numero, String asunto, UUID categoriaId, Prioridad prioridad,
        EstadoSolicitud estado, UUID solicitanteId, UUID analistaAsignadoId, Instant creadaEn, Instant actualizadaEn) {}
