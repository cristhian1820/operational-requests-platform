package co.com.operationalrequests.requests.infrastructure.adapter.out.persistence;

import co.com.operationalrequests.requests.application.port.out.CategoriaRepositoryPort;
import co.com.operationalrequests.requests.application.port.out.RequestNumberPort;
import co.com.operationalrequests.requests.application.port.out.SolicitudRepositoryPort;
import co.com.operationalrequests.requests.application.query.*;
import co.com.operationalrequests.requests.domain.model.*;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class JpaPersistenceAdapter implements CategoriaRepositoryPort, SolicitudRepositoryPort, RequestNumberPort {
 private final CategoryJpaRepository categories; private final RequestJpaRepository requests;
 private final ObservationJpaRepository observations; private final StatusHistoryJpaRepository history; private final EntityManager entityManager;
 public JpaPersistenceAdapter(CategoryJpaRepository categories, RequestJpaRepository requests,
   ObservationJpaRepository observations, StatusHistoryJpaRepository history, EntityManager entityManager) {
  this.categories=categories;this.requests=requests;this.observations=observations;this.history=history;this.entityManager=entityManager;
 }
 @Override public List<Categoria> findActive(){return categories.findByActiveTrueOrderByNameAsc().stream().map(e->new Categoria(e.id,e.code,e.name)).toList();}
 @Override public Optional<Categoria> findActiveById(UUID id){return categories.findByIdAndActiveTrue(id).map(e->new Categoria(e.id,e.code,e.name));}
 @Override public Solicitud save(Solicitud s){
  RequestEntity e=requests.findById(s.id()).orElseGet(()->new RequestEntity(s.id()));
  e.requestNumber=s.numero();e.subject=s.asunto();e.categoryId=s.categoriaId();e.requesterId=s.solicitanteId();e.assignedActorId=s.analistaAsignadoId();
  e.status=s.estado().name();e.priority=s.prioridad().name();e.description=s.descripcion();e.createdAt=s.creadaEn();e.updatedAt=s.actualizadaEn();
  e=requests.save(e); observations.saveAll(s.observaciones().stream().map(o->toEntity(s.id(),o)).toList());
  history.saveAll(s.historial().stream().map(h->toEntity(s.id(),h)).toList()); return toDomain(e,true);
 }
 @Override public Optional<Solicitud> findDetailById(UUID id){return requests.findById(id).map(e->toDomain(e,true));}
 @Override public PageResult<SolicitudSummary> search(SolicitudCriteria c){
  Specification<RequestEntity> spec=(root,q,cb)->cb.conjunction();
  if(c.estado()!=null) spec=spec.and((r,q,cb)->cb.equal(r.get("status"),c.estado().name()));
  if(c.categoriaId()!=null) spec=spec.and((r,q,cb)->cb.equal(r.get("categoryId"),c.categoriaId()));
  if(c.prioridad()!=null) spec=spec.and((r,q,cb)->cb.equal(r.get("priority"),c.prioridad().name()));
  if(c.solicitanteId()!=null) spec=spec.and((r,q,cb)->cb.equal(r.get("requesterId"),c.solicitanteId()));
  var page=requests.findAll(spec,PageRequest.of(c.page(),c.size(),Sort.by(Sort.Direction.DESC,"createdAt")));
  return new PageResult<>(page.stream().map(this::summary).toList(),page.getNumber(),page.getSize(),page.getTotalElements(),page.getTotalPages());
 }
 @Override public boolean assignIfRegistered(UUID id,UUID analystId,Instant now){return requests.assignIfRegistered(id,analystId,now)==1;}
 @Override public void addHistory(UUID requestId,HistorialEstado h){history.save(toEntity(requestId,h));}
 @Override public String next(Instant now){long value=((Number)entityManager.createNativeQuery("SELECT NEXT VALUE FOR request_number_seq").getSingleResult()).longValue();return "SOL-%d-%06d".formatted(now.atZone(ZoneOffset.UTC).getYear(),value);}
 private Solicitud toDomain(RequestEntity e,boolean detail){
  List<Observacion> os=detail?observations.findByRequestIdOrderByCreatedAtAsc(e.id).stream().map(this::toDomain).toList():List.of();
  List<HistorialEstado> hs=detail?history.findByRequestIdOrderByOccurredAtAsc(e.id).stream().map(this::toDomain).toList():List.of();
  return Solicitud.reconstruir(e.id,e.requestNumber,e.subject,e.categoryId,e.requesterId,Prioridad.valueOf(e.priority),e.description,
    EstadoSolicitud.valueOf(e.status),e.assignedActorId,e.createdAt,e.updatedAt,e.version,os,hs);
 }
 private SolicitudSummary summary(RequestEntity e){return new SolicitudSummary(e.id,e.requestNumber,e.subject,e.categoryId,Prioridad.valueOf(e.priority),EstadoSolicitud.valueOf(e.status),e.requesterId,e.assignedActorId,e.createdAt,e.updatedAt);}
 private ObservationEntity toEntity(UUID requestId,Observacion o){var e=new ObservationEntity();e.id=o.id();e.requestId=requestId;e.actorId=o.actorId();e.actorRole=o.rolActor().name();e.body=o.texto();e.createdAt=o.creadaEn();return e;}
 private StatusHistoryEntity toEntity(UUID requestId,HistorialEstado h){var e=new StatusHistoryEntity();e.id=h.id();e.requestId=requestId;e.previousStatus=h.estadoAnterior()==null?null:h.estadoAnterior().name();e.newStatus=h.estadoNuevo().name();e.actorId=h.actorId();e.actorRole=h.rolActor().name();e.reason=h.motivo();e.occurredAt=h.ocurridoEn();return e;}
 private Observacion toDomain(ObservationEntity e){return new Observacion(e.id,e.actorId,RolActor.valueOf(e.actorRole),e.body,e.createdAt);}
 private HistorialEstado toDomain(StatusHistoryEntity e){return new HistorialEstado(e.id,e.previousStatus==null?null:EstadoSolicitud.valueOf(e.previousStatus),EstadoSolicitud.valueOf(e.newStatus),e.actorId,RolActor.valueOf(e.actorRole),e.reason,e.occurredAt);}
}
