package com.example.esfocarapp.model

import com.example.esfocarapp.Datos
import com.example.esfocarapp.Datos.HOY
import com.example.esfocarapp.Datos.ZONA
import com.example.esfocarapp.data.local.RendimientoUnidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalificacionTest {

    @Test
    fun porcentaje_redondea_y_protege_division_por_cero() {
        assertEquals(67, Calificacion.porcentaje(2, 3))
        assertEquals(0, Calificacion.porcentaje(0, 0))
        assertEquals(100, Calificacion.porcentaje(5, 5))
    }

    @Test
    fun nota_escala_chilena_con_60_por_ciento_de_exigencia() {
        assertEquals(1.0, Calificacion.nota(0, 10), 0.0)
        assertEquals(4.0, Calificacion.nota(6, 10), 0.0)    // 60 % = 4.0
        assertEquals(7.0, Calificacion.nota(10, 10), 0.0)
        assertEquals(2.5, Calificacion.nota(3, 10), 0.0)
        assertEquals(5.5, Calificacion.nota(8, 10), 0.0)
        assertEquals(1.0, Calificacion.nota(0, 0), 0.0)
    }

    @Test
    fun aprobado_desde_cuatro() {
        assertTrue(Calificacion.aprobado(4.0))
        assertFalse(Calificacion.aprobado(3.9))
    }

    @Test
    fun nivel_de_actuacion_segun_puntaje() {
        assertEquals(NivelActuacion.OPTIMA, NivelActuacion.de(2))
        assertEquals(NivelActuacion.ACEPTABLE, NivelActuacion.de(1))
        assertEquals(NivelActuacion.INCORRECTA, NivelActuacion.de(0))
    }

    @Test
    fun dificultad_desconocida_es_media() {
        assertEquals(Dificultad.AVANZADA, Dificultad.de("AVANZADA"))
        assertEquals(Dificultad.MEDIA, Dificultad.de("RARA"))
    }
}

class RachaTest {

    @Test
    fun cuenta_dias_seguidos_hasta_hoy() {
        val marcas = listOf(HOY, HOY.minusDays(1), HOY.minusDays(2), HOY.minusDays(4)).map { Datos.ms(it) }
        assertEquals(3, Racha.calcular(marcas, HOY, ZONA))
    }

    @Test
    fun si_hoy_no_estudio_la_racha_cuenta_hasta_ayer() {
        val marcas = listOf(HOY.minusDays(1), HOY.minusDays(2)).map { Datos.ms(it) }
        assertEquals(2, Racha.calcular(marcas, HOY, ZONA))
    }

    @Test
    fun racha_se_corta_si_falta_ayer_y_hoy() {
        assertEquals(0, Racha.calcular(listOf(Datos.ms(HOY.minusDays(3))), HOY, ZONA))
        assertEquals(0, Racha.calcular(emptyList(), HOY, ZONA))
    }

    @Test
    fun varias_actividades_el_mismo_dia_cuentan_una_vez() {
        val marcas = listOf(Datos.ms(HOY, 8), Datos.ms(HOY, 20))
        assertEquals(1, Racha.calcular(marcas, HOY, ZONA))
    }

    @Test
    fun ultima_semana_de_mas_antiguo_a_hoy() {
        val marcas = listOf(Datos.ms(HOY), Datos.ms(HOY), Datos.ms(HOY.minusDays(6)), Datos.ms(HOY.minusDays(10)))
        assertEquals(listOf(1, 0, 0, 0, 0, 0, 2), Racha.ultimaSemana(marcas, HOY, ZONA))
    }
}

class CasoDelDiaTest {

    @Test
    fun es_estable_durante_el_dia_y_cambia_entre_dias() {
        val casos = (1L..5L).map { Datos.caso.copy(id = it) }
        assertEquals(casoDelDia(casos, HOY), casoDelDia(casos, HOY))
        val semana = (0L..4L).map { casoDelDia(casos, HOY.plusDays(it))!!.id }.toSet()
        assertEquals(5, semana.size)
    }

    @Test
    fun sin_casos_devuelve_null() {
        assertNull(casoDelDia(emptyList(), HOY))
    }
}

class ConstructorPanelTest {

    private val asignaturas = listOf(Datos.asignatura, Datos.asignatura2)
    private val unidades = listOf(Datos.unidad, Datos.unidad2)

    @Test
    fun avance_por_asignatura_y_unidad() {
        val rendimiento = listOf(RendimientoUnidad(11, 3, 4))
        val avance = ConstructorPanel.avanceAsignaturas(asignaturas, unidades, rendimiento)

        assertEquals(75, avance[0].porcentaje)
        assertEquals(4, avance[0].respondidas)
        assertEquals(1, avance[0].unidades)
        assertNull(avance[1].porcentaje)

        val unidadesAvance = ConstructorPanel.avanceUnidades(unidades, rendimiento)
        assertEquals(75, unidadesAvance[0].porcentaje)
        assertNull(unidadesAvance[1].porcentaje)
    }

    @Test
    fun panel_completo() {
        val intentos = listOf(
            Datos.intento(1, aciertos = 2, total = 2),
            Datos.intento(2, tipo = "SIMULACRO", aciertos = 6, total = 10, dia = HOY.minusDays(1)),
            Datos.intento(3, tipo = "CASO", aciertos = 2, total = 2, refId = 1151),
            Datos.intento(4, tipo = "CASO", aciertos = 0, total = 2, refId = 1151)
        )
        // Unidad 21 con 1 de 4 (25 %) es tema débil; la 11 con 4 de 5 no
        val rendimiento = listOf(RendimientoUnidad(11, 4, 5), RendimientoUnidad(21, 1, 4))

        val panel = ConstructorPanel.construir(asignaturas, unidades, intentos, rendimiento, 3, 12, HOY, ZONA)

        assertEquals(4, panel.actividades)
        assertEquals(2, panel.racha)
        assertEquals(4.0, panel.mejorNota!!, 0.0)
        assertEquals(1, panel.casosResueltos)                 // el mismo caso dos veces cuenta una
        assertEquals(12, panel.totalCasos)
        assertEquals(3, panel.errores)
        assertEquals(listOf(21L), panel.debiles.map { it.unidad.id })
        assertEquals("Normativa", panel.debiles[0].asignatura)
        assertEquals(65, panel.promedio)                     // (100 + 60 + 100 + 0) / 4
        assertEquals(4, panel.recientes.size)
        assertEquals(2L, panel.recientes.last().id)            // el más antiguo (ayer) queda al final
        assertEquals(3, panel.semana.last())
    }

    @Test
    fun panel_vacio() {
        val panel = ConstructorPanel.construir(asignaturas, unidades, emptyList(), emptyList(), 0, 0, HOY, ZONA)
        assertNull(panel.promedio)
        assertNull(panel.mejorNota)
        assertEquals(0, panel.racha)
        assertTrue(panel.debiles.isEmpty())
    }

    @Test
    fun una_unidad_con_pocas_respuestas_no_se_marca_como_debil() {
        val panel = ConstructorPanel.construir(
            asignaturas, unidades, emptyList(), listOf(RendimientoUnidad(11, 0, 2)), 0, 0, HOY, ZONA
        )
        assertTrue(panel.debiles.isEmpty())
    }
}
