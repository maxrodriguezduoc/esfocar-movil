package com.example.esfocarapp.data

import com.example.esfocarapp.data.local.ContenidoDao
import com.example.esfocarapp.data.remote.ContenidosApi
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.llamarApi
import com.example.esfocarapp.model.Asignatura
import com.example.esfocarapp.model.Caso
import com.example.esfocarapp.model.Ficha
import com.example.esfocarapp.model.Pregunta
import com.example.esfocarapp.model.Unidad
import com.example.esfocarapp.util.Preferencias
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Contenido académico OFFLINE-FIRST:
//  1. La app lee SIEMPRE desde Room (funciona sin internet).
//  2. En el primer uso, Room se llena con el contenido incluido en la app (assets).
//  3. Cuando hay conexión, se consulta la versión en ms-contenidos y, si hay una más nueva, se descarga.
class ContenidoRepository(
    private val dao: ContenidoDao,
    private val api: ContenidosApi,
    private val prefs: Preferencias,
    private val leerContenidoIncluido: () -> String
) {

    val asignaturas: Flow<List<Asignatura>> = dao.asignaturas().map { it.map(MapeoContenido::asignatura) }

    val unidades: Flow<List<Unidad>> = dao.unidades().map { it.map(MapeoContenido::unidad) }

    val casos: Flow<List<Caso>> = dao.casos().map { it.map(MapeoContenido::caso) }

    fun fichas(unidadId: Long): Flow<List<Ficha>> = dao.fichas(unidadId).map { it.map(MapeoContenido::ficha) }

    fun fichasPorIds(ids: List<Long>): Flow<List<Ficha>> = dao.fichasPorIds(ids).map { it.map(MapeoContenido::ficha) }

    fun buscar(texto: String): Flow<List<Ficha>> = dao.buscarFichas(texto).map { it.map(MapeoContenido::ficha) }

    suspend fun ficha(id: Long): Ficha? = dao.ficha(id)?.let(MapeoContenido::ficha)

    suspend fun unidad(id: Long): Unidad? = dao.unidad(id)?.let(MapeoContenido::unidad)

    suspend fun caso(id: Long): Caso? = dao.caso(id)?.let(MapeoContenido::caso)

    suspend fun pregunta(id: Long): Pregunta? = dao.pregunta(id)?.let(MapeoContenido::pregunta)

    fun preguntasFlujo(unidadId: Long): Flow<List<Pregunta>> =
        dao.preguntasDeUnidadFlujo(unidadId).map { it.map(MapeoContenido::pregunta) }

    suspend fun preguntasDeUnidad(unidadId: Long): List<Pregunta> =
        dao.preguntasDeUnidad(unidadId).map(MapeoContenido::pregunta)

    suspend fun preguntasDeAsignatura(asignaturaId: Long): List<Pregunta> =
        dao.preguntasDeAsignatura(asignaturaId).map(MapeoContenido::pregunta)

    suspend fun todasLasPreguntas(): List<Pregunta> = dao.todasLasPreguntas().map(MapeoContenido::pregunta)

    suspend fun preguntasPorIds(ids: List<Long>): List<Pregunta> =
        if (ids.isEmpty()) emptyList() else dao.preguntasPorIds(ids).map(MapeoContenido::pregunta)

    // Primer uso sin internet: carga el contenido que viene dentro de la app
    suspend fun asegurarContenidoInicial() {
        if (dao.contarAsignaturas() > 0) return
        val paquete = MapeoContenido.leerPaquete(leerContenidoIncluido())
        dao.reemplazarTodo(MapeoContenido.aEntidades(paquete))
        prefs.versionContenido = paquete.version
    }

    // Devuelve Exito(true) si se descargó contenido distinto, Exito(false) si ya estaba al día
    suspend fun sincronizar(): Respuesta<Boolean> {
        val version = when (val r = llamarApi { api.version() }) {
            is Respuesta.Fallo -> return r
            is Respuesta.Exito -> r.datos.version
        }
        // Distinta (no solo mayor): también cubre un servidor reiniciado con su base de datos nueva
        if (version == prefs.versionContenido) return Respuesta.Exito(false)

        return when (val r = llamarApi { api.paquete() }) {
            is Respuesta.Fallo -> r
            is Respuesta.Exito -> {
                dao.reemplazarTodo(MapeoContenido.aEntidades(r.datos))
                prefs.versionContenido = r.datos.version
                Respuesta.Exito(true)
            }
        }
    }
}
