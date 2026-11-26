@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.esfocarapp.docente

import com.example.esfocarapp.Datos
import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.GestionContenidoRepository
import com.example.esfocarapp.data.local.ContenidoDao
import com.example.esfocarapp.data.remote.CasoDto
import com.example.esfocarapp.data.remote.ContenidosApi
import com.example.esfocarapp.data.remote.PreguntaDto
import com.example.esfocarapp.data.remote.PreguntaSolicitud
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.Dificultad
import com.example.esfocarapp.model.OpcionCaso
import com.example.esfocarapp.util.ValidadorContenido
import com.example.esfocarapp.viewmodel.DocenteViewModel
import com.example.esfocarapp.viewmodel.FormCasoViewModel
import com.example.esfocarapp.viewmodel.FormPreguntaViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

private val alternativasValidas = listOf(Alternativa("Sí", true, "Bien"), Alternativa("No", false, "Mal"))
private val opcionesValidas = listOf(OpcionCaso("Actuar bien", 2, "Óptimo"), OpcionCaso("Actuar mal", 0, "Mal"))

class ValidadorContenidoTest {

    @Test
    fun enunciado_y_explicacion() {
        assertNotNull(ValidadorContenido.enunciado(""))
        assertNotNull(ValidadorContenido.enunciado("corto"))
        assertNotNull(ValidadorContenido.enunciado("x".repeat(501)))
        assertNull(ValidadorContenido.enunciado("¿Cuál es el primer paso?"))
        assertNull(ValidadorContenido.explicacion(""))
        assertNotNull(ValidadorContenido.explicacion("x".repeat(1001)))
    }

    @Test
    fun alternativas() {
        assertEquals("Agrega al menos 2 alternativas", ValidadorContenido.alternativas(alternativasValidas.take(1)))
        assertNotNull(ValidadorContenido.alternativas(List(6) { Alternativa("a", it == 0, "r") }))
        assertNotNull(ValidadorContenido.alternativas(listOf(Alternativa("", true, "r"), Alternativa("b", false, "r"))))
        assertNotNull(ValidadorContenido.alternativas(listOf(Alternativa("a", true, ""), Alternativa("b", false, "r"))))
        assertEquals(
            "Marca exactamente una alternativa correcta",
            ValidadorContenido.alternativas(listOf(Alternativa("a", true, "r"), Alternativa("b", true, "r")))
        )
        assertNull(ValidadorContenido.alternativas(alternativasValidas))
    }

    @Test
    fun campos_del_caso() {
        assertNotNull(ValidadorContenido.tituloCaso(""))
        assertNotNull(ValidadorContenido.tituloCaso("ab"))
        assertNotNull(ValidadorContenido.tituloCaso("x".repeat(121)))
        assertNull(ValidadorContenido.tituloCaso("Caso nuevo"))
        assertNotNull(ValidadorContenido.situacion(""))
        assertNotNull(ValidadorContenido.situacion("muy corta"))
        assertNotNull(ValidadorContenido.situacion("x".repeat(2001)))
        assertNull(ValidadorContenido.situacion("Una situación suficientemente descrita"))
        assertNotNull(ValidadorContenido.preguntaCaso(""))
        assertNotNull(ValidadorContenido.preguntaCaso("x".repeat(501)))
        assertNull(ValidadorContenido.preguntaCaso("¿Qué haces?"))
        assertNotNull(ValidadorContenido.fundamento(""))
        assertNotNull(ValidadorContenido.fundamento("x".repeat(1501)))
        assertNull(ValidadorContenido.fundamento("Porque así lo indica el protocolo"))
    }

