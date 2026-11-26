package com.example.esfocarapp.data

import com.example.esfocarapp.data.remote.AlternativaDto
import com.example.esfocarapp.data.remote.CasoSolicitud
import com.example.esfocarapp.data.remote.ContenidosApi
import com.example.esfocarapp.data.remote.OpcionDto
import com.example.esfocarapp.data.remote.PreguntaSolicitud
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.llamarApi
import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.OpcionCaso

// Modo docente: publica contenido en ms-contenidos.
// Publicar SÍ requiere conexión (el contenido debe llegar al servidor para que lo reciban los estudiantes).
// Tras cada cambio exitoso se sincroniza el contenido local, así el docente ve el resultado al instante.
class GestionContenidoRepository(
    private val api: ContenidosApi,
    private val contenido: ContenidoRepository
) {

    suspend fun crearPregunta(
        unidadId: Long, enunciado: String, explicacion: String, alternativas: List<Alternativa>
    ): Respuesta<Unit> = publicar("La unidad ya no existe") {
        api.crearPregunta(unidadId, PreguntaSolicitud(enunciado, explicacion, alternativas.map(::aDto)))
    }

    suspend fun actualizarPregunta(
        id: Long, enunciado: String, explicacion: String, alternativas: List<Alternativa>
    ): Respuesta<Unit> = publicar("La pregunta ya no existe") {
        api.actualizarPregunta(id, PreguntaSolicitud(enunciado, explicacion, alternativas.map(::aDto)))
    }

    suspend fun eliminarPregunta(id: Long): Respuesta<Unit> =
        publicar("La pregunta ya no existe") { api.eliminarPregunta(id) }

    suspend fun crearCaso(
        unidadId: Long, titulo: String, situacion: String, pregunta: String, dificultad: String,
        fundamento: String, opciones: List<OpcionCaso>
    ): Respuesta<Unit> = publicar("La unidad ya no existe") {
        api.crearCaso(unidadId, CasoSolicitud(titulo, situacion, pregunta, dificultad, fundamento, opciones.map(::aDto)))
    }

    suspend fun actualizarCaso(
        id: Long, titulo: String, situacion: String, pregunta: String, dificultad: String,
        fundamento: String, opciones: List<OpcionCaso>
    ): Respuesta<Unit> = publicar("El caso ya no existe") {
        api.actualizarCaso(id, CasoSolicitud(titulo, situacion, pregunta, dificultad, fundamento, opciones.map(::aDto)))
    }

    suspend fun eliminarCaso(id: Long): Respuesta<Unit> =
        publicar("El caso ya no existe") { api.eliminarCaso(id) }

    private suspend fun publicar(noEncontrado: String, llamada: suspend () -> Any): Respuesta<Unit> {
        return when (val r = llamarApi(noEncontrado) { llamada() }) {
            is Respuesta.Fallo -> if (r.mensaje == SIN_CONEXION) {
                Respuesta.Fallo("Se necesita conexión con el servidor para publicar contenido")
            } else {
                r
            }
            is Respuesta.Exito -> {
                contenido.sincronizar()      // trae la nueva versión a Room (si falla, llegará en la próxima sincronización)
                Respuesta.Exito(Unit)
            }
        }
    }

    private companion object {
        const val SIN_CONEXION = "Sin conexión con el servidor"
    }

    private fun aDto(a: Alternativa) = AlternativaDto(a.texto.trim(), a.esCorrecta, a.retroalimentacion.trim())
    private fun aDto(o: OpcionCaso) = OpcionDto(o.texto.trim(), o.puntaje, o.retroalimentacion.trim())
}
