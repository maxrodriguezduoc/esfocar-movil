package com.example.esfocarapp.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.esfocarapp.EsfocarApp
import com.example.esfocarapp.data.remote.Respuesta

data class ResultadoSincronizacion(
    val exito: Boolean,
    val contenidoActualizado: Boolean,
    val mensaje: String
)

// Orquesta la sincronización completa con los tres microservicios
class Sincronizador(
    private val contenido: ContenidoRepository,
    private val apuntes: ApuntesRepository,
    private val progreso: ProgresoRepository,
    private val aliasActual: () -> String?
) {
    suspend fun ejecutar(): ResultadoSincronizacion {
        val c = contenido.sincronizar()
        val a = apuntes.sincronizar(aliasActual())
        val p = progreso.subirPendientes()

        val actualizado = c is Respuesta.Exito && c.datos
        val fallo = listOf(c, a, p).filterIsInstance<Respuesta.Fallo>().firstOrNull()
        return when {
            fallo != null -> ResultadoSincronizacion(false, actualizado, fallo.mensaje)
            actualizado -> ResultadoSincronizacion(true, true, "Contenido actualizado desde el servidor")
            else -> ResultadoSincronizacion(true, false, "Todo está sincronizado")
        }
    }
}

// Trabajo en segundo plano: Android lo ejecuta cuando hay internet, aunque la app esté cerrada
class TrabajoSincronizacion(contexto: Context, parametros: WorkerParameters) :
    CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val contenedor = (applicationContext as EsfocarApp).container
        val resultado = contenedor.sincronizador.ejecutar()
        return if (resultado.exito || runAttemptCount >= 3) Result.success() else Result.retry()
    }

    companion object {
        private const val NOMBRE = "sincronizacion-esfocar"

        fun programar(contexto: Context) {
            val pedido = OneTimeWorkRequestBuilder<TrabajoSincronizacion>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(contexto)
                .enqueueUniqueWork(NOMBRE, ExistingWorkPolicy.APPEND_OR_REPLACE, pedido)
        }
    }
}
