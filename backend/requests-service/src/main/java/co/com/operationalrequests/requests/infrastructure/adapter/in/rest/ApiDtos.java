package co.com.operationalrequests.requests.infrastructure.adapter.in.rest;

import co.com.operationalrequests.requests.domain.model.EstadoSolicitud;
import co.com.operationalrequests.requests.domain.model.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class ApiDtos {
    private ApiDtos() {
    }

    public record CreateRequest(@NotBlank @Size(max = 200) String asunto,
                                @NotBlank @Size(max = 2000) String descripcion, @NotNull UUID categoriaId,
                                @NotNull Prioridad prioridad) {
    }

    public record ObservationRequest(@NotBlank @Size(max = 2000) String texto) {
    }

    public record TransitionRequest(@NotNull EstadoSolicitud estadoDestino, @Size(max = 500) String motivo,
                                    @Size(max = 2000) String observacion) {
    }
}
