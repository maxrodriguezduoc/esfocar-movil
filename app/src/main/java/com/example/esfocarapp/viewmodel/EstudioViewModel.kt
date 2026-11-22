package com.example.esfocarapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.ProgresoRepository
import com.example.esfocarapp.model.Asignatura
import com.example.esfocarapp.model.AvanceAsignatura
import com.example.esfocarapp.model.AvanceUnidad
import com.example.esfocarapp.model.Caso
import com.example.esfocarapp.model.ConstructorPanel
import com.example.esfocarapp.model.Ficha
import com.example.esfocarapp.model.TipoActividad
import com.example.esfocarapp.model.Unidad
import com.example.esfocarapp.util.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CasoConEstado(val caso: Caso, val asignatura: Asignatura?, val mejorPuntaje: Int?)

// Navegación del contenido: asignaturas → unidades → fichas, búsqueda, favoritos y listado de casos
@OptIn(ExperimentalCoroutinesApi::class)
class EstudioViewModel(
    private val contenido: ContenidoRepository,
    private val progreso: ProgresoRepository,
    aliasActual: () -> String? = { Sesion.alias }
) : ViewModel() {

    private val alias = aliasActual() ?: ""
    private val rendimiento = progreso.rendimiento(alias)

    val asignaturas: StateFlow<List<AvanceAsignatura>> =
        combine(contenido.asignaturas, contenido.unidades, rendimiento) { a, u, r ->
            ConstructorPanel.avanceAsignaturas(a, u, r)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoritos: StateFlow<Set<Long>> =
        progreso.favoritos(alias).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val fichasFavoritas: StateFlow<List<Ficha>> = progreso.favoritos(alias)
        .flatMapLatest { ids -> if (ids.isEmpty()) flowOf(emptyList()) else contenido.fichasPorIds(ids.toList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _consulta = MutableStateFlow("")
    val consulta: StateFlow<String> = _consulta.asStateFlow()

    val resultadosBusqueda: StateFlow<List<Ficha>> = _consulta
        .flatMapLatest { t -> if (t.trim().length < 2) flowOf(emptyList()) else contenido.buscar(t.trim()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val casos: StateFlow<List<CasoConEstado>> =
        combine(contenido.casos, contenido.unidades, contenido.asignaturas, progreso.intentos(alias)) { c, u, a, i ->
            val asignaturaDeUnidad = u.associate { it.id to it.asignaturaId }
            val mejores = i.filter { it.tipo == TipoActividad.CASO.name && it.refId != null }
                .groupBy { it.refId!! }
                .mapValues { (_, lista) -> lista.maxOf { it.aciertos } }
            c.map { caso ->
                CasoConEstado(caso, a.find { it.id == asignaturaDeUnidad[caso.unidadId] }, mejores[caso.id])
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun asignatura(id: Long): Flow<Asignatura?> = contenido.asignaturas.map { lista -> lista.find { it.id == id } }

    fun unidad(id: Long): Flow<Unidad?> = contenido.unidades.map { lista -> lista.find { it.id == id } }

    fun unidadesDe(asignaturaId: Long): Flow<List<AvanceUnidad>> =
        combine(contenido.unidades, rendimiento) { u, r ->
            ConstructorPanel.avanceUnidades(u.filter { it.asignaturaId == asignaturaId }, r)
        }

    fun fichas(unidadId: Long): Flow<List<Ficha>> = contenido.fichas(unidadId)

    suspend fun ficha(id: Long): Ficha? = contenido.ficha(id)

    fun buscar(texto: String) {
        _consulta.value = texto
    }

    fun alternarFavorito(fichaId: Long) {
        val agregar = fichaId !in favoritos.value
        viewModelScope.launch { progreso.cambiarFavorito(alias, fichaId, agregar) }
    }
}
