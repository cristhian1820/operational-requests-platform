package co.com.operationalrequests.requests.application.query;

import co.com.operationalrequests.requests.domain.model.EstadoSolicitud;
import co.com.operationalrequests.requests.domain.model.Prioridad;

import java.util.UUID;

public record SolicitudCriteria(EstadoSolicitud estado, UUID categoriaId, Prioridad prioridad,
                                UUID solicitanteId, int page, int size) {
}
