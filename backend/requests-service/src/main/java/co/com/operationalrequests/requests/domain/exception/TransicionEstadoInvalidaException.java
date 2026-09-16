package co.com.operationalrequests.requests.domain.exception;
import co.com.operationalrequests.requests.domain.model.EstadoSolicitud;
public class TransicionEstadoInvalidaException extends RuntimeException { public TransicionEstadoInvalidaException(EstadoSolicitud origen, EstadoSolicitud destino){super("Transición no permitida: "+origen+" -> "+destino);} }
