package co.com.operationalrequests.requests.application.usecase;

import co.com.operationalrequests.requests.application.command.ActorActual;
import co.com.operationalrequests.requests.application.command.CrearSolicitudCommand;
import co.com.operationalrequests.requests.application.command.TransicionarSolicitudCommand;
import co.com.operationalrequests.requests.application.port.in.RequestsUseCase;
import co.com.operationalrequests.requests.application.port.out.CategoriaRepositoryPort;
import co.com.operationalrequests.requests.application.port.out.ClockPort;
import co.com.operationalrequests.requests.application.port.out.OutboxPort;
import co.com.operationalrequests.requests.application.port.out.RequestNumberPort;
import co.com.operationalrequests.requests.application.port.out.SolicitudRepositoryPort;
import co.com.operationalrequests.requests.application.query.PageResult;
import co.com.operationalrequests.requests.application.query.SolicitudCriteria;
import co.com.operationalrequests.requests.application.query.SolicitudDetail;
import co.com.operationalrequests.requests.application.query.SolicitudSummary;
import co.com.operationalrequests.requests.domain.exception.AccesoDominioNoPermitidoException;
import co.com.operationalrequests.requests.domain.exception.CategoriaNoDisponibleException;
import co.com.operationalrequests.requests.domain.exception.ConflictoAsignacionException;
import co.com.operationalrequests.requests.domain.exception.SolicitudNoEncontradaException;
import co.com.operationalrequests.requests.domain.model.Categoria;
import co.com.operationalrequests.requests.domain.model.EstadoSolicitud;
import co.com.operationalrequests.requests.domain.model.HistorialEstado;
import co.com.operationalrequests.requests.domain.model.RolActor;
import co.com.operationalrequests.requests.domain.model.Solicitud;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestsApplicationService implements RequestsUseCase {
    private final CategoriaRepositoryPort categories;
    private final SolicitudRepositoryPort requests;
    private final OutboxPort outbox;
    private final RequestNumberPort numbers;
    private final ClockPort clock;

    public RequestsApplicationService(CategoriaRepositoryPort categories, SolicitudRepositoryPort requests,
            OutboxPort outbox, RequestNumberPort numbers, ClockPort clock) {
        this.categories = categories; this.requests = requests; this.outbox = outbox;
        this.numbers = numbers; this.clock = clock;
    }

    @Override @Transactional(readOnly = true)
    public List<Categoria> categories() { return categories.findActive(); }

    @Override @Transactional
    public SolicitudDetail create(CrearSolicitudCommand command) {
        requireRole(command.actor(), RolActor.SOLICITANTE);
        categories.findActiveById(command.categoriaId())
                .orElseThrow(() -> new CategoriaNoDisponibleException(command.categoriaId()));
        Instant now = clock.now();
        Solicitud request = Solicitud.crear(UUID.randomUUID(), numbers.next(now), command.asunto(),
                command.categoriaId(), command.actor().id(), command.prioridad(), command.descripcion(), now);
        requests.save(request);
        appendEvent("SolicitudRegistrada", request, command.actor().rolOperacional(), command.correlationId(), now);
        return toDetail(request);
    }

    @Override @Transactional(readOnly = true)
    public PageResult<SolicitudSummary> list(SolicitudCriteria criteria, ActorActual actor) {
        UUID owner = actor.tiene(RolActor.SOLICITANTE) && !actor.tiene(RolActor.ANALISTA)
                && !actor.tiene(RolActor.SUPERVISOR) ? actor.id() : null;
        return requests.search(new SolicitudCriteria(criteria.estado(), criteria.categoriaId(), criteria.prioridad(),
                owner, Math.max(0, criteria.page()), Math.min(Math.max(criteria.size(), 1), 100)));
    }

    @Override @Transactional(readOnly = true)
    public SolicitudDetail detail(UUID id, ActorActual actor) {
        Solicitud request = get(id);
        assertVisible(request, actor);
        return toDetail(request);
    }

    @Override @Transactional
    public SolicitudDetail assign(UUID id, ActorActual actor, UUID correlationId) {
        requireRole(actor, RolActor.ANALISTA);
        get(id); // diferencia 404 de un conflicto sobre un identificador existente
        Instant now = clock.now();
        if (!requests.assignIfRegistered(id, actor.id(), now)) {
            throw new ConflictoAsignacionException("La solicitud ya fue tomada o no está REGISTRADA");
        }
        requests.addHistory(id, new HistorialEstado(UUID.randomUUID(), EstadoSolicitud.REGISTRADA,
                EstadoSolicitud.EN_ATENCION, actor.id(), RolActor.ANALISTA, null, now));
        Solicitud assigned = get(id);
        appendEvent("SolicitudTomada", assigned, RolActor.ANALISTA, correlationId, now,
                Map.of("fromStatus", EstadoSolicitud.REGISTRADA.name()));
        return toDetail(assigned);
    }

    @Override @Transactional
    public SolicitudDetail observe(UUID id, String text, ActorActual actor) {
        requireRole(actor, RolActor.ANALISTA);
        Solicitud request = get(id);
        request.agregarObservacion(actor.id(), RolActor.ANALISTA, text, clock.now());
        return toDetail(requests.save(request));
    }

    @Override @Transactional
    public SolicitudDetail transition(TransicionarSolicitudCommand command) {
        Solicitud request = get(command.solicitudId());
        Instant now = clock.now();
        if (command.actor().tiene(RolActor.ANALISTA) && command.destino() == EstadoSolicitud.RESUELTA) {
            request.resolver(command.actor().id(), command.observacion(), now);
            requests.save(request);
            appendEvent("SolicitudResuelta", request, RolActor.ANALISTA, command.correlationId(), now);
        } else if (command.actor().tiene(RolActor.SUPERVISOR)
                && command.destino() == EstadoSolicitud.EN_ATENCION) {
            request.devolver(command.actor().id(), command.motivo(), now);
            requests.save(request);
        } else if (command.actor().tiene(RolActor.SUPERVISOR)
                && command.destino() == EstadoSolicitud.CERRADA) {
            request.cerrar(command.actor().id(), command.motivo(), now);
            requests.save(request);
            appendEvent("SolicitudCerrada", request, RolActor.SUPERVISOR, command.correlationId(), now);
        } else {
            throw new AccesoDominioNoPermitidoException("El rol no puede realizar la transición solicitada");
        }
        return toDetail(request);
    }

    private Solicitud get(UUID id) {
        return requests.findDetailById(id).orElseThrow(() -> new SolicitudNoEncontradaException(id));
    }
    private static void requireRole(ActorActual actor, RolActor role) {
        if (!actor.tiene(role)) throw new AccesoDominioNoPermitidoException("Se requiere el rol " + role);
    }
    private static void assertVisible(Solicitud request, ActorActual actor) {
        boolean requesterOnly = actor.tiene(RolActor.SOLICITANTE) && !actor.tiene(RolActor.ANALISTA)
                && !actor.tiene(RolActor.SUPERVISOR);
        if (requesterOnly && !request.solicitanteId().equals(actor.id())) {
            throw new AccesoDominioNoPermitidoException("No puede consultar solicitudes de otro solicitante");
        }
    }
    private void appendEvent(String type, Solicitud request, RolActor role, UUID correlationId, Instant now) {
        appendEvent(type, request, role, correlationId, now, Map.of());
    }
    private void appendEvent(String type, Solicitud request, RolActor role, UUID correlationId, Instant now,
            Map<String, Object> extra) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("requestId", request.id()); payload.put("readableId", request.numero());
        payload.put("categoryId", request.categoriaId()); payload.put("status", request.estado().name());
        payload.put("priority", request.prioridad().name()); payload.put("actorRole", role.name());
        payload.put("occurredAt", now); payload.putAll(extra);
        outbox.append(UUID.randomUUID(), request.id(), type, 1, correlationId, now, payload);
    }
    private SolicitudDetail toDetail(Solicitud r) {
        String categoryName = categories.findActiveById(r.categoriaId()).map(Categoria::nombre).orElse(null);
        return new SolicitudDetail(r.id(), r.numero(), r.asunto(), r.descripcion(), r.categoriaId(), categoryName, r.prioridad(),
                r.estado(), r.solicitanteId(), r.analistaAsignadoId(), r.creadaEn(), r.actualizadaEn(),
                r.observaciones(), r.historial());
    }
}
