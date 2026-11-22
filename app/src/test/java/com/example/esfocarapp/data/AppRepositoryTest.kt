package com.example.esfocarapp.data

import com.example.esfocarapp.data.local.Documento
import com.example.esfocarapp.data.local.DocumentoDao
import com.example.esfocarapp.data.local.Usuario
import com.example.esfocarapp.data.local.UsuarioDao
import com.example.esfocarapp.util.Seguridad
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRepositoryTest {

    private val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    private val documentoDao = mockk<DocumentoDao>(relaxed = true).also { every { it.todos() } returns flowOf(emptyList()) }
    private val repo = AppRepository(usuarioDao, documentoDao) { "19-11-2026 10:00" }

    @Test
    fun registrar_usuario_nuevo_guarda_la_clave_con_hash() = runBlocking {
        coEvery { usuarioDao.buscarPorAlias("cadete_01") } returns null

        assertTrue(repo.registrarUsuario("cadete_01", "abc12345"))

        coVerify { usuarioDao.insertar(Usuario(alias = "cadete_01", claveHash = Seguridad.hash("abc12345"))) }
    }

    @Test
    fun registrar_usuario_existente_devuelve_false() = runBlocking {
        coEvery { usuarioDao.buscarPorAlias("cadete_01") } returns Usuario(1, "cadete_01", "hash")

        assertFalse(repo.registrarUsuario("cadete_01", "abc12345"))
        coVerify(exactly = 0) { usuarioDao.insertar(any()) }
    }

    @Test
    fun iniciar_sesion_con_clave_correcta() = runBlocking {
        coEvery { usuarioDao.buscarPorAlias("cadete_01") } returns
            Usuario(1, "cadete_01", Seguridad.hash("abc12345"))

        assertTrue(repo.iniciarSesion("cadete_01", "abc12345"))
    }

    @Test
    fun iniciar_sesion_con_clave_incorrecta() = runBlocking {
        coEvery { usuarioDao.buscarPorAlias("cadete_01") } returns
            Usuario(1, "cadete_01", Seguridad.hash("abc12345"))

        assertFalse(repo.iniciarSesion("cadete_01", "otra123"))
    }

    @Test
    fun iniciar_sesion_de_usuario_inexistente() = runBlocking {
        coEvery { usuarioDao.buscarPorAlias(any()) } returns null
        assertFalse(repo.iniciarSesion("nadie", "abc12345"))
    }

    @Test
    fun guardar_y_borrar_documento() = runBlocking {
        repo.guardarDocumento("guia.pdf", "content://x", "application/pdf", 10L)
        coVerify {
            documentoDao.insertar(
                Documento(nombre = "guia.pdf", uri = "content://x", tipo = "application/pdf", tamano = 10L, fecha = "19-11-2026 10:00")
            )
        }

        val documento = Documento(1, "guia.pdf", "content://x", "application/pdf", 10L, "19-11-2026 10:00")
        repo.borrarDocumento(documento)
        coVerify { documentoDao.eliminar(documento) }
    }

    @Test
    fun el_flujo_de_documentos_viene_del_dao() = runBlocking {
        val documento = Documento(1, "guia.pdf", "content://x", "application/pdf", 10L, "x")
        every { documentoDao.todos() } returns flowOf(listOf(documento))
        val repo2 = AppRepository(usuarioDao, documentoDao)
        var recibido: List<Documento> = emptyList()
        repo2.documentos.collect { recibido = it }
        assertEquals(listOf(documento), recibido)
    }

    @Test
    fun fecha_por_defecto_tiene_formato_esperado() {
        assertTrue(AppRepository.fechaPorDefecto().matches(Regex("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}")))
    }
}
