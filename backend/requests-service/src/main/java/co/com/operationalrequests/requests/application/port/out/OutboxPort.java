package co.com.operationalrequests.requests.application.port.out;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
public interface OutboxPort {
    void append(UUID eventId, UUID aggregateId, String type, int version, UUID correlationId,
            Instant occurredAt, Map<String, Object> payload);
}
