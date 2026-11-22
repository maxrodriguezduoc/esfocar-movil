package com.example.esfocarapp.ui.mas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.BuildConfig
import com.example.esfocarapp.ui.components.InsigniaIcono
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.theme.VerdeInstitucional
import com.example.esfocarapp.util.Sesion
import com.example.esfocarapp.util.Tema
import com.example.esfocarapp.viewmodel.AjustesViewModel

@Composable
fun MasContent(vm: AjustesViewModel, nav: Navegacion) {
    val alias = Sesion.alias ?: "Estudiante"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- Perfil ----------
        item {
            Tarjeta(color = MaterialTheme.colorScheme.primaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(56.dp).clip(CircleShape).background(VerdeInstitucional),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            alias.take(2).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(alias, style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (Sesion.esDocente) "Docente" else "Estudiante en formación",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // ---------- Modo docente ----------
        if (Sesion.esDocente) {
            item {
                Tarjeta(onClick = nav::panelDocente, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(Icons.Rounded.School, MaterialTheme.colorScheme.onSecondaryContainer, 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Panel docente", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Publica, edita y elimina preguntas y casos simulados para tus estudiantes",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        // ---------- Herramientas ----------
        item { TituloSeccion("Herramientas de estudio") }
        item {
            Tarjeta(relleno = PaddingValues(vertical = 4.dp)) {
                Opcion(Icons.Rounded.EditNote, "Mis apuntes", "Se guardan sin conexión y se sincronizan", nav::apuntes)
                HorizontalDivider(Modifier.padding(start = 72.dp))
                Opcion(Icons.Rounded.Bookmark, "Fichas guardadas", "Tus fichas marcadas para repasar", nav::favoritos)
                HorizontalDivider(Modifier.padding(start = 72.dp))
                Opcion(Icons.Rounded.Description, "Documentos", "Tus archivos PDF y Word", nav::documentos)
                HorizontalDivider(Modifier.padding(start = 72.dp))
                Opcion(Icons.Rounded.Public, "Explorar conceptos", "Definiciones desde Wikipedia (requiere internet)", nav::conceptos)
            }
        }

        // ---------- Preferencias ----------
        item { TituloSeccion("Preferencias") }
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InsigniaIcono(Icons.Rounded.Palette, MaterialTheme.colorScheme.tertiary, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Apariencia", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(12.dp))
                val opciones = listOf(Tema.SISTEMA to "Sistema", Tema.CLARO to "Claro", Tema.OSCURO to "Oscuro")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    opciones.forEachIndexed { i, (tema, etiqueta) ->
                        SegmentedButton(
                            selected = vm.tema == tema,
                            onClick = { vm.cambiarTema(tema) },
                            shape = SegmentedButtonDefaults.itemShape(i, opciones.size)
                        ) { Text(etiqueta) }
                    }
                }
            }
        }
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InsigniaIcono(Icons.Rounded.Sync, MaterialTheme.colorScheme.primary, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sincronización", style = MaterialTheme.typography.titleSmall)
                        Text(
                            vm.mensajeSync ?: "Contenido, apuntes y resultados se sincronizan automáticamente al tener internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(onClick = vm::sincronizarAhora, enabled = !vm.sincronizando, modifier = Modifier.fillMaxWidth()) {
                    if (vm.sincronizando) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Sincronizando...")
                    } else {
                        Text("Sincronizar ahora")
                    }
                }
            }
        }

        // ---------- Acerca de ----------
        item { TituloSeccion("Acerca de") }
        item {
            Tarjeta {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("ESFOCAR Estudio ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Herramienta complementaria de estudio y autoevaluación para estudiantes en formación. " +
                                "El contenido, las normas y los casos son ficticios y de uso académico.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            Tarjeta(onClick = nav::cerrarSesion, color = MaterialTheme.colorScheme.errorContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(Modifier.width(12.dp))
                    Text("Cerrar sesión", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Composable
private fun Opcion(icono: ImageVector, titulo: String, descripcion: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(titulo, style = MaterialTheme.typography.titleSmall) },
        supportingContent = { Text(descripcion, style = MaterialTheme.typography.bodySmall) },
        leadingContent = { InsigniaIcono(icono, MaterialTheme.colorScheme.primary, 40.dp) },
        trailingContent = {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}
