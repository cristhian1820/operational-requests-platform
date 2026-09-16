package co.com.operationalrequests.indicators.application;
import com.fasterxml.jackson.databind.JsonNode; import java.time.Instant; import java.util.UUID;
public record EventEnvelope(UUID eventId,Instant occurredAt,UUID aggregateId,String type,int version,UUID correlationId,JsonNode payload) {}
