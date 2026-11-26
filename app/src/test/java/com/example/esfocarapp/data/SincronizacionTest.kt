package com.example.esfocarapp.data

import com.example.esfocarapp.Datos
import com.example.esfocarapp.data.local.ApunteDao
import com.example.esfocarapp.data.local.ApunteLocal
import com.example.esfocarapp.data.local.EstadoSync
import com.example.esfocarapp.data.local.Favorito
import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.data.local.ProgresoDao
import com.example.esfocarapp.data.local.RendimientoUnidad
import com.example.esfocarapp.data.local.RespuestaEntity
import com.example.esfocarapp.data.remote.ApunteRemoto
import com.example.esfocarapp.data.remote.ApunteSolicitud
import com.example.esfocarapp.data.remote.ApuntesApi
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResultadoRemoto
import com.example.esfocarapp.data.remote.ResumenRemoto
import com.example.esfocarapp.model.TipoActividad
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

private fun http(codigo: Int) =
    HttpException(Response.error<Any>(codigo, "{}".toResponseBody("application/json".toMediaType())))

// Apuntes offline-first: cambios locales inmediatos + sincronización con ms-apuntes
class ApuntesRepositoryTest {

    private val dao = mockk<ApunteDao>(relaxed = true)
    private val api = mockk<ApuntesApi>()
    private var cambios = 0
    private val repo = ApuntesRepository(dao, api, { "22-11-2026 10:00" }) { cambios++ }

    private fun local(id: Long, idRemoto: Long?, estado: String, titulo: String = "Título") =
        ApunteLocal(id, idRemoto, "cadete_01", titulo, "Contenido largo", "x", estado)

    private fun remoto(id: Long, titulo: String = "Título") =
        ApunteRemoto(id, "cadete_01", titulo, "Contenido largo", "22-11-2026 10:05")

    @Test
    fun crear_guarda_local_como_pendiente_y_avisa() = runBlocking {
        val guardado = slot<ApunteLocal>()
        coEvery { dao.insertar(capture(guardado)) } returns 1L

        repo.crear("cadete_01", "Título", "Contenido largo")

        assertEquals(EstadoSync.CREAR, guardado.captured.estado)
        assertEquals("22-11-2026 10:00", guardado.captured.fecha)
        assertEquals(1, cambios)
    }

    @Test
    fun actualizar_marca_el_estado_correcto() = runBlocking {
        repo.actualizar(local(1, null, EstadoSync.CREAR), "Nuevo", "Contenido nuevo")
        coVerify { dao.actualizar(match { it.estado == EstadoSync.CREAR && it.titulo == "Nuevo" }) }

        repo.actualizar(local(2, 50, EstadoSync.SINCRONIZADO), "Nuevo", "Contenido nuevo")
        coVerify { dao.actualizar(match { it.id == 2L && it.estado == EstadoSync.ACTUALIZAR }) }
    }

    @Test
    fun eliminar_borra_directo_si_nunca_se_subio() = runBlocking {
        val nuevo = local(1, null, EstadoSync.CREAR)
        repo.eliminar(nuevo)
        coVerify { dao.eliminar(nuevo) }

        repo.eliminar(local(2, 50, EstadoSync.SINCRONIZADO))
        coVerify { dao.actualizar(match { it.id == 2L && it.estado == EstadoSync.ELIMINAR }) }
    }

    @Test
    fun subir_pendientes_crea_actualiza_y_elimina_en_el_servidor() = runBlocking {
        coEvery { dao.pendientes() } returns listOf(
            local(1, null, EstadoSync.CREAR),
            local(2, 50, EstadoSync.ACTUALIZAR),
            local(3, 60, EstadoSync.ELIMINAR)
        )
        coEvery { api.crear(any()) } returns remoto(70)
        coEvery { api.actualizar(50, any()) } returns remoto(50)
        coEvery { api.eliminar(60) } returns Unit

        assertEquals(Respuesta.Exito(3), repo.subirPendientes())

        coVerify { dao.actualizar(match { it.id == 1L && it.idRemoto == 70L && it.estado == EstadoSync.SINCRONIZADO }) }
        coVerify { dao.actualizar(match { it.id == 2L && it.estado == EstadoSync.SINCRONIZADO }) }
        coVerify { dao.eliminar(match { it.id == 3L }) }
    }

    @Test
    fun si_lo_borraron_en_el_servidor_la_edicion_se_vuelve_a_crear() = runBlocking {
        coEvery { dao.pendientes() } returns listOf(local(2, 50, EstadoSync.ACTUALIZAR))
        coEvery { api.actualizar(50, any()) } throws http(404)
        coEvery { api.crear(ApunteSolicitud("cadete_01", "Título", "Contenido largo")) } returns remoto(99)

        assertEquals(Respuesta.Exito(1), repo.subirPendientes())
        coVerify { dao.actualizar(match { it.idRemoto == 99L && it.estado == EstadoSync.SINCRONIZADO }) }
    }

