package com.example.esfocarapp.ui.documentos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.data.local.Documento
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.util.Documentos
import com.example.esfocarapp.util.Validador
import com.example.esfocarapp.viewmodel.DocumentosViewModel

@Composable
fun DocumentosScreen(onBack: () -> Unit, vm: DocumentosViewModel = viewModel()) {
    val context = LocalContext.current
    val documentos by vm.documentos.collectAsState(initial = emptyList<Documento>())

    // Selector de archivos del teléfono (no necesita permisos de almacenamiento)
    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.agregar(uri)
    }

    Scaffold(topBar = { AppTopBar("Documentos", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Carga archivos PDF o Word. En el futuro aquí estarán las rúbricas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // ---------- CARGAR ----------
            item {
                Button(
                    onClick = { selector.launch(Validador.tiposDocumento.toTypedArray()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("  Cargar documento")
                }
            }

            // Animación: el aviso aparece y desaparece suavemente
            item {
                AnimatedVisibility(visible = vm.mensaje != null) {
                    Text(vm.mensaje ?: "", color = MaterialTheme.colorScheme.secondary)
                }
            }

            item { Text("Mis documentos", style = MaterialTheme.typography.titleLarge) }

            if (documentos.isEmpty()) {
                item { Text("Todavía no has cargado documentos.") }
            }

            items(documentos, key = { it.id }) { documento ->
                Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(documento.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${Documentos.etiquetaTipo(documento.tipo)} · ${Documentos.formatearTamano(documento.tamano)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(documento.fecha, style = MaterialTheme.typography.labelMedium)
                        }

                        // ---------- VISUALIZAR ----------
                        TextButton(
                            onClick = {
                                if (!Documentos.abrir(context, documento)) {
                                    vm.avisar("No se pudo abrir: instala una app lectora de PDF/Word")
                                }
                            }
                        ) {
                            Text("Ver")
                        }

                        // ---------- ELIMINAR ----------
                        IconButton(onClick = { vm.borrar(documento) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar documento")
                        }
                    }
                }
            }
        }
    }
}
