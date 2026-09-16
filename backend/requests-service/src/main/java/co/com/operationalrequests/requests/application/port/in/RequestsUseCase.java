package co.com.operationalrequests.requests.application.port.in;
import co.com.operationalrequests.requests.application.command.ActorActual;
import co.com.operationalrequests.requests.application.command.CrearSolicitudCommand;
import co.com.operationalrequests.requests.application.command.TransicionarSolicitudCommand;
import co.com.operationalrequests.requests.application.query.PageResult;
import co.com.operationalrequests.requests.application.query.SolicitudCriteria;
import co.com.operationalrequests.requests.application.query.SolicitudDetail;
import co.com.operationalrequests.requests.application.query.SolicitudSummary;
import co.com.operationalrequests.requests.domain.model.Categoria;
import java.util.List;
import java.util.UUID;
public interface RequestsUseCase {
    List<Categoria> categories();
    SolicitudDetail create(CrearSolicitudCommand command);
    PageResult<SolicitudSummary> list(SolicitudCriteria criteria, ActorActual actor);
    SolicitudDetail detail(UUID id, ActorActual actor);
    SolicitudDetail assign(UUID id, ActorActual actor, UUID correlationId);
    SolicitudDetail observe(UUID id, String text, ActorActual actor);
    SolicitudDetail transition(TransicionarSolicitudCommand command);
}