    @Test
    fun eliminar_algo_que_ya_no_existe_en_el_servidor_igual_se_limpia() = runBlocking {
        coEvery { dao.pendientes() } returns listOf(local(3, 60, EstadoSync.ELIMINAR))
        coEvery { api.eliminar(60) } throws http(404)

        assertEquals(Respuesta.Exito(1), repo.subirPendientes())
        coVerify { dao.eliminar(match { it.id == 3L }) }
    }

    @Test
    fun sin_conexion_se_detiene_y_conserva_los_pendientes() = runBlocking {
        coEvery { dao.pendientes() } returns listOf(local(1, null, EstadoSync.CREAR), local(2, 50, EstadoSync.ACTUALIZAR))
        coEvery { api.crear(any()) } throws IOException()

        assertEquals(Respuesta.Fallo("Sin conexión con el servidor"), repo.sincronizar("cadete_01"))
        coVerify(exactly = 0) { api.actualizar(any(), any()) }
        coVerify(exactly = 0) { dao.actualizar(any()) }
    }

    @Test
    fun error_del_servidor_al_actualizar_o_eliminar_detiene_la_subida() = runBlocking {
        coEvery { dao.pendientes() } returns listOf(local(2, 50, EstadoSync.ACTUALIZAR))
        coEvery { api.actualizar(50, any()) } throws http(500)
        assertEquals(Respuesta.Fallo("Error del servidor (500)", 500), repo.subirPendientes())

        coEvery { dao.pendientes() } returns listOf(local(3, 60, EstadoSync.ELIMINAR))
        coEvery { api.eliminar(60) } throws http(500)
        assertTrue(repo.subirPendientes() is Respuesta.Fallo)
        coVerify(exactly = 0) { dao.eliminar(any()) }
    }

    @Test
    fun sincronizar_trae_nuevos_actualiza_y_quita_borrados_remotamente() = runBlocking {
        coEvery { dao.pendientes() } returns emptyList()
        coEvery { dao.todosDe("cadete_01") } returns listOf(
            local(1, 50, EstadoSync.SINCRONIZADO, "Viejo"),       // cambió en el servidor
            local(2, 60, EstadoSync.SINCRONIZADO),                 // ya no existe en el servidor
            local(3, null, EstadoSync.CREAR),                      // pendiente local: no se toca
            local(4, 80, EstadoSync.SINCRONIZADO)                  // igual en ambos lados
        )
        coEvery { api.listar("cadete_01") } returns listOf(remoto(50, "Nuevo"), remoto(70), remoto(80))

        assertEquals(Respuesta.Exito(0), repo.sincronizar("cadete_01"))

        coVerify { dao.actualizar(match { it.id == 1L && it.titulo == "Nuevo" }) }
        coVerify { dao.insertar(match { it.idRemoto == 70L && it.estado == EstadoSync.SINCRONIZADO }) }
        coVerify { dao.eliminar(match { it.id == 2L }) }
        coVerify(exactly = 0) { dao.eliminar(match { it.id == 3L || it.id == 4L }) }
        coVerify(exactly = 1) { dao.actualizar(any()) }
    }

    @Test
    fun sincronizar_sin_sesion_solo_sube() = runBlocking {
        coEvery { dao.pendientes() } returns emptyList()
        assertEquals(Respuesta.Exito(0), repo.sincronizar(null))
        coVerify(exactly = 0) { api.listar(any()) }
    }

    @Test
    fun sincronizar_con_falla_al_listar() = runBlocking {
        coEvery { dao.pendientes() } returns emptyList()
        coEvery { api.listar(any()) } throws IOException()
        assertTrue(repo.sincronizar("cadete_01") is Respuesta.Fallo)
    }

    @Test
    fun el_flujo_viene_del_dao() = runBlocking {
        every { dao.apuntes("cadete_01") } returns flowOf(listOf(local(1, null, EstadoSync.CREAR)))
        val lista = repo.apuntes("cadete_01").first()
        assertTrue(lista.first().pendiente)
        assertFalse(local(1, 1, EstadoSync.SINCRONIZADO).pendiente)
    }
}

class ProgresoRepositoryTest {

    private val dao = mockk<ProgresoDao>(relaxed = true)
    private val resultados = mockk<ResultadosRepository>()
    private var cambios = 0
    private val repo = ProgresoRepository(dao, resultados, { 1_000L }) { cambios++ }

    @Test
    fun registrar_actividad_guarda_local_y_programa_sincronizacion() = runBlocking {
        val guardado = slot<Intento>()
        coEvery { dao.insertarIntento(capture(guardado)) } returns 7L

        val intento = repo.registrarActividad("cadete_01", TipoActividad.SIMULACRO, "S".repeat(80), 8, 10, 1, null)

        assertEquals(7L, intento.id)
        assertEquals("SIMULACRO", guardado.captured.tipo)
        assertEquals(60, guardado.captured.titulo.length)     // se recorta para el microservicio
        assertEquals(1_000L, guardado.captured.timestamp)
        assertFalse(guardado.captured.sincronizado)
        assertEquals(1, cambios)
    }