    @Test
    fun opciones_del_caso() {
        assertNotNull(ValidadorContenido.opciones(opcionesValidas.take(1)))
        assertNotNull(ValidadorContenido.opciones(List(6) { OpcionCaso("a", 2, "r") }))
        assertNotNull(ValidadorContenido.opciones(listOf(OpcionCaso("", 2, "r"), OpcionCaso("b", 0, "r"))))
        assertNotNull(ValidadorContenido.opciones(listOf(OpcionCaso("a", 2, ""), OpcionCaso("b", 0, "r"))))
        assertNotNull(ValidadorContenido.opciones(listOf(OpcionCaso("a", 3, "r"), OpcionCaso("b", 0, "r"))))
        assertEquals(
            "Marca al menos una actuación como óptima",
            ValidadorContenido.opciones(listOf(OpcionCaso("a", 1, "r"), OpcionCaso("b", 0, "r")))
        )
        assertNull(ValidadorContenido.opciones(opcionesValidas))
    }
}

class GestionContenidoRepositoryTest {

    private val api = mockk<ContenidosApi>()
    private val contenido = mockk<ContenidoRepository>().also { coEvery { it.sincronizar() } returns Respuesta.Exito(true) }
    private val repo = GestionContenidoRepository(api, contenido)

    private fun http(codigo: Int, cuerpo: String = "{}") =
        HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

    @Test
    fun publicar_pregunta_envia_datos_limpios_y_sincroniza() = runBlocking {
        val enviada = slot<PreguntaSolicitud>()
        coEvery { api.crearPregunta(11, capture(enviada)) } returns PreguntaDto(4206, "x", "y", emptyList())

        val r = repo.crearPregunta(11, "¿Enunciado?", "Explicación",
            listOf(Alternativa("  Sí ", true, " Bien "), Alternativa("No", false, "Mal")))

        assertEquals(Respuesta.Exito(Unit), r)
        assertEquals("Sí", enviada.captured.alternativas[0].texto)
        assertEquals("Bien", enviada.captured.alternativas[0].retroalimentacion)
        assertTrue(enviada.captured.alternativas[0].correcta)
        coVerify { contenido.sincronizar() }
    }

    @Test
    fun editar_y_eliminar_pregunta_y_caso() = runBlocking {
        coEvery { api.actualizarPregunta(1101, any()) } returns PreguntaDto(1101, "x", "y", emptyList())
        coEvery { api.eliminarPregunta(1101) } returns Unit
        coEvery { api.crearCaso(11, any()) } returns CasoDto(4253, "t", "s", "p", "MEDIA", "f", emptyList())
        coEvery { api.actualizarCaso(1151, any()) } returns CasoDto(1151, "t", "s", "p", "MEDIA", "f", emptyList())
        coEvery { api.eliminarCaso(1151) } returns Unit

        assertEquals(Respuesta.Exito(Unit), repo.actualizarPregunta(1101, "e", "x", alternativasValidas))
        assertEquals(Respuesta.Exito(Unit), repo.eliminarPregunta(1101))
        assertEquals(Respuesta.Exito(Unit), repo.crearCaso(11, "t", "s", "p", "MEDIA", "f", opcionesValidas))
        assertEquals(Respuesta.Exito(Unit), repo.actualizarCaso(1151, "t", "s", "p", "MEDIA", "f", opcionesValidas))
        assertEquals(Respuesta.Exito(Unit), repo.eliminarCaso(1151))
        coVerify(exactly = 5) { contenido.sincronizar() }
    }

    @Test
    fun sin_conexion_avisa_que_publicar_requiere_servidor() = runBlocking {
        coEvery { api.eliminarCaso(any()) } throws IOException()
        assertEquals(
            Respuesta.Fallo("Se necesita conexión con el servidor para publicar contenido"),
            repo.eliminarCaso(1151)
        )
        coVerify(exactly = 0) { contenido.sincronizar() }
    }

    @Test
    fun errores_del_servidor_se_muestran_tal_cual() = runBlocking {
        coEvery { api.crearPregunta(any(), any()) } throws
            http(400, """{"error":"La pregunta debe tener exactamente una alternativa correcta"}""")
        coEvery { api.eliminarPregunta(any()) } throws http(404)

        assertEquals(
            Respuesta.Fallo("La pregunta debe tener exactamente una alternativa correcta", 400),
            repo.crearPregunta(11, "e", "x", alternativasValidas)
        )
        assertEquals(Respuesta.Fallo("La pregunta ya no existe", 404), repo.eliminarPregunta(9))
    }
}

