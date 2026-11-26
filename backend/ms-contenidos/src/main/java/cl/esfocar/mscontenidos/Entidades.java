package cl.esfocar.mscontenidos;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo del contenido académico: Asignatura → Unidad → (Fichas, Preguntas, Casos).
 * Los identificadores se asignan explícitamente para que sean estables entre el servidor y la app.
 */
final class Entidades {
    private Entidades() {
    }

    @Entity(name = "Asignatura")
    @Table(name = "asignaturas")
    static class Asignatura {
        @Id Long id;
        @Column(nullable = false, length = 80) String nombre;
        @Column(nullable = false, length = 300) String descripcion;
        @Column(nullable = false, length = 30) String icono;
        @Column(nullable = false, length = 9) String color;
        int orden;

        @OneToMany(mappedBy = "asignatura", cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("numero")
        List<Unidad> unidades = new ArrayList<>();
    }

    @Entity(name = "Unidad")
    @Table(name = "unidades")
    static class Unidad {
        @Id Long id;
        int numero;
        @Column(nullable = false, length = 120) String titulo;
        @Column(nullable = false, length = 500) String resumen;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "asignatura_id")
        Asignatura asignatura;

        @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("id")
        List<Ficha> fichas = new ArrayList<>();

        @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("id")
        List<Pregunta> preguntas = new ArrayList<>();

        @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, orphanRemoval = true)
        @OrderBy("id")
        List<Caso> casos = new ArrayList<>();
    }

    @Entity(name = "Ficha")
    @Table(name = "fichas")
    static class Ficha {
        @Id Long id;
        @Column(nullable = false, length = 120) String titulo;
        @Column(nullable = false, length = 8000) String contenido;

        @ElementCollection
        @CollectionTable(name = "ficha_puntos_clave", joinColumns = @JoinColumn(name = "ficha_id"))
        @OrderColumn(name = "orden")
        @Column(name = "punto", length = 500)
        List<String> puntosClave = new ArrayList<>();

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "unidad_id")
        Unidad unidad;
    }

    @Embeddable
    static class Alternativa {
        @Column(length = 500) String texto;
        boolean correcta;
        @Column(length = 1000) String retroalimentacion;
    }

    @Entity(name = "Pregunta")
    @Table(name = "preguntas")
    static class Pregunta {
        @Id Long id;
        @Column(nullable = false, length = 500) String enunciado;
        @Column(length = 1000) String explicacion;

        @ElementCollection
        @CollectionTable(name = "pregunta_alternativas", joinColumns = @JoinColumn(name = "pregunta_id"))
        @OrderColumn(name = "orden")
        List<Alternativa> alternativas = new ArrayList<>();

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "unidad_id")
        Unidad unidad;
    }

    @Embeddable
    static class Opcion {
        @Column(length = 500) String texto;
        int puntaje;
        @Column(length = 1000) String retroalimentacion;
    }

    @Entity(name = "Caso")
    @Table(name = "casos")
    static class Caso {
        @Id Long id;
        @Column(nullable = false, length = 120) String titulo;
        @Column(nullable = false, length = 2000) String situacion;
        @Column(nullable = false, length = 500) String pregunta;
        @Column(nullable = false, length = 10) String dificultad;
        @Column(length = 1500) String fundamento;

        @ElementCollection
        @CollectionTable(name = "caso_opciones", joinColumns = @JoinColumn(name = "caso_id"))
        @OrderColumn(name = "orden")
        List<Opcion> opciones = new ArrayList<>();

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "unidad_id")
        Unidad unidad;
    }

    /** Fila única con la versión del contenido. Sube cada vez que un docente modifica algo. */
    @Entity(name = "Meta")
    @Table(name = "meta_contenido")
    static class Meta {
        @Id Long id = 1L;
        int version;
    }
}
