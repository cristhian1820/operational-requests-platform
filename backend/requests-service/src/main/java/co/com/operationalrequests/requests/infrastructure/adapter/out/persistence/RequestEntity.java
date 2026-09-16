package co.com.operationalrequests.requests.infrastructure.adapter.out.persistence;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="operational_request")
public class RequestEntity {
 @Id UUID id; @Column(name="request_number",nullable=false,length=24) String requestNumber; @Column(name="subject",nullable=false,length=200) String subject;
 @Column(name="category_id",nullable=false) UUID categoryId; @Column(name="requester_id",nullable=false) UUID requesterId;
 @Column(name="assigned_actor_id") UUID assignedActorId; @Column(name="status",nullable=false,length=30) String status;
 @Column(name="priority",nullable=false,length=10) String priority; @Column(name="description",nullable=false,length=2000) String description;
 @Version @Column(name="version",nullable=false) long version; @Column(name="created_at",nullable=false) Instant createdAt;
 @Column(name="updated_at",nullable=false) Instant updatedAt;
 protected RequestEntity() {}
 RequestEntity(UUID id){this.id=id;}
}
