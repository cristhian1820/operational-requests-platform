package co.com.operationalrequests.requests.application.port.out;
public interface EventPublisherPort { void publish(PendingOutboxEvent event) throws Exception; }
