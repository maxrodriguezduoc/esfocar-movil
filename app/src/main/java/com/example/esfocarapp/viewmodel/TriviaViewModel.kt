package com.example.esfocarapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.FakeData
import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.model.Pregunta
import kotlinx.coroutines.launch

// ViewModel de la trivia: controla en qué pregunta vamos, qué se eligió y el puntaje
class TriviaViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.obtener(app))

    val preguntas: List<Pregunta> = FakeData.preguntas

    var indice by mutableIntStateOf(0)
        private set
    var seleccion by mutableStateOf<Int?>(null)
        private set
    var respondida by mutableStateOf(false)
        private set
    var aciertos by mutableIntStateOf(0)
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
        // Al terminar, guardamos el resultado en la base de datos local
        if (terminada) {
            viewModelScope.launch {
                repo.guardarIntento(actividad, aciertos, preguntas.size)
            }
        }
    }

    fun reiniciar() {
        indice = 0
        seleccion = null
        respondida = false
        aciertos = 0
    }
}
