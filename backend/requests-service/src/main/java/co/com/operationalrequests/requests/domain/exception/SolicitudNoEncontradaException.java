package co.com.operationalrequests.requests.domain.exception;
import java.util.UUID; public class SolicitudNoEncontradaException extends RuntimeException { public SolicitudNoEncontradaException(UUID id){super("Solicitud no encontrada: "+id);} }
