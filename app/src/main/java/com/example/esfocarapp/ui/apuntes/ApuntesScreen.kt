package com.example.esfocarapp.ui.apuntes

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.data.local.ApunteLocal
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.CampoTexto
import com.example.esfocarapp.ui.components.EstadoVacio
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.viewmodel.ApuntesViewModel
import com.example.esfocarapp.viewmodel.Fabrica

@Composable
fun ApuntesScreen(onBack: () -> Unit, vm: ApuntesViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val apuntes by vm.apuntes.collectAsState()
    var porBorrar by remember { mutableStateOf<ApunteLocal?>(null) }
    val editando = vm.editando != null

    // Al abrir la pantalla se intenta sincronizar con el microservicio (si no hay red, se sigue trabajando local)
    LaunchedEffect(Unit) { vm.sincronizar() }

    Scaffold(
        topBar = {
            AppTopBar("Mis apuntes", onBack = onBack, acciones = {
                if (vm.sincronizando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    IconButton(onClick = vm::sincronizar) { Icon(Icons.Rounded.Sync, contentDescription = "Sincronizar") }
                }
            })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- Formulario (crear / editar) ----------
            item {
                Tarjeta {
                    Text(if (editando) "Editar apunte" else "Nuevo apunte", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = vm.titulo,
                        alCambiar = vm::cambiarTitulo,
                        etiqueta = "Título",
                        icono = Icons.Default.Edit,
                        error = vm.errorTitulo
                    )
                    CampoTexto(
                        valor = vm.contenido,
                        alCambiar = vm::cambiarContenido,
                        etiqueta = "Contenido",
                        icono = Icons.Default.Create,
                        error = vm.errorContenido,
                        unaLinea = false
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = vm::guardar, modifier = Modifier.weight(1f)) {
                            Text(if (editando) "Actualizar" else "Guardar apunte")
                        }
                        if (editando) {
                            OutlinedButton(onClick = vm::cancelarEdicion, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        }
                    }
                    AnimatedVisibility(visible = vm.mensaje != null) {
                        Text(
                            vm.mensaje ?: "",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            vm.estadoSync?.let { estado ->
                item {
                    Text(estado, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ---------- Lista ----------
            item { TituloSeccion("Apuntes guardados (${apuntes.size})") }
            if (apuntes.isEmpty()) {
                item {
                    EstadoVacio(
                        Icons.Rounded.EditNote, "Todavía no tienes apuntes",
                        "Escribe tus resúmenes y ideas clave. Se guardan aunque no tengas conexión."
                    )
                }
            }
            items(apuntes, key = { it.id }) { apunte ->
                Tarjeta(modifier = Modifier.animateItem()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(apunte.titulo, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(apunte.contenido, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (apunte.pendiente) Icons.Rounded.CloudUpload else Icons.Rounded.CloudDone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (apunte.pendiente) ColoresEstado.advertencia else ColoresEstado.exito
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${apunte.fecha} · ${if (apunte.pendiente) "Pendiente de sincronizar" else "Sincronizado"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { vm.editar(apunte) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Editar apunte")
                        }
                        IconButton(onClick = { porBorrar = apunte }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Eliminar apunte")
                        }
                    }
                }
            }
        }
    }

    porBorrar?.let { apunte ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            title = { Text("¿Eliminar apunte?") },
            text = { Text("\"${apunte.titulo}\" se eliminará del teléfono y del servidor.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.borrar(apunte)
                    porBorrar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porBorrar = null }) { Text("Cancelar") } }
        )
    }
}
