package co.com.operationalrequests.requests.domain.model;

import co.com.operationalrequests.requests.domain.exception.TransicionEstadoInvalidaException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitudTest {
    private Solicitud solicitud() {
        return new Solicitud(
                UUID.randomUUID(),
                "SOL-2026-000001",
                new Categoria(UUID.randomUUID(), "GENERAL", "General"),
                UUID.randomUUID(),
                Prioridad.MEDIA,
                "Solicitud de prueba",
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void permiteTodasLasTransicionesDefinidas() {
        Solicitud solicitud = solicitud();
        UUID actorId = UUID.randomUUID();

        solicitud.cambiarEstado(EstadoSolicitud.EN_ATENCION, actorId, RolActor.ANALISTA, Instant.now());
        solicitud.cambiarEstado(EstadoSolicitud.RESUELTA, actorId, RolActor.ANALISTA, Instant.now());
        solicitud.cambiarEstado(EstadoSolicitud.EN_ATENCION, actorId, RolActor.ANALISTA, Instant.now());
        solicitud.cambiarEstado(EstadoSolicitud.RESUELTA, actorId, RolActor.ANALISTA, Instant.now());
        solicitud.cambiarEstado(EstadoSolicitud.CERRADA, actorId, RolActor.SUPERVISOR, Instant.now());

        assertEquals(EstadoSolicitud.CERRADA, solicitud.estado());
        assertEquals(5, solicitud.historial().size());
    }

    @Test
    void rechazaTransicionNoDefinida() {
        Solicitud solicitud = solicitud();

        assertThrows(
                TransicionEstadoInvalidaException.class,
                () -> solicitud.cambiarEstado(
                        EstadoSolicitud.RESUELTA,
                        UUID.randomUUID(),
                        RolActor.ANALISTA,
                        Instant.now()));
    }

    @Test
    void rechazaDatosObligatoriosVacios() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Solicitud(
                        UUID.randomUUID(),
                        " ",
                        new Categoria(UUID.randomUUID(), "GENERAL", "General"),
                        UUID.randomUUID(),
                        Prioridad.MEDIA,
                        "Descripción",
                        Instant.now()));
    }
}
