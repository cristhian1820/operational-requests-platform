package co.com.operationalrequests.requests.infrastructure.adapter.out.persistence;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="request_observation")
public class ObservationEntity {
 @Id UUID id; @Column(name="request_id",nullable=false) UUID requestId; @Column(name="actor_id",nullable=false) UUID actorId;
 @Column(name="actor_role",nullable=false,length=20) String actorRole; @Column(name="body",nullable=false,length=2000) String body;
 @Column(name="created_at",nullable=false) Instant createdAt; protected ObservationEntity() {}
}
