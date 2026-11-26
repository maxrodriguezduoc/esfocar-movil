package cl.esfocar.mscontenidos;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
public class MsContenidosApplication {

    private static final Logger LOG = LoggerFactory.getLogger(MsContenidosApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MsContenidosApplication.class, args);
    }

    /** Al iniciar, si la base está vacía, carga el banco de contenido ficticio (seed/contenido_inicial.json). */
    @Bean
    CommandLineRunner cargarSemilla(ContenidoService service, ObjectMapper mapper) {
        return args -> {
            try (InputStream in = new ClassPathResource("seed/contenido_inicial.json").getInputStream()) {
                Dtos.Paquete paquete = mapper.readValue(in, Dtos.Paquete.class);
                if (service.cargarSemillaSiVacio(paquete)) {
                    LOG.info("Contenido inicial cargado: {} asignaturas (versión {})",
                            paquete.asignaturas().size(), paquete.version());
                }
            }
        };
    }
}

class NoEncontradoException extends RuntimeException {
    NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

@RestControllerAdvice
class ManejadorErrores {

    @ExceptionHandler(NoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> noEncontrado(NoEncontradoException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> invalido(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage()).findFirst().orElse("Datos inválidos");
        return Map.of("error", mensaje);
    }
}
