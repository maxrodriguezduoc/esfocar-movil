package com.example.esfocarapp.ui.practica

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalPolice
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.model.Dificultad
import com.example.esfocarapp.model.NivelActuacion
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.EstadoVacio
import com.example.esfocarapp.ui.components.Etiqueta
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.util.vibrar
import com.example.esfocarapp.viewmodel.CasoViewModel
import com.example.esfocarapp.viewmodel.Fabrica

@Composable
fun CasoScreen(nav: Navegacion, vm: CasoViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val context = LocalContext.current
    Scaffold(topBar = { AppTopBar("Caso simulado", onBack = nav::volver) }) { padding ->
        val caso = vm.caso
        when {
            vm.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            caso == null -> EstadoVacio(
                Icons.Rounded.Info, "Caso no disponible", "Este caso ya no existe en el contenido actual.",
                Modifier.padding(padding), "Volver", nav::volver
            )
            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(caso.titulo, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    Etiqueta(Dificultad.de(caso.dificultad).etiqueta, colorDificultad(caso.dificultad))
                }

                // Situación planteada
                Tarjeta(color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalPolice, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Situación",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(caso.situacion, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }

                Text(caso.pregunta, style = MaterialTheme.typography.titleMedium)

                caso.opciones.forEachIndexed { i, opcion ->
                    Opcion(
                        texto = opcion.texto,
                        seleccionada = vm.seleccion == i,
                        revelada = vm.confirmado,
                        nivel = NivelActuacion.de(opcion.puntaje),
                        retroalimentacion = opcion.retroalimentacion,
                        onClick = { vm.elegir(i) }
                    )
                }

                AnimatedVisibility(visible = vm.confirmado, enter = fadeIn() + expandVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        vm.nivel?.let { nivel ->
                            val color = colorNivel(nivel)
                            Tarjeta(color = color.copy(alpha = 0.12f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (nivel == NivelActuacion.INCORRECTA) Icons.Rounded.Cancel else Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = color
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(nivel.etiqueta, style = MaterialTheme.typography.titleMedium, color = color)
                                }
                            }
                        }
                        if (caso.fundamento.isNotBlank()) {
                            Tarjeta(color = MaterialTheme.colorScheme.secondaryContainer) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Fundamento",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    caso.fundamento,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                if (!vm.confirmado) {
                    Button(
                        onClick = {
                            vm.confirmar()
                            vm.nivel?.let { vibrar(context, if (it == NivelActuacion.OPTIMA) 60 else 300) }
                        },
                        enabled = vm.seleccion != null,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) { Text("Confirmar actuación") }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = vm::reintentar, modifier = Modifier.weight(1f).height(52.dp)) {
                            Text("Reintentar")
                        }
                        Button(onClick = nav::volver, modifier = Modifier.weight(1f).height(52.dp)) { Text("Terminar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Opcion(
    texto: String,
    seleccionada: Boolean,
    revelada: Boolean,
    nivel: NivelActuacion,
    retroalimentacion: String,
    onClick: () -> Unit
) {
    val colorNivel = colorNivel(nivel)
    val fondoObjetivo = when {
        revelada && (seleccionada || nivel == NivelActuacion.OPTIMA) -> colorNivel.copy(alpha = 0.12f)
        seleccionada -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val bordeObjetivo = when {
        revelada && (seleccionada || nivel == NivelActuacion.OPTIMA) -> colorNivel
        seleccionada -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val fondo by animateColorAsState(fondoObjetivo, label = "fondo")
    val borde by animateColorAsState(bordeObjetivo, label = "borde")

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(if (seleccionada || revelada) 2.dp else 1.dp, borde),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = seleccionada, onClick = null)
                Spacer(Modifier.width(8.dp))
                Text(texto, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            }
            if (revelada) {
                Spacer(Modifier.height(8.dp))
                Etiqueta(nivel.etiqueta, colorNivel)
                Spacer(Modifier.height(4.dp))
                Text(retroalimentacion, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
