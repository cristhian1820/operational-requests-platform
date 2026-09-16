package co.com.operationalrequests.requests.infrastructure.adapter.out.messaging;
import co.com.operationalrequests.requests.application.port.out.OutboxPort;
import co.com.operationalrequests.requests.infrastructure.adapter.out.persistence.OutboxEventEntity;
import com.fasterxml.jackson.core.JsonProcessingException; import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant; import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Component;

@Component
public class JpaOutboxAdapter implements OutboxPort {
 private final OutboxStore store; private final ObjectMapper mapper;
 public JpaOutboxAdapter(OutboxStore store,ObjectMapper mapper){this.store=store;this.mapper=mapper;}
 @Override public void append(UUID eventId,UUID aggregateId,String type,int version,UUID correlationId,Instant occurredAt,Map<String,Object> payload){
  var envelope=new LinkedHashMap<String,Object>(); envelope.put("eventId",eventId);envelope.put("occurredAt",occurredAt);envelope.put("aggregateId",aggregateId);envelope.put("type",type);envelope.put("version",version);envelope.put("correlationId",correlationId);envelope.put("payload",payload);
  try { store.save(new OutboxEventEntity(eventId,aggregateId,type,version,correlationId,mapper.writeValueAsString(envelope),occurredAt)); }
  catch(JsonProcessingException ex){throw new IllegalStateException("No fue posible serializar el evento",ex);}
 }
}
interface OutboxStore extends JpaRepository<OutboxEventEntity,UUID>{}
