package cl.esfocar.msapuntes;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apuntes")
public class ApunteController {

    private final ApunteService service;

    public ApunteController(ApunteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Apunte> listar(@RequestParam String alias) {
        return service.listar(alias);
    }

    @GetMapping("/{id}")
    public Apunte obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Apunte crear(@Valid @RequestBody ApunteRequest datos) {
        return service.crear(datos);
    }

    @PutMapping("/{id}")
    public Apunte actualizar(@PathVariable Long id, @Valid @RequestBody ApunteRequest datos) {
        return service.actualizar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
