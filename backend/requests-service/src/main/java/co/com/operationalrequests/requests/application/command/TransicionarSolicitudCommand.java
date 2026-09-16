package co.com.operationalrequests.requests.application.command;
import co.com.operationalrequests.requests.domain.model.EstadoSolicitud;
import java.util.UUID;
public record TransicionarSolicitudCommand(UUID solicitudId, EstadoSolicitud destino, String motivo,
        String observacion, ActorActual actor, UUID correlationId) {}
