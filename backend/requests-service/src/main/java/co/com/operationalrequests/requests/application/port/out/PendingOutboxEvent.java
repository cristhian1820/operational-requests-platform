package co.com.operationalrequests.requests.application.port.out;
import java.time.Instant;
import java.util.UUID;
public record PendingOutboxEvent(UUID eventId,UUID aggregateId,String type,int version,UUID correlationId,String envelope,Instant occurredAt,int attempts) {}
