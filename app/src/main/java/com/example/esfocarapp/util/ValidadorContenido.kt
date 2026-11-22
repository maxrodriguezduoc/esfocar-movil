package com.example.esfocarapp.util

import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.OpcionCaso

// Reglas de los formularios docentes. Son las mismas que valida el microservicio ms-contenidos,
// así el docente ve el error antes de enviar y sin necesitar conexión.
object ValidadorContenido {

    const val MIN_OPCIONES = 2
    const val MAX_OPCIONES = 5

    fun enunciado(valor: String): String? = when {
        valor.isBlank() -> "El enunciado es obligatorio"
        valor.trim().length < 10 -> "Escribe al menos 10 caracteres"
        valor.length > 500 -> "Máximo 500 caracteres"
        else -> null
    }

    fun explicacion(valor: String): String? =
        if (valor.length > 1000) "Máximo 1000 caracteres" else null

    fun alternativas(lista: List<Alternativa>): String? = when {
        lista.size < MIN_OPCIONES -> "Agrega al menos $MIN_OPCIONES alternativas"
        lista.size > MAX_OPCIONES -> "Máximo $MAX_OPCIONES alternativas"
        lista.any { it.texto.isBlank() } -> "Todas las alternativas necesitan texto"
        lista.any { it.retroalimentacion.isBlank() } -> "Todas las alternativas necesitan retroalimentación"
        lista.count { it.esCorrecta } != 1 -> "Marca exactamente una alternativa correcta"
        else -> null
    }

    fun tituloCaso(valor: String): String? = when {
        valor.isBlank() -> "El título es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        valor.length > 120 -> "Máximo 120 caracteres"
        else -> null
    }

    fun situacion(valor: String): String? = when {
        valor.isBlank() -> "Describe la situación"
        valor.trim().length < 20 -> "Describe la situación con al menos 20 caracteres"
        valor.length > 2000 -> "Máximo 2000 caracteres"
        else -> null
    }

    fun preguntaCaso(valor: String): String? = when {
        valor.isBlank() -> "La pregunta es obligatoria"
        valor.length > 500 -> "Máximo 500 caracteres"
        else -> null
    }

    fun fundamento(valor: String): String? = when {
        valor.isBlank() -> "Explica el fundamento de la actuación correcta"
        valor.length > 1500 -> "Máximo 1500 caracteres"
        else -> null
    }

    fun opciones(lista: List<OpcionCaso>): String? = when {
        lista.size < MIN_OPCIONES -> "Agrega al menos $MIN_OPCIONES opciones"
        lista.size > MAX_OPCIONES -> "Máximo $MAX_OPCIONES opciones"
        lista.any { it.texto.isBlank() } -> "Todas las opciones necesitan texto"
        lista.any { it.retroalimentacion.isBlank() } -> "Todas las opciones necesitan retroalimentación"
        lista.any { it.puntaje !in 0..2 } -> "El puntaje debe ser 0, 1 o 2"
        lista.none { it.puntaje == 2 } -> "Marca al menos una actuación como óptima"
        else -> null
    }
}
