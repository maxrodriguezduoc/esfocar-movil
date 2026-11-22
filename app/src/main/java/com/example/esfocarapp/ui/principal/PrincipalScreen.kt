package com.example.esfocarapp.ui.principal

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.estudio.EstudiarContent
import com.example.esfocarapp.ui.inicio.InicioContent
import com.example.esfocarapp.ui.mas.MasContent
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.practica.PracticarContent
import com.example.esfocarapp.ui.progreso.ProgresoContent
import com.example.esfocarapp.viewmodel.AjustesViewModel
import com.example.esfocarapp.viewmodel.EstudioViewModel
import com.example.esfocarapp.viewmodel.Fabrica
import com.example.esfocarapp.viewmodel.ProgresoViewModel

// Las 5 secciones de la barra inferior
private enum class Seccion(val titulo: String, val etiqueta: String, val icono: ImageVector, val iconoActivo: ImageVector) {
    Inicio("Inicio", "Inicio", Icons.Outlined.Home, Icons.Rounded.Home),
    Estudiar("Estudiar", "Estudiar", Icons.AutoMirrored.Outlined.MenuBook, Icons.AutoMirrored.Rounded.MenuBook),
    Practicar("Practicar", "Practicar", Icons.Outlined.Quiz, Icons.Rounded.Quiz),
    Progreso("Mi progreso", "Progreso", Icons.Outlined.Insights, Icons.Rounded.Insights),
    Mas("Mi cuenta", "Cuenta", Icons.Outlined.Person, Icons.Rounded.Person)
}

@Composable
fun PrincipalScreen(nav: Navegacion) {
    val context = LocalContext.current

    // Desde Android 17 (API 37) conectarse a los microservicios en la red local requiere este permiso
    val pedirRedLocal = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val permiso = "android.permission.ACCESS_LOCAL_NETWORK"
        if (Build.VERSION.SDK_INT >= 37 &&
            ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED
        ) {
            pedirRedLocal.launch(permiso)
        }
    }

    val secciones = Seccion.entries
    var seleccionada by rememberSaveable { mutableIntStateOf(0) }

    val progresoVm: ProgresoViewModel = viewModel(factory = Fabrica.Proveedor)
    val estudioVm: EstudioViewModel = viewModel(factory = Fabrica.Proveedor)
    val ajustesVm: AjustesViewModel = viewModel(factory = Fabrica.Proveedor)

    Scaffold(
        // El contenido se dibuja de borde a borde: la barra superior y la inferior manejan sus márgenes
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // El inicio tiene su propio encabezado de marca
            if (secciones[seleccionada] != Seccion.Inicio) AppTopBar(secciones[seleccionada].titulo)
        },
        bottomBar = {
            NavigationBar {
                secciones.forEachIndexed { posicion, s ->
                    val activa = seleccionada == posicion
                    NavigationBarItem(
                        selected = activa,
                        onClick = { seleccionada = posicion },
                        icon = { Icon(if (activa) s.iconoActivo else s.icono, contentDescription = s.etiqueta) },
                        label = { Text(s.etiqueta) }
                    )
                }
            }
        }
    ) { padding ->
        Crossfade(targetState = seleccionada, modifier = Modifier.padding(padding), label = "seccion") { posicion ->
            when (secciones[posicion]) {
                Seccion.Inicio -> InicioContent(
                    vm = progresoVm,
                    estudio = estudioVm,
                    nav = nav,
                    irAEstudiar = { seleccionada = Seccion.Estudiar.ordinal },
                    irAProgreso = { seleccionada = Seccion.Progreso.ordinal }
                )
                Seccion.Estudiar -> EstudiarContent(vm = estudioVm, nav = nav)
                Seccion.Practicar -> PracticarContent(estudio = estudioVm, progreso = progresoVm, nav = nav)
                Seccion.Progreso -> ProgresoContent(vm = progresoVm, nav = nav)
                Seccion.Mas -> MasContent(vm = ajustesVm, nav = nav)
            }
        }
    }
}
