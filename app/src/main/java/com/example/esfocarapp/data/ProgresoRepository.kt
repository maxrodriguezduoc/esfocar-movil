package com.example.esfocarapp.data

import com.example.esfocarapp.data.local.Favorito
import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.data.local.ProgresoDao
import com.example.esfocarapp.data.local.RendimientoUnidad
import com.example.esfocarapp.data.local.RespuestaEntity
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResumenRemoto
import com.example.esfocarapp.model.Pregunta
import com.example.esfocarapp.model.TipoActividad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Progreso del estudiante. Se guarda primero en el teléfono y luego se sube a ms-resultados.
class ProgresoRepository(
    private val dao: ProgresoDao,
    private val resultados: ResultadosRepository,
    private val reloj: () -> Long = System::currentTimeMillis,
    private val alCambiar: () -> Unit = {}
) {

    fun intentos(alias: String): Flow<List<Intento>> = dao.intentos(alias)

    fun rendimiento(alias: String): Flow<List<RendimientoUnidad>> = dao.rendimientoPorUnidad(alias)

    fun errores(alias: String): Flow<List<Long>> = dao.errores(alias)

    suspend fun erroresActuales(alias: String): List<Long> = dao.erroresActuales(alias)

    fun favoritos(alias: String): Flow<Set<Long>> = dao.favoritos(alias).map { it.toSet() }

    suspend fun cambiarFavorito(alias: String, fichaId: Long, agregar: Boolean) {
        if (agregar) dao.agregarFavorito(Favorito(alias, fichaId)) else dao.quitarFavorito(alias, fichaId)
    }

    suspend fun registrarRespuesta(alias: String, pregunta: Pregunta, correcta: Boolean) {
        dao.insertarRespuesta(RespuestaEntity(0, alias, pregunta.id, pregunta.unidadId, correcta, reloj()))
    }

    suspend fun registrarActividad(
        alias: String,
        tipo: TipoActividad,
        titulo: String,
        aciertos: Int,
        total: Int,
        asignaturaId: Long? = null,
        refId: Long? = null
    ): Intento {
        val ahora = reloj()
        val intento = Intento(
            alias = alias, tipo = tipo.name, titulo = titulo.take(60), aciertos = aciertos, total = total,
            asignaturaId = asignaturaId, refId = refId, timestamp = ahora, fecha = formatearFecha(ahora)
        )
        val id = dao.insertarIntento(intento)
        alCambiar()
        return intento.copy(id = id)
    }

    // Sube al microservicio los resultados que aún están solo en el teléfono
    suspend fun subirPendientes(): Respuesta<Int> {
        var subidos = 0
        for (i in dao.intentosPendientes()) {
            when (val r = resultados.guardar(i.alias, i.titulo, i.aciertos, i.total)) {
                is Respuesta.Fallo -> return r
                is Respuesta.Exito -> {
                    dao.marcarSincronizado(i.id)
                    subidos++
                }
            }
        }
        return Respuesta.Exito(subidos)
    }

    suspend fun resumenRemoto(alias: String): Respuesta<ResumenRemoto> = resultados.resumen(alias)

    companion object {
        fun formatearFecha(ms: Long): String =
            SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(ms))
    }
}
