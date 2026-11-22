package com.example.esfocarapp.data

import com.example.esfocarapp.Datos
import com.example.esfocarapp.PreferenciasFalsas
import com.example.esfocarapp.data.local.ContenidoDao
import com.example.esfocarapp.data.local.ContenidoEntidades
import com.example.esfocarapp.data.remote.ContenidosApi
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.VersionDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

// El contenido incluido en la app se valida igual que el del servidor
class ContenidoIncluidoTest {

    private val paquete = MapeoContenido.leerPaquete(Datos.contenidoIncluido())
    private val entidades = MapeoContenido.aEntidades(paquete)

    @Test
    fun el_banco_tiene_el_contenido_esperado() {
        assertEquals(4, paquete.asignaturas.size)
        assertEquals(9, entidades.unidades.size)
        assertEquals(18, entidades.fichas.size)
        assertEquals(45, entidades.preguntas.size)
        assertEquals(12, entidades.casos.size)
    }

    @Test
    fun cada_pregunta_tiene_exactamente_una_correcta() {
        entidades.preguntas.map(MapeoContenido::pregunta).forEach { p ->
            assertEquals("Pregunta ${p.id}", 1, p.alternativas.count { it.esCorrecta })
            assertTrue(p.alternativas.all { it.retroalimentacion.isNotBlank() })
        }
    }

    @Test
    fun cada_caso_tiene_una_actuacion_optima() {
        entidades.casos.map(MapeoContenido::caso).forEach { c ->
            assertTrue("Caso ${c.id}", c.opciones.any { it.puntaje == 2 })
            assertTrue(c.fundamento.isNotBlank())
        }
    }

    @Test
    fun los_identificadores_no_se_repiten() {
        assertEquals(entidades.preguntas.size, entidades.preguntas.map { it.id }.toSet().size)
        assertEquals(entidades.casos.size, entidades.casos.map { it.id }.toSet().size)
        assertEquals(entidades.fichas.size, entidades.fichas.map { it.id }.toSet().size)
    }

    @Test
    fun el_mapeo_conserva_relaciones_y_orden() {
        assertEquals(listOf(0, 1, 2, 3), entidades.asignaturas.map { it.orden })
        val ficha = MapeoContenido.ficha(entidades.fichas.first())
        assertEquals(11L, ficha.unidadId)
        assertEquals(4, ficha.puntosClave.size)
        val unidad = MapeoContenido.unidad(entidades.unidades.first())
        assertEquals(1L, unidad.asignaturaId)
        val asignatura = MapeoContenido.asignatura(entidades.asignaturas.first())
        assertEquals("procedimientos", asignatura.icono)
    }
}

class ContenidoRepositoryTest {

    private val dao = mockk<ContenidoDao>(relaxed = true)
    private val api = mockk<ContenidosApi>()
    private val prefs = PreferenciasFalsas()
    private val paquete = MapeoContenido.leerPaquete(Datos.contenidoIncluido())
    private val entidades = MapeoContenido.aEntidades(paquete)
    private val repo = ContenidoRepository(dao, api, prefs) { Datos.contenidoIncluido() }

    @Test
    fun primer_uso_carga_el_contenido_incluido() = runBlocking {
        coEvery { dao.contarAsignaturas() } returns 0
        val guardado = slot<ContenidoEntidades>()
        coEvery { dao.reemplazarTodo(capture(guardado)) } returns Unit

        repo.asegurarContenidoInicial()

        assertEquals(45, guardado.captured.preguntas.size)
        assertEquals(1, prefs.versionContenido)
    }

    @Test
    fun si_ya_hay_contenido_no_se_vuelve_a_cargar() = runBlocking {
        coEvery { dao.contarAsignaturas() } returns 4
        repo.asegurarContenidoInicial()
        coVerify(exactly = 0) { dao.reemplazarTodo(any()) }
    }

    @Test
    fun sincronizar_descarga_solo_si_hay_version_nueva() = runBlocking {
        prefs.versionContenido = 1
        coEvery { api.version() } returns VersionDto(1)
        assertEquals(Respuesta.Exito(false), repo.sincronizar())
        coVerify(exactly = 0) { api.paquete() }

        coEvery { api.version() } returns VersionDto(2)
        coEvery { api.paquete() } returns paquete.copy(version = 2)
        assertEquals(Respuesta.Exito(true), repo.sincronizar())
        coVerify { dao.reemplazarTodo(any()) }
        assertEquals(2, prefs.versionContenido)
    }

    @Test
    fun sin_conexion_se_mantiene_el_contenido_local() = runBlocking {
        coEvery { api.version() } throws IOException()
        assertEquals(Respuesta.Fallo("Sin conexión con el servidor"), repo.sincronizar())
        coVerify(exactly = 0) { dao.reemplazarTodo(any()) }
    }

    @Test
    fun falla_al_descargar_el_paquete() = runBlocking {
        coEvery { api.version() } returns VersionDto(5)
        coEvery { api.paquete() } throws IOException()
        assertTrue(repo.sincronizar() is Respuesta.Fallo)
        assertEquals(0, prefs.versionContenido)
    }

    @Test
    fun lecturas_convierten_entidades_a_modelo() = runBlocking {
        every { dao.asignaturas() } returns flowOf(entidades.asignaturas)
        every { dao.unidades() } returns flowOf(entidades.unidades)
        every { dao.casos() } returns flowOf(entidades.casos)
        every { dao.fichas(11) } returns flowOf(entidades.fichas.take(2))
        every { dao.fichasPorIds(listOf(111)) } returns flowOf(entidades.fichas.take(1))
        every { dao.buscarFichas("perímetro") } returns flowOf(entidades.fichas.takeLast(1))
        coEvery { dao.ficha(111) } returns entidades.fichas.first()
        coEvery { dao.ficha(999) } returns null
        coEvery { dao.unidad(11) } returns entidades.unidades.first()
        coEvery { dao.caso(1151) } returns entidades.casos.first()
        coEvery { dao.preguntasDeUnidad(11) } returns entidades.preguntas.take(5)
        coEvery { dao.preguntasDeAsignatura(1) } returns entidades.preguntas.take(15)
        coEvery { dao.todasLasPreguntas() } returns entidades.preguntas
        coEvery { dao.preguntasPorIds(listOf(1101L)) } returns entidades.preguntas.take(1)

        val r = ContenidoRepository(dao, api, prefs) { "" }
        assertEquals(4, r.asignaturas.first().size)
        assertEquals(9, r.unidades.first().size)
        assertEquals(12, r.casos.first().size)
        assertEquals(2, r.fichas(11).first().size)
        assertEquals(1, r.fichasPorIds(listOf(111)).first().size)
        assertEquals(1, r.buscar("perímetro").first().size)
        assertEquals("Secuencia general de intervención", r.ficha(111)?.titulo)
        assertNull(r.ficha(999))
        assertEquals(1, r.unidad(11)?.numero)
        assertEquals(3, r.caso(1151)?.opciones?.size)
        assertEquals(5, r.preguntasDeUnidad(11).size)
        assertEquals(15, r.preguntasDeAsignatura(1).size)
        assertEquals(45, r.todasLasPreguntas().size)
        assertEquals(1, r.preguntasPorIds(listOf(1101L)).size)
        assertTrue(r.preguntasPorIds(emptyList()).isEmpty())
    }
}
