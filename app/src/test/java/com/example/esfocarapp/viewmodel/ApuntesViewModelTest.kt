package com.example.esfocarapp.viewmodel

import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.data.ApuntesRepository
import com.example.esfocarapp.data.local.ApunteLocal
import com.example.esfocarapp.data.local.EstadoSync
import com.example.esfocarapp.data.remote.Respuesta
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ApuntesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<ApuntesRepository>(relaxed = true)
    private var alias: String? = "cadete_01"
    private lateinit var vm: ApuntesViewModel

    private val apunte = ApunteLocal(1, 50, "cadete_01", "Protocolo", "Revisar el protocolo antes", "x", EstadoSync.SINCRONIZADO)

    @Before
    fun preparar() {
        every { repo.apuntes("cadete_01") } returns flowOf(listOf(apunte))
        coEvery { repo.sincronizar(any()) } returns Respuesta.Exito(0)
        vm = ApuntesViewModel(repo) { alias }
    }

    private fun llenarFormulario() {
        vm.cambiarTitulo("Mi apunte")
        vm.cambiarContenido("Contenido de al menos diez caracteres")
    }

    @Test
    fun la_lista_viene_del_repositorio_local() = runTest {
        assertEquals(listOf(apunte), vm.apuntes.first { it.isNotEmpty() })
    }

    @Test
    fun guardar_con_datos_invalidos_no_guarda() {
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
    fun guardar_crea_local_limpia_y_sincroniza() {
        llenarFormulario()
        vm.guardar()

        coVerify { repo.crear("cadete_01", "Mi apunte", "Contenido de al menos diez caracteres") }
        coVerify { repo.sincronizar("cadete_01") }
        assertEquals("Apunte guardado", vm.mensaje)
        assertEquals("", vm.titulo)
        assertEquals("Sincronizado con el servidor", vm.estadoSync)
    }

    @Test
    fun sin_conexion_queda_guardado_en_el_telefono() {
        coEvery { repo.sincronizar(any()) } returns Respuesta.Fallo("Sin conexión con el servidor")
        llenarFormulario()
        vm.guardar()
        assertEquals("Apunte guardado", vm.mensaje)
        assertEquals("Guardado en el teléfono. Se sincronizará al recuperar la conexión.", vm.estadoSync)
    }

    @Test
    fun editar_y_guardar_actualiza() {
        vm.editar(apunte)
        assertEquals(apunte, vm.editando)
        assertEquals("Protocolo", vm.titulo)

        vm.cambiarTitulo("Protocolo editado")
        vm.guardar()

        coVerify { repo.actualizar(apunte, "Protocolo editado", "Revisar el protocolo antes") }
        assertEquals("Apunte actualizado", vm.mensaje)
        assertNull(vm.editando)
    }

    @Test
    fun cancelar_edicion_limpia_el_formulario() {
        vm.editar(apunte)
        vm.cancelarEdicion()
        assertNull(vm.editando)
        assertEquals("", vm.contenido)
    }

    @Test
    fun borrar_elimina_y_si_estaba_en_edicion_limpia() {
        vm.editar(apunte)
        vm.borrar(apunte)
        coVerify { repo.eliminar(apunte) }
        assertNull(vm.editando)
        assertEquals("Apunte eliminado", vm.mensaje)
    }

    @Test
    fun sincronizar_manual() {
        vm.sincronizar()
        coVerify { repo.sincronizar("cadete_01") }
    }
}
