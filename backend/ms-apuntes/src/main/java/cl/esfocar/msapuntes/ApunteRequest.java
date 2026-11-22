package cl.esfocar.msapuntes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo JSON que envía la app para crear o actualizar un apunte. */
public record ApunteRequest(
        @NotBlank(message = "El alias es obligatorio")
        @Size(max = 15, message = "El alias admite máximo 15 caracteres")
        String alias,

        @NotBlank(message = "El título es obligatorio")
        @Size(min = 3, max = 40, message = "El título debe tener entre 3 y 40 caracteres")
        String titulo,

        @NotBlank(message = "El contenido es obligatorio")
        @Size(min = 10, max = 2000, message = "El contenido debe tener entre 10 y 2000 caracteres")
        String contenido
) {
}
