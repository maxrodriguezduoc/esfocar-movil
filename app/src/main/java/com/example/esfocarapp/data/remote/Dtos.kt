package com.example.esfocarapp.data.remote

// Objetos que viajan por la red (JSON). Los nombres coinciden con los de los microservicios.

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
