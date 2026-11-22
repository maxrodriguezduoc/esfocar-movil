package com.example.esfocarapp.data

import com.example.esfocarapp.data.remote.ApuntesApi
import com.example.esfocarapp.data.remote.ApunteSolicitud
import com.example.esfocarapp.data.remote.ConceptoRemoto
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.data.remote.ResultadoSolicitud
import com.example.esfocarapp.data.remote.ResultadosApi
import com.example.esfocarapp.data.remote.RetrofitFactory
import com.example.esfocarapp.data.remote.WikipediaApi
import com.example.esfocarapp.data.remote.ContenidosApi
import com.example.esfocarapp.data.remote.llamarApi
import com.example.esfocarapp.Datos
import com.google.gson.JsonParseException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

// Repositorios con la API simulada con MockK (prueba la lógica y el manejo de errores)
class RedRepositoriosTest {

    private val apuntesApi = mockk<ApuntesApi>()
    private val resultadosApi = mockk<ResultadosApi>()
    private val wikipediaApi = mockk<WikipediaApi>()

    private fun errorHttp(codigo: Int, cuerpo: String = "{}"): HttpException =
        HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

    @Test
    fun llamada_exitosa_devuelve_los_datos() = runBlocking {
        coEvery { apuntesApi.listar("cadete_01") } returns emptyList()
        assertEquals(Respuesta.Exito(emptyList<Any>()), llamarApi { apuntesApi.listar("cadete_01") })
    }

    @Test
    fun sin_conexion_y_404_se_traducen_a_mensajes() = runBlocking {
        val solicitud = ApunteSolicitud("cadete_01", "T", "Contenido largo")
        coEvery { apuntesApi.crear(solicitud) } throws IOException("sin red")
        coEvery { apuntesApi.actualizar(5L, solicitud) } throws errorHttp(404)

        assertEquals(Respuesta.Fallo("Sin conexión con el servidor"), llamarApi { apuntesApi.crear(solicitud) })
        assertEquals(
            Respuesta.Fallo("El apunte ya no existe", 404),
            llamarApi("El apunte ya no existe") { apuntesApi.actualizar(5L, solicitud) }
        )
    }

    @Test
    fun error_400_usa_el_mensaje_del_servidor() = runBlocking {
        coEvery { apuntesApi.crear(any()) } throws errorHttp(400, """{"error":"El título es obligatorio"}""")
        assertEquals(Respuesta.Fallo("El título es obligatorio", 400), llamarApi { apuntesApi.crear(ApunteSolicitud("a", "", "x")) })
    }

    @Test
    fun error_400_sin_detalle_usa_mensaje_generico() = runBlocking {
        coEvery { apuntesApi.crear(any()) } throws errorHttp(400, "no es json")
        assertEquals(
            Respuesta.Fallo("Los datos enviados no son válidos", 400),
            llamarApi { apuntesApi.crear(ApunteSolicitud("a", "", "x")) }
        )
    }

    @Test
    fun error_500_muestra_el_codigo() = runBlocking {
        coEvery { apuntesApi.listar(any()) } throws errorHttp(500)
        assertEquals(Respuesta.Fallo("Error del servidor (500)", 500), llamarApi { apuntesApi.listar("x") })
    }

    @Test
    fun json_invalido_se_informa_al_usuario() = runBlocking {
        coEvery { apuntesApi.listar(any()) } throws JsonParseException("malo")
        assertEquals(Respuesta.Fallo("El servidor envió una respuesta inesperada"), llamarApi { apuntesApi.listar("x") })
    }

    @Test
    fun resultados_guardar_listar_y_resumen() = runBlocking {
        coEvery { resultadosApi.crear(ResultadoSolicitud("a", "Trivia", 2, 3)) } throws IOException()
        coEvery { resultadosApi.listar("a") } returns emptyList()
        coEvery { resultadosApi.resumen("a") } throws errorHttp(500)

        val repo = ResultadosRepository(resultadosApi)
        assertTrue(repo.guardar("a", "Trivia", 2, 3) is Respuesta.Fallo)
        assertEquals(Respuesta.Exito(emptyList<Any>()), repo.listar("a"))
        assertTrue(repo.resumen("a") is Respuesta.Fallo)
    }

    @Test
    fun conceptos_buscar_quita_espacios_y_maneja_404() = runBlocking {
        val concepto = ConceptoRemoto("Anatomía", "Ciencia", "La anatomía es...")
        coEvery { wikipediaApi.resumen("Anatomía") } returns concepto
        coEvery { wikipediaApi.resumen("zzz") } throws errorHttp(404)

        val repo = ConceptosRepository(wikipediaApi)
        assertEquals(Respuesta.Exito(concepto), repo.buscar("  Anatomía "))
        assertEquals(Respuesta.Fallo("No se encontró un artículo para \"zzz\"", 404), repo.buscar("zzz"))
        coVerify { wikipediaApi.resumen("Anatomía") }
    }
}

// Prueba de integración ligera: Retrofit real contra un servidor HTTP falso (MockWebServer)
class RetrofitContratoTest {

    private lateinit var servidor: MockWebServer

