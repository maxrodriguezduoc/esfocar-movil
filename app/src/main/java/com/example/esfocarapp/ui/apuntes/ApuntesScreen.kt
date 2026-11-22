package com.example.esfocarapp.ui.apuntes

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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.CampoTexto
import com.example.esfocarapp.viewmodel.ApuntesViewModel
import com.example.esfocarapp.viewmodel.Fabrica

@Composable
fun ApuntesScreen(onBack: () -> Unit, vm: ApuntesViewModel = viewModel(factory = Fabrica.Proveedor)) {
    // Al abrir la pantalla se leen los apuntes desde el microservicio
    LaunchedEffect(Unit) { vm.cargar() }

    val editando = vm.editandoId != null

    Scaffold(topBar = { AppTopBar("Mis apuntes", onBack = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- Formulario (crear / editar) ----------
            item {
                Text(
                    if (editando) "Editando apunte" else "Nuevo apunte",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                CampoTexto(
                    valor = vm.titulo,
                    alCambiar = vm::cambiarTitulo,
                    etiqueta = "Título",
                    icono = Icons.Default.Edit,
                    error = vm.errorTitulo
                )
            }
            item {
                CampoTexto(
                    valor = vm.contenido,
                    alCambiar = vm::cambiarContenido,
                    etiqueta = "Contenido",
                    icono = Icons.Default.Create,
                    error = vm.errorContenido,
                    unaLinea = false
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = vm::guardar,
                        enabled = !vm.cargando,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (editando) "Actualizar apunte" else "Guardar apunte")
                    }
                    if (editando) {
                        OutlinedButton(onClick = vm::cancelarEdicion, modifier = Modifier.weight(1f)) {
                            Text("Cancelar")
                        }
                    }
                }
            }
            item {
                AnimatedVisibility(visible = vm.mensaje != null) {
                    Text(vm.mensaje ?: "", color = MaterialTheme.colorScheme.secondary)
                }
            }
            if (vm.cargando) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            // ---------- Lista de apuntes guardados en el microservicio ----------
            item { Text("Apuntes guardados", style = MaterialTheme.typography.titleLarge) }

            if (vm.apuntes.isEmpty() && !vm.cargando) {
                item { Text("Todavía no has guardado apuntes.") }
            }

            items(vm.apuntes, key = { it.id }) { apunte ->
                Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(apunte.titulo, style = MaterialTheme.typography.titleMedium)
                            Text(apunte.contenido, style = MaterialTheme.typography.bodyMedium)
                            Text(apunte.fecha, style = MaterialTheme.typography.labelMedium)
                        }
                        IconButton(onClick = { vm.editar(apunte) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar apunte")
                        }
                        IconButton(onClick = { vm.borrar(apunte) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar apunte")
                        }
                    }
                }
            }
        }
    }
}
