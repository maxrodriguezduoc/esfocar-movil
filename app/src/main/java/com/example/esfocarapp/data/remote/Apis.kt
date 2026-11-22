package com.example.esfocarapp.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Cada interfaz describe los endpoints; Retrofit genera el código que hace la llamada HTTP.

// Microservicio propio (Spring Boot) - puerto 8083: contenido académico para estudiar sin conexión
interface ContenidosApi {
    @GET("api/contenidos/version")
    suspend fun version(): VersionDto

    @GET("api/contenidos/paquete")
    suspend fun paquete(): PaqueteDto

    // ----- Gestión docente: publicar, editar y eliminar preguntas y casos -----
    @POST("api/unidades/{unidadId}/preguntas")
    suspend fun crearPregunta(@Path("unidadId") unidadId: Long, @Body datos: PreguntaSolicitud): PreguntaDto

    @PUT("api/preguntas/{id}")
    suspend fun actualizarPregunta(@Path("id") id: Long, @Body datos: PreguntaSolicitud): PreguntaDto

    @DELETE("api/preguntas/{id}")
    suspend fun eliminarPregunta(@Path("id") id: Long)

    @POST("api/unidades/{unidadId}/casos")
    suspend fun crearCaso(@Path("unidadId") unidadId: Long, @Body datos: CasoSolicitud): CasoDto

    @PUT("api/casos/{id}")
    suspend fun actualizarCaso(@Path("id") id: Long, @Body datos: CasoSolicitud): CasoDto

    @DELETE("api/casos/{id}")
    suspend fun eliminarCaso(@Path("id") id: Long)
}

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
