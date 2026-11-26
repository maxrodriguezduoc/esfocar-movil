package com.example.esfocarapp.ui.practica

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Grading
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.model.Dificultad
import com.example.esfocarapp.model.NivelActuacion
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.EstadoVacio
import com.example.esfocarapp.ui.components.Etiqueta
import com.example.esfocarapp.ui.components.InsigniaIcono
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TarjetaAccion
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.components.colorAsignatura
import com.example.esfocarapp.ui.components.iconoAsignatura
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.viewmodel.EstudioViewModel
import com.example.esfocarapp.viewmodel.Fabrica
import com.example.esfocarapp.viewmodel.ModoQuiz
import com.example.esfocarapp.viewmodel.ProgresoViewModel

// ------------------------------- Pestaña "Practicar" -------------------------------

@Composable
fun PracticarContent(estudio: EstudioViewModel, progreso: ProgresoViewModel, nav: Navegacion) {
    val asignaturas by estudio.asignaturas.collectAsState()
    val panel by progreso.panel.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Tarjeta(onClick = nav::simulacro, color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InsigniaIcono(Icons.Rounded.Grading, MaterialTheme.colorScheme.onSecondaryContainer, 52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Simulacro de evaluación", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "15 preguntas con tiempo y nota final. Ideal antes de una prueba.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        panel.mejorNota?.let {
                            Spacer(Modifier.height(4.dp))
                            Text("Tu mejor nota: $it", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaAccion(
                    Icons.Rounded.Bolt, "Trivia rápida", "10 preguntas al azar",
                    MaterialTheme.colorScheme.primary, { nav.quiz(ModoQuiz.RAPIDA, 0) }, Modifier.weight(1f)
                )
                TarjetaAccion(
                    Icons.Rounded.Replay, "Repasar errores",
                    if (panel.errores == 0) "Sin errores pendientes" else "${panel.errores} preguntas por corregir",
                    MaterialTheme.colorScheme.error, { nav.quiz(ModoQuiz.ERRORES, 0) }, Modifier.weight(1f),
                    contador = panel.errores
                )
            }
        }
        item {
            Tarjeta(onClick = nav::casos, color = MaterialTheme.colorScheme.tertiaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InsigniaIcono(Icons.Rounded.Psychology, MaterialTheme.colorScheme.onTertiaryContainer, 52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Casos simulados", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Analiza situaciones y elige cómo actuar. ${panel.casosResueltos} de ${panel.totalCasos} resueltos.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
        item { TituloSeccion("Trivia por asignatura") }
        items(asignaturas, key = { it.asignatura.id }) { avance ->
            val a = avance.asignatura
            val color = colorAsignatura(a.color)
            Tarjeta(onClick = { nav.quiz(ModoQuiz.ASIGNATURA, a.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InsigniaIcono(iconoAsignatura(a.icono), color, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(a.nombre, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

// ------------------------------- Configurar simulacro -------------------------------

@Composable
fun SimulacroScreen(nav: Navegacion, estudio: EstudioViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val asignaturas by estudio.asignaturas.collectAsState()
    var elegida by rememberSaveable { mutableLongStateOf(0L) }

    Scaffold(
        topBar = { AppTopBar("Simulacro de evaluación", onBack = nav::volver) },
        bottomBar = {
            Button(
                onClick = { nav.quiz(ModoQuiz.SIMULACRO, elegida) },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
            ) {
                Icon(Icons.Rounded.Timer, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Comenzar simulacro")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Tarjeta(color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text("Cómo funciona", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Regla(Icons.Rounded.HelpOutline, "${ModoQuiz.PREGUNTAS_SIMULACRO} preguntas al azar de la asignatura elegida.")
                    Regla(
                        Icons.Rounded.Schedule,
                        "${ModoQuiz.PREGUNTAS_SIMULACRO * ModoQuiz.SEGUNDOS_POR_PREGUNTA / 60} minutos en total. Al terminar el tiempo, el simulacro se cierra."
                    )
                    Regla(Icons.Rounded.Grading, "Nota de 1.0 a 7.0 con 60 % de exigencia.")
                    Regla(Icons.Rounded.CheckCircle, "Verás las respuestas correctas y la explicación al final.")
                }
            }
            item { TituloSeccion("Elige el contenido") }
            item { OpcionRadio("General (todas las asignaturas)", elegida == 0L) { elegida = 0L } }
            items(asignaturas, key = { it.asignatura.id }) { avance ->
                OpcionRadio(avance.asignatura.nombre, elegida == avance.asignatura.id) { elegida = avance.asignatura.id }
            }
        }
    }
}

@Composable
private fun Regla(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(Modifier.width(10.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun OpcionRadio(texto: String, seleccionada: Boolean, onClick: () -> Unit) {
    Tarjeta(
        modifier = Modifier.selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton),
        color = if (seleccionada) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        relleno = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = seleccionada, onClick = null, modifier = Modifier.padding(12.dp))
            Text(texto, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// ------------------------------- Listado de casos simulados -------------------------------

@Composable
fun CasosScreen(nav: Navegacion, vm: EstudioViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val casos by vm.casos.collectAsState()
    var filtro by rememberSaveable { mutableStateOf("TODOS") }
    val filtros = listOf("TODOS" to "Todos", "PENDIENTES" to "Pendientes") +
        Dificultad.entries.map { it.name to it.etiqueta }

    val visibles = casos.filter {
        when (filtro) {
            "TODOS" -> true
            "PENDIENTES" -> it.mejorPuntaje != 2
            else -> it.caso.dificultad == filtro
        }
    }

    Scaffold(topBar = { AppTopBar("Casos simulados", onBack = nav::volver) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Lee cada situación, elige la actuación que consideres correcta y revisa el fundamento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtros) { (clave, etiqueta) ->
                        FilterChip(selected = filtro == clave, onClick = { filtro = clave }, label = { Text(etiqueta) })
                    }
                }
            }
            if (visibles.isEmpty()) {
                item { EstadoVacio(Icons.Rounded.CheckCircle, "¡Nada por aquí!", "No hay casos con este filtro.") }
            }
            items(visibles, key = { it.caso.id }) { item ->
                val c = item.caso
                val colorAsig = item.asignatura?.let { colorAsignatura(it.color) } ?: MaterialTheme.colorScheme.primary
                Tarjeta(onClick = { nav.caso(c.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            item.asignatura?.nombre ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorAsig,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Etiqueta(Dificultad.de(c.dificultad).etiqueta, colorDificultad(c.dificultad))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(c.titulo, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        c.situacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    item.mejorPuntaje?.let { puntaje ->
                        Spacer(Modifier.height(10.dp))
                        val nivel = NivelActuacion.de(puntaje)
                        Etiqueta("Mejor intento: ${nivel.etiqueta}", colorNivel(nivel), Icons.Rounded.CheckCircle)
                    }
                }
            }
        }
    }
}

@Composable
fun colorDificultad(dificultad: String) = when (Dificultad.de(dificultad)) {
    Dificultad.BASICA -> ColoresEstado.exito
    Dificultad.MEDIA -> ColoresEstado.advertencia
    Dificultad.AVANZADA -> MaterialTheme.colorScheme.error
}

@Composable
fun colorNivel(nivel: NivelActuacion) = when (nivel) {
    NivelActuacion.OPTIMA -> ColoresEstado.exito
    NivelActuacion.ACEPTABLE -> ColoresEstado.advertencia
    NivelActuacion.INCORRECTA -> MaterialTheme.colorScheme.error
}
