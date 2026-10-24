package com.example.esfocarapp.util

import java.security.MessageDigest

object Seguridad {
    // Convierte la contraseña en un código (hash) irreversible antes de guardarla
    fun hash(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
