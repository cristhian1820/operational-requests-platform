package co.com.operationalrequests.requests.infrastructure.adapter.out.persistence;
import java.time.Instant; import java.util.*;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import org.springframework.transaction.annotation.Transactional;
interface CategoryJpaRepository extends JpaRepository<CategoryEntity,UUID> { List<CategoryEntity> findByActiveTrueOrderByNameAsc(); Optional<CategoryEntity> findByIdAndActiveTrue(UUID id); }
interface RequestJpaRepository extends JpaRepository<RequestEntity,UUID>, JpaSpecificationExecutor<RequestEntity> {
 @Modifying(flushAutomatically=true,clearAutomatically=true) @Transactional @Query(value="UPDATE operational_request SET status='EN_ATENCION', assigned_actor_id=:actor, updated_at=:now, version=version+1 WHERE id=:id AND status='REGISTRADA'",nativeQuery=true)
 int assignIfRegistered(@Param("id") UUID id,@Param("actor") UUID actor,@Param("now") Instant now);
}
interface ObservationJpaRepository extends JpaRepository<ObservationEntity,UUID> { List<ObservationEntity> findByRequestIdOrderByCreatedAtAsc(UUID requestId); }
interface StatusHistoryJpaRepository extends JpaRepository<StatusHistoryEntity,UUID> { List<StatusHistoryEntity> findByRequestIdOrderByOccurredAtAsc(UUID requestId); }
