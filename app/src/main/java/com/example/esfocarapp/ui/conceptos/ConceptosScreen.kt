package com.example.esfocarapp.ui.conceptos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.CampoTexto
import com.example.esfocarapp.viewmodel.ConceptosViewModel
import com.example.esfocarapp.viewmodel.Fabrica

// Pantalla que consume la API externa (Wikipedia) para repasar el significado de un concepto
@Composable
fun ConceptosScreen(onBack: () -> Unit, vm: ConceptosViewModel = viewModel(factory = Fabrica.Proveedor)) {
    Scaffold(topBar = { AppTopBar("Explorar conceptos", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Busca un concepto y revisa su resumen en Wikipedia (requiere internet).",
                style = MaterialTheme.typography.bodyMedium
            )

            CampoTexto(
                valor = vm.termino,
                alCambiar = vm::cambiarTermino,
                etiqueta = "Concepto (ej: Primeros auxilios)",
                icono = Icons.Default.Search,
                error = vm.errorTermino
            )

            Button(
                onClick = vm::buscar,
                enabled = !vm.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Buscar")
            }

            if (vm.cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            vm.mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            vm.concepto?.let { concepto ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(concepto.title ?: "Sin título", style = MaterialTheme.typography.titleLarge)
                        concepto.description?.let {
                            Text(it, style = MaterialTheme.typography.labelLarge)
                        }
                        Text(
                            concepto.extract ?: "Este artículo no tiene resumen disponible.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text("Fuente: Wikipedia", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
