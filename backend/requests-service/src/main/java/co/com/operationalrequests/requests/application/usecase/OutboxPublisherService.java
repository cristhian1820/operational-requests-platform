package co.com.operationalrequests.requests.application.usecase;
import co.com.operationalrequests.requests.application.port.out.*; import java.time.*; import java.util.List;
import org.slf4j.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
@Service public class OutboxPublisherService {
 private static final Logger log=LoggerFactory.getLogger(OutboxPublisherService.class);
 private final OutboxClaimPort outbox; private final EventPublisherPort publisher; private final ClockPort clock;
 private final int batchSize,maxAttempts,backoffSeconds,lockTimeoutSeconds;
 public OutboxPublisherService(OutboxClaimPort outbox,EventPublisherPort publisher,ClockPort clock,
  @Value("${outbox.batch-size:20}") int batchSize,@Value("${outbox.max-attempts:5}") int maxAttempts,
  @Value("${outbox.backoff-seconds:10}") int backoffSeconds,@Value("${outbox.lock-timeout-seconds:60}") int lockTimeoutSeconds){
  this.outbox=outbox;this.publisher=publisher;this.clock=clock;this.batchSize=batchSize;this.maxAttempts=maxAttempts;this.backoffSeconds=backoffSeconds;this.lockTimeoutSeconds=lockTimeoutSeconds;
 }
 public int publishBatch(){Instant now=clock.now();List<PendingOutboxEvent> events=outbox.claim(batchSize,now,now.minusSeconds(lockTimeoutSeconds));
  if(!events.isEmpty())log.info("Outbox reclamó {} eventos",events.size());
  for(var event:events){try{publisher.publish(event);outbox.markPublished(event.eventId(),clock.now());log.info("Evento publicado eventId={} type={}",event.eventId(),event.type());}
   catch(Exception ex){int attempt=event.attempts()+1;Instant next=clock.now().plusSeconds((long)backoffSeconds*attempt);String error=safe(ex);outbox.markFailed(event.eventId(),maxAttempts,next,error);log.warn("Falló publicación eventId={} type={} intento={}/{}",event.eventId(),event.type(),attempt,maxAttempts);}}
  return events.size();
 }
 private static String safe(Exception ex){String value=ex.getClass().getSimpleName();return value.length()>500?value.substring(0,500):value;}
}
