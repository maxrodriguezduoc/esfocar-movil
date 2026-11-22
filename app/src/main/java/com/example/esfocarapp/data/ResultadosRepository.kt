package com.example.esfocarapp.data

import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResultadoRemoto
import com.example.esfocarapp.data.remote.ResultadoSolicitud
import com.example.esfocarapp.data.remote.ResultadosApi
import com.example.esfocarapp.data.remote.ResumenRemoto
import com.example.esfocarapp.data.remote.llamarApi

// Resultados de trivias sincronizados con el microservicio ms-resultados
class ResultadosRepository(private val api: ResultadosApi) {

    suspend fun guardar(alias: String, actividad: String, aciertos: Int, total: Int): Respuesta<ResultadoRemoto> =
        llamarApi { api.crear(ResultadoSolicitud(alias, actividad, aciertos, total)) }

    suspend fun listar(alias: String): Respuesta<List<ResultadoRemoto>> =
        llamarApi { api.listar(alias) }

    suspend fun resumen(alias: String): Respuesta<ResumenRemoto> =
        llamarApi { api.resumen(alias) }
}
