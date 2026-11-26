package cl.esfocar.msresultados;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResultadoRequest(
        @NotBlank(message = "El alias es obligatorio")
        @Size(max = 15, message = "El alias admite máximo 15 caracteres")
        String alias,

        @NotBlank(message = "La actividad es obligatoria")
        @Size(max = 60, message = "La actividad admite máximo 60 caracteres")
        String actividad,

        @Min(value = 0, message = "Los aciertos no pueden ser negativos")
        int aciertos,

        @Min(value = 1, message = "El total debe ser al menos 1")
        int total
) {
}
