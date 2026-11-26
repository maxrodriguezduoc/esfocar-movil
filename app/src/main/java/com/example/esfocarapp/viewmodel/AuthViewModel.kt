package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.util.Preferencias
import com.example.esfocarapp.util.Rol
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.launch

// ViewModel de los formularios de ingreso y registro.
// Guarda el ESTADO (textos, errores) y la LÓGICA. La pantalla solo lo muestra.
class AuthViewModel(
    private val repo: AppRepository,
    private val prefs: Preferencias? = null,
    private val codigoDocente: String = ""
) : ViewModel() {

    // Valores de los campos
    var alias by mutableStateOf("")
        private set
    var clave by mutableStateOf("")
        private set
    var confirmacion by mutableStateOf("")
        private set

    // Errores de cada campo (null = sin error)
    var errorAlias by mutableStateOf<String?>(null)
        private set
    var errorClave by mutableStateOf<String?>(null)
        private set
    var errorConfirmacion by mutableStateOf<String?>(null)
        private set

    // Registro como docente (requiere el código entregado a los docentes)
    var esDocente by mutableStateOf(false)
        private set
    var codigo by mutableStateOf("")
        private set
    var errorCodigo by mutableStateOf<String?>(null)
        private set

    var mensaje by mutableStateOf<String?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set

    fun cambiarAlias(valor: String) {
        alias = valor
        errorAlias = Validador.alias(valor)
        mensaje = null
    }

    fun cambiarClave(valor: String) {
        clave = valor
        errorClave = Validador.clave(valor)
        if (confirmacion.isNotEmpty()) errorConfirmacion = Validador.confirmacion(valor, confirmacion)
        mensaje = null
    }

    fun cambiarConfirmacion(valor: String) {
        confirmacion = valor
        errorConfirmacion = Validador.confirmacion(clave, valor)
        mensaje = null
    }

    fun cambiarEsDocente(valor: Boolean) {
        esDocente = valor
        if (!valor) {
            codigo = ""
            errorCodigo = null
        }
        mensaje = null
    }

    fun cambiarCodigo(valor: String) {
        codigo = valor
        errorCodigo = Validador.codigoDocente(valor, codigoDocente)
        mensaje = null
    }

    // registro = true  -> crear cuenta nueva
    // registro = false -> iniciar sesión
    fun enviar(registro: Boolean, onExito: () -> Unit) {
        // 1. Validar todos los campos
        errorAlias = Validador.alias(alias)
        errorClave = Validador.clave(clave)
        errorConfirmacion = if (registro) Validador.confirmacion(clave, confirmacion) else null
        errorCodigo = if (registro && esDocente) Validador.codigoDocente(codigo, codigoDocente) else null

        if (errorAlias != null || errorClave != null || errorConfirmacion != null || errorCodigo != null) {
            mensaje = "Revisa los campos marcados"
            return
        }

        // 2. Registrar o iniciar sesión usando la base de datos
        viewModelScope.launch {
            cargando = true
            if (registro) {
                val rol = if (esDocente) Rol.DOCENTE else Rol.ESTUDIANTE
                if (repo.registrarUsuario(alias, clave, rol)) {
                    iniciar(rol)
                    onExito()
                } else {
                    mensaje = "Ese usuario ya existe"
                }
            } else {
                val usuario = repo.autenticar(alias, clave)
                if (usuario != null) {
                    iniciar(usuario.rol)
                    onExito()
                } else {
                    mensaje = "Usuario o contraseña incorrectos"
                }
            }
            cargando = false
        }
    }

    // La sesión queda guardada: al volver a abrir la app no se pide ingresar de nuevo
    private fun iniciar(rol: String) {
        Sesion.alias = alias
        Sesion.rol = rol
        prefs?.aliasSesion = alias
        prefs?.rolSesion = rol
    }
}