    @Test
    fun registrar_respuesta_guarda_pregunta_y_unidad() = runBlocking {
        repo.registrarRespuesta("cadete_01", Datos.pregunta(1101), false)
        coVerify { dao.insertarRespuesta(RespuestaEntity(0, "cadete_01", 1101, 11, false, 1_000L)) }
    }

    @Test
    fun favoritos_agregar_y_quitar() = runBlocking {
        repo.cambiarFavorito("cadete_01", 111, true)
        coVerify { dao.agregarFavorito(Favorito("cadete_01", 111)) }
        repo.cambiarFavorito("cadete_01", 111, false)
        coVerify { dao.quitarFavorito("cadete_01", 111) }

        every { dao.favoritos("cadete_01") } returns flowOf(listOf(111L, 112L))
        assertEquals(setOf(111L, 112L), repo.favoritos("cadete_01").first())
    }

    @Test
    fun subir_pendientes_marca_cada_resultado() = runBlocking {
        coEvery { dao.intentosPendientes() } returns listOf(Datos.intento(1), Datos.intento(2))
        coEvery { resultados.guardar(any(), any(), any(), any()) } returns
            Respuesta.Exito(ResultadoRemoto(1, "cadete_01", "x", 1, 2, "f"))

        assertEquals(Respuesta.Exito(2), repo.subirPendientes())
        coVerify { dao.marcarSincronizado(1) }
        coVerify { dao.marcarSincronizado(2) }
    }

    @Test
    fun subir_pendientes_sin_conexion_no_marca_nada() = runBlocking {
        coEvery { dao.intentosPendientes() } returns listOf(Datos.intento(1))
        coEvery { resultados.guardar(any(), any(), any(), any()) } returns Respuesta.Fallo("Sin conexión")

        assertEquals(Respuesta.Fallo("Sin conexión"), repo.subirPendientes())
        coVerify(exactly = 0) { dao.marcarSincronizado(any()) }
    }

    @Test
    fun lecturas_delegan_en_el_dao_y_el_servidor() = runBlocking {
        every { dao.intentos("a") } returns flowOf(listOf(Datos.intento(1)))
        every { dao.rendimientoPorUnidad("a") } returns flowOf(listOf(RendimientoUnidad(11, 1, 2)))
        every { dao.errores("a") } returns flowOf(listOf(1101L))
        coEvery { dao.erroresActuales("a") } returns listOf(1101L)
        coEvery { resultados.resumen("a") } returns Respuesta.Exito(ResumenRemoto("a", 1, 50.0, 50))

        assertEquals(1, repo.intentos("a").first().size)
        assertEquals(1, repo.rendimiento("a").first().size)
        assertEquals(listOf(1101L), repo.errores("a").first())
        assertEquals(listOf(1101L), repo.erroresActuales("a"))
        assertTrue(repo.resumenRemoto("a") is Respuesta.Exito)
        assertTrue(ProgresoRepository.formatearFecha(0).isNotBlank())
    }
}

class SincronizadorTest {

    private val contenido = mockk<ContenidoRepository>()
    private val apuntes = mockk<ApuntesRepository>()
    private val progreso = mockk<ProgresoRepository>()
    private val sincronizador = Sincronizador(contenido, apuntes, progreso) { "cadete_01" }

    @Test
    fun todo_al_dia() = runBlocking {
        coEvery { contenido.sincronizar() } returns Respuesta.Exito(false)
        coEvery { apuntes.sincronizar("cadete_01") } returns Respuesta.Exito(0)
        coEvery { progreso.subirPendientes() } returns Respuesta.Exito(0)

        val r = sincronizador.ejecutar()
        assertTrue(r.exito)
        assertEquals("Todo está sincronizado", r.mensaje)
    }

    @Test
    fun contenido_nuevo() = runBlocking {
        coEvery { contenido.sincronizar() } returns Respuesta.Exito(true)
        coEvery { apuntes.sincronizar(any()) } returns Respuesta.Exito(2)
        coEvery { progreso.subirPendientes() } returns Respuesta.Exito(1)

        val r = sincronizador.ejecutar()
        assertTrue(r.contenidoActualizado)
        assertEquals("Contenido actualizado desde el servidor", r.mensaje)
    }

    @Test
    fun una_falla_se_informa_pero_lo_demas_igual_se_intenta() = runBlocking {
        coEvery { contenido.sincronizar() } returns Respuesta.Exito(true)
        coEvery { apuntes.sincronizar(any()) } returns Respuesta.Fallo("Sin conexión con el servidor")
        coEvery { progreso.subirPendientes() } returns Respuesta.Exito(0)

        val r = sincronizador.ejecutar()
        assertFalse(r.exito)
        assertTrue(r.contenidoActualizado)
        assertEquals("Sin conexión con el servidor", r.mensaje)
        coVerify { progreso.subirPendientes() }
    }
}
