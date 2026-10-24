package com.example.esfocarapp.ui.progreso

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.util.Notificaciones

@Composable
fun ProgresoContent(intentos: List<Intento>) {
    val context = LocalContext.current
    var aviso by remember { mutableStateOf<String?>(null) }

    // Pide el permiso de notificaciones y, si el usuario acepta, envía el recordatorio
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            Notificaciones.mostrarRecordatorio(context)
            aviso = "Recordatorio enviado"
        } else {
            aviso = "Sin permiso de notificaciones"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Recordatorio de estudio", style = MaterialTheme.typography.titleMedium)
                    Text("Recibe una notificación para repasar.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                Notificaciones.mostrarRecordatorio(context)
                                aviso = "Recordatorio enviado"
                            }
                        }
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                        Text("  Enviar recordatorio")
                    }
                    // Animación: el aviso aparece suavemente
                    AnimatedVisibility(visible = aviso != null) {
                        Text(aviso ?: "", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item { Text("Historial de actividades", style = MaterialTheme.typography.titleLarge) }

        if (intentos.isEmpty()) {
            item { Text("Aún no has completado ninguna actividad.") }
        }

        items(intentos, key = { it.id }) { intento ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(intento.actividad, style = MaterialTheme.typography.titleMedium)
                    Text("Puntaje: ${intento.aciertos} de ${intento.total}")
                    Text(intento.fecha, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
