package co.com.operationalrequests.requests.application.port.out;

import co.com.operationalrequests.requests.application.query.PageResult;
import co.com.operationalrequests.requests.application.query.SolicitudCriteria;
import co.com.operationalrequests.requests.application.query.SolicitudSummary;
import co.com.operationalrequests.requests.domain.model.HistorialEstado;
import co.com.operationalrequests.requests.domain.model.Solicitud;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudRepositoryPort {
    Solicitud save(Solicitud solicitud);

    Optional<Solicitud> findDetailById(UUID id);

    PageResult<SolicitudSummary> search(SolicitudCriteria criteria);

    boolean assignIfRegistered(UUID id, UUID analystId, Instant now);

    void addHistory(UUID requestId, HistorialEstado history);
}
