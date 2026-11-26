package cl.esfocar.mscontenidos;

import cl.esfocar.mscontenidos.Dtos.AlternativaDto;
import cl.esfocar.mscontenidos.Dtos.AsignaturaDto;
import cl.esfocar.mscontenidos.Dtos.CasoDto;
import cl.esfocar.mscontenidos.Dtos.CasoRequest;
import cl.esfocar.mscontenidos.Dtos.FichaDto;
import cl.esfocar.mscontenidos.Dtos.OpcionDto;
import cl.esfocar.mscontenidos.Dtos.Paquete;
import cl.esfocar.mscontenidos.Dtos.PreguntaDto;
import cl.esfocar.mscontenidos.Dtos.PreguntaRequest;
import cl.esfocar.mscontenidos.Dtos.UnidadDto;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContenidoService {

    private static final Set<String> DIFICULTADES = Set.of("BASICA", "MEDIA", "AVANZADA");

    private final Repositorios.AsignaturaRepo asignaturas;
    private final Repositorios.UnidadRepo unidades;
    private final Repositorios.PreguntaRepo preguntas;
    private final Repositorios.CasoRepo casos;
    private final Repositorios.MetaRepo meta;

    public ContenidoService(Repositorios.AsignaturaRepo asignaturas, Repositorios.UnidadRepo unidades,
                            Repositorios.PreguntaRepo preguntas, Repositorios.CasoRepo casos,
                            Repositorios.MetaRepo meta) {
        this.asignaturas = asignaturas;
        this.unidades = unidades;
        this.preguntas = preguntas;
        this.casos = casos;
        this.meta = meta;
    }

    // ---------------- Lectura (la usa la app) ----------------

    @Transactional(readOnly = true)
    public int version() {
        return meta.findById(1L).map(m -> m.version).orElse(0);
    }

    @Transactional(readOnly = true)
    public Paquete paquete() {
        List<AsignaturaDto> lista = asignaturas.findAllByOrderByOrdenAsc().stream().map(this::aDto).toList();
        return new Paquete(version(), lista);
    }

    @Transactional(readOnly = true)
    public AsignaturaDto asignatura(Long id) {
        return asignaturas.findById(id).map(this::aDto)
                .orElseThrow(() -> new NoEncontradoException("No existe la asignatura " + id));
    }

    private AsignaturaDto aDto(Entidades.Asignatura a) {
        List<UnidadDto> us = a.unidades.stream().map(u -> new UnidadDto(
                u.id, u.numero, u.titulo, u.resumen,
                u.fichas.stream().map(f -> new FichaDto(f.id, f.titulo, f.contenido, List.copyOf(f.puntosClave))).toList(),
                u.preguntas.stream().map(this::aDto).toList(),
                u.casos.stream().map(this::aDto).toList()
        )).toList();
        return new AsignaturaDto(a.id, a.nombre, a.descripcion, a.icono, a.color, us);
    }

    private PreguntaDto aDto(Entidades.Pregunta p) {
        return new PreguntaDto(p.id, p.enunciado, p.explicacion, p.alternativas.stream()
                .map(x -> new AlternativaDto(x.texto, x.correcta, x.retroalimentacion)).toList());
    }

    private CasoDto aDto(Entidades.Caso c) {
        return new CasoDto(c.id, c.titulo, c.situacion, c.pregunta, c.dificultad, c.fundamento, c.opciones.stream()
                .map(o -> new OpcionDto(o.texto, o.puntaje, o.retroalimentacion)).toList());
    }

    // ---------------- Gestión docente (crear / editar / eliminar) ----------------

    @Transactional
    public PreguntaDto crearPregunta(Long unidadId, PreguntaRequest datos) {
        Entidades.Unidad unidad = unidad(unidadId);
        validarAlternativas(datos.alternativas());
        Entidades.Pregunta p = new Entidades.Pregunta();
        p.id = preguntas.maxId() + 1;
        p.unidad = unidad;
        copiar(datos, p);
        preguntas.save(p);
        subirVersion();
        return aDto(p);
    }

    @Transactional
    public PreguntaDto actualizarPregunta(Long id, PreguntaRequest datos) {
        Entidades.Pregunta p = preguntas.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la pregunta " + id));
        validarAlternativas(datos.alternativas());
        copiar(datos, p);
        subirVersion();
        return aDto(p);
    }

    @Transactional
    public void eliminarPregunta(Long id) {
        Entidades.Pregunta p = preguntas.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la pregunta " + id));
        p.unidad.preguntas.remove(p);
        preguntas.delete(p);
        subirVersion();
    }

    @Transactional
    public CasoDto crearCaso(Long unidadId, CasoRequest datos) {
        Entidades.Unidad unidad = unidad(unidadId);
        validarCaso(datos);
        Entidades.Caso c = new Entidades.Caso();
        c.id = casos.maxId() + 1;
        c.unidad = unidad;
        copiar(datos, c);
        casos.save(c);
        subirVersion();
        return aDto(c);
    }

    @Transactional
    public CasoDto actualizarCaso(Long id, CasoRequest datos) {
        Entidades.Caso c = casos.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el caso " + id));
        validarCaso(datos);
        copiar(datos, c);
        subirVersion();
        return aDto(c);
    }

    @Transactional
    public void eliminarCaso(Long id) {
        Entidades.Caso c = casos.findById(id).orElseThrow(() -> new NoEncontradoException("No existe el caso " + id));
        c.unidad.casos.remove(c);
        casos.delete(c);
        subirVersion();
    }

    private Entidades.Unidad unidad(Long id) {
        return unidades.findById(id).orElseThrow(() -> new NoEncontradoException("No existe la unidad " + id));
    }

    private void validarAlternativas(List<AlternativaDto> alternativas) {
        long correctas = alternativas.stream().filter(AlternativaDto::correcta).count();
        if (correctas != 1) {
            throw new IllegalArgumentException("La pregunta debe tener exactamente una alternativa correcta");
        }
    }

    private void validarCaso(CasoRequest datos) {
        if (!DIFICULTADES.contains(datos.dificultad())) {
            throw new IllegalArgumentException("La dificultad debe ser BASICA, MEDIA o AVANZADA");
        }
        if (datos.opciones().stream().noneMatch(o -> o.puntaje() == 2)) {
            throw new IllegalArgumentException("El caso debe tener al menos una actuación óptima (puntaje 2)");
        }
    }

    private void copiar(PreguntaRequest datos, Entidades.Pregunta p) {
        p.enunciado = datos.enunciado().trim();
        p.explicacion = datos.explicacion();
        p.alternativas.clear();
        for (AlternativaDto a : datos.alternativas()) {
            Entidades.Alternativa e = new Entidades.Alternativa();
            e.texto = a.texto().trim();
            e.correcta = a.correcta();
            e.retroalimentacion = a.retroalimentacion().trim();
            p.alternativas.add(e);
        }
    }

    private void copiar(CasoRequest datos, Entidades.Caso c) {
        c.titulo = datos.titulo().trim();
        c.situacion = datos.situacion().trim();
        c.pregunta = datos.pregunta().trim();
        c.dificultad = datos.dificultad();
        c.fundamento = datos.fundamento();
        c.opciones.clear();
        for (OpcionDto o : datos.opciones()) {
            Entidades.Opcion e = new Entidades.Opcion();
            e.texto = o.texto().trim();
            e.puntaje = o.puntaje();
            e.retroalimentacion = o.retroalimentacion().trim();
            c.opciones.add(e);
        }
    }

    private void subirVersion() {
        Entidades.Meta m = meta.findById(1L).orElseGet(Entidades.Meta::new);
        m.version++;
        meta.save(m);
    }

    // ---------------- Carga inicial desde el archivo semilla ----------------

    @Transactional
    public boolean cargarSemillaSiVacio(Paquete paquete) {
        if (asignaturas.count() > 0) {
            return false;
        }
        int orden = 0;
        for (AsignaturaDto a : paquete.asignaturas()) {
            Entidades.Asignatura ea = new Entidades.Asignatura();
            ea.id = a.id();
            ea.nombre = a.nombre();
            ea.descripcion = a.descripcion();
            ea.icono = a.icono();
            ea.color = a.color();
            ea.orden = orden++;
            for (UnidadDto u : a.unidades()) {
                Entidades.Unidad eu = new Entidades.Unidad();
                eu.id = u.id();
                eu.numero = u.numero();
                eu.titulo = u.titulo();
                eu.resumen = u.resumen();
                eu.asignatura = ea;
                for (FichaDto f : u.fichas()) {
                    Entidades.Ficha ef = new Entidades.Ficha();
                    ef.id = f.id();
                    ef.titulo = f.titulo();
                    ef.contenido = f.contenido();
                    ef.puntosClave.addAll(f.puntosClave());
                    ef.unidad = eu;
                    eu.fichas.add(ef);
                }
                for (PreguntaDto p : u.preguntas()) {
                    Entidades.Pregunta ep = new Entidades.Pregunta();
                    ep.id = p.id();
                    ep.unidad = eu;
                    copiar(new PreguntaRequest(p.enunciado(), p.explicacion(), p.alternativas()), ep);
                    eu.preguntas.add(ep);
                }
                for (CasoDto c : u.casos()) {
                    Entidades.Caso ec = new Entidades.Caso();
                    ec.id = c.id();
                    ec.unidad = eu;
                    copiar(new CasoRequest(c.titulo(), c.situacion(), c.pregunta(), c.dificultad(), c.fundamento(),
                            c.opciones()), ec);
                    eu.casos.add(ec);
                }
                ea.unidades.add(eu);
            }
            asignaturas.save(ea);
        }
        Entidades.Meta m = new Entidades.Meta();
        m.version = paquete.version();
        meta.save(m);
        return true;
    }
}
