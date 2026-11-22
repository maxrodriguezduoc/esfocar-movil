package com.example.esfocarapp

import com.example.esfocarapp.util.Validador
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorTest {

    @Test
    fun alias_vacio_da_error() {
        assertNotNull(Validador.alias(""))
    }

    @Test
    fun alias_valido_no_da_error() {
        assertNull(Validador.alias("cadete_01"))
    }

    @Test
    fun clave_sin_numeros_da_error() {
        assertNotNull(Validador.clave("abcdefg"))
    }

    @Test
    fun clave_valida_no_da_error() {
        assertNull(Validador.clave("abc12345"))
    }

    @Test
    fun confirmacion_distinta_da_error() {
        assertNotNull(Validador.confirmacion("abc123", "abc124"))
    }

    @Test
    fun titulo_apunte_vacio_da_error() {
        assertNotNull(Validador.tituloApunte("  "))
    }

    @Test
    fun documento_pdf_es_valido() {
        assertNull(Validador.documento("application/pdf", 1024L))
    }

    @Test
    fun documento_de_imagen_da_error() {
        assertNotNull(Validador.documento("image/png", 1024L))
    }

    @Test
    fun documento_muy_grande_da_error() {
        assertNotNull(Validador.documento("application/pdf", 20L * 1024 * 1024))
    }
}
