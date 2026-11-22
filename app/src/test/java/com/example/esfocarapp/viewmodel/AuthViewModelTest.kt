package com.example.esfocarapp.viewmodel

import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.PreferenciasFalsas
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.util.Sesion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<AppRepository>(relaxed = true)
    private lateinit var vm: AuthViewModel
    private lateinit var prefs: PreferenciasFalsas

    @Before
    fun preparar() {
        Sesion.alias = null
        prefs = PreferenciasFalsas()
        vm = AuthViewModel(repo, prefs)
    }

    @After
    fun limpiar() {
        Sesion.alias = null
    }

    @Test
    fun cambiar_campos_valida_en_vivo() {
        vm.cambiarAlias("ab")
        vm.cambiarClave("abc")
        assertNotNull(vm.errorAlias)
        assertNotNull(vm.errorClave)

        vm.cambiarAlias("cadete_01")
        vm.cambiarClave("abc12345")
        assertNull(vm.errorAlias)
        assertNull(vm.errorClave)
    }

    @Test
    fun cambiar_clave_revalida_la_confirmacion() {
        vm.cambiarClave("abc12345")
        vm.cambiarConfirmacion("abc12345")
        assertNull(vm.errorConfirmacion)

        vm.cambiarClave("abc99999")
        assertNotNull(vm.errorConfirmacion)
    }

    @Test
    fun enviar_con_campos_invalidos_no_llama_al_repositorio() {
        var exito = false
        vm.enviar(registro = true) { exito = true }

        assertEquals("Revisa los campos marcados", vm.mensaje)
        assertFalse(exito)
        coVerify(exactly = 0) { repo.registrarUsuario(any(), any()) }
    }

    @Test
    fun registro_exitoso_guarda_la_sesion() {
        coEvery { repo.registrarUsuario("cadete_01", "abc12345") } returns true
        vm.cambiarAlias("cadete_01")
        vm.cambiarClave("abc12345")
        vm.cambiarConfirmacion("abc12345")

        var exito = false
        vm.enviar(registro = true) { exito = true }

        assertTrue(exito)
        assertEquals("cadete_01", Sesion.alias)
        assertEquals("cadete_01", prefs.aliasSesion)       // la sesión queda recordada
        assertFalse(vm.cargando)
    }

    @Test
    fun registro_de_usuario_existente_muestra_mensaje() {
        coEvery { repo.registrarUsuario(any(), any()) } returns false
        vm.cambiarAlias("cadete_01")
        vm.cambiarClave("abc12345")
        vm.cambiarConfirmacion("abc12345")

        var exito = false
        vm.enviar(registro = true) { exito = true }

        assertFalse(exito)
        assertEquals("Ese usuario ya existe", vm.mensaje)
        assertNull(Sesion.alias)
    }

    @Test
    fun login_correcto_guarda_la_sesion() {
        coEvery { repo.iniciarSesion("cadete_01", "abc12345") } returns true
        vm.cambiarAlias("cadete_01")
        vm.cambiarClave("abc12345")

        var exito = false
        vm.enviar(registro = false) { exito = true }

        assertTrue(exito)
        assertEquals("cadete_01", Sesion.alias)
    }

    @Test
    fun login_incorrecto_muestra_mensaje() {
        coEvery { repo.iniciarSesion(any(), any()) } returns false
        vm.cambiarAlias("cadete_01")
        vm.cambiarClave("abc12345")

        var exito = false
        vm.enviar(registro = false) { exito = true }

        assertFalse(exito)
        assertEquals("Usuario o contraseña incorrectos", vm.mensaje)
    }
}
