package com.example.esfocarapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.launch

// ViewModel de los formularios de ingreso y registro.
// Guarda el ESTADO (textos, errores) y la LÓGICA. La pantalla solo lo muestra.
class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.obtener(app))

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

    // registro = true  -> crear cuenta nueva
    // registro = false -> iniciar sesión
    fun enviar(registro: Boolean, onExito: () -> Unit) {
        // 1. Validar todos los campos
        errorAlias = Validador.alias(alias)
        errorClave = Validador.clave(clave)
        errorConfirmacion = if (registro) Validador.confirmacion(clave, confirmacion) else null

        if (errorAlias != null || errorClave != null || errorConfirmacion != null) {
            mensaje = "Revisa los campos marcados"
            return
        }

        // 2. Registrar o iniciar sesión usando la base de datos
        viewModelScope.launch {
            cargando = true
            if (registro) {
                if (repo.registrarUsuario(alias, clave)) {
                    Sesion.alias = alias
                    onExito()
                } else {
                    mensaje = "Ese usuario ya existe"
                }
            } else {
                if (repo.iniciarSesion(alias, clave)) {
                    Sesion.alias = alias
                    onExito()
                } else {
                    mensaje = "Usuario o contraseña incorrectos"
                }
            }
            cargando = false
        }
    }
}
