package com.example.esfocarapp.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Guarda quién inició sesión. Si cambia, las pantallas que lo usan se actualizan.
object Sesion {
    var alias: String? by mutableStateOf(null)
}
