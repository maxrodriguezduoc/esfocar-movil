package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.GestionContenidoRepository
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.Asignatura
import com.example.esfocarapp.model.Caso
import com.example.esfocarapp.model.Dificultad
import com.example.esfocarapp.model.OpcionCaso
import com.example.esfocarapp.model.Pregunta
import com.example.esfocarapp.model.Unidad
import com.example.esfocarapp.util.ValidadorContenido
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ------------------------------- Panel docente -------------------------------

// Lista el contenido de la unidad elegida y permite eliminar preguntas y casos
@OptIn(ExperimentalCoroutinesApi::class)
class DocenteViewModel(
    private val contenido: ContenidoRepository,
    private val gestion: GestionContenidoRepository
) : ViewModel() {

    val asignaturas: StateFlow<List<Asignatura>> =
        contenido.asignaturas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _asignaturaId = MutableStateFlow<Long?>(null)
    val asignaturaId: StateFlow<Long?> = _asignaturaId.asStateFlow()

    private val _unidadId = MutableStateFlow<Long?>(null)
    val unidadId: StateFlow<Long?> = _unidadId.asStateFlow()

    val unidades: StateFlow<List<Unidad>> = combine(contenido.unidades, _asignaturaId) { u, a ->
        if (a == null) emptyList() else u.filter { it.asignaturaId == a }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val preguntas: StateFlow<List<Pregunta>> = _unidadId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else contenido.preguntasFlujo(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val casos: StateFlow<List<Caso>> = combine(contenido.casos, _unidadId) { c, id ->
        if (id == null) emptyList() else c.filter { it.unidadId == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var mensaje by mutableStateOf<String?>(null)
        private set
    var procesando by mutableStateOf(false)
        private set

    fun elegirAsignatura(id: Long) {
        if (_asignaturaId.value != id) {
            _asignaturaId.value = id
            _unidadId.value = null
        }
    }

    fun elegirUnidad(id: Long) {
        _unidadId.value = id
    }

    fun eliminarPregunta(id: Long) = ejecutar("Pregunta eliminada") { gestion.eliminarPregunta(id) }

    fun eliminarCaso(id: Long) = ejecutar("Caso eliminado") { gestion.eliminarCaso(id) }

    fun limpiarMensaje() {
        mensaje = null
    }

    private fun ejecutar(exito: String, accion: suspend () -> Respuesta<Unit>) {
        viewModelScope.launch {
            procesando = true
            mensaje = when (val r = accion()) {
                is Respuesta.Exito -> exito
                is Respuesta.Fallo -> r.mensaje
            }
            procesando = false
        }
    }
}

// ------------------------------- Formulario de pregunta -------------------------------

data class AlternativaEditable(val texto: String = "", val retroalimentacion: String = "", val correcta: Boolean = false)

// Crea (preguntaId = 0) o edita una pregunta de trivia
class FormPreguntaViewModel(
    private val contenido: ContenidoRepository,
    private val gestion: GestionContenidoRepository,
    private val unidadId: Long,
    private val preguntaId: Long
) : ViewModel() {

    val editando: Boolean get() = preguntaId > 0

    var enunciado by mutableStateOf("")
        private set
    var explicacion by mutableStateOf("")
        private set
    var alternativas by mutableStateOf(List(3) { AlternativaEditable(correcta = it == 0) })
        private set
    var errorEnunciado by mutableStateOf<String?>(null)
        private set
    var errorExplicacion by mutableStateOf<String?>(null)
        private set
    var errorAlternativas by mutableStateOf<String?>(null)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var guardando by mutableStateOf(false)
        private set
    var guardado by mutableStateOf(false)
        private set
    var cargando by mutableStateOf(preguntaId > 0)
        private set

    init {
        if (editando) {
            viewModelScope.launch {
                contenido.pregunta(preguntaId)?.let { p ->
                    enunciado = p.enunciado
                    explicacion = p.explicacion
                    alternativas = p.alternativas.map { AlternativaEditable(it.texto, it.retroalimentacion, it.esCorrecta) }
                } ?: run { mensaje = "La pregunta ya no existe" }
                cargando = false
            }
        }
    }

    fun cambiarEnunciado(valor: String) {
        enunciado = valor
        errorEnunciado = ValidadorContenido.enunciado(valor)
    }

    fun cambiarExplicacion(valor: String) {
        explicacion = valor
        errorExplicacion = ValidadorContenido.explicacion(valor)
    }

    fun cambiarTexto(i: Int, valor: String) = modificar(i) { it.copy(texto = valor) }

    fun cambiarRetroalimentacion(i: Int, valor: String) = modificar(i) { it.copy(retroalimentacion = valor) }

    // Solo una alternativa puede ser la correcta
    fun marcarCorrecta(i: Int) {
        alternativas = alternativas.mapIndexed { j, a -> a.copy(correcta = j == i) }
        errorAlternativas = null
    }

    fun agregarAlternativa() {
        if (alternativas.size < ValidadorContenido.MAX_OPCIONES) alternativas = alternativas + AlternativaEditable()
    }

    fun quitarAlternativa(i: Int) {
        if (alternativas.size <= ValidadorContenido.MIN_OPCIONES) return
        val eraCorrecta = alternativas[i].correcta
        val restantes = alternativas.filterIndexed { j, _ -> j != i }
        alternativas = if (eraCorrecta) restantes.mapIndexed { j, a -> a.copy(correcta = j == 0) } else restantes
    }

    private fun modificar(i: Int, cambio: (AlternativaEditable) -> AlternativaEditable) {
        alternativas = alternativas.mapIndexed { j, a -> if (j == i) cambio(a) else a }
        errorAlternativas = null
    }

    fun guardar() {
        val lista = alternativas.map { Alternativa(it.texto.trim(), it.correcta, it.retroalimentacion.trim()) }
        errorEnunciado = ValidadorContenido.enunciado(enunciado)
        errorExplicacion = ValidadorContenido.explicacion(explicacion)
        errorAlternativas = ValidadorContenido.alternativas(lista)
        if (errorEnunciado != null || errorExplicacion != null || errorAlternativas != null) {
            mensaje = "Revisa los campos marcados"
            return
        }
        viewModelScope.launch {
            guardando = true
            mensaje = null
            val r = if (editando) {
                gestion.actualizarPregunta(preguntaId, enunciado.trim(), explicacion.trim(), lista)
            } else {
                gestion.crearPregunta(unidadId, enunciado.trim(), explicacion.trim(), lista)
            }
            when (r) {
                is Respuesta.Exito -> guardado = true
                is Respuesta.Fallo -> mensaje = r.mensaje
            }
            guardando = false
        }
    }
}

// ------------------------------- Formulario de caso simulado -------------------------------

data class OpcionEditable(val texto: String = "", val retroalimentacion: String = "", val puntaje: Int = 0)

// Crea (casoId = 0) o edita un caso simulado
class FormCasoViewModel(
    private val contenido: ContenidoRepository,
    private val gestion: GestionContenidoRepository,
    private val unidadId: Long,
    private val casoId: Long
) : ViewModel() {

    val editando: Boolean get() = casoId > 0

    var titulo by mutableStateOf("")
        private set
    var situacion by mutableStateOf("")
        private set
    var pregunta by mutableStateOf("¿Cuál es la actuación más adecuada?")
        private set
    var dificultad by mutableStateOf(Dificultad.MEDIA)
        private set
    var fundamento by mutableStateOf("")
        private set
    var opciones by mutableStateOf(listOf(OpcionEditable(puntaje = 2), OpcionEditable(puntaje = 1), OpcionEditable()))
        private set
    var errorTitulo by mutableStateOf<String?>(null)
        private set
    var errorSituacion by mutableStateOf<String?>(null)
        private set
    var errorPregunta by mutableStateOf<String?>(null)
        private set
    var errorFundamento by mutableStateOf<String?>(null)
        private set
    var errorOpciones by mutableStateOf<String?>(null)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var guardando by mutableStateOf(false)
        private set
    var guardado by mutableStateOf(false)
        private set
    var cargando by mutableStateOf(casoId > 0)
        private set

    init {
        if (editando) {
            viewModelScope.launch {
                contenido.caso(casoId)?.let { c ->
                    titulo = c.titulo
                    situacion = c.situacion
                    pregunta = c.pregunta
                    dificultad = Dificultad.de(c.dificultad)
                    fundamento = c.fundamento
                    opciones = c.opciones.map { OpcionEditable(it.texto, it.retroalimentacion, it.puntaje) }
                } ?: run { mensaje = "El caso ya no existe" }
                cargando = false
            }
        }
    }

    fun cambiarTitulo(valor: String) {
        titulo = valor
        errorTitulo = ValidadorContenido.tituloCaso(valor)
    }

    fun cambiarSituacion(valor: String) {
        situacion = valor
        errorSituacion = ValidadorContenido.situacion(valor)
    }

    fun cambiarPregunta(valor: String) {
        pregunta = valor
        errorPregunta = ValidadorContenido.preguntaCaso(valor)
    }

    fun cambiarDificultad(valor: Dificultad) {
        dificultad = valor
    }

    fun cambiarFundamento(valor: String) {
        fundamento = valor
        errorFundamento = ValidadorContenido.fundamento(valor)
    }

    fun cambiarTexto(i: Int, valor: String) = modificar(i) { it.copy(texto = valor) }

    fun cambiarRetroalimentacion(i: Int, valor: String) = modificar(i) { it.copy(retroalimentacion = valor) }

    fun cambiarPuntaje(i: Int, valor: Int) = modificar(i) { it.copy(puntaje = valor.coerceIn(0, 2)) }

    fun agregarOpcion() {
        if (opciones.size < ValidadorContenido.MAX_OPCIONES) opciones = opciones + OpcionEditable()
    }

    fun quitarOpcion(i: Int) {
        if (opciones.size > ValidadorContenido.MIN_OPCIONES) opciones = opciones.filterIndexed { j, _ -> j != i }
    }

    private fun modificar(i: Int, cambio: (OpcionEditable) -> OpcionEditable) {
        opciones = opciones.mapIndexed { j, o -> if (j == i) cambio(o) else o }
        errorOpciones = null
    }

    fun guardar() {
        val lista = opciones.map { OpcionCaso(it.texto.trim(), it.puntaje, it.retroalimentacion.trim()) }
        errorTitulo = ValidadorContenido.tituloCaso(titulo)
        errorSituacion = ValidadorContenido.situacion(situacion)
        errorPregunta = ValidadorContenido.preguntaCaso(pregunta)
        errorFundamento = ValidadorContenido.fundamento(fundamento)
        errorOpciones = ValidadorContenido.opciones(lista)
        if (listOf(errorTitulo, errorSituacion, errorPregunta, errorFundamento, errorOpciones).any { it != null }) {
            mensaje = "Revisa los campos marcados"
            return
        }
        viewModelScope.launch {
            guardando = true
            mensaje = null
            val r = if (editando) {
                gestion.actualizarCaso(
                    casoId, titulo.trim(), situacion.trim(), pregunta.trim(), dificultad.name, fundamento.trim(), lista
                )
            } else {
                gestion.crearCaso(
                    unidadId, titulo.trim(), situacion.trim(), pregunta.trim(), dificultad.name, fundamento.trim(), lista
                )
            }
            when (r) {
                is Respuesta.Exito -> guardado = true
                is Respuesta.Fallo -> mensaje = r.mensaje
            }
            guardando = false
        }
    }
}
