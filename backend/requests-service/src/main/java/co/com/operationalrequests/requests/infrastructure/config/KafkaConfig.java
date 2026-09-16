package co.com.operationalrequests.requests.infrastructure.config;
import org.apache.kafka.clients.admin.NewTopic; import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.kafka.config.TopicBuilder;
@Configuration @Profile("!test") public class KafkaConfig { @Bean NewTopic requestEventsTopic(@Value("${events.topic:operational-requests.events.v1}") String topic,@Value("${events.partitions:3}") int partitions){return TopicBuilder.name(topic).partitions(partitions).replicas(1).build();} }
