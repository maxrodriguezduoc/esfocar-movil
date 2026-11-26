package cl.esfocar.mscontenidos;

import cl.esfocar.mscontenidos.Dtos.AsignaturaDto;
import cl.esfocar.mscontenidos.Dtos.CasoDto;
import cl.esfocar.mscontenidos.Dtos.CasoRequest;
import cl.esfocar.mscontenidos.Dtos.Paquete;
import cl.esfocar.mscontenidos.Dtos.PreguntaDto;
import cl.esfocar.mscontenidos.Dtos.PreguntaRequest;
import cl.esfocar.mscontenidos.Dtos.VersionDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ContenidoController {

    private final ContenidoService service;

    public ContenidoController(ContenidoService service) {
        this.service = service;
    }

    // ----- Consumo desde la app (sincronización offline-first) -----

    @GetMapping("/contenidos/version")
    public VersionDto version() {
        return new VersionDto(service.version());
    }

    @GetMapping("/contenidos/paquete")
    public Paquete paquete() {
        return service.paquete();
    }

    @GetMapping("/asignaturas/{id}")
    public AsignaturaDto asignatura(@PathVariable Long id) {
        return service.asignatura(id);
    }

    // ----- Gestión de contenido por docentes -----

    @PostMapping("/unidades/{unidadId}/preguntas")
    @ResponseStatus(HttpStatus.CREATED)
    public PreguntaDto crearPregunta(@PathVariable Long unidadId, @Valid @RequestBody PreguntaRequest datos) {
        return service.crearPregunta(unidadId, datos);
    }

    @PutMapping("/preguntas/{id}")
    public PreguntaDto actualizarPregunta(@PathVariable Long id, @Valid @RequestBody PreguntaRequest datos) {
        return service.actualizarPregunta(id, datos);
    }

    @DeleteMapping("/preguntas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarPregunta(@PathVariable Long id) {
        service.eliminarPregunta(id);
    }

    @PostMapping("/unidades/{unidadId}/casos")
    @ResponseStatus(HttpStatus.CREATED)
    public CasoDto crearCaso(@PathVariable Long unidadId, @Valid @RequestBody CasoRequest datos) {
        return service.crearCaso(unidadId, datos);
    }

    @PutMapping("/casos/{id}")
    public CasoDto actualizarCaso(@PathVariable Long id, @Valid @RequestBody CasoRequest datos) {
        return service.actualizarCaso(id, datos);
    }

    @DeleteMapping("/casos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCaso(@PathVariable Long id) {
        service.eliminarCaso(id);
    }
}
