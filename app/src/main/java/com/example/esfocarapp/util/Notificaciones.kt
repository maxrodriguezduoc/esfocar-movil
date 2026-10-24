package com.example.esfocarapp.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

// RECURSO NATIVO 2: notificaciones (recordatorio de estudio)
object Notificaciones {

    private const val CANAL_ID = "recordatorios_estudio"

    fun mostrarRecordatorio(context: Context) {
        // En Android 13+ hay que tener el permiso concedido por el usuario
        if (Build.VERSION.SDK_INT >= 33) {
            val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (permiso != PackageManager.PERMISSION_GRANTED) return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Desde Android 8 las notificaciones necesitan un "canal"
        if (Build.VERSION.SDK_INT >= 26) {
            val canal = NotificationChannel(CANAL_ID, "Recordatorios de estudio", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(canal)
        }

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Hora de repasar")
            .setContentText("Resuelve una trivia rápida y revisa tu progreso.")
            .setAutoCancel(true)
            .build()

        manager.notify(1, notificacion)
    }
}
