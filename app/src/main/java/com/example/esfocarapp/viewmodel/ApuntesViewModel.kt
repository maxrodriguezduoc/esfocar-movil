package com.example.esfocarapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.data.local.Apunte
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ViewModel del segundo formulario: apuntes personales del estudiante
class ApuntesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.obtener(app))

    // Lista de apuntes guardados (se actualiza sola)
    val apuntes: Flow<List<Apunte>> = repo.apuntes

    var titulo by mutableStateOf("")
        private set
    var contenido by mutableStateOf("")
        private set
    var errorTitulo by mutableStateOf<String?>(null)
        private set
    var errorContenido by mutableStateOf<String?>(null)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    fun cambiarTitulo(valor: String) {
        titulo = valor
        errorTitulo = Validador.tituloApunte(valor)
        mensaje = null
    }

    fun cambiarContenido(valor: String) {
        contenido = valor
        errorContenido = Validador.contenidoApunte(valor)
        mensaje = null
    }

    fun guardar() {
        errorTitulo = Validador.tituloApunte(titulo)
        errorContenido = Validador.contenidoApunte(contenido)
        if (errorTitulo != null || errorContenido != null) return

        viewModelScope.launch {
            repo.guardarApunte(titulo.trim(), contenido.trim())
            titulo = ""
            contenido = ""
            errorTitulo = null
            errorContenido = null
            mensaje = "Apunte guardado"
        }
    }

    fun borrar(apunte: Apunte) {
        viewModelScope.launch {
            repo.borrarApunte(apunte)
        }
    }
}
