package co.com.operationalrequests.requests.domain.model;

import java.util.Objects;
import java.util.UUID;

public record Categoria(UUID id, String codigo, String nombre) {
    public Categoria {
        Objects.requireNonNull(id, "El id de categoría es obligatorio");
        codigo = textoObligatorio(codigo, "El código de categoría es obligatorio");
        nombre = textoObligatorio(nombre, "El nombre de categoría es obligatorio");
    }

    private static String textoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
