package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ApuntesRepository
import com.example.esfocarapp.data.local.ApunteLocal
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Apuntes personales offline-first: se guardan al instante en el teléfono y se sincronizan con ms-apuntes
class ApuntesViewModel(
    private val repo: ApuntesRepository,
    private val aliasActual: () -> String? = { Sesion.alias }
) : ViewModel() {

    val apuntes: StateFlow<List<ApunteLocal>> = repo.apuntes(aliasActual() ?: "")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
    var sincronizando by mutableStateOf(false)
        private set
    var estadoSync by mutableStateOf<String?>(null)
        private set

    // Si no es null, el formulario está editando ese apunte
    var editando by mutableStateOf<ApunteLocal?>(null)
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

        val alias = aliasActual()
        if (alias == null) {
            mensaje = "Inicia sesión para guardar apuntes"
            return
        }
        val enEdicion = editando
        viewModelScope.launch {
            if (enEdicion == null) {
                repo.crear(alias, titulo.trim(), contenido.trim())
            } else {
                repo.actualizar(enEdicion, titulo.trim(), contenido.trim())
            }
            mensaje = if (enEdicion == null) "Apunte guardado" else "Apunte actualizado"
            limpiarFormulario()
            sincronizarInterno()
        }
    }

    fun editar(apunte: ApunteLocal) {
        editando = apunte
        titulo = apunte.titulo
        contenido = apunte.contenido
        errorTitulo = null
        errorContenido = null
        mensaje = null
    }

    fun cancelarEdicion() {
        limpiarFormulario()
        mensaje = null
    }

    fun borrar(apunte: ApunteLocal) {
        viewModelScope.launch {
            repo.eliminar(apunte)
            if (editando?.id == apunte.id) limpiarFormulario()
            mensaje = "Apunte eliminado"
            sincronizarInterno()
        }
    }

    fun sincronizar() {
        viewModelScope.launch { sincronizarInterno() }
    }

    private suspend fun sincronizarInterno() {
        sincronizando = true
        estadoSync = when (val r = repo.sincronizar(aliasActual())) {
            is Respuesta.Exito -> "Sincronizado con el servidor"
            is Respuesta.Fallo -> "Guardado en el teléfono. Se sincronizará al recuperar la conexión."
        }
        sincronizando = false
    }

    private fun limpiarFormulario() {
        titulo = ""
        contenido = ""
        errorTitulo = null
        errorContenido = null
        editando = null
    }
}
