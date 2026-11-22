package cl.esfocar.msresultados;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoRepository extends JpaRepository<Resultado, Long> {
    List<Resultado> findByAliasOrderByIdDesc(String alias);
}
