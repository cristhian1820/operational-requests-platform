package co.com.operationalrequests.requests.infrastructure.config;
import co.com.operationalrequests.requests.application.port.out.ClockPort;
import java.time.Clock; import java.time.Instant;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class ApplicationConfig {
 @Bean Clock utcClock(){return Clock.systemUTC();}
 @Bean ClockPort clockPort(Clock clock){return clock::instant;}
}
