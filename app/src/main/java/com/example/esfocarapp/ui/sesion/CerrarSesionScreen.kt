package com.example.esfocarapp.ui.sesion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.LogoEsfocar
import com.example.esfocarapp.util.Sesion

// Vista de cierre de sesión: pide confirmación antes de salir
@Composable
fun CerrarSesionScreen(onCancelar: () -> Unit, onConfirmar: () -> Unit) {
    Scaffold(topBar = { AppTopBar("Cerrar sesión", onBack = onCancelar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            LogoEsfocar(alto = 140.dp)
            Text(
                text = "¿Quieres cerrar sesión, ${Sesion.alias ?: "estudiante"}?",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Tus apuntes, documentos y resultados quedan guardados en este dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onConfirmar, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
            OutlinedButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}
