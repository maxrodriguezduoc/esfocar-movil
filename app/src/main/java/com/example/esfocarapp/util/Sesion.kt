package com.example.esfocarapp.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Guarda quién inició sesión. Si cambia, las pantallas que lo usan se actualizan.
object Sesion {
    var alias: String? by mutableStateOf(null)
    var rol: String by mutableStateOf(Rol.ESTUDIANTE)

    val esDocente: Boolean get() = rol == Rol.DOCENTE
}

// Perfiles de usuario. El docente puede publicar preguntas y casos para sus estudiantes.
object Rol {
    const val ESTUDIANTE = "ESTUDIANTE"
    const val DOCENTE = "DOCENTE"
}
