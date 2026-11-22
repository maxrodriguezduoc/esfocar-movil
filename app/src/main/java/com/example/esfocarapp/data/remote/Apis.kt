package com.example.esfocarapp.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Cada interfaz describe los endpoints; Retrofit genera el código que hace la llamada HTTP.

// Microservicio propio (Spring Boot) - puerto 8081
interface ApuntesApi {
    @GET("api/apuntes")
    suspend fun listar(@Query("alias") alias: String): List<ApunteRemoto>

    @POST("api/apuntes")
    suspend fun crear(@Body datos: ApunteSolicitud): ApunteRemoto

    @PUT("api/apuntes/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: ApunteSolicitud): ApunteRemoto

    @DELETE("api/apuntes/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}

// Microservicio propio (Spring Boot) - puerto 8082
interface ResultadosApi {
    @POST("api/resultados")
    suspend fun crear(@Body datos: ResultadoSolicitud): ResultadoRemoto

    @GET("api/resultados")
    suspend fun listar(@Query("alias") alias: String): List<ResultadoRemoto>

    @GET("api/resultados/resumen")
    suspend fun resumen(@Query("alias") alias: String): ResumenRemoto
}

// API externa pública: resumen de un artículo de Wikipedia en español
interface WikipediaApi {
    @GET("page/summary/{titulo}")
    suspend fun resumen(@Path("titulo") titulo: String): ConceptoRemoto
}
