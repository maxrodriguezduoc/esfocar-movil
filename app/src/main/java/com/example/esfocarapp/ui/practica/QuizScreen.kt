package com.example.esfocarapp.ui.practica

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.model.Calificacion
import com.example.esfocarapp.ui.components.AnilloProgreso
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.BarraProgreso
import com.example.esfocarapp.ui.components.EstadoVacio
import com.example.esfocarapp.ui.components.Etiqueta
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.util.vibrar
import com.example.esfocarapp.viewmodel.Fabrica
import com.example.esfocarapp.viewmodel.ModoQuiz
import com.example.esfocarapp.viewmodel.QuizViewModel
import kotlinx.coroutines.delay

@Composable
fun QuizScreen(nav: Navegacion, vm: QuizViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val context = LocalContext.current
    var confirmarSalida by remember { mutableStateOf(false) }
    val enCurso = !vm.cargando && !vm.terminado && !vm.vacio && vm.respuestas.isNotEmpty()
    val salir = { if (enCurso) confirmarSalida = true else nav.volver() }

    // Cronómetro del simulacro
    LaunchedEffect(vm.cargando, vm.terminado) {
        while (!vm.cargando && !vm.terminado && vm.tiempoRestante != null) {
            delay(1_000)
            vm.descontarSegundo()
        }
    }

    Scaffold(
        topBar = { AppTopBar(vm.titulo.ifBlank { "Autoevaluación" }, onBack = salir) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                vm.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                vm.vacio -> EstadoVacio(
                    icono = Icons.Rounded.TaskAlt,
                    titulo = if (vm.modo == ModoQuiz.ERRORES) "¡No tienes errores pendientes!" else "Aún no hay preguntas",
                    mensaje = if (vm.modo == ModoQuiz.ERRORES) "Las preguntas que respondas mal aparecerán aquí para repasarlas."
                    else "Esta sección todavía no tiene preguntas disponibles.",
                    textoAccion = "Volver",
                    onAccion = nav::volver,
                    modifier = Modifier.align(Alignment.Center)
                )
                vm.terminado -> ResultadoQuiz(vm, nav)
                else -> PreguntaEnCurso(vm) { correcta ->
                    // Recurso nativo: vibración corta si acierta, larga si falla (solo con retroalimentación inmediata)
                    if (vm.modo.retroalimentacionInmediata) vibrar(context, if (correcta) 60 else 300)
                }
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Salir de la actividad?") },
            text = { Text("Perderás el avance de esta actividad. Las respuestas ya dadas quedan registradas.") },
            confirmButton = { TextButton(onClick = { confirmarSalida = false; nav.volver() }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Continuar") } }
        )
    }
}

@Composable
private fun PreguntaEnCurso(vm: QuizViewModel, alResponder: (Boolean) -> Unit) {
    val pregunta = vm.preguntaActual ?: return
    val total = vm.preguntas.size
    val hechas = vm.indice + if (vm.respondida) 1 else 0

    val desplazamiento = rememberScrollState()
    // Al responder, se baja automáticamente hasta la retroalimentación (en pantallas pequeñas queda fuera de vista)
    LaunchedEffect(vm.respondida) {
        if (vm.respondida) {
            delay(250)
            desplazamiento.animateScrollTo(desplazamiento.maxValue)
        } else {
            desplazamiento.scrollTo(0)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(desplazamiento).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Pregunta ${vm.indice + 1} de $total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                vm.tiempoRestante?.let { segundos ->
                    val urgente = segundos <= 60
                    Etiqueta(
                        "%d:%02d".format(segundos / 60, segundos % 60),
                        if (urgente) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        Icons.Rounded.Timer
                    )
                }
            }
            BarraProgreso(hechas.toFloat() / total)
            Spacer(Modifier.height(4.dp))
            Text(pregunta.enunciado, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))

            pregunta.alternativas.forEachIndexed { i, alternativa ->
                val elegida = vm.seleccion == i
                val estado = when {
                    !vm.respondida -> if (elegida) EstadoAlt.SELECCIONADA else EstadoAlt.NORMAL
                    alternativa.esCorrecta -> EstadoAlt.CORRECTA
                    elegida -> EstadoAlt.INCORRECTA
                    else -> EstadoAlt.NORMAL
                }
                Alternativa(letra = ('A' + i).toString(), texto = alternativa.texto, estado = estado) {
                    vm.seleccionar(i)
                }
            }

            // Retroalimentación inmediata
            val ultima = vm.respuestas.lastOrNull()
            AnimatedVisibility(visible = vm.respondida && ultima != null, enter = fadeIn() + expandVertically()) {
                if (ultima != null) {
                    val ok = ultima.correcta
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (ok) ColoresEstado.exitoContenedor else MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = if (ok) ColoresEstado.exito else MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (ok) "¡Correcto!" else "Incorrecto", style = MaterialTheme.typography.titleMedium)
                            }
                            ultima.elegida?.let { Text(ultima.pregunta.alternativas[it].retroalimentacion) }
                            if (pregunta.explicacion.isNotBlank()) {
                                Text(
                                    pregunta.explicacion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (vm.respondida) vm.siguiente() else vm.responder()?.let(alResponder)
            },
            enabled = vm.respondida || vm.seleccion != null,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
        ) {
            Text(
                when {
                    !vm.respondida && vm.modo.retroalimentacionInmediata -> "Responder"
                    !vm.respondida -> if (vm.esUltima) "Responder y terminar" else "Responder y continuar"
                    vm.esUltima -> "Ver resultado"
                    else -> "Siguiente pregunta"
                }
            )
        }
    }
}

private enum class EstadoAlt { NORMAL, SELECCIONADA, CORRECTA, INCORRECTA }

@Composable
private fun Alternativa(letra: String, texto: String, estado: EstadoAlt, onClick: () -> Unit) {
    val fondoObjetivo = when (estado) {
        EstadoAlt.NORMAL -> MaterialTheme.colorScheme.surfaceContainerLow
        EstadoAlt.SELECCIONADA -> MaterialTheme.colorScheme.primaryContainer
        EstadoAlt.CORRECTA -> ColoresEstado.exitoContenedor
        EstadoAlt.INCORRECTA -> MaterialTheme.colorScheme.errorContainer
    }
    val bordeObjetivo = when (estado) {
        EstadoAlt.NORMAL -> MaterialTheme.colorScheme.outlineVariant
        EstadoAlt.SELECCIONADA -> MaterialTheme.colorScheme.primary
        EstadoAlt.CORRECTA -> ColoresEstado.exito
        EstadoAlt.INCORRECTA -> MaterialTheme.colorScheme.error
    }
    // Animación: el color cambia suavemente al seleccionar y al revelar la respuesta
    val fondo by animateColorAsState(fondoObjetivo, label = "fondo")
    val borde by animateColorAsState(bordeObjetivo, label = "borde")

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(if (estado == EstadoAlt.NORMAL) 1.dp else 2.dp, borde),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(borde.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    letra,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (estado == EstadoAlt.NORMAL) MaterialTheme.colorScheme.onSurfaceVariant else borde
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            when (estado) {
                EstadoAlt.CORRECTA -> Icon(Icons.Rounded.CheckCircle, contentDescription = "Correcta", tint = ColoresEstado.exito)
                EstadoAlt.INCORRECTA -> Icon(Icons.Rounded.Cancel, contentDescription = "Incorrecta", tint = MaterialTheme.colorScheme.error)
                else -> {}
            }
        }
    }
}

