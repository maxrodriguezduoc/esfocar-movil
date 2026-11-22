package cl.esfocar.msapuntes;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApunteRepository extends JpaRepository<Apunte, Long> {
    List<Apunte> findByAliasOrderByIdDesc(String alias);
}
