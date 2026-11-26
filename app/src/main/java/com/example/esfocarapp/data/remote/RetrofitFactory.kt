package com.example.esfocarapp.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Crea los servicios Retrofit (uno por cada servidor distinto)
object RetrofitFactory {

    // Wikipedia pide que cada app se identifique con un User-Agent descriptivo
    private const val USER_AGENT = "EsfocarApp/1.0 (proyecto academico DSY1105)"

    private fun cliente(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val pedido = cadena.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
                cadena.proceed(pedido)
            }
            .build()
    }

    fun <T> crear(baseUrl: String, servicio: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(cliente())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(servicio)
    }
}
