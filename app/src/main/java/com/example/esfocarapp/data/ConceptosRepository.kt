package com.example.esfocarapp.data

import com.example.esfocarapp.data.remote.ConceptoRemoto
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.WikipediaApi
import com.example.esfocarapp.data.remote.llamarApi

// Consulta conceptos en la API externa de Wikipedia (solo lectura, no toca datos locales ni de los microservicios)
class ConceptosRepository(private val api: WikipediaApi) {

    suspend fun buscar(termino: String): Respuesta<ConceptoRemoto> =
        llamarApi("No se encontró un artículo para \"$termino\"") { api.resumen(termino.trim()) }
}
