package com.example.esfocarapp.viewmodel

import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.ConceptosRepository
import com.example.esfocarapp.data.ResultadosRepository
import com.example.esfocarapp.data.local.Documento
import com.example.esfocarapp.data.remote.ConceptoRemoto
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResumenRemoto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProgresoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<AppRepository>().also { every { it.intentos } returns flowOf(emptyList()) }
    private val resultados = mockk<ResultadosRepository>()

    @Test
    fun cargar_resumen_exitoso() {
        val resumen = ResumenRemoto("cadete_01", 3, 66.7, 100)
        coEvery { resultados.resumen("cadete_01") } returns Respuesta.Exito(resumen)
        val vm = ProgresoViewModel(repo, resultados) { "cadete_01" }

        vm.cargarResumen()

        assertEquals(resumen, vm.resumen)
        assertNull(vm.errorNube)
        assertFalse(vm.cargandoNube)
    }

    @Test
    fun cargar_resumen_con_error() {
        coEvery { resultados.resumen(any()) } returns Respuesta.Fallo("Sin conexión con el servidor")
        val vm = ProgresoViewModel(repo, resultados) { "cadete_01" }

        vm.cargarResumen()

        assertNull(vm.resumen)
        assertEquals("Sin conexión con el servidor", vm.errorNube)
    }

    @Test
    fun sin_sesion_no_consulta_el_servidor() {
        val vm = ProgresoViewModel(repo, resultados) { null }
        vm.cargarResumen()
        coVerify(exactly = 0) { resultados.resumen(any()) }
    }
}

class DocumentosViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<AppRepository>(relaxed = true).also {
        every { it.documentos } returns flowOf(emptyList())
    }
    private val vm = DocumentosViewModel(repo)

    @Test
    fun agregar_pdf_valido_lo_guarda() {
        val ok = vm.agregar("guia.pdf", "content://x/1", "application/pdf", 2048L)

        assertTrue(ok)
        coVerify { repo.guardarDocumento("guia.pdf", "content://x/1", "application/pdf", 2048L) }
        assertEquals("Documento cargado: guia.pdf", vm.mensaje)
    }

    @Test
    fun agregar_tipo_no_permitido_muestra_error() {
        val ok = vm.agregar("foto.png", "content://x/2", "image/png", 100L)

        assertFalse(ok)
        assertNotNull(vm.mensaje)
        coVerify(exactly = 0) { repo.guardarDocumento(any(), any(), any(), any()) }
    }

    @Test
    fun borrar_elimina_el_documento() {
        val documento = Documento(1, "guia.pdf", "content://x/1", "application/pdf", 10L, "01-01-2026 10:00")
        vm.borrar(documento)

        coVerify { repo.borrarDocumento(documento) }
        assertEquals("Documento eliminado", vm.mensaje)
    }

    @Test
    fun avisar_actualiza_el_mensaje() {
        vm.avisar("hola")
        assertEquals("hola", vm.mensaje)
    }
}

class ConceptosViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<ConceptosRepository>()
    private val vm = ConceptosViewModel(repo)

    @Test
    fun buscar_vacio_muestra_error_y_no_llama_a_la_api() {
        vm.buscar()
        assertNotNull(vm.errorTermino)
        coVerify(exactly = 0) { repo.buscar(any()) }
    }

    @Test
    fun buscar_exitoso_muestra_el_concepto() {
        val concepto = ConceptoRemoto("Primeros auxilios", "Atención inicial", "Los primeros auxilios son...")
        coEvery { repo.buscar("Primeros auxilios") } returns Respuesta.Exito(concepto)

        vm.cambiarTermino("Primeros auxilios")
        vm.buscar()

        assertEquals(concepto, vm.concepto)
        assertNull(vm.mensaje)
        assertFalse(vm.cargando)
    }

    @Test
    fun buscar_con_error_limpia_el_concepto_y_muestra_mensaje() {
        coEvery { repo.buscar(any()) } returns Respuesta.Fallo("No se encontró un artículo")

        vm.cambiarTermino("zzzzzz")
        vm.buscar()

        assertNull(vm.concepto)
        assertEquals("No se encontró un artículo", vm.mensaje)
    }

    @Test
    fun cambiar_termino_valida_en_vivo() {
        vm.cambiarTermino("a")
        assertNotNull(vm.errorTermino)
        vm.cambiarTermino("Anatomía")
        assertNull(vm.errorTermino)
    }
}
