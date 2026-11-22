package com.example.esfocarapp.util

import com.example.esfocarapp.data.FakeData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeguridadTest {
    @Test
    fun hash_es_determinista_y_no_deja_la_clave_en_texto_plano() {
        val h = Seguridad.hash("abc12345")
        assertEquals(h, Seguridad.hash("abc12345"))
        assertNotEquals("abc12345", h)
        assertEquals(64, h.length)
    }

    @Test
    fun claves_distintas_generan_hash_distinto() {
        assertNotEquals(Seguridad.hash("abc12345"), Seguridad.hash("abc12346"))
    }
}

class DocumentosUtilTest {
    @Test
    fun etiqueta_de_tipo() {
        assertEquals("PDF", Documentos.etiquetaTipo("application/pdf"))
        assertEquals("Word", Documentos.etiquetaTipo("application/msword"))
    }

    @Test
    fun formatea_tamanos() {
        assertTrue(Documentos.formatearTamano(2048).endsWith("KB"))
        assertTrue(Documentos.formatearTamano(3L * 1024 * 1024).endsWith("MB"))
    }
}

class SesionTest {
    @Test
    fun la_sesion_guarda_y_borra_el_alias() {
        Sesion.alias = "cadete_01"
        assertEquals("cadete_01", Sesion.alias)
        Sesion.alias = null
        assertNull(Sesion.alias)
    }
}

class FakeDataTest {
    @Test
    fun cada_pregunta_tiene_exactamente_una_respuesta_correcta() {
        FakeData.preguntas.forEach { pregunta ->
            assertEquals(1, pregunta.alternativas.count { it.esCorrecta })
        }
    }

    @Test
    fun hay_asignaturas_con_datos() {
        assertTrue(FakeData.asignaturas.isNotEmpty())
        assertNotNull(FakeData.asignaturas.first().nombre)
    }
}

class ValidadorExtraTest {
    @Test
    fun alias_limites_y_caracteres() {
        assertNotNull(Validador.alias("abc"))
        assertNotNull(Validador.alias("a".repeat(16)))
        assertNotNull(Validador.alias("hola mundo"))
        assertNull(Validador.alias("abcd"))
    }

    @Test
    fun clave_reglas() {
        assertNotNull(Validador.clave(""))
        assertNotNull(Validador.clave("ab1"))
        assertNotNull(Validador.clave("12345678"))
        assertNull(Validador.clave("abc123"))
    }

    @Test
    fun confirmacion_reglas() {
        assertNotNull(Validador.confirmacion("abc123", ""))
        assertNull(Validador.confirmacion("abc123", "abc123"))
    }

    @Test
    fun apunte_reglas() {
        assertNotNull(Validador.tituloApunte("ab"))
        assertNotNull(Validador.tituloApunte("a".repeat(41)))
        assertNull(Validador.tituloApunte("Protocolo"))
        assertNotNull(Validador.contenidoApunte(""))
        assertNotNull(Validador.contenidoApunte("corto"))
        assertNull(Validador.contenidoApunte("contenido suficientemente largo"))
    }

    @Test
    fun termino_reglas() {
        assertNotNull(Validador.termino(""))
        assertNotNull(Validador.termino("a"))
        assertNotNull(Validador.termino("a".repeat(61)))
        assertNull(Validador.termino("Anatomía"))
    }

    @Test
    fun documento_word_es_valido() {
        assertNull(Validador.documento("application/msword", 100))
        assertNull(
            Validador.documento("application/vnd.openxmlformats-officedocument.wordprocessingml.document", 100)
        )
    }
}
