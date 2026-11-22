package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.ResultadosRepository
import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResumenRemoto
import com.example.esfocarapp.util.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ViewModel del progreso: historial local (Room) + resumen que calcula el microservicio
class ProgresoViewModel(
    repo: AppRepository,
    private val resultados: ResultadosRepository,
    private val aliasActual: () -> String? = { Sesion.alias }
) : ViewModel() {

    val intentos: Flow<List<Intento>> = repo.intentos

    var resumen by mutableStateOf<ResumenRemoto?>(null)
        private set
    var errorNube by mutableStateOf<String?>(null)
        private set
    var cargandoNube by mutableStateOf(false)
        private set

    fun cargarResumen() {
        val alias = aliasActual() ?: return
        viewModelScope.launch {
            cargandoNube = true
            when (val respuesta = resultados.resumen(alias)) {
                is Respuesta.Exito -> {
                    resumen = respuesta.datos
                    errorNube = null
                }
                is Respuesta.Fallo -> errorNube = respuesta.mensaje
            }
            cargandoNube = false
        }
    }
}
