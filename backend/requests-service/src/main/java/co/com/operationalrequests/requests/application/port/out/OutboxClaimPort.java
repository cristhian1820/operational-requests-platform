package co.com.operationalrequests.requests.application.port.out;
import java.time.Instant; import java.util.*;
public interface OutboxClaimPort {
 List<PendingOutboxEvent> claim(int batchSize,Instant now,Instant expiredBefore);
 void markPublished(UUID eventId,Instant publishedAt);
 void markFailed(UUID eventId,int maxAttempts,Instant nextAttemptAt,String error);
}
