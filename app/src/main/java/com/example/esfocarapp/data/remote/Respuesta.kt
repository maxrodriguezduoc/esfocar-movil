package com.example.esfocarapp.data.remote

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

// Resultado de una llamada de red: o salió bien (Exito) o falló con un mensaje legible (Fallo)
sealed class Respuesta<out T> {
    data class Exito<T>(val datos: T) : Respuesta<T>()
    data class Fallo(val mensaje: String) : Respuesta<Nothing>()
}

// Ejecuta una llamada Retrofit y convierte los errores técnicos en mensajes para el usuario
suspend fun <T> llamarApi(
    noEncontrado: String = "No se encontró el recurso",
    bloque: suspend () -> T
): Respuesta<T> {
    return try {
        Respuesta.Exito(bloque())
    } catch (e: HttpException) {
        Respuesta.Fallo(mensajeHttp(e.code(), e.response()?.errorBody()?.string(), noEncontrado))
    } catch (e: IOException) {
        Respuesta.Fallo("Sin conexión con el servidor")
    } catch (e: JsonParseException) {
        Respuesta.Fallo("El servidor envió una respuesta inesperada")
    }
}

// Elige el mensaje según el código HTTP. Si el servidor explicó el error ({"error": "..."}), se usa ese texto.
internal fun mensajeHttp(codigo: Int, cuerpo: String?, noEncontrado: String): String {
    if (codigo == 404) return noEncontrado
    if (codigo == 400) {
        val detalle = extraerError(cuerpo)
        return detalle ?: "Los datos enviados no son válidos"
    }
    return "Error del servidor ($codigo)"
}

private fun extraerError(cuerpo: String?): String? {
    if (cuerpo.isNullOrBlank()) return null
    return try {
        JsonParser.parseString(cuerpo).asJsonObject.get("error")?.asString
    } catch (e: Exception) {
        null
    }
}
