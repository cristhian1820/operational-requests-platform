package co.com.operationalrequests.requests.application.query;
import co.com.operationalrequests.requests.domain.model.EstadoSolicitud;
import co.com.operationalrequests.requests.domain.model.HistorialEstado;
import co.com.operationalrequests.requests.domain.model.Observacion;
import co.com.operationalrequests.requests.domain.model.Prioridad;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
public record SolicitudDetail(UUID id, String numero, String asunto, String descripcion, UUID categoriaId,
        Prioridad prioridad, EstadoSolicitud estado, UUID solicitanteId, UUID analistaAsignadoId,
        Instant creadaEn, Instant actualizadaEn, List<Observacion> observaciones, List<HistorialEstado> historial) {}
