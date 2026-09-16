package co.com.operationalrequests.requests.infrastructure.adapter.out.messaging;
import co.com.operationalrequests.requests.application.port.out.*; import java.sql.*; import java.time.Instant; import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
@Component public class SqlServerOutboxClaimAdapter implements OutboxClaimPort {
 private final JdbcTemplate jdbc; public SqlServerOutboxClaimAdapter(JdbcTemplate jdbc){this.jdbc=jdbc;}
 @Override @Transactional public List<PendingOutboxEvent> claim(int size,Instant now,Instant expiredBefore){
  String sql="""
   ;WITH candidates AS (SELECT TOP (?) * FROM outbox_event WITH (UPDLOCK,READPAST,ROWLOCK) WHERE (status='PENDING' AND (next_attempt_at IS NULL OR next_attempt_at<=?)) OR (status='PROCESSING' AND locked_at<?) ORDER BY occurred_at,event_id) UPDATE candidates SET status='PROCESSING',locked_at=? OUTPUT inserted.event_id,inserted.aggregate_id,inserted.event_type,inserted.event_version,inserted.correlation_id,inserted.payload,inserted.occurred_at,inserted.attempts
   """;
  return jdbc.query(sql,(rs,row)->map(rs),size,Timestamp.from(now),Timestamp.from(expiredBefore),Timestamp.from(now));
 }
 @Override @Transactional public void markPublished(UUID id,Instant at){jdbc.update("UPDATE outbox_event SET status='PUBLISHED',published_at=?,locked_at=NULL,last_error=NULL WHERE event_id=? AND status='PROCESSING'",Timestamp.from(at),id);}
 @Override @Transactional public void markFailed(UUID id,int max,Instant next,String error){jdbc.update("UPDATE outbox_event SET attempts=attempts+1,status=CASE WHEN attempts+1>=? THEN 'FAILED' ELSE 'PENDING' END,next_attempt_at=CASE WHEN attempts+1>=? THEN NULL ELSE ? END,locked_at=NULL,last_error=? WHERE event_id=? AND status='PROCESSING'",max,max,Timestamp.from(next),error,id);}
 private static PendingOutboxEvent map(ResultSet rs)throws SQLException{return new PendingOutboxEvent(rs.getObject(1,UUID.class),rs.getObject(2,UUID.class),rs.getString(3),rs.getInt(4),rs.getObject(5,UUID.class),rs.getString(6),rs.getTimestamp(7).toInstant(),rs.getInt(8));}
}
