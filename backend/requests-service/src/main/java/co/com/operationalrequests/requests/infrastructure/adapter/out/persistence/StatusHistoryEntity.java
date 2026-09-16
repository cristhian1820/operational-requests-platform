package co.com.operationalrequests.requests.infrastructure.adapter.out.persistence;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="request_status_history")
public class StatusHistoryEntity {
 @Id UUID id; @Column(name="request_id",nullable=false) UUID requestId; @Column(name="previous_status",length=30) String previousStatus;
 @Column(name="new_status",nullable=false,length=30) String newStatus; @Column(name="actor_id",nullable=false) UUID actorId;
 @Column(name="actor_role",nullable=false,length=20) String actorRole; @Column(name="reason",length=500) String reason;
 @Column(name="occurred_at",nullable=false) Instant occurredAt; protected StatusHistoryEntity() {}
}
