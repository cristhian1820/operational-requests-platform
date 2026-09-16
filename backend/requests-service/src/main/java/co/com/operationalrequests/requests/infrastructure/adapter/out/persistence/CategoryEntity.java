package co.com.operationalrequests.requests.infrastructure.adapter.out.persistence;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="category")
public class CategoryEntity {
 @Id UUID id; @Column(name="code",nullable=false,length=50) String code; @Column(name="name",nullable=false,length=120) String name;
 @Column(name="active",nullable=false) boolean active; @Column(name="created_at",nullable=false) Instant createdAt;
 protected CategoryEntity() {}
}
