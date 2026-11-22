package cl.esfocar.mscontenidos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

final class Repositorios {
    private Repositorios() {
    }

    interface AsignaturaRepo extends JpaRepository<Entidades.Asignatura, Long> {
        java.util.List<Entidades.Asignatura> findAllByOrderByOrdenAsc();
    }

    interface UnidadRepo extends JpaRepository<Entidades.Unidad, Long> {
    }

    interface PreguntaRepo extends JpaRepository<Entidades.Pregunta, Long> {
        @Query("select coalesce(max(p.id), 0) from Pregunta p")
        long maxId();
    }

    interface CasoRepo extends JpaRepository<Entidades.Caso, Long> {
        @Query("select coalesce(max(c.id), 0) from Caso c")
        long maxId();
    }

    interface MetaRepo extends JpaRepository<Entidades.Meta, Long> {
    }
}
