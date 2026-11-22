package com.example.esfocarapp.data

import com.example.esfocarapp.data.remote.ApuntesApi
import com.example.esfocarapp.data.remote.ApunteRemoto
import com.example.esfocarapp.data.remote.ApunteSolicitud
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.llamarApi

// Apuntes: viven en el microservicio ms-apuntes (CRUD completo)
class ApuntesRepository(private val api: ApuntesApi) {

    suspend fun listar(alias: String): Respuesta<List<ApunteRemoto>> =
        llamarApi { api.listar(alias) }

    suspend fun crear(alias: String, titulo: String, contenido: String): Respuesta<ApunteRemoto> =
        llamarApi { api.crear(ApunteSolicitud(alias, titulo, contenido)) }

    suspend fun actualizar(id: Long, alias: String, titulo: String, contenido: String): Respuesta<ApunteRemoto> =
        llamarApi("El apunte ya no existe") { api.actualizar(id, ApunteSolicitud(alias, titulo, contenido)) }

    suspend fun eliminar(id: Long): Respuesta<Unit> =
        llamarApi("El apunte ya no existe") { api.eliminar(id) }
}
