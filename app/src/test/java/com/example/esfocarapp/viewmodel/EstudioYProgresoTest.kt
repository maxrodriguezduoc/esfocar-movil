package com.example.esfocarapp.viewmodel

import com.example.esfocarapp.Datos
import com.example.esfocarapp.Datos.HOY
import com.example.esfocarapp.MainDispatcherRule
import com.example.esfocarapp.PreferenciasFalsas
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.ProgresoRepository
import com.example.esfocarapp.data.ResultadoSincronizacion
import com.example.esfocarapp.data.Sincronizador
import com.example.esfocarapp.data.local.RendimientoUnidad
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResumenRemoto
import com.example.esfocarapp.model.NivelActuacion
import com.example.esfocarapp.model.TipoActividad
import com.example.esfocarapp.ui.theme.PreferenciaTema
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.util.Tema
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private fun contenidoFalso(): ContenidoRepository = mockk<ContenidoRepository>().also {
    every { it.asignaturas } returns flowOf(listOf(Datos.asignatura, Datos.asignatura2))
    every { it.unidades } returns flowOf(listOf(Datos.unidad, Datos.unidad2))
    every { it.casos } returns flowOf(listOf(Datos.caso, Datos.caso.copy(id = 2151, unidadId = 21)))
    every { it.fichas(11) } returns flowOf(listOf(Datos.ficha))
    every { it.fichasPorIds(any()) } returns flowOf(listOf(Datos.ficha))
    every { it.buscar(any()) } returns flowOf(listOf(Datos.ficha))
    coEvery { it.ficha(111) } returns Datos.ficha
    coEvery { it.caso(1151) } returns Datos.caso
    coEvery { it.caso(999) } returns null
    coEvery { it.unidad(11) } returns Datos.unidad
}

private fun progresoFalso(favoritos: Set<Long> = emptySet()): ProgresoRepository =
    mockk<ProgresoRepository>(relaxed = true).also {
        every { it.rendimiento(any()) } returns flowOf(listOf(RendimientoUnidad(11, 1, 4)))
        every { it.favoritos(any()) } returns flowOf(favoritos)
        every { it.errores(any()) } returns flowOf(listOf(1101L, 1102L))
        every { it.intentos(any()) } returns flowOf(
            listOf(Datos.intento(1, tipo = "CASO", aciertos = 1, total = 2, refId = 1151),
                Datos.intento(2, tipo = "CASO", aciertos = 2, total = 2, refId = 1151))
        )
    }

class EstudioViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun asignaturas_con_su_dominio() = runTest {
        val vm = EstudioViewModel(contenidoFalso(), progresoFalso()) { "cadete_01" }
        val lista = vm.asignaturas.first { it.isNotEmpty() }
        assertEquals(25, lista[0].porcentaje)
        assertNull(lista[1].porcentaje)
    }

    @Test
    fun unidades_asignatura_y_fichas() = runTest {
        val vm = EstudioViewModel(contenidoFalso(), progresoFalso()) { "cadete_01" }
        assertEquals(listOf(11L), vm.unidadesDe(1).first().map { it.unidad.id })
        assertEquals("Normativa", vm.asignatura(2).first()?.nombre)
        assertEquals("Principios", vm.unidad(11).first()?.titulo)
        assertEquals(1, vm.fichas(11).first().size)
        assertEquals("Secuencia", vm.ficha(111)?.titulo)
    }

    @Test
    fun casos_con_mejor_puntaje_y_asignatura() = runTest {
        val vm = EstudioViewModel(contenidoFalso(), progresoFalso()) { "cadete_01" }
        val casos = vm.casos.first { it.isNotEmpty() }
        assertEquals(2, casos[0].mejorPuntaje)
        assertEquals("Procedimientos", casos[0].asignatura?.nombre)
        assertNull(casos[1].mejorPuntaje)
        assertEquals("Normativa", casos[1].asignatura?.nombre)
    }

    @Test
    fun busqueda_requiere_al_menos_dos_letras() = runTest {
        val contenido = contenidoFalso()
        val vm = EstudioViewModel(contenido, progresoFalso()) { "cadete_01" }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.resultadosBusqueda.collect {} }

        vm.buscar("a")
        assertTrue(vm.resultadosBusqueda.value.isEmpty())
        vm.buscar("  secuencia ")
        assertEquals("  secuencia ", vm.consulta.value)
        assertEquals(1, vm.resultadosBusqueda.value.size)
        io.mockk.verify { contenido.buscar("secuencia") }
    }

    @Test
    fun favoritos_agregar_y_quitar() = runTest {
        val progreso = progresoFalso(favoritos = setOf(111L))
        val vm = EstudioViewModel(contenidoFalso(), progreso) { "cadete_01" }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.favoritos.collect {} }

        vm.alternarFavorito(111)
        coVerify { progreso.cambiarFavorito("cadete_01", 111, false) }
        vm.alternarFavorito(222)
        coVerify { progreso.cambiarFavorito("cadete_01", 222, true) }
        assertEquals(1, vm.fichasFavoritas.first { it.isNotEmpty() }.size)
    }

    @Test
    fun sin_favoritos_la_lista_esta_vacia() = runTest {
        val vm = EstudioViewModel(contenidoFalso(), progresoFalso()) { null }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.fichasFavoritas.collect {} }
        assertTrue(vm.fichasFavoritas.value.isEmpty())
    }
}

class ProgresoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun vm(progreso: ProgresoRepository = progresoFalso(), alias: String? = "cadete_01") =
        ProgresoViewModel(contenidoFalso(), progreso, flowOf(false), { alias }, { HOY }, Datos.ZONA)

    @Test
    fun panel_combina_contenido_y_progreso() = runTest {
        val panel = vm().panel.first { it.actividades > 0 }
        assertEquals(2, panel.actividades)
        assertEquals(2, panel.errores)
        assertEquals(1, panel.casosResueltos)
        assertEquals(2, panel.totalCasos)
        assertEquals(1, panel.racha)
    }

    @Test
    fun caso_del_dia_y_conexion() = runTest {
        val vm = vm()
        assertTrue(vm.casoDelDia.first { it != null } != null)
        assertFalse(vm.enLinea.first { !it })
    }

    @Test
    fun resumen_remoto_exito_y_error() {
        val progreso = progresoFalso()
        val resumen = ResumenRemoto("cadete_01", 3, 66.7, 100)
        coEvery { progreso.resumenRemoto("cadete_01") } returns Respuesta.Exito(resumen)
        val vm = vm(progreso)
        vm.cargarResumen()
        assertEquals(resumen, vm.resumen)
        assertNull(vm.errorNube)
        assertFalse(vm.cargandoNube)

        coEvery { progreso.resumenRemoto(any()) } returns Respuesta.Fallo("Sin conexión con el servidor")
        vm.cargarResumen()
        assertEquals("Sin conexión con el servidor", vm.errorNube)
    }

    @Test
    fun sin_sesion_no_consulta_el_servidor() {
        val progreso = progresoFalso()
        vm(progreso, alias = null).cargarResumen()
        coVerify(exactly = 0) { progreso.resumenRemoto(any()) }
    }
}

class CasoViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val progreso = progresoFalso()

    @Test
    fun elegir_confirmar_y_registrar_el_resultado() {
        val vm = CasoViewModel(contenidoFalso(), progreso, 1151) { "cadete_01" }
        assertFalse(vm.cargando)
        assertEquals("Discusión", vm.caso?.titulo)

        vm.confirmar()                                    // sin selección no pasa nada
        assertFalse(vm.confirmado)

        vm.elegir(1)
        assertEquals(NivelActuacion.ACEPTABLE, vm.nivel)
        vm.confirmar()
        assertTrue(vm.confirmado)
        vm.elegir(0)                                      // confirmado: no cambia
        assertEquals(1, vm.seleccion)
        vm.confirmar()                                    // no registra dos veces
        coVerify(exactly = 1) {
            progreso.registrarActividad("cadete_01", TipoActividad.CASO, "Discusión", 1, 2, 1, 1151)
        }

        vm.reintentar()
        assertNull(vm.seleccion)
        assertFalse(vm.confirmado)
        vm.elegir(0)
        assertEquals(NivelActuacion.OPTIMA, vm.nivel)
    }

    @Test
    fun caso_inexistente() {
        val vm = CasoViewModel(contenidoFalso(), progreso, 999) { "cadete_01" }
        assertNull(vm.caso)
        vm.elegir(0)
        vm.confirmar()
        assertFalse(vm.confirmado)
    }

    @Test
    fun sin_sesion_no_registra() {
        val vm = CasoViewModel(contenidoFalso(), progreso, 1151) { null }
        vm.elegir(0)
        vm.confirmar()
        assertTrue(vm.confirmado)
        coVerify(exactly = 0) { progreso.registrarActividad(any(), any(), any(), any(), any(), any(), any()) }
    }
}

class AjustesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val sincronizador = mockk<Sincronizador>()

    @After
    fun limpiar() {
        Sesion.alias = null
        PreferenciaTema.tema = Tema.SISTEMA
    }

    @Test
    fun destino_inicial_segun_estado() {
        val prefs = PreferenciasFalsas()
        val vm = AjustesViewModel(prefs, sincronizador)
        assertEquals(Rutas.ONBOARDING, vm.destinoInicial())

        vm.completarOnboarding()
        assertTrue(prefs.onboardingVisto)
        assertEquals(Rutas.LOGIN, vm.destinoInicial())

        Sesion.alias = "cadete_01"
        assertEquals(Rutas.PRINCIPAL, vm.destinoInicial())
    }

    @Test
    fun cambiar_tema_se_guarda_y_se_aplica() {
        val prefs = PreferenciasFalsas()
        val vm = AjustesViewModel(prefs, sincronizador)
        vm.cambiarTema(Tema.OSCURO)
        assertEquals(Tema.OSCURO, vm.tema)
        assertEquals(Tema.OSCURO, prefs.tema)
        assertEquals(Tema.OSCURO, PreferenciaTema.tema)
    }

    @Test
    fun cerrar_sesion_borra_la_sesion_guardada() {
        val prefs = PreferenciasFalsas(aliasSesion = "cadete_01")
        Sesion.alias = "cadete_01"
        AjustesViewModel(prefs, sincronizador).cerrarSesion()
        assertNull(Sesion.alias)
        assertNull(prefs.aliasSesion)
    }

    @Test
    fun sincronizar_ahora_muestra_el_resultado() {
        coEvery { sincronizador.ejecutar() } returns ResultadoSincronizacion(true, false, "Todo está sincronizado")
        val vm = AjustesViewModel(PreferenciasFalsas(), sincronizador)
        vm.sincronizarAhora()
        assertEquals("Todo está sincronizado", vm.mensajeSync)
        assertFalse(vm.sincronizando)
    }
}
