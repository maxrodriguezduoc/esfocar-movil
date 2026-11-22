package cl.esfocar.msapuntes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ApunteService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ApunteRepository repository;

    public ApunteService(ApunteRepository repository) {
        this.repository = repository;
    }

    public List<Apunte> listar(String alias) {
        return repository.findByAliasOrderByIdDesc(alias);
    }

    public Apunte obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new ApunteNoEncontradoException(id));
    }

    public Apunte crear(ApunteRequest datos) {
        Apunte apunte = new Apunte();
        apunte.setAlias(datos.alias().trim());
        apunte.setTitulo(datos.titulo().trim());
        apunte.setContenido(datos.contenido().trim());
        apunte.setFecha(LocalDateTime.now().format(FORMATO));
        return repository.save(apunte);
    }

    public Apunte actualizar(Long id, ApunteRequest datos) {
        Apunte apunte = obtener(id);
        apunte.setTitulo(datos.titulo().trim());
        apunte.setContenido(datos.contenido().trim());
        apunte.setFecha(LocalDateTime.now().format(FORMATO));
        return repository.save(apunte);
    }

    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }
}
