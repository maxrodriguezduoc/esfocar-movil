package com.example.esfocarapp.ui.progreso

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Grading
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.model.Calificacion
import com.example.esfocarapp.model.TipoActividad
import com.example.esfocarapp.ui.components.BarraProgreso
import com.example.esfocarapp.ui.components.EstadoVacio
import com.example.esfocarapp.ui.components.FilaDato
import com.example.esfocarapp.ui.components.InsigniaIcono
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.components.colorAsignatura
import com.example.esfocarapp.ui.components.colorDominio
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.util.Notificaciones
import com.example.esfocarapp.viewmodel.ModoQuiz
import com.example.esfocarapp.viewmodel.ProgresoViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgresoContent(vm: ProgresoViewModel, nav: Navegacion) {
    val context = LocalContext.current
    val panel by vm.panel.collectAsState()
    var aviso by remember { mutableStateOf<String?>(null) }

    // Al mostrar la pestaña se pide el resumen que calcula el microservicio
    LaunchedEffect(Unit) { vm.cargarResumen() }

    // Pide el permiso de notificaciones y, si el usuario acepta, envía el recordatorio
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) Notificaciones.mostrarRecordatorio(context)
        aviso = if (concedido) "Recordatorio enviado" else "Sin permiso de notificaciones"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- Indicadores principales ----------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador(Icons.Rounded.LocalFireDepartment, "${panel.racha}", "Días de racha", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                Indicador(Icons.Rounded.TaskAlt, panel.promedio?.let { "$it%" } ?: "—", "Promedio", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador(Icons.Rounded.Grading, panel.mejorNota?.let { "%.1f".format(it) } ?: "—", "Mejor nota simulacro", MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                Indicador(Icons.Rounded.Psychology, "${panel.casosResueltos}/${panel.totalCasos}", "Casos resueltos", ColoresEstado.exito, Modifier.weight(1f))
            }
        }

        // ---------- Actividad de la semana ----------
        item {
            Tarjeta {
                Text("Actividad de los últimos 7 días", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                GraficoSemana(panel.semana)
            }
        }

        // ---------- Dominio por asignatura ----------
        item { TituloSeccion("Dominio por asignatura") }
        item {
            Tarjeta {
                panel.avance.forEachIndexed { i, avance ->
                    if (i > 0) Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(avance.asignatura.nombre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            avance.porcentaje?.let { "$it%" } ?: "Sin datos",
                            style = MaterialTheme.typography.labelLarge,
                            color = colorDominio(avance.porcentaje)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    BarraProgreso((avance.porcentaje ?: 0) / 100f, colorAsignatura(avance.asignatura.color))
                }
            }
        }

        // ---------- Temas débiles ----------
        if (panel.debiles.isNotEmpty() || panel.errores > 0) {
            item { TituloSeccion("Recomendado para ti") }
        }
        if (panel.errores > 0) {
            item {
                Tarjeta(onClick = { nav.quiz(ModoQuiz.ERRORES, 0) }, color = MaterialTheme.colorScheme.errorContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(Icons.Rounded.Replay, MaterialTheme.colorScheme.onErrorContainer, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Repasar ${panel.errores} preguntas falladas", style = MaterialTheme.typography.titleSmall)
                            Text("Al responderlas bien desaparecen de esta lista", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        items(panel.debiles, key = { "d-${it.unidad.id}" }) { tema ->
            Tarjeta(onClick = { nav.unidad(tema.unidad.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InsigniaIcono(Icons.Rounded.TrendingDown, MaterialTheme.colorScheme.error, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(tema.unidad.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(tema.asignatura, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${tema.porcentaje}%", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // ---------- Servidor (microservicio ms-resultados) ----------
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Resumen en el servidor", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = vm::cargarResumen) { Icon(Icons.Rounded.Refresh, contentDescription = "Actualizar") }
                }
                val resumen = vm.resumen
                when {
                    vm.cargandoNube -> Text("Consultando...", style = MaterialTheme.typography.bodyMedium)
                    vm.errorNube != null -> Text(
                        "${vm.errorNube}. Tus resultados están guardados en el teléfono y se subirán automáticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    resumen != null -> {
                        FilaDato("Actividades sincronizadas", "${resumen.intentos}")
                        FilaDato("Promedio", "%.0f%%".format(resumen.promedioPorcentaje))
                        FilaDato("Mejor resultado", "${resumen.mejorPorcentaje}%")
                    }
                }
            }
        }

        // ---------- Recordatorio ----------
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InsigniaIcono(Icons.Rounded.Notifications, MaterialTheme.colorScheme.secondary, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Recordatorio de estudio", style = MaterialTheme.typography.titleSmall)
                        Text(aviso ?: "Recibe una notificación para repasar", style = MaterialTheme.typography.bodySmall)
                    }
                    FilledTonalButton(onClick = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            Notificaciones.mostrarRecordatorio(context)
                            aviso = "Recordatorio enviado"
                        }
                    }) { Text("Enviar") }
                }
            }
        }

        // ---------- Historial ----------
        item { TituloSeccion("Historial reciente") }
        if (panel.recientes.isEmpty()) {
            item {
                EstadoVacio(
                    Icons.Rounded.Insights, "Aún no hay actividades",
                    "Completa una trivia, un simulacro o un caso para ver tu historial aquí.",
                    textoAccion = "Hacer una trivia rápida",
                    onAccion = { nav.quiz(ModoQuiz.RAPIDA, 0) }
                )
            }
        }
        items(panel.recientes, key = { it.id }) { intento -> FilaIntento(intento) }
    }
}

@Composable
private fun Indicador(icono: ImageVector, valor: String, etiqueta: String, color: Color, modifier: Modifier) {
    Tarjeta(modifier = modifier) {
        InsigniaIcono(icono, color, 36.dp)
        Spacer(Modifier.height(10.dp))
        Text(valor, style = MaterialTheme.typography.headlineSmall)
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Gráfico de barras simple con la cantidad de actividades por día
@Composable
private fun GraficoSemana(valores: List<Int>) {
    val maximo = (valores.maxOrNull() ?: 0).coerceAtLeast(1)
    val hoy = LocalDate.now()
    Row(
        Modifier.fillMaxWidth().height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        valores.forEachIndexed { i, valor ->
            val dia = hoy.minusDays((valores.size - 1 - i).toLong())
            val altura by animateFloatAsState(valor.toFloat() / maximo, label = "barra")
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(if (valor > 0) "$valor" else "", style = MaterialTheme.typography.labelSmall)
                Box(Modifier.height(80.dp).width(22.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(altura.coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (i == valores.lastIndex) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary.copy(alpha = if (valor > 0) 0.8f else 0.15f)
                            )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")).take(2).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FilaIntento(intento: Intento) {
    val tipo = TipoActividad.entries.find { it.name == intento.tipo } ?: TipoActividad.TRIVIA
    val icono = when (tipo) {
        TipoActividad.TRIVIA -> Icons.Rounded.Bolt
        TipoActividad.SIMULACRO -> Icons.Rounded.Grading
        TipoActividad.CASO -> Icons.Rounded.Psychology
        TipoActividad.REPASO -> Icons.Rounded.Replay
    }
    val porcentaje = Calificacion.porcentaje(intento.aciertos, intento.total)
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InsigniaIcono(icono, colorDominio(porcentaje), 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(intento.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${tipo.etiqueta} · ${intento.fecha}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (tipo == TipoActividad.SIMULACRO) "%.1f".format(Calificacion.nota(intento.aciertos, intento.total))
                    else "${intento.aciertos}/${intento.total}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorDominio(porcentaje)
                )
                Icon(
                    if (intento.sincronizado) Icons.Rounded.CloudDone else Icons.Rounded.CloudUpload,
                    contentDescription = if (intento.sincronizado) "Sincronizado" else "Pendiente de sincronizar",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
