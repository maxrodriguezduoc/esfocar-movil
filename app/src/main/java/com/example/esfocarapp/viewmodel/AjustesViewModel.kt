package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.Sincronizador
import com.example.esfocarapp.ui.theme.PreferenciaTema
import com.example.esfocarapp.util.Preferencias
import com.example.esfocarapp.util.Rol
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.util.Tema
import kotlinx.coroutines.launch

object Rutas {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val PRINCIPAL = "principal"
}

// Sesión, tema visual, bienvenida y sincronización manual
class AjustesViewModel(
    private val prefs: Preferencias,
    private val sincronizador: Sincronizador
) : ViewModel() {

    var tema by mutableStateOf(prefs.tema)
        private set
    var sincronizando by mutableStateOf(false)
        private set
    var mensajeSync by mutableStateOf<String?>(null)
        private set

    fun destinoInicial(): String = when {
        !prefs.onboardingVisto -> Rutas.ONBOARDING
        Sesion.alias != null -> Rutas.PRINCIPAL
        else -> Rutas.LOGIN
    }

    fun completarOnboarding() {
        prefs.onboardingVisto = true
    }

    fun cambiarTema(nuevo: Tema) {
        tema = nuevo
        prefs.tema = nuevo
        PreferenciaTema.tema = nuevo
    }

    fun sincronizarAhora() {
        if (sincronizando) return
        viewModelScope.launch {
            sincronizando = true
            mensajeSync = sincronizador.ejecutar().mensaje
            sincronizando = false
        }
    }

    fun cerrarSesion() {
        Sesion.alias = null
        Sesion.rol = Rol.ESTUDIANTE
        prefs.aliasSesion = null
        prefs.rolSesion = Rol.ESTUDIANTE
    }
}
