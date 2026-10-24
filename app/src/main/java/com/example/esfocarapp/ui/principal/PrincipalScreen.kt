package com.example.esfocarapp.ui.principal

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.data.FakeData
import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.ui.asignaturas.AsignaturasContent
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.ItemMenu
import com.example.esfocarapp.ui.progreso.ProgresoContent
import com.example.esfocarapp.ui.trivia.TriviaContent
import com.example.esfocarapp.viewmodel.ProgresoViewModel
import com.example.esfocarapp.viewmodel.TriviaViewModel

// Las 3 opciones de la barra inferior
private enum class Destino(val titulo: String, val etiqueta: String, val icono: ImageVector) {
    Estudiar("Estudio y Autoevaluación", "Estudiar", Icons.Default.Home),
    Trivia("Trivia rápida", "Trivia", Icons.Default.Star),
    Progreso("Mi progreso", "Progreso", Icons.Default.CheckCircle)
}

@Composable
fun PrincipalScreen(
    onAsignatura: (Int) -> Unit,
    onApuntes: () -> Unit,
    onDocumentos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val destinos = Destino.values()
    var seleccionado by rememberSaveable { mutableIntStateOf(0) }

    val triviaVm: TriviaViewModel = viewModel()
    val progresoVm: ProgresoViewModel = viewModel()
    val intentos by progresoVm.intentos.collectAsState(initial = emptyList<Intento>())

    // Opciones del menú de tres puntos de la barra superior
    val opcionesMenu = listOf(
        ItemMenu("Documentos", onDocumentos),
        ItemMenu("Mis apuntes", onApuntes),
        ItemMenu("Cerrar sesión", onCerrarSesion)
    )

    Scaffold(
        topBar = { AppTopBar(destinos[seleccionado].titulo, menu = opcionesMenu) },
        bottomBar = {
            NavigationBar {
                destinos.forEachIndexed { posicion, destino ->
                    NavigationBarItem(
                        selected = seleccionado == posicion,
                        onClick = { seleccionado = posicion },
                        icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                        label = { Text(destino.etiqueta) }
                    )
                }
            }
        }
    ) { padding ->
        // Animación: al cambiar de pestaña el contenido se desvanece suavemente
        Crossfade(
            targetState = seleccionado,
            modifier = Modifier.padding(padding),
            label = "cambio de pestaña"
        ) { posicion ->
            when (destinos[posicion]) {
                Destino.Estudiar -> AsignaturasContent(
                    asignaturas = FakeData.asignaturas,
                    actividadesCompletadas = intentos.size,
                    onAsignatura = onAsignatura,
                    onApuntes = onApuntes,
                    onDocumentos = onDocumentos
                )
                Destino.Trivia -> TriviaContent(
                    vm = triviaVm,
                    actividad = "Trivia rápida",
                    textoFinal = "Reintentar",
                    onFinal = { }
                )
                Destino.Progreso -> ProgresoContent(intentos = intentos)
            }
        }
    }
}