class ContenidoParaDocenteTest {

    @Test
    fun pregunta_por_id_y_flujo_por_unidad() = runBlocking {
        val dao = mockk<ContenidoDao>(relaxed = true)
        val entidades = com.example.esfocarapp.data.MapeoContenido.aEntidades(
            com.example.esfocarapp.data.MapeoContenido.leerPaquete(Datos.contenidoIncluido())
        )
        coEvery { dao.pregunta(1101) } returns entidades.preguntas.first()
        coEvery { dao.pregunta(9) } returns null
        every { dao.preguntasDeUnidadFlujo(11) } returns flowOf(entidades.preguntas.take(5))
        val repo = ContenidoRepository(dao, mockk(), com.example.esfocarapp.PreferenciasFalsas()) { "" }

        assertEquals(1101L, repo.pregunta(1101)?.id)
        assertNull(repo.pregunta(9))
        assertEquals(5, repo.preguntasFlujo(11).first().size)
    }
}

private fun contenidoFalso(): ContenidoRepository = mockk<ContenidoRepository>().also {
    every { it.asignaturas } returns flowOf(listOf(Datos.asignatura, Datos.asignatura2))
    every { it.unidades } returns flowOf(listOf(Datos.unidad, Datos.unidad2))
    every { it.casos } returns flowOf(listOf(Datos.caso, Datos.caso.copy(id = 2151, unidadId = 21)))
    every { it.preguntasFlujo(11) } returns flowOf(listOf(Datos.pregunta(1101), Datos.pregunta(1102)))
    coEvery { it.pregunta(1101) } returns Datos.pregunta(1101)
    coEvery { it.pregunta(999) } returns null
    coEvery { it.caso(1151) } returns Datos.caso
    coEvery { it.caso(999) } returns null
}

class DocenteViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val gestion = mockk<GestionContenidoRepository>()

    @Test
    fun elegir_asignatura_y_unidad_muestra_su_contenido() = runTest {
        val vm = DocenteViewModel(contenidoFalso(), gestion)
        val sub = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(sub) { vm.unidades.collect {} }
        backgroundScope.launch(sub) { vm.preguntas.collect {} }
        backgroundScope.launch(sub) { vm.casos.collect {} }

        assertTrue(vm.unidades.value.isEmpty())
        vm.elegirAsignatura(1)
        assertEquals(listOf(11L), vm.unidades.value.map { it.id })
        vm.elegirUnidad(11)
        assertEquals(2, vm.preguntas.value.size)
        assertEquals(listOf(1151L), vm.casos.value.map { it.id })

        vm.elegirAsignatura(2)                              // cambiar de asignatura limpia la unidad
        assertNull(vm.unidadId.value)
        assertTrue(vm.preguntas.value.isEmpty())
        assertEquals(2, vm.asignaturas.first { it.isNotEmpty() }.size)
    }

    @Test
    fun eliminar_muestra_resultado() {
        coEvery { gestion.eliminarPregunta(1101) } returns Respuesta.Exito(Unit)
        coEvery { gestion.eliminarCaso(1151) } returns Respuesta.Fallo("Se necesita conexión con el servidor para publicar contenido")
        val vm = DocenteViewModel(contenidoFalso(), gestion)

        vm.eliminarPregunta(1101)
        assertEquals("Pregunta eliminada", vm.mensaje)
        vm.eliminarCaso(1151)
        assertEquals("Se necesita conexión con el servidor para publicar contenido", vm.mensaje)
        assertFalse(vm.procesando)
        vm.limpiarMensaje()
        assertNull(vm.mensaje)
    }
}

class FormPreguntaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val gestion = mockk<GestionContenidoRepository>()

    private fun FormPreguntaViewModel.llenar() {
        cambiarEnunciado("¿Cuál es el primer paso?")
        cambiarExplicacion("Evaluar el entorno")
        cambiarTexto(0, "Evaluar")
        cambiarRetroalimentacion(0, "Correcto")
        cambiarTexto(1, "Actuar")
        cambiarRetroalimentacion(1, "Incorrecto")
        quitarAlternativa(2)
    }

    @Test
    fun nueva_pregunta_se_publica() {
        coEvery { gestion.crearPregunta(11, any(), any(), any()) } returns Respuesta.Exito(Unit)
        val vm = FormPreguntaViewModel(contenidoFalso(), gestion, 11, 0)
        assertFalse(vm.editando)
        assertEquals(3, vm.alternativas.size)

        vm.llenar()
        vm.guardar()

        assertTrue(vm.guardado)
        coVerify {
            gestion.crearPregunta(11, "¿Cuál es el primer paso?", "Evaluar el entorno", match {
                it.size == 2 && it[0].esCorrecta && it[0].texto == "Evaluar" && !it[1].esCorrecta
            })
        }
    }

    @Test
    fun datos_invalidos_no_se_envian() {
        val vm = FormPreguntaViewModel(contenidoFalso(), gestion, 11, 0)
        vm.guardar()
        assertNotNull(vm.errorEnunciado)
        assertNotNull(vm.errorAlternativas)
        assertEquals("Revisa los campos marcados", vm.mensaje)
        coVerify(exactly = 0) { gestion.crearPregunta(any(), any(), any(), any()) }
    }

    @Test
    fun editar_carga_la_pregunta_y_la_actualiza() {
        coEvery { gestion.actualizarPregunta(1101, any(), any(), any()) } returns Respuesta.Exito(Unit)
        val vm = FormPreguntaViewModel(contenidoFalso(), gestion, 11, 1101)
        assertTrue(vm.editando)
        assertFalse(vm.cargando)
        assertEquals("¿Pregunta 1101?", vm.enunciado)

        vm.cambiarEnunciado("¿Pregunta editada por el docente?")
        vm.guardar()
        assertTrue(vm.guardado)
        coVerify { gestion.actualizarPregunta(1101, "¿Pregunta editada por el docente?", "Explicación 1101", any()) }
    }

    @Test
    fun pregunta_inexistente() {
        val vm = FormPreguntaViewModel(contenidoFalso(), gestion, 11, 999)
        assertEquals("La pregunta ya no existe", vm.mensaje)
    }

    @Test
    fun error_del_servidor_conserva_el_formulario() {
        coEvery { gestion.crearPregunta(any(), any(), any(), any()) } returns Respuesta.Fallo("Sin conexión")
        val vm = FormPreguntaViewModel(contenidoFalso(), gestion, 11, 0)
        vm.llenar()
        vm.guardar()
        assertFalse(vm.guardado)
        assertEquals("Sin conexión", vm.mensaje)
        assertFalse(vm.guardando)
    }

    @Test
    fun manejo_de_alternativas() {
        val vm = FormPreguntaViewModel(contenidoFalso(), gestion, 11, 0)
        vm.marcarCorrecta(2)
        assertEquals(listOf(false, false, true), vm.alternativas.map { it.correcta })

        vm.quitarAlternativa(2)                             // se quita la correcta: pasa a ser la primera
        assertEquals(listOf(true, false), vm.alternativas.map { it.correcta })
        vm.quitarAlternativa(0)                             // mínimo 2: no se puede quitar
        assertEquals(2, vm.alternativas.size)

        repeat(5) { vm.agregarAlternativa() }
        assertEquals(ValidadorContenido.MAX_OPCIONES, vm.alternativas.size)
        vm.quitarAlternativa(4)                             // quitar una que no es correcta
        assertEquals(4, vm.alternativas.size)
        assertTrue(vm.alternativas[0].correcta)
        vm.cambiarExplicacion("x".repeat(1001))
        assertNotNull(vm.errorExplicacion)
    }
}

class FormCasoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val gestion = mockk<GestionContenidoRepository>()

    private fun FormCasoViewModel.llenar() {
        cambiarTitulo("Persona herida en la vía")
        cambiarSituacion("Una persona está herida en la calzada y hay tránsito vehicular.")
        cambiarPregunta("¿Qué haces primero?")
        cambiarFundamento("La seguridad de la escena es lo primero")
        cambiarDificultad(Dificultad.AVANZADA)
        cambiarTexto(0, "Asegurar la escena y pedir ayuda")
        cambiarRetroalimentacion(0, "Óptimo")
        cambiarTexto(1, "Atender sin señalizar")
        cambiarRetroalimentacion(1, "Riesgoso")
        cambiarTexto(2, "Esperar")
        cambiarRetroalimentacion(2, "Incorrecto")
    }

    @Test
    fun nuevo_caso_se_publica() {
        coEvery { gestion.crearCaso(any(), any(), any(), any(), any(), any(), any()) } returns Respuesta.Exito(Unit)
        val vm = FormCasoViewModel(contenidoFalso(), gestion, 31, 0)
        vm.llenar()
        vm.guardar()

        assertTrue(vm.guardado)
        coVerify {
            gestion.crearCaso(31, "Persona herida en la vía", any(), "¿Qué haces primero?", "AVANZADA", any(),
                match { it.map { o -> o.puntaje } == listOf(2, 1, 0) })
        }
    }

    @Test
    fun sin_actuacion_optima_no_se_envia() {
        val vm = FormCasoViewModel(contenidoFalso(), gestion, 31, 0)
        vm.llenar()
        vm.cambiarPuntaje(0, 1)
        vm.guardar()
        assertEquals("Marca al menos una actuación como óptima", vm.errorOpciones)
        coVerify(exactly = 0) { gestion.crearCaso(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun campos_vacios_marcan_errores() {
        val vm = FormCasoViewModel(contenidoFalso(), gestion, 31, 0)
        vm.cambiarPregunta("")
        vm.guardar()
        assertNotNull(vm.errorTitulo)
        assertNotNull(vm.errorSituacion)
        assertNotNull(vm.errorPregunta)
        assertNotNull(vm.errorFundamento)
        assertNotNull(vm.errorOpciones)
    }

    @Test
    fun editar_carga_el_caso_y_lo_actualiza() {
        coEvery { gestion.actualizarCaso(1151, any(), any(), any(), any(), any(), any()) } returns Respuesta.Exito(Unit)
        val vm = FormCasoViewModel(contenidoFalso(), gestion, 11, 1151)
        assertTrue(vm.editando)
        assertEquals("Discusión", vm.titulo)
        assertEquals(Dificultad.BASICA, vm.dificultad)
        assertEquals(3, vm.opciones.size)

        vm.cambiarSituacion("Situación editada con suficiente detalle")
        vm.guardar()
        assertTrue(vm.guardado)
        coVerify { gestion.actualizarCaso(1151, "Discusión", "Situación editada con suficiente detalle", any(), "BASICA", any(), any()) }
    }

    @Test
    fun caso_inexistente_y_error_del_servidor() {
        assertEquals("El caso ya no existe", FormCasoViewModel(contenidoFalso(), gestion, 11, 999).mensaje)

        coEvery { gestion.crearCaso(any(), any(), any(), any(), any(), any(), any()) } returns Respuesta.Fallo("Error del servidor (500)")
        val vm = FormCasoViewModel(contenidoFalso(), gestion, 31, 0)
        vm.llenar()
        vm.guardar()
        assertEquals("Error del servidor (500)", vm.mensaje)
        assertFalse(vm.guardado)
    }

    @Test
    fun manejo_de_opciones() {
        val vm = FormCasoViewModel(contenidoFalso(), gestion, 31, 0)
        vm.cambiarPuntaje(2, 5)
        assertEquals(2, vm.opciones[2].puntaje)            // se limita a 0..2
        repeat(5) { vm.agregarOpcion() }
        assertEquals(ValidadorContenido.MAX_OPCIONES, vm.opciones.size)
        repeat(5) { vm.quitarOpcion(0) }
        assertEquals(ValidadorContenido.MIN_OPCIONES, vm.opciones.size)
    }
}
