package co.com.operationalrequests.requests.application.usecase;
import static org.assertj.core.api.Assertions.*; import static org.mockito.Mockito.*;
import co.com.operationalrequests.requests.application.port.out.*; import java.time.*; import java.util.*; import org.junit.jupiter.api.*; import org.mockito.*;
class OutboxPublisherServiceTest {
 @Mock OutboxClaimPort outbox; @Mock EventPublisherPort publisher; @Mock ClockPort clock; AutoCloseable mocks; Instant now=Instant.parse("2026-09-16T12:00:00Z");
 @BeforeEach void open(){mocks=MockitoAnnotations.openMocks(this);when(clock.now()).thenReturn(now);}@AfterEach void close()throws Exception{mocks.close();}
 private PendingOutboxEvent event(int attempts){return new PendingOutboxEvent(UUID.randomUUID(),UUID.randomUUID(),"SolicitudRegistrada",1,UUID.randomUUID(),"{}",now,attempts);}
 private OutboxPublisherService service(int max){return new OutboxPublisherService(outbox,publisher,clock,10,max,10,60);}
 @Test void claimsPendingAndMarksPublished()throws Exception{var e=event(0);when(outbox.claim(10,now,now.minusSeconds(60))).thenReturn(List.of(e));assertThat(service(5).publishBatch()).isOne();verify(publisher).publish(e);verify(outbox).markPublished(e.eventId(),now);}
 @Test void failureSchedulesBackoffWithoutSensitiveMessage()throws Exception{var e=event(1);when(outbox.claim(anyInt(),any(),any())).thenReturn(List.of(e));doThrow(new IllegalStateException("payload-secreto-no-debe-loguearse")).when(publisher).publish(e);service(5).publishBatch();verify(outbox).markFailed(e.eventId(),5,now.plusSeconds(20),"IllegalStateException");}
 @Test void maximumAttemptDelegatesTerminalDecision()throws Exception{var e=event(4);when(outbox.claim(anyInt(),any(),any())).thenReturn(List.of(e));doThrow(new RuntimeException()).when(publisher).publish(e);service(5).publishBatch();verify(outbox).markFailed(e.eventId(),5,now.plusSeconds(50),"RuntimeException");}
 @Test void expiredLocksAreIncludedInClaimCutoff(){when(outbox.claim(anyInt(),any(),any())).thenReturn(List.of());service(5).publishBatch();verify(outbox).claim(10,now,now.minusSeconds(60));}
 @Test void emptySecondClaimCannotRepublish(){when(outbox.claim(anyInt(),any(),any())).thenReturn(List.of());assertThat(service(5).publishBatch()).isZero();verifyNoInteractions(publisher);}
}
