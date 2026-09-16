package co.com.operationalrequests.requests.application.command;
import co.com.operationalrequests.requests.domain.model.Prioridad;
import java.util.UUID;
public record CrearSolicitudCommand(String asunto, String descripcion, UUID categoriaId, Prioridad prioridad,
        ActorActual actor, UUID correlationId) {}
