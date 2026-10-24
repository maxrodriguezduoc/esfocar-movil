package com.example.esfocarapp.ui.trivia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.LogoEsfocar
import com.example.esfocarapp.util.vibrar
import com.example.esfocarapp.viewmodel.TriviaViewModel

// Pantalla completa de trivia (se abre al tocar una asignatura)
@Composable
fun TriviaScreen(titulo: String, onBack: () -> Unit, vm: TriviaViewModel = viewModel()) {
    Scaffold(topBar = { AppTopBar(titulo, onBack = onBack) }) { padding ->
        TriviaContent(
            vm = vm,
            actividad = titulo,
            textoFinal = "Volver",
            onFinal = onBack,
            modifier = Modifier.padding(padding)
        )
    }
}

// Contenido de la trivia (se usa también en la pestaña "Trivia" de la barra inferior)
@Composable
fun TriviaContent(
    vm: TriviaViewModel,
    actividad: String,
    textoFinal: String,
    onFinal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Animación: la barra de progreso avanza suavemente
    val preguntasHechas = vm.indice + if (vm.respondida) 1 else 0
    val progreso by animateFloatAsState(
        targetValue = preguntasHechas.toFloat() / vm.preguntas.size,
        animationSpec = tween(durationMillis = 500),
        label = "progreso trivia"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (vm.terminada) {
            // ---------- Pantalla de resultado ----------
            LogoEsfocar(modifier = Modifier.align(Alignment.CenterHorizontally), alto = 100.dp)
            Text("¡Actividad terminada!", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Puntaje: ${vm.aciertos} de ${vm.preguntas.size}",
                style = MaterialTheme.typography.titleLarge
            )
            Button(
                onClick = {
                    vm.reiniciar()
                    onFinal()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(textoFinal)
            }
        } else {
            // ---------- Pregunta actual ----------
            val pregunta = vm.preguntaActual

            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
            Text(
                "Pregunta ${vm.indice + 1} de ${vm.preguntas.size}",
                style = MaterialTheme.typography.labelLarge
            )
            Text(pregunta.enunciado, style = MaterialTheme.typography.titleMedium)

            pregunta.alternativas.forEachIndexed { posicion, alternativa ->
                // Color de cada alternativa según el estado
                val colorObjetivo = when {
                    !vm.respondida && vm.seleccion == posicion -> MaterialTheme.colorScheme.primaryContainer
                    !vm.respondida -> MaterialTheme.colorScheme.surfaceVariant
                    alternativa.esCorrecta -> MaterialTheme.colorScheme.secondaryContainer
                    vm.seleccion == posicion -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                // Animación: el color cambia suavemente
                val color by animateColorAsState(targetValue = colorObjetivo, label = "color alternativa")

                Card(
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !vm.respondida) { vm.seleccionar(posicion) }
                ) {
                    Text(alternativa.texto, modifier = Modifier.padding(16.dp))
                }
            }

            // Animación: la retroalimentación aparece al responder
            AnimatedVisibility(visible = vm.respondida) {
                val elegida = vm.seleccion ?: 0
                Text(
                    pregunta.alternativas[elegida].retroalimentacion,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (vm.respondida) {
                Button(
                    onClick = { vm.siguiente(actividad) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (vm.indice == vm.preguntas.size - 1) "Ver resultado" else "Siguiente")
                }
            } else {
                Button(
                    onClick = {
                        val correcta = vm.responder()
                        // Recurso nativo: vibración corta si acierta, larga si falla
                        if (correcta) vibrar(context, 60) else vibrar(context, 300)
                    },
                    enabled = vm.seleccion != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Responder")
                }
            }
        }
    }
}
