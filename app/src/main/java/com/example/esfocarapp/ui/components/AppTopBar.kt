package com.example.esfocarapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.esfocarapp.ui.theme.VerdeInstitucional
import com.example.esfocarapp.ui.theme.esOscuro
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Una opción del menú: el texto que se ve y lo que hace al tocarla
data class ItemMenu(val texto: String, val accion: () -> Unit)

// Barra superior reutilizable: título, botón volver (opcional), acciones, menú (opcional) y emblema
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    onBack: (() -> Unit)? = null,
    menu: List<ItemMenu> = emptyList(),
    acciones: @Composable RowScope.() -> Unit = {}
) {
    var menuAbierto by remember { mutableStateOf(false) }
    // En modo claro la barra es verde institucional; en oscuro, una superficie oscura
    val oscuro = esOscuro()
    val contenido = if (oscuro) MaterialTheme.colorScheme.onSurface else Color.White

    TopAppBar(
        title = { Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = {
            acciones()
            LogoEsfocar(
                modifier = Modifier.padding(start = 4.dp, end = if (menu.isEmpty()) 12.dp else 0.dp),
                alto = 32.dp
            )
            if (menu.isNotEmpty()) {
                Box {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        menu.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion.texto) },
                                onClick = {
                                    menuAbierto = false
                                    opcion.accion()
                                }
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (oscuro) MaterialTheme.colorScheme.surfaceContainer else VerdeInstitucional,
            titleContentColor = contenido,
            navigationIconContentColor = contenido,
            actionIconContentColor = contenido
        )
    )
}
