package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.ProgresoRepository
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResumenRemoto
import com.example.esfocarapp.model.Caso
import com.example.esfocarapp.model.ConstructorPanel
import com.example.esfocarapp.model.Panel
import com.example.esfocarapp.model.casoDelDia
import com.example.esfocarapp.util.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

// Panel de inicio y de progreso: racha, promedio, avance por asignatura, temas débiles y resumen del servidor
class ProgresoViewModel(
    contenido: ContenidoRepository,
    private val progreso: ProgresoRepository,
    conexion: Flow<Boolean>,
    private val aliasActual: () -> String? = { Sesion.alias },
    private val hoy: () -> LocalDate = LocalDate::now,
    private val zona: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val alias = aliasActual() ?: ""

    val panel: StateFlow<Panel> = combine(
        combine(contenido.asignaturas, contenido.unidades, ::Pair),
        progreso.intentos(alias),
        progreso.rendimiento(alias),
        progreso.errores(alias),
        contenido.casos
    ) { (asignaturas, unidades), intentos, rendimiento, errores, casos ->
        ConstructorPanel.construir(asignaturas, unidades, intentos, rendimiento, errores.size, casos.size, hoy(), zona)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Panel())

    val casoDelDia: StateFlow<Caso?> = contenido.casos.map { casoDelDia(it, hoy()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val enLinea: StateFlow<Boolean> = conexion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

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
            when (val r = progreso.resumenRemoto(alias)) {
                is Respuesta.Exito -> {
                    resumen = r.datos
                    errorNube = null
                }
                is Respuesta.Fallo -> errorNube = r.mensaje
            }
            cargandoNube = false
        }
    }
}
