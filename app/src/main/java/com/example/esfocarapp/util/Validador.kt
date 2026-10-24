package com.example.esfocarapp.util

// TODA la lógica de validación vive aquí, separada de la pantalla.
// Cada función devuelve un mensaje de error, o null si el dato es válido.
object Validador {

    // Tipos de archivo permitidos: PDF y Word (.doc y .docx)
    val tiposDocumento = listOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    fun alias(valor: String): String? {
        if (valor.isBlank()) return "El usuario es obligatorio"
        if (valor.length < 4) return "Debe tener al menos 4 caracteres"
        if (valor.length > 15) return "Máximo 15 caracteres"
        if (!valor.all { it.isLetterOrDigit() || it == '_' }) return "Solo letras, números y _"
        return null
    }

    fun clave(valor: String): String? {
        if (valor.isBlank()) return "La contraseña es obligatoria"
        if (valor.length < 6) return "Debe tener al menos 6 caracteres"
        if (!valor.any { it.isLetter() } || !valor.any { it.isDigit() }) return "Debe incluir letras y números"
        return null
    }

    fun confirmacion(clave: String, confirmacion: String): String? {
        if (confirmacion.isBlank()) return "Repite la contraseña"
        if (clave != confirmacion) return "Las contraseñas no coinciden"
        return null
    }

    fun tituloApunte(valor: String): String? {
        if (valor.isBlank()) return "El título es obligatorio"
        if (valor.trim().length < 3) return "Debe tener al menos 3 caracteres"
        if (valor.length > 40) return "Máximo 40 caracteres"
        return null
    }

    fun contenidoApunte(valor: String): String? {
        if (valor.isBlank()) return "El contenido es obligatorio"
        if (valor.trim().length < 10) return "Escribe al menos 10 caracteres"
        return null
    }

    fun documento(tipo: String, tamano: Long): String? {
        if (tipo !in tiposDocumento) return "Solo se permiten archivos PDF o Word"
        if (tamano > 10L * 1024 * 1024) return "El archivo no puede superar los 10 MB"
        return null
    }
}
