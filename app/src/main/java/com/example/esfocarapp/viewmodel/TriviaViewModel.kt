package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.FakeData
import com.example.esfocarapp.data.ResultadosRepository
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.model.Pregunta
import com.example.esfocarapp.util.Sesion
import kotlinx.coroutines.launch

// ViewModel de la trivia: controla en qué pregunta vamos, qué se eligió y el puntaje
class TriviaViewModel(
    private val repo: AppRepository,
    private val resultados: ResultadosRepository,
    val preguntas: List<Pregunta> = FakeData.preguntas,
    private val aliasActual: () -> String? = { Sesion.alias }
) : ViewModel() {

    var indice by mutableIntStateOf(0)
        private set
    var seleccion by mutableStateOf<Int?>(null)
        private set
    var respondida by mutableStateOf(false)
        private set
    var aciertos by mutableIntStateOf(0)
        private set

    // Estado de la sincronización con el microservicio al terminar
    var avisoNube by mutableStateOf<String?>(null)
        private set

    val terminada: Boolean
        get() = indice >= preguntas.size

    val preguntaActual: Pregunta
        get() = preguntas[indice]

    fun seleccionar(posicion: Int) {
        if (!respondida) seleccion = posicion
    }

    // Devuelve true si la respuesta fue correcta
    fun responder(): Boolean {
        val elegida = seleccion ?: return false
        respondida = true
        val correcta = preguntaActual.alternativas[elegida].esCorrecta
        if (correcta) aciertos++
        return correcta
    }

    fun siguiente(actividad: String) {
        indice++
        seleccion = null
        respondida = false
        // Al terminar: se guarda en el teléfono (Room) y se envía al microservicio (red)
        if (terminada) {
            viewModelScope.launch {
                repo.guardarIntento(actividad, aciertos, preguntas.size)
                enviarANube(actividad)
            }
        }
    }

    private suspend fun enviarANube(actividad: String) {
        val alias = aliasActual() ?: return
        avisoNube = when (resultados.guardar(alias, actividad, aciertos, preguntas.size)) {
            is Respuesta.Exito -> "Resultado sincronizado con el servidor"
            is Respuesta.Fallo -> "Resultado guardado solo en el teléfono (sin conexión)"
        }
    }

    fun reiniciar() {
        indice = 0
        seleccion = null
        respondida = false
        aciertos = 0
        avisoNube = null
    }
}
