package co.com.operationalrequests.requests.application.port.out;
import java.time.Instant;
public interface RequestNumberPort { String next(Instant now); }
