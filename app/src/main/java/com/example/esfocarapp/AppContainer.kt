package com.example.esfocarapp

import android.app.Application
import android.content.Context
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.ApuntesRepository
import com.example.esfocarapp.data.ConceptosRepository
import com.example.esfocarapp.data.ContenidoRepository
import com.example.esfocarapp.data.GestionContenidoRepository
import com.example.esfocarapp.data.ProgresoRepository
import com.example.esfocarapp.data.ResultadosRepository
import com.example.esfocarapp.data.Sincronizador
import com.example.esfocarapp.data.TrabajoSincronizacion
import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.data.remote.ApuntesApi
import com.example.esfocarapp.data.remote.ContenidosApi
import com.example.esfocarapp.data.remote.ResultadosApi
import com.example.esfocarapp.data.remote.RetrofitFactory
import com.example.esfocarapp.data.remote.WikipediaApi
import com.example.esfocarapp.ui.theme.PreferenciaTema
import com.example.esfocarapp.util.MonitorConexion
import com.example.esfocarapp.util.Preferencias
import com.example.esfocarapp.util.PreferenciasAndroid
import com.example.esfocarapp.util.Sesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Aquí se crean (una sola vez) las bases de datos, repositorios y servicios que usan los ViewModels
class AppContainer(private val context: Context) {

    private val db = AppDatabase.obtener(context)
    private val programarSync: () -> Unit = { TrabajoSincronizacion.programar(context) }

    val preferencias: Preferencias = PreferenciasAndroid(context)
    val monitorConexion = MonitorConexion(context)

    val appRepository by lazy { AppRepository(db.usuarioDao(), db.documentoDao()) }

    private val contenidosApi by lazy { RetrofitFactory.crear(BuildConfig.URL_CONTENIDOS, ContenidosApi::class.java) }

    val contenidoRepository by lazy {
        ContenidoRepository(db.contenidoDao(), contenidosApi, preferencias) {
            context.assets.open(ARCHIVO_CONTENIDO).bufferedReader().use { it.readText() }
        }
    }

    // Modo docente: publicar preguntas y casos en ms-contenidos
    val gestionContenidoRepository by lazy { GestionContenidoRepository(contenidosApi, contenidoRepository) }

    val resultadosRepository by lazy {
        ResultadosRepository(RetrofitFactory.crear(BuildConfig.URL_RESULTADOS, ResultadosApi::class.java))
    }

    val progresoRepository by lazy {
        ProgresoRepository(db.progresoDao(), resultadosRepository, alCambiar = programarSync)
    }

    val apuntesRepository by lazy {
        ApuntesRepository(
            db.apunteDao(),
            RetrofitFactory.crear(BuildConfig.URL_APUNTES, ApuntesApi::class.java),
            alCambiar = programarSync
        )
    }

    val conceptosRepository by lazy {
        ConceptosRepository(RetrofitFactory.crear(URL_WIKIPEDIA, WikipediaApi::class.java))
    }

    val sincronizador by lazy {
        Sincronizador(contenidoRepository, apuntesRepository, progresoRepository) { Sesion.alias }
    }

    companion object {
        const val URL_WIKIPEDIA = "https://es.wikipedia.org/api/rest_v1/"
        const val ARCHIVO_CONTENIDO = "contenido_inicial.json"
    }
}

// Clase Application: se crea al abrir la app, restaura la sesión y prepara el contenido offline
class EsfocarApp : Application() {

    val container: AppContainer by lazy { AppContainer(this) }
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Sesion.alias = container.preferencias.aliasSesion
        Sesion.rol = container.preferencias.rolSesion
        PreferenciaTema.tema = container.preferencias.tema
        alcance.launch { container.contenidoRepository.asegurarContenidoInicial() }
        TrabajoSincronizacion.programar(this)
    }
}
