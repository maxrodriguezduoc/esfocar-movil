package cl.esfocar.mscontenidos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Objetos JSON que expone el microservicio. El mismo formato se usa en el archivo semilla y en la app. */
final class Dtos {
    private Dtos() {
    }

    record Paquete(int version, List<AsignaturaDto> asignaturas) {
    }

    record VersionDto(int version) {
    }

    record AsignaturaDto(Long id, String nombre, String descripcion, String icono, String color,
                         List<UnidadDto> unidades) {
    }

    record UnidadDto(Long id, int numero, String titulo, String resumen, List<FichaDto> fichas,
                     List<PreguntaDto> preguntas, List<CasoDto> casos) {
    }

    record FichaDto(Long id, String titulo, String contenido, List<String> puntosClave) {
    }

    record AlternativaDto(
            @NotBlank(message = "Cada alternativa necesita texto") String texto,
            boolean correcta,
            @NotBlank(message = "Cada alternativa necesita retroalimentación") String retroalimentacion) {
    }

    record PreguntaDto(Long id, String enunciado, String explicacion, List<AlternativaDto> alternativas) {
    }

    record OpcionDto(
            @NotBlank(message = "Cada opción necesita texto") String texto,
            @Min(value = 0, message = "El puntaje mínimo es 0") @Max(value = 2, message = "El puntaje máximo es 2") int puntaje,
            @NotBlank(message = "Cada opción necesita retroalimentación") String retroalimentacion) {
    }

    record CasoDto(Long id, String titulo, String situacion, String pregunta, String dificultad, String fundamento,
                   List<OpcionDto> opciones) {
    }

    /** Lo que envía un docente para crear o editar una pregunta. */
    record PreguntaRequest(
            @NotBlank(message = "El enunciado es obligatorio") @Size(max = 500) String enunciado,
            @Size(max = 1000) String explicacion,
            @NotNull @Size(min = 2, max = 5, message = "Debe tener entre 2 y 5 alternativas")
            List<@Valid AlternativaDto> alternativas) {
    }

    /** Lo que envía un docente para crear o editar un caso simulado. */
    record CasoRequest(
            @NotBlank(message = "El título es obligatorio") @Size(max = 120) String titulo,
            @NotBlank(message = "La situación es obligatoria") @Size(max = 2000) String situacion,
            @NotBlank(message = "La pregunta es obligatoria") @Size(max = 500) String pregunta,
            @NotBlank(message = "La dificultad es obligatoria") String dificultad,
            @Size(max = 1500) String fundamento,
            @NotNull @Size(min = 2, max = 5, message = "Debe tener entre 2 y 5 opciones")
            List<@Valid OpcionDto> opciones) {
    }
}
