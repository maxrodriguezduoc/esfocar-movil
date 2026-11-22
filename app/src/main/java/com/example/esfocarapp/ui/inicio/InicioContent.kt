package com.example.esfocarapp.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.model.Dificultad
import com.example.esfocarapp.ui.components.BarraProgreso
import com.example.esfocarapp.ui.components.ChipConexion
import com.example.esfocarapp.ui.components.DatoEncabezado
import com.example.esfocarapp.ui.components.Etiqueta
import com.example.esfocarapp.ui.components.FondoInstitucional
import com.example.esfocarapp.ui.components.InsigniaIcono
import com.example.esfocarapp.ui.components.LogoEsfocar
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TarjetaAccion
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.components.colorAsignatura
import com.example.esfocarapp.ui.components.colorDominio
import com.example.esfocarapp.ui.components.iconoAsignatura
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.ui.theme.VerdeProfundo
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.viewmodel.EstudioViewModel
import com.example.esfocarapp.viewmodel.ModoQuiz
import com.example.esfocarapp.viewmodel.ProgresoViewModel
import java.time.LocalTime

@Composable
fun InicioContent(
    vm: ProgresoViewModel,
    estudio: EstudioViewModel,
    nav: Navegacion,
    irAEstudiar: () -> Unit,
    irAProgreso: () -> Unit
) {
    val panel by vm.panel.collectAsState()
    val caso by vm.casoDelDia.collectAsState()
    val enLinea by vm.enLinea.collectAsState()
    val asignaturas by estudio.asignaturas.collectAsState()

    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- Encabezado institucional ----------
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(FondoInstitucional())
                    .statusBarsPadding()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoEsfocar(alto = 52.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(saludo(), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        Text(
                            Sesion.alias ?: "Estudiante",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    ChipConexion(enLinea, sobreFondoOscuro = true)
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DatoEncabezado(
                        "${panel.racha}", if (panel.racha == 1) "día de racha" else "días de racha",
                        Icons.Rounded.LocalFireDepartment, Modifier.weight(1f)
                    )
                    DatoEncabezado(
                        panel.promedio?.let { "$it%" } ?: "—", "promedio", Icons.Rounded.TaskAlt, Modifier.weight(1f)
                    )
                    DatoEncabezado(
                        "${panel.actividades}", "actividades", Icons.Rounded.EmojiEvents, Modifier.weight(1f)
                    )
                }
                if (!enLinea) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Sin conexión: puedes seguir estudiando. Tu avance se sincronizará después.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // ---------- Caso del día ----------
        caso?.let { c ->
            item {
                Tarjeta(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = { nav.caso(c.id) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(Icons.Rounded.Psychology, MaterialTheme.colorScheme.onTertiaryContainer, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "CASO DEL DÍA",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                c.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Etiqueta(Dificultad.de(c.dificultad).etiqueta, MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        c.situacion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(onClick = { nav.caso(c.id) }) {
                        Text("Analizar situación")
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        // ---------- Accesos rápidos ----------
        item { TituloSeccion("Practica ahora", Modifier.padding(horizontal = 16.dp)) }
        item {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaAccion(
                        Icons.Rounded.Bolt, "Trivia rápida", "10 preguntas al azar de todas las asignaturas",
                        MaterialTheme.colorScheme.primary, { nav.quiz(ModoQuiz.RAPIDA, 0) }, Modifier.weight(1f)
                    )
                    TarjetaAccion(
                        Icons.Rounded.Timer, "Simulacro", "Prueba con tiempo y nota de 1.0 a 7.0",
                        MaterialTheme.colorScheme.secondary, { nav.simulacro() }, Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TarjetaAccion(
                        Icons.Rounded.Psychology, "Casos simulados",
                        "${panel.casosResueltos} de ${panel.totalCasos} resueltos",
                        MaterialTheme.colorScheme.tertiary, { nav.casos() }, Modifier.weight(1f)
                    )
                    TarjetaAccion(
                        Icons.Rounded.Replay, "Repasar errores",
                        if (panel.errores == 0) "No tienes errores pendientes" else "Preguntas que fallaste",
                        MaterialTheme.colorScheme.error, { nav.quiz(ModoQuiz.ERRORES, 0) }, Modifier.weight(1f),
                        contador = panel.errores
                    )
                }
            }
        }

        // ---------- Temas débiles ----------
        if (panel.debiles.isNotEmpty()) {
            item { TituloSeccion("Temas para reforzar", Modifier.padding(horizontal = 16.dp), "Ver progreso", irAProgreso) }
            items(panel.debiles, key = { "debil-${it.unidad.id}" }) { tema ->
                Tarjeta(modifier = Modifier.padding(horizontal = 16.dp), onClick = { nav.unidad(tema.unidad.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(Icons.Rounded.TrendingDown, MaterialTheme.colorScheme.error, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tema.unidad.titulo, style = MaterialTheme.typography.titleSmall)
                            Text(
                                tema.asignatura,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${tema.porcentaje}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorDominio(tema.porcentaje)
                        )
                    }
                }
            }
        }

        // ---------- Asignaturas ----------
        item { TituloSeccion("Tus asignaturas", Modifier.padding(horizontal = 16.dp), "Ver todas", irAEstudiar) }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(asignaturas, key = { it.asignatura.id }) { avance ->
                    val color = colorAsignatura(avance.asignatura.color)
                    Tarjeta(modifier = Modifier.width(220.dp), onClick = { nav.asignatura(avance.asignatura.id) }) {
                        InsigniaIcono(iconoAsignatura(avance.asignatura.icono), color, 44.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            avance.asignatura.nombre,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        BarraProgreso((avance.porcentaje ?: 0) / 100f, color)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            avance.porcentaje?.let { "Dominio $it%" } ?: "Sin práctica aún",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------- Pie ----------
        item {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (panel.racha > 0) "¡Sigue así! Llevas ${panel.racha} ${if (panel.racha == 1) "día" else "días"} estudiando seguido."
                    else "Completa una actividad hoy para iniciar tu racha.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (panel.racha > 0) ColoresEstado.exito else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    // Franja fija bajo la barra de estado: al desplazar, el contenido no queda debajo de la hora y los íconos
    Spacer(
        Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(VerdeProfundo)
    )
    }
}

private fun saludo(): String {
    val hora = LocalTime.now().hour
    return when {
        hora < 12 -> "Buenos días,"
        hora < 20 -> "Buenas tardes,"
        else -> "Buenas noches,"
    }
}
