package com.example.esfocarapp.ui.asignaturas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.model.Asignatura
import com.example.esfocarapp.ui.components.LogoEsfocar
import com.example.esfocarapp.util.Sesion

@Composable
fun AsignaturasContent(
    asignaturas: List<Asignatura>,
    actividadesCompletadas: Int,
    onAsignatura: (Int) -> Unit,
    onApuntes: () -> Unit,
    onDocumentos: () -> Unit
) {
    val meta = 5
    // Animación: la barra de avance se llena suavemente
    val avance by animateFloatAsState(
        targetValue = (actividadesCompletadas.toFloat() / meta).coerceAtMost(1f),
        animationSpec = tween(durationMillis = 800),
        label = "avance"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { LogoEsfocar(modifier = Modifier.fillMaxWidth(), alto = 120.dp) }

        item {
            Text(
                text = "Hola, ${Sesion.alias ?: "estudiante"}",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            Text("Modo sin conexión disponible", style = MaterialTheme.typography.labelLarge)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Tu avance", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { avance }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text("$actividadesCompletadas de $meta actividades completadas")
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onApuntes, modifier = Modifier.weight(1f)) {
                    Text("Mis apuntes")
                }
                OutlinedButton(onClick = onDocumentos, modifier = Modifier.weight(1f)) {
                    Text("Documentos")
                }
            }
        }

        item { Text("Asignaturas", style = MaterialTheme.typography.titleLarge) }

        items(asignaturas) { asignatura ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAsignatura(asignatura.id) }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(asignatura.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(asignatura.descripcion, style = MaterialTheme.typography.bodyMedium)
                    Text("${asignatura.unidades} unidades", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