@Composable
private fun ResultadoQuiz(vm: QuizViewModel, nav: Navegacion) {
    val esSimulacro = vm.modo == ModoQuiz.SIMULACRO
    val nota = vm.nota
    val aprobado = Calificacion.aprobado(nota)
    val colorResultado = when {
        vm.porcentaje >= 80 -> ColoresEstado.exito
        vm.porcentaje >= 60 -> ColoresEstado.advertencia
        else -> MaterialTheme.colorScheme.error
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(8.dp))
                AnilloProgreso(vm.porcentaje / 100f, tamano = 160.dp, grosor = 14.dp, color = colorResultado) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (esSimulacro) {
                            Text("%.1f".format(nota), style = MaterialTheme.typography.displaySmall, color = colorResultado)
                            Text("nota", style = MaterialTheme.typography.labelMedium)
                        } else {
                            Text("${vm.porcentaje}%", style = MaterialTheme.typography.displaySmall, color = colorResultado)
                            Text("logro", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(mensajeResultado(vm.porcentaje), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${vm.aciertos} de ${vm.preguntas.size} respuestas correctas" +
                        if (esSimulacro) " · ${if (aprobado) "Aprobado" else "Reprobado"}" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (vm.tiempoRestante == 0) {
                    Spacer(Modifier.height(8.dp))
                    Etiqueta("Se acabó el tiempo", MaterialTheme.colorScheme.error, Icons.Rounded.Timer)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = nav::volver, modifier = Modifier.weight(1f).height(52.dp)) { Text("Volver") }
                Button(onClick = vm::reiniciar, modifier = Modifier.weight(1f).height(52.dp)) { Text("Reintentar") }
            }
        }

        if (vm.errores.isEmpty()) {
            item {
                Tarjeta(color = ColoresEstado.exitoContenedor) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = ColoresEstado.exito)
                        Spacer(Modifier.width(8.dp))
                        Text("¡Respondiste todo correctamente!", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        } else {
            item { TituloSeccion("Revisa tus errores") }
            itemsIndexed(vm.errores) { _, error ->
                Tarjeta {
                    Text(error.pregunta.enunciado, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    val elegida = error.elegida?.let { error.pregunta.alternativas[it].texto } ?: "Sin responder"
                    LineaRespuesta(Icons.Rounded.Cancel, "Tu respuesta: $elegida", MaterialTheme.colorScheme.error)
                    error.pregunta.alternativas.firstOrNull { it.esCorrecta }?.let {
                        LineaRespuesta(Icons.Rounded.CheckCircle, "Correcta: ${it.texto}", ColoresEstado.exito)
                    }
                    if (error.pregunta.explicacion.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            error.pregunta.explicacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LineaRespuesta(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String, color: Color) {
    Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun mensajeResultado(porcentaje: Int): String = when {
    porcentaje >= 90 -> "¡Excelente trabajo!"
    porcentaje >= 70 -> "¡Muy bien!"
    porcentaje >= 60 -> "Vas por buen camino"
    else -> "Sigue practicando"
}
