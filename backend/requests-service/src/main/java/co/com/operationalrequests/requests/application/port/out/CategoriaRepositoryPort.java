package co.com.operationalrequests.requests.application.port.out;
import co.com.operationalrequests.requests.domain.model.Categoria;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface CategoriaRepositoryPort {
    List<Categoria> findActive();
    Optional<Categoria> findActiveById(UUID id);
}
