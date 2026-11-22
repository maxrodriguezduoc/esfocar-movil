package com.example.esfocarapp

import android.app.Application
import android.content.Context
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.ApuntesRepository
import com.example.esfocarapp.data.ConceptosRepository
import com.example.esfocarapp.data.ResultadosRepository
import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.data.remote.ApuntesApi
import com.example.esfocarapp.data.remote.ResultadosApi
import com.example.esfocarapp.data.remote.RetrofitFactory
import com.example.esfocarapp.data.remote.WikipediaApi

// Aquí se crean (una sola vez) las bases de datos y repositorios que usan los ViewModels
class AppContainer(context: Context) {

    private val db = AppDatabase.obtener(context)

    val appRepository by lazy { AppRepository(db.usuarioDao(), db.intentoDao(), db.documentoDao()) }

    val apuntesRepository by lazy {
        ApuntesRepository(RetrofitFactory.crear(BuildConfig.URL_APUNTES, ApuntesApi::class.java))
    }

    val resultadosRepository by lazy {
        ResultadosRepository(RetrofitFactory.crear(BuildConfig.URL_RESULTADOS, ResultadosApi::class.java))
    }

    val conceptosRepository by lazy {
        ConceptosRepository(RetrofitFactory.crear(URL_WIKIPEDIA, WikipediaApi::class.java))
    }

    companion object {
        const val URL_WIKIPEDIA = "https://es.wikipedia.org/api/rest_v1/"
    }
}

// Clase Application: se crea al abrir la app y guarda el contenedor
class EsfocarApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
