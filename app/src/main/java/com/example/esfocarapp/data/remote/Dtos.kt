package com.example.esfocarapp.data.remote

// Objetos que viajan por la red (JSON). Los nombres coinciden con los de los microservicios.

// ---------- Microservicio de contenidos (ms-contenidos) ----------
data class VersionDto(val version: Int)

data class PaqueteDto(val version: Int, val asignaturas: List<AsignaturaDto>)

data class AsignaturaDto(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val color: String,
    val unidades: List<UnidadDto>
)

data class UnidadDto(
    val id: Long,
    val numero: Int,
    val titulo: String,
    val resumen: String,
    val fichas: List<FichaDto>,
    val preguntas: List<PreguntaDto>,
    val casos: List<CasoDto>
)

data class FichaDto(val id: Long, val titulo: String, val contenido: String, val puntosClave: List<String>)

data class AlternativaDto(val texto: String, val correcta: Boolean, val retroalimentacion: String)

data class PreguntaDto(
    val id: Long,
    val enunciado: String,
    val explicacion: String?,
    val alternativas: List<AlternativaDto>
)

data class OpcionDto(val texto: String, val puntaje: Int, val retroalimentacion: String)

data class CasoDto(
    val id: Long,
    val titulo: String,
    val situacion: String,
    val pregunta: String,
    val dificultad: String,
    val fundamento: String?,
    val opciones: List<OpcionDto>
)

// Lo que envía un docente para crear o editar contenido
data class PreguntaSolicitud(
    val enunciado: String,
    val explicacion: String,
    val alternativas: List<AlternativaDto>
)

data class CasoSolicitud(
    val titulo: String,
    val situacion: String,
    val pregunta: String,
    val dificultad: String,
    val fundamento: String,
    val opciones: List<OpcionDto>
)

// ---------- Microservicio de apuntes (ms-apuntes) ----------
data class ApunteRemoto(
    val id: Long,
    val alias: String,
    val titulo: String,
    val contenido: String,
    val fecha: String
)

data class ApunteSolicitud(
    val alias: String,
    val titulo: String,
    val contenido: String
)

// ---------- Microservicio de resultados (ms-resultados) ----------
data class ResultadoSolicitud(
    val alias: String,
    val actividad: String,
    val aciertos: Int,
    val total: Int
)

data class ResultadoRemoto(
    val id: Long,
    val alias: String,
    val actividad: String,
    val aciertos: Int,
    val total: Int,
    val fecha: String
)

data class ResumenRemoto(
    val alias: String,
    val intentos: Int,
    val promedioPorcentaje: Double,
    val mejorPorcentaje: Int
)

// ---------- API externa: Wikipedia (es.wikipedia.org/api/rest_v1) ----------
data class ConceptoRemoto(
    val title: String? = null,
    val description: String? = null,
    val extract: String? = null
)
