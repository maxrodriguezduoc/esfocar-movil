package com.example.esfocarapp.viewmodel

import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.data.ApuntesRepository
import com.example.esfocarapp.data.remote.ApunteRemoto
import com.example.esfocarapp.data.remote.Respuesta
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ApuntesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<ApuntesRepository>()
    private var alias: String? = "cadete_01"
    private lateinit var vm: ApuntesViewModel

    private val apunte = ApunteRemoto(1, "cadete_01", "Protocolo", "Revisar el protocolo antes", "01-01-2026 10:00")

    @Before
    fun preparar() {
        vm = ApuntesViewModel(repo) { alias }
        coEvery { repo.listar(any()) } returns Respuesta.Exito(listOf(apunte))
    }

    private fun llenarFormulario() {
        vm.cambiarTitulo("Mi apunte")
        vm.cambiarContenido("Contenido de al menos diez caracteres")
    }

    @Test
    fun cargar_trae_los_apuntes_del_servidor() {
        vm.cargar()
        assertEquals(listOf(apunte), vm.apuntes)
    }

    @Test
    fun cargar_con_error_muestra_mensaje() {
        coEvery { repo.listar(any()) } returns Respuesta.Fallo("Sin conexión con el servidor")
        vm.cargar()
        assertEquals("Sin conexión con el servidor", vm.mensaje)
        assertEquals(emptyList<ApunteRemoto>(), vm.apuntes)
    }

    @Test
    fun cargar_sin_sesion_no_llama_al_servidor() {
        alias = null
        vm.cargar()
        coVerify(exactly = 0) { repo.listar(any()) }
    }

    @Test
    fun guardar_con_datos_invalidos_no_llama_al_servidor() {
        vm.guardar()
        assertNotNull(vm.errorTitulo)
        assertNotNull(vm.errorContenido)
        coVerify(exactly = 0) { repo.crear(any(), any(), any()) }
    }

    @Test
    fun guardar_sin_sesion_muestra_mensaje() {
        alias = null
        llenarFormulario()
        vm.guardar()
        assertEquals("Inicia sesión para guardar apuntes", vm.mensaje)
    }

    @Test
    fun guardar_crea_el_apunte_limpia_el_formulario_y_recarga() {
        coEvery { repo.crear(any(), any(), any()) } returns Respuesta.Exito(apunte)
        llenarFormulario()

        vm.guardar()

        coVerify { repo.crear("cadete_01", "Mi apunte", "Contenido de al menos diez caracteres") }
        assertEquals("Apunte guardado", vm.mensaje)
        assertEquals("", vm.titulo)
        assertEquals("", vm.contenido)
        assertEquals(1, vm.apuntes.size)
    }

    @Test
    fun guardar_con_error_del_servidor_conserva_el_formulario() {
        coEvery { repo.crear(any(), any(), any()) } returns Respuesta.Fallo("Error del servidor (500)")
        llenarFormulario()

        vm.guardar()

        assertEquals("Error del servidor (500)", vm.mensaje)
        assertEquals("Mi apunte", vm.titulo)
    }

    @Test
    fun editar_y_guardar_actualiza_el_apunte() {
        coEvery { repo.actualizar(any(), any(), any(), any()) } returns Respuesta.Exito(apunte)

        vm.editar(apunte)
        assertEquals(1L, vm.editandoId)
        assertEquals("Protocolo", vm.titulo)

        vm.cambiarTitulo("Protocolo editado")
        vm.guardar()

        coVerify { repo.actualizar(1L, "cadete_01", "Protocolo editado", "Revisar el protocolo antes") }
        assertEquals("Apunte actualizado", vm.mensaje)
        assertNull(vm.editandoId)
    }

    @Test
    fun cancelar_edicion_limpia_el_formulario() {
        vm.editar(apunte)
        vm.cancelarEdicion()
        assertNull(vm.editandoId)
        assertEquals("", vm.titulo)
    }

    @Test
    fun borrar_elimina_y_recarga() {
        coEvery { repo.eliminar(1L) } returns Respuesta.Exito(Unit)

        vm.borrar(apunte)

        coVerify { repo.eliminar(1L) }
        assertEquals("Apunte eliminado", vm.mensaje)
    }

    @Test
    fun borrar_el_apunte_en_edicion_limpia_el_formulario() {
        coEvery { repo.eliminar(1L) } returns Respuesta.Exito(Unit)
        vm.editar(apunte)

        vm.borrar(apunte)

        assertNull(vm.editandoId)
    }

    @Test
    fun borrar_con_error_muestra_mensaje() {
        coEvery { repo.eliminar(any()) } returns Respuesta.Fallo("El apunte ya no existe")
        vm.borrar(apunte)
        assertEquals("El apunte ya no existe", vm.mensaje)
    }
}
