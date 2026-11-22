package com.example.esfocarapp.viewmodel

import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.ResultadosRepository
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResultadoRemoto
import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.Pregunta
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TriviaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = mockk<AppRepository>(relaxed = true)
    private val resultados = mockk<ResultadosRepository>()
    private var alias: String? = "cadete_01"
    private lateinit var vm: TriviaViewModel

    private val preguntas = listOf(
        Pregunta("P1", listOf(Alternativa("Buena", true, "Correcto"), Alternativa("Mala", false, "Incorrecto"))),
        Pregunta("P2", listOf(Alternativa("Mala", false, "Incorrecto"), Alternativa("Buena", true, "Correcto")))
    )

    private val remoto = ResultadoRemoto(1, "cadete_01", "Trivia", 1, 2, "01-01-2026 10:00")

    @Before
    fun preparar() {
        vm = TriviaViewModel(repo, resultados, preguntas) { alias }
        coEvery { resultados.guardar(any(), any(), any(), any()) } returns Respuesta.Exito(remoto)
    }

    @Test
    fun estado_inicial() {
        assertEquals(0, vm.indice)
        assertFalse(vm.terminada)
        assertEquals("P1", vm.preguntaActual.enunciado)
    }

    @Test
    fun responder_sin_seleccionar_devuelve_false() {
        assertFalse(vm.responder())
        assertFalse(vm.respondida)
    }

    @Test
    fun respuesta_correcta_suma_acierto() {
        vm.seleccionar(0)
        assertTrue(vm.responder())
        assertEquals(1, vm.aciertos)
        assertTrue(vm.respondida)
    }

    @Test
    fun respuesta_incorrecta_no_suma_acierto() {
        vm.seleccionar(1)
        assertFalse(vm.responder())
        assertEquals(0, vm.aciertos)
    }

    @Test
    fun no_se_puede_cambiar_la_seleccion_despues_de_responder() {
        vm.seleccionar(0)
        vm.responder()
        vm.seleccionar(1)
        assertEquals(0, vm.seleccion)
    }

    @Test
    fun siguiente_avanza_y_reinicia_la_seleccion() {
        vm.seleccionar(0)
        vm.responder()
        vm.siguiente("Trivia")

        assertEquals(1, vm.indice)
        assertNull(vm.seleccion)
        assertFalse(vm.respondida)
        coVerify(exactly = 0) { repo.guardarIntento(any(), any(), any()) }
    }

    @Test
    fun al_terminar_guarda_local_y_sincroniza_con_el_servidor() {
        vm.seleccionar(0); vm.responder(); vm.siguiente("Trivia")
        vm.seleccionar(0); vm.responder(); vm.siguiente("Trivia")

        assertTrue(vm.terminada)
        coVerify { repo.guardarIntento("Trivia", 1, 2) }
        coVerify { resultados.guardar("cadete_01", "Trivia", 1, 2) }
        assertEquals("Resultado sincronizado con el servidor", vm.avisoNube)
    }

    @Test
    fun si_falla_la_red_el_resultado_queda_en_el_telefono() {
        coEvery { resultados.guardar(any(), any(), any(), any()) } returns Respuesta.Fallo("Sin conexión")
        vm.seleccionar(0); vm.responder(); vm.siguiente("Trivia")
        vm.seleccionar(0); vm.responder(); vm.siguiente("Trivia")

        coVerify { repo.guardarIntento("Trivia", 1, 2) }
        assertEquals("Resultado guardado solo en el teléfono (sin conexión)", vm.avisoNube)
    }

    @Test
    fun sin_sesion_no_se_envia_al_servidor() {
        alias = null
        vm.seleccionar(0); vm.responder(); vm.siguiente("Trivia")
        vm.seleccionar(0); vm.responder(); vm.siguiente("Trivia")

        coVerify { repo.guardarIntento("Trivia", 1, 2) }
        coVerify(exactly = 0) { resultados.guardar(any(), any(), any(), any()) }
        assertNull(vm.avisoNube)
    }

    @Test
    fun reiniciar_vuelve_al_estado_inicial() {
        vm.seleccionar(0); vm.responder(); vm.siguiente("Trivia")
        vm.reiniciar()

        assertEquals(0, vm.indice)
        assertEquals(0, vm.aciertos)
        assertNull(vm.seleccion)
        assertNull(vm.avisoNube)
    }
}
