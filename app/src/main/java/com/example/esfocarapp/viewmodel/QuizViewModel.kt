package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.ProgresoRepository
import com.example.esfocarapp.model.Calificacion
import com.example.esfocarapp.model.Pregunta
import com.example.esfocarapp.model.TipoActividad
import com.example.esfocarapp.util.Sesion
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ModoQuiz(val tipo: TipoActividad) {
    UNIDAD(TipoActividad.TRIVIA),
    ASIGNATURA(TipoActividad.TRIVIA),
    RAPIDA(TipoActividad.TRIVIA),
    SIMULACRO(TipoActividad.SIMULACRO),
    ERRORES(TipoActividad.REPASO);

    // En el simulacro no se muestra si acertó hasta el final, como en una prueba real
    val retroalimentacionInmediata: Boolean get() = this != SIMULACRO

    companion object {
        const val PREGUNTAS_TRIVIA = 10
        const val PREGUNTAS_SIMULACRO = 15
        const val SEGUNDOS_POR_PREGUNTA = 40
    }
}

data class RespuestaDada(val pregunta: Pregunta, val elegida: Int?) {
    val correcta: Boolean get() = elegida != null && pregunta.alternativas[elegida].esCorrecta
}

// Motor único de autoevaluación: trivia por unidad/asignatura, trivia rápida, simulacro y repaso de errores
class QuizViewModel(
    private val contenido: ContenidoRepository,
    private val progreso: ProgresoRepository,
    val modo: ModoQuiz,
    private val refId: Long,
    private val aliasActual: () -> String? = { Sesion.alias },
    private val aleatorio: Random = Random.Default
) : ViewModel() {

    var cargando by mutableStateOf(true)
        private set
    var titulo by mutableStateOf("")
        private set
    var preguntas by mutableStateOf<List<Pregunta>>(emptyList())
        private set
    var indice by mutableIntStateOf(0)
        private set
    var seleccion by mutableStateOf<Int?>(null)
        private set
    var respondida by mutableStateOf(false)
        private set
    var respuestas by mutableStateOf<List<RespuestaDada>>(emptyList())
        private set
    var terminado by mutableStateOf(false)
        private set
    var tiempoRestante by mutableStateOf<Int?>(null)
        private set

    private var asignaturaId: Long? = null

    val vacio: Boolean get() = !cargando && preguntas.isEmpty()
    val preguntaActual: Pregunta? get() = preguntas.getOrNull(indice)
    val esUltima: Boolean get() = indice == preguntas.lastIndex
    val aciertos: Int get() = respuestas.count { it.correcta }
    val porcentaje: Int get() = Calificacion.porcentaje(aciertos, preguntas.size)
    val nota: Double get() = Calificacion.nota(aciertos, preguntas.size)
    val errores: List<RespuestaDada> get() = respuestas.filter { !it.correcta }

    init {
        viewModelScope.launch { cargar() }
    }

    private suspend fun cargar() {
        cargando = true
        val lista: List<Pregunta>
        when (modo) {
            ModoQuiz.UNIDAD -> {
                val unidad = contenido.unidad(refId)
                asignaturaId = unidad?.asignaturaId
                titulo = "Unidad ${unidad?.numero ?: ""} · ${unidad?.titulo ?: "Trivia"}"
                lista = contenido.preguntasDeUnidad(refId)
            }
            ModoQuiz.ASIGNATURA -> {
                asignaturaId = refId
                titulo = "Trivia por asignatura"
                lista = contenido.preguntasDeAsignatura(refId).shuffled(aleatorio).take(ModoQuiz.PREGUNTAS_TRIVIA)
            }
            ModoQuiz.RAPIDA -> {
                titulo = "Trivia rápida"
                lista = contenido.todasLasPreguntas().shuffled(aleatorio).take(ModoQuiz.PREGUNTAS_TRIVIA)
            }
            ModoQuiz.SIMULACRO -> {
                asignaturaId = refId.takeIf { it > 0 }
                titulo = "Simulacro de evaluación"
                val banco = if (refId > 0) contenido.preguntasDeAsignatura(refId) else contenido.todasLasPreguntas()
                lista = banco.shuffled(aleatorio).take(ModoQuiz.PREGUNTAS_SIMULACRO)
            }
            ModoQuiz.ERRORES -> {
                titulo = "Repaso de errores"
                val ids = aliasActual()?.let { progreso.erroresActuales(it) } ?: emptyList()
                lista = contenido.preguntasPorIds(ids).shuffled(aleatorio).take(ModoQuiz.PREGUNTAS_SIMULACRO)
            }
        }
        // Se mezclan preguntas y alternativas para que no se memorice la posición de la respuesta
        preguntas = lista.shuffled(aleatorio).map { it.copy(alternativas = it.alternativas.shuffled(aleatorio)) }
        tiempoRestante = if (modo == ModoQuiz.SIMULACRO) preguntas.size * ModoQuiz.SEGUNDOS_POR_PREGUNTA else null
        cargando = false
    }

    fun seleccionar(posicion: Int) {
        if (!respondida && !terminado) seleccion = posicion
    }

    // Devuelve si acertó (o null si no había nada seleccionado)
    fun responder(): Boolean? {
        val elegida = seleccion ?: return null
        val pregunta = preguntaActual ?: return null
        val dada = RespuestaDada(pregunta, elegida)
        respuestas = respuestas + dada
        aliasActual()?.let { alias ->
            viewModelScope.launch { progreso.registrarRespuesta(alias, pregunta, dada.correcta) }
        }
        if (modo.retroalimentacionInmediata) respondida = true else avanzar()
        return dada.correcta
    }

    fun siguiente() {
        if (respondida) avanzar()
    }

    private fun avanzar() {
        respondida = false
        seleccion = null
        if (indice >= preguntas.lastIndex) finalizar() else indice++
    }

    // La pantalla lo llama cada segundo durante el simulacro
    fun descontarSegundo() {
        val restante = tiempoRestante ?: return
        if (terminado) return
        if (restante <= 1) {
            tiempoRestante = 0
            // Las preguntas no respondidas cuentan como incorrectas
            respuestas = respuestas + preguntas.drop(respuestas.size).map { RespuestaDada(it, null) }
            finalizar()
        } else {
            tiempoRestante = restante - 1
        }
    }

    private fun finalizar() {
        if (terminado) return
        terminado = true
        val alias = aliasActual() ?: return
        viewModelScope.launch {
            progreso.registrarActividad(alias, modo.tipo, titulo, aciertos, preguntas.size, asignaturaId)
        }
    }

    fun reiniciar() {
        indice = 0
        seleccion = null
        respondida = false
        respuestas = emptyList()
        terminado = false
        viewModelScope.launch { cargar() }
    }
}
