package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ApuntesRepository
import com.example.esfocarapp.data.remote.ApunteRemoto
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.launch

// ViewModel de apuntes personales. Los datos viven en el microservicio ms-apuntes (CRUD completo).
class ApuntesViewModel(
    private val repo: ApuntesRepository,
    private val aliasActual: () -> String? = { Sesion.alias }
) : ViewModel() {

    var apuntes by mutableStateOf<List<ApunteRemoto>>(emptyList())
        private set
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
    var cargando by mutableStateOf(false)
        private set

    // Si no es null, el formulario está editando el apunte con ese id (UPDATE)
    var editandoId by mutableStateOf<Long?>(null)
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

    // READ: trae los apuntes del usuario desde el microservicio
    fun cargar() {
        viewModelScope.launch { recargar() }
    }

    private suspend fun recargar() {
        val alias = aliasActual() ?: return
        cargando = true
        when (val respuesta = repo.listar(alias)) {
            is Respuesta.Exito -> apuntes = respuesta.datos
            is Respuesta.Fallo -> mensaje = respuesta.mensaje
        }
        cargando = false
    }

    // CREATE o UPDATE según si se está editando
    fun guardar() {
        errorTitulo = Validador.tituloApunte(titulo)
        errorContenido = Validador.contenidoApunte(contenido)
        if (errorTitulo != null || errorContenido != null) return

        val alias = aliasActual()
        if (alias == null) {
            mensaje = "Inicia sesión para guardar apuntes"
            return
        }

        viewModelScope.launch {
            cargando = true
            val id = editandoId
            val respuesta = if (id == null) {
                repo.crear(alias, titulo.trim(), contenido.trim())
            } else {
                repo.actualizar(id, alias, titulo.trim(), contenido.trim())
            }
            when (respuesta) {
                is Respuesta.Exito -> {
                    mensaje = if (id == null) "Apunte guardado" else "Apunte actualizado"
                    limpiarFormulario()
                    recargar()
                }
                is Respuesta.Fallo -> mensaje = respuesta.mensaje
            }
            cargando = false
        }
    }

    // Carga un apunte en el formulario para editarlo
    fun editar(apunte: ApunteRemoto) {
        editandoId = apunte.id
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

    // DELETE
    fun borrar(apunte: ApunteRemoto) {
        viewModelScope.launch {
            when (val respuesta = repo.eliminar(apunte.id)) {
                is Respuesta.Exito -> {
                    if (editandoId == apunte.id) limpiarFormulario()
                    mensaje = "Apunte eliminado"
                    recargar()
                }
                is Respuesta.Fallo -> mensaje = respuesta.mensaje
            }
        }
    }

    private fun limpiarFormulario() {
        titulo = ""
        contenido = ""
        errorTitulo = null
        errorContenido = null
        editandoId = null
    }
}
