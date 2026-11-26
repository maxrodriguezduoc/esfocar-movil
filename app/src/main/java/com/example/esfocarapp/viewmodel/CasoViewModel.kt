package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.ProgresoRepository
import com.example.esfocarapp.model.Caso
import com.example.esfocarapp.model.NivelActuacion
import com.example.esfocarapp.model.OpcionCaso
import com.example.esfocarapp.model.TipoActividad
import com.example.esfocarapp.util.Sesion
import kotlinx.coroutines.launch

// Análisis de una situación simulada: el estudiante elige cómo actuar y recibe retroalimentación fundamentada
class CasoViewModel(
    private val contenido: ContenidoRepository,
    private val progreso: ProgresoRepository,
    private val casoId: Long,
    private val aliasActual: () -> String? = { Sesion.alias }
) : ViewModel() {

    var caso by mutableStateOf<Caso?>(null)
        private set
    var cargando by mutableStateOf(true)
        private set
    var seleccion by mutableStateOf<Int?>(null)
        private set
    var confirmado by mutableStateOf(false)
        private set

    private var asignaturaId: Long? = null

    val opcionElegida: OpcionCaso? get() = seleccion?.let { caso?.opciones?.getOrNull(it) }
    val nivel: NivelActuacion? get() = opcionElegida?.let { NivelActuacion.de(it.puntaje) }

    init {
        viewModelScope.launch {
            val c = contenido.caso(casoId)
            caso = c
            asignaturaId = c?.let { contenido.unidad(it.unidadId)?.asignaturaId }
            cargando = false
        }
    }

    fun elegir(posicion: Int) {
        if (!confirmado) seleccion = posicion
    }

    fun confirmar() {
        val c = caso ?: return
        val opcion = opcionElegida ?: return
        if (confirmado) return
        confirmado = true
        val alias = aliasActual() ?: return
        viewModelScope.launch {
            progreso.registrarActividad(alias, TipoActividad.CASO, c.titulo, opcion.puntaje, 2, asignaturaId, c.id)
        }
    }

    fun reintentar() {
        seleccion = null
        confirmado = false
    }
}
