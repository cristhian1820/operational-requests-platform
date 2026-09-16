package co.com.operationalrequests.requests.infrastructure.adapter.out.persistence;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="outbox_event")
public class OutboxEventEntity {
 @Id @Column(name="event_id") UUID eventId; @Column(name="aggregate_id",nullable=false) UUID aggregateId;
 @Column(name="event_type",nullable=false,length=100) String eventType; @Column(name="event_version",nullable=false) int eventVersion;
 @Column(name="correlation_id",nullable=false) UUID correlationId; @Column(name="payload",nullable=false,columnDefinition="nvarchar(max)") String payload;
 @Column(name="occurred_at",nullable=false) Instant occurredAt; @Column(name="status",nullable=false,length=20) String status;
 @Column(name="published_at") Instant publishedAt; @Column(name="attempts",nullable=false) int attempts; protected OutboxEventEntity() {}
 public OutboxEventEntity(UUID eventId,UUID aggregateId,String eventType,int eventVersion,UUID correlationId,String payload,Instant occurredAt){
  this.eventId=eventId;this.aggregateId=aggregateId;this.eventType=eventType;this.eventVersion=eventVersion;this.correlationId=correlationId;this.payload=payload;this.occurredAt=occurredAt;this.status="PENDING";this.attempts=0;
 }
}
