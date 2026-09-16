package co.com.operationalrequests.requests.domain.model;
import co.com.operationalrequests.requests.domain.exception.*; import java.time.Instant; import java.util.*; import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class SolicitudTest {
 private static final UUID REQUESTER=UUID.randomUUID(),ANALYST=UUID.randomUUID(),CATEGORY=UUID.randomUUID();
 private Solicitud created(){return Solicitud.crear(UUID.randomUUID(),"SOL-2026-000001","Asunto",CATEGORY,REQUESTER,Prioridad.MEDIA,"Descripción",Instant.parse("2026-01-01T00:00:00Z"));}
 @Test void createsRegisteredWithInitialHistory(){var s=created();assertThat(s.estado()).isEqualTo(EstadoSolicitud.REGISTRADA);assertThat(s.historial()).singleElement().satisfies(h->{assertThat(h.estadoAnterior()).isNull();assertThat(h.estadoNuevo()).isEqualTo(EstadoSolicitud.REGISTRADA);});}
 @Test void supportsDefinedTransitions(){var s=created();var now=Instant.now();s.tomar(ANALYST,now);s.resolver(ANALYST,"Lista",now);s.devolver(UUID.randomUUID(),"Requiere ajuste",now);s.resolver(ANALYST,null,now);s.cerrar(UUID.randomUUID(),"Aceptada",now);assertThat(s.estado()).isEqualTo(EstadoSolicitud.CERRADA);}
 @Test void rejectsResolvedToRegistered(){var s=Solicitud.reconstruir(UUID.randomUUID(),"SOL-2026-1","A",CATEGORY,REQUESTER,Prioridad.ALTA,"D",EstadoSolicitud.RESUELTA,ANALYST,Instant.now(),Instant.now(),1,List.of(),List.of());assertThatThrownBy(()->s.cambiarEstado(EstadoSolicitud.REGISTRADA,UUID.randomUUID(),RolActor.SUPERVISOR,"No permitido",Instant.now())).isInstanceOf(TransicionEstadoInvalidaException.class);}
 @Test void rejectsUnassignedAnalystResolution(){var s=created();s.tomar(ANALYST,Instant.now());assertThatThrownBy(()->s.resolver(UUID.randomUUID(),null,Instant.now())).isInstanceOf(AccesoDominioNoPermitidoException.class);}
 @Test void supervisorReasonIsRequired(){var s=Solicitud.reconstruir(UUID.randomUUID(),"SOL-2026-1","A",CATEGORY,REQUESTER,Prioridad.ALTA,"D",EstadoSolicitud.RESUELTA,ANALYST,Instant.now(),Instant.now(),1,List.of(),List.of());assertThatThrownBy(()->s.cerrar(UUID.randomUUID()," ",Instant.now())).isInstanceOf(IllegalArgumentException.class);}
}
