package co.com.operationalrequests.requests.application.command;
import co.com.operationalrequests.requests.domain.model.RolActor;
import java.util.Set;
import java.util.UUID;
public record ActorActual(UUID id, String username, Set<RolActor> roles) {
    public boolean tiene(RolActor rol) { return roles.contains(rol); }
    public RolActor rolOperacional() {
        if (roles.contains(RolActor.SUPERVISOR)) return RolActor.SUPERVISOR;
        if (roles.contains(RolActor.ANALISTA)) return RolActor.ANALISTA;
        return RolActor.SOLICITANTE;
    }
}
