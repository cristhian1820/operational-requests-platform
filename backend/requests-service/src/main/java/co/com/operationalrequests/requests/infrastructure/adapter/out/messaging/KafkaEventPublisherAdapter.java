package co.com.operationalrequests.requests.infrastructure.adapter.out.messaging;
import co.com.operationalrequests.requests.application.port.out.*; import java.nio.charset.StandardCharsets; import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.ProducerRecord; import org.springframework.beans.factory.annotation.Value; import org.springframework.kafka.core.KafkaTemplate; import org.springframework.stereotype.Component;
@Component public class KafkaEventPublisherAdapter implements EventPublisherPort {
 private final KafkaTemplate<String,String> kafka; private final String topic;
 public KafkaEventPublisherAdapter(KafkaTemplate<String,String> kafka,@Value("${events.topic:operational-requests.events.v1}") String topic){this.kafka=kafka;this.topic=topic;}
 @Override public void publish(PendingOutboxEvent e)throws Exception{var record=new ProducerRecord<String,String>(topic,e.aggregateId().toString(),e.envelope());header(record,"eventId",e.eventId().toString());header(record,"eventType",e.type());header(record,"eventVersion",String.valueOf(e.version()));header(record,"correlationId",e.correlationId().toString());kafka.send(record).get(10,TimeUnit.SECONDS);}
 private static void header(ProducerRecord<String,String> record,String key,String value){record.headers().add(key,value.getBytes(StandardCharsets.UTF_8));}
}
