package com.example.esfocarapp.viewmodel

import com.example.esfocarapp.Datos
import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.ProgresoRepository
import com.example.esfocarapp.model.TipoActividad
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class QuizViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val contenido = mockk<ContenidoRepository>()
    private val progreso = mockk<ProgresoRepository>(relaxed = true)
    private var alias: String? = "cadete_01"

    private val banco = (1L..20L).map { Datos.pregunta(it) }

    init {
        coEvery { contenido.unidad(11) } returns Datos.unidad
        coEvery { contenido.preguntasDeUnidad(11) } returns banco.take(3)
        coEvery { contenido.preguntasDeAsignatura(1) } returns banco
        coEvery { contenido.todasLasPreguntas() } returns banco
        coEvery { contenido.preguntasPorIds(any()) } answers { banco.filter { it.id in firstArg<List<Long>>() } }
    }

    private fun vm(modo: ModoQuiz, id: Long = 0) = QuizViewModel(contenido, progreso, modo, id, { alias }, Random(7))

    // Índice de la alternativa correcta / incorrecta de la pregunta actual (las alternativas se mezclan)
    private fun QuizViewModel.correcta() = preguntaActual!!.alternativas.indexOfFirst { it.esCorrecta }
    private fun QuizViewModel.incorrecta() = preguntaActual!!.alternativas.indexOfFirst { !it.esCorrecta }

    @Test
    fun trivia_de_unidad_carga_todas_sus_preguntas() {
        val vm = vm(ModoQuiz.UNIDAD, 11)
        assertFalse(vm.cargando)
        assertEquals(3, vm.preguntas.size)
        assertEquals("Unidad 1 · Principios", vm.titulo)
        assertNull(vm.tiempoRestante)
    }

    @Test
    fun cantidades_por_modo() {
        assertEquals(ModoQuiz.PREGUNTAS_TRIVIA, vm(ModoQuiz.RAPIDA).preguntas.size)
        assertEquals(ModoQuiz.PREGUNTAS_TRIVIA, vm(ModoQuiz.ASIGNATURA, 1).preguntas.size)
        val simulacro = vm(ModoQuiz.SIMULACRO, 1)
        assertEquals(ModoQuiz.PREGUNTAS_SIMULACRO, simulacro.preguntas.size)
        assertEquals(ModoQuiz.PREGUNTAS_SIMULACRO * ModoQuiz.SEGUNDOS_POR_PREGUNTA, simulacro.tiempoRestante)
        assertEquals(ModoQuiz.PREGUNTAS_SIMULACRO, vm(ModoQuiz.SIMULACRO, 0).preguntas.size)
    }

    @Test
    fun flujo_completo_con_retroalimentacion_inmediata() {
        val vm = vm(ModoQuiz.UNIDAD, 11)

        assertNull(vm.responder())                     // sin selección no hace nada
        vm.seleccionar(vm.correcta())
        assertEquals(true, vm.responder())
        assertTrue(vm.respondida)
        vm.seleccionar(vm.incorrecta())                // ya respondida: no cambia
        vm.siguiente()

        vm.seleccionar(vm.incorrecta())
        assertEquals(false, vm.responder())
        vm.siguiente()

        vm.seleccionar(vm.correcta())
        vm.responder()
        assertTrue(vm.esUltima)
        vm.siguiente()

        assertTrue(vm.terminado)
        assertEquals(2, vm.aciertos)
        assertEquals(67, vm.porcentaje)
        assertEquals(1, vm.errores.size)
        coVerify(exactly = 3) { progreso.registrarRespuesta("cadete_01", any(), any()) }
        coVerify { progreso.registrarActividad("cadete_01", TipoActividad.TRIVIA, "Unidad 1 · Principios", 2, 3, 1, null) }
    }

    @Test
    fun siguiente_sin_responder_no_avanza() {
        val vm = vm(ModoQuiz.UNIDAD, 11)
        vm.siguiente()
        assertEquals(0, vm.indice)
    }

    @Test
    fun simulacro_avanza_sin_mostrar_la_respuesta_y_calcula_nota() {
        val vm = vm(ModoQuiz.SIMULACRO, 1)
        repeat(ModoQuiz.PREGUNTAS_SIMULACRO) { i ->
            vm.seleccionar(if (i < 9) vm.correcta() else vm.incorrecta())
            vm.responder()
            assertFalse(vm.respondida)
        }
        assertTrue(vm.terminado)
        assertEquals(9, vm.aciertos)
        assertEquals(4.0, vm.nota, 0.0)              // 9/15 = 60 % → 4.0
        coVerify { progreso.registrarActividad("cadete_01", TipoActividad.SIMULACRO, any(), 9, 15, 1, null) }
    }

    @Test
    fun al_acabarse_el_tiempo_las_no_respondidas_cuentan_como_incorrectas() {
        val vm = vm(ModoQuiz.SIMULACRO, 1)
        vm.seleccionar(vm.correcta())
        vm.responder()
        repeat(ModoQuiz.PREGUNTAS_SIMULACRO * ModoQuiz.SEGUNDOS_POR_PREGUNTA) { vm.descontarSegundo() }

        assertTrue(vm.terminado)
        assertEquals(0, vm.tiempoRestante)
        assertEquals(ModoQuiz.PREGUNTAS_SIMULACRO, vm.respuestas.size)
        assertEquals(1, vm.aciertos)
        assertEquals(null, vm.errores.last().elegida)
        vm.descontarSegundo()                          // ya terminado: no hace nada
        coVerify(exactly = 1) { progreso.registrarActividad(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun descontar_segundo_sin_temporizador_no_hace_nada() {
        val vm = vm(ModoQuiz.RAPIDA)
        vm.descontarSegundo()
        assertNull(vm.tiempoRestante)
        assertFalse(vm.terminado)
    }

    @Test
    fun repaso_de_errores_usa_las_preguntas_falladas() {
        coEvery { progreso.erroresActuales("cadete_01") } returns listOf(3L, 5L)
        val vm = vm(ModoQuiz.ERRORES)
        assertEquals(setOf(3L, 5L), vm.preguntas.map { it.id }.toSet())
        assertEquals("Repaso de errores", vm.titulo)
    }

    @Test
    fun repaso_sin_errores_queda_vacio() {
        coEvery { progreso.erroresActuales(any()) } returns emptyList()
        val vm = vm(ModoQuiz.ERRORES)
        assertTrue(vm.vacio)
        assertNull(vm.preguntaActual)
        assertNull(vm.responder())
    }

    @Test
    fun sin_sesion_no_registra_nada() {
        alias = null
        val vm = vm(ModoQuiz.UNIDAD, 11)
        repeat(3) {
            vm.seleccionar(vm.correcta())
            vm.responder()
            vm.siguiente()
        }
        assertTrue(vm.terminado)
        coVerify(exactly = 0) { progreso.registrarRespuesta(any(), any(), any()) }
        coVerify(exactly = 0) { progreso.registrarActividad(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun reiniciar_carga_un_nuevo_intento() {
        val vm = vm(ModoQuiz.UNIDAD, 11)
        vm.seleccionar(vm.correcta())
        vm.responder()
        vm.reiniciar()
        assertEquals(0, vm.indice)
        assertTrue(vm.respuestas.isEmpty())
        assertFalse(vm.terminado)
        assertEquals(3, vm.preguntas.size)
    }

    @Test
    fun las_respuestas_dadas_saben_si_son_correctas() {
        val p = Datos.pregunta(1)
        assertTrue(RespuestaDada(p, 0).correcta)
        assertFalse(RespuestaDada(p, 1).correcta)
        assertFalse(RespuestaDada(p, null).correcta)
        assertTrue(ModoQuiz.RAPIDA.retroalimentacionInmediata)
        assertFalse(ModoQuiz.SIMULACRO.retroalimentacionInmediata)
    }
}
