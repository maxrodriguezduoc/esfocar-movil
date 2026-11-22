package cl.esfocar.msresultados;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResultadoService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ResultadoRepository repository;

    public ResultadoService(ResultadoRepository repository) {
        this.repository = repository;
    }

    public List<Resultado> listar(String alias) {
        return repository.findByAliasOrderByIdDesc(alias);
    }

    public Resultado crear(ResultadoRequest datos) {
        if (datos.aciertos() > datos.total()) {
            throw new IllegalArgumentException("Los aciertos no pueden superar el total de preguntas");
        }
        Resultado resultado = new Resultado();
        resultado.setAlias(datos.alias().trim());
        resultado.setActividad(datos.actividad().trim());
        resultado.setAciertos(datos.aciertos());
        resultado.setTotal(datos.total());
        resultado.setFecha(LocalDateTime.now().format(FORMATO));
        return repository.save(resultado);
    }

    public void eliminar(Long id) {
        Resultado resultado = repository.findById(id)
                .orElseThrow(() -> new ResultadoNoEncontradoException(id));
        repository.delete(resultado);
    }

    public ResumenResultados resumen(String alias) {
        List<Resultado> resultados = repository.findByAliasOrderByIdDesc(alias);
        if (resultados.isEmpty()) {
            return new ResumenResultados(alias, 0, 0.0, 0);
        }
        double suma = 0;
        int mejor = 0;
        for (Resultado r : resultados) {
            int porcentaje = Math.round(100f * r.getAciertos() / r.getTotal());
            suma += porcentaje;
            mejor = Math.max(mejor, porcentaje);
        }
        return new ResumenResultados(alias, resultados.size(), suma / resultados.size(), mejor);
    }
}
