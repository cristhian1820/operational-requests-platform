package co.com.operationalrequests.requests.domain.exception;
import java.util.UUID;
public class CategoriaNoDisponibleException extends RuntimeException {
    public CategoriaNoDisponibleException(UUID id) { super("Categoría inexistente o inactiva: " + id); }
}