    @Before
    fun iniciar() {
        servidor = MockWebServer()
        servidor.start()
    }

    @After
    fun detener() {
        servidor.shutdown()
    }

    private fun url() = servidor.url("/").toString()

    @Test
    fun listar_apuntes_hace_GET_con_alias_y_parsea_el_json() = runBlocking {
        servidor.enqueue(
            MockResponse().setBody(
                """[{"id":7,"alias":"cadete_01","titulo":"Protocolo","contenido":"Contenido largo","fecha":"01-01-2026 10:00"}]"""
            )
        )
        val api = RetrofitFactory.crear(url(), ApuntesApi::class.java)

        val lista = api.listar("cadete_01")

        assertEquals(1, lista.size)
        assertEquals(7L, lista[0].id)
        val pedido = servidor.takeRequest()
        assertEquals("GET", pedido.method)
        assertEquals("/api/apuntes?alias=cadete_01", pedido.path)
    }

    @Test
    fun crear_apunte_envia_json_por_POST() = runBlocking {
        servidor.enqueue(
            MockResponse().setResponseCode(201).setBody(
                """{"id":1,"alias":"cadete_01","titulo":"T","contenido":"Contenido largo","fecha":"x"}"""
            )
        )
        val api = RetrofitFactory.crear(url(), ApuntesApi::class.java)

        val creado = api.crear(ApunteSolicitud("cadete_01", "T", "Contenido largo"))

        assertEquals(1L, creado.id)
        val pedido = servidor.takeRequest()
        assertEquals("POST", pedido.method)
        assertEquals("/api/apuntes", pedido.path)
        assertTrue(pedido.body.readUtf8().contains("\"titulo\":\"T\""))
    }

    @Test
    fun actualizar_y_eliminar_usan_PUT_y_DELETE() = runBlocking {
        servidor.enqueue(MockResponse().setBody("""{"id":4,"alias":"a","titulo":"T","contenido":"C","fecha":"x"}"""))
        servidor.enqueue(MockResponse().setResponseCode(204))
        val api = RetrofitFactory.crear(url(), ApuntesApi::class.java)

        api.actualizar(4L, ApunteSolicitud("a", "T", "C"))
        api.eliminar(4L)

        val put = servidor.takeRequest()
        val delete = servidor.takeRequest()
        assertEquals("PUT /api/apuntes/4", "${put.method} ${put.path}")
        assertEquals("DELETE /api/apuntes/4", "${delete.method} ${delete.path}")
    }

    @Test
    fun resumen_de_resultados() = runBlocking {
        servidor.enqueue(
            MockResponse().setBody("""{"alias":"a","intentos":2,"promedioPorcentaje":75.0,"mejorPorcentaje":100}""")
        )
        val api = RetrofitFactory.crear(url(), ResultadosApi::class.java)

        val resumen = api.resumen("a")

        assertEquals(2, resumen.intentos)
        assertEquals(75.0, resumen.promedioPorcentaje, 0.001)
        assertEquals("/api/resultados/resumen?alias=a", servidor.takeRequest().path)
    }

    @Test
    fun wikipedia_codifica_el_titulo_y_envia_user_agent() = runBlocking {
        servidor.enqueue(MockResponse().setBody("""{"title":"Primeros auxilios","extract":"Texto"}"""))
        val api = RetrofitFactory.crear(url(), WikipediaApi::class.java)

        val concepto = api.resumen("Primeros auxilios")

        assertEquals("Primeros auxilios", concepto.title)
        val pedido = servidor.takeRequest()
        assertEquals("/page/summary/Primeros%20auxilios", pedido.path)
        assertTrue(pedido.getHeader("User-Agent")!!.startsWith("EsfocarApp"))
    }

    @Test
    fun el_repositorio_traduce_un_404_real() = runBlocking {
        servidor.enqueue(MockResponse().setResponseCode(404).setBody("""{"error":"no existe"}"""))
        val api = RetrofitFactory.crear(url(), ApuntesApi::class.java)

        val r = llamarApi("El apunte ya no existe") { api.eliminar(99L) }

        assertEquals(Respuesta.Fallo("El apunte ya no existe", 404), r)
    }

    @Test
    fun sin_servidor_el_repositorio_informa_falta_de_conexion() = runBlocking {
        val direccion = url()
        servidor.shutdown()
        val repo = ResultadosRepository(RetrofitFactory.crear(direccion, ResultadosApi::class.java))

        assertEquals(Respuesta.Fallo("Sin conexión con el servidor"), repo.listar("a"))
    }

    @Test
    fun contenidos_consulta_version_y_paquete() = runBlocking {
        servidor.enqueue(MockResponse().setBody("""{"version":3}"""))
        servidor.enqueue(MockResponse().setBody(Datos.contenidoIncluido()))
        val api = RetrofitFactory.crear(url(), ContenidosApi::class.java)

        assertEquals(3, api.version().version)
        assertEquals(4, api.paquete().asignaturas.size)
        assertEquals("/api/contenidos/version", servidor.takeRequest().path)
        assertEquals("/api/contenidos/paquete", servidor.takeRequest().path)
    }
}
