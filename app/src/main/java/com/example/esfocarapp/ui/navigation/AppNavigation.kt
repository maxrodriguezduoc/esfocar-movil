package com.example.esfocarapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.esfocarapp.data.FakeData
import com.example.esfocarapp.ui.apuntes.ApuntesScreen
import com.example.esfocarapp.ui.documentos.DocumentosScreen
import com.example.esfocarapp.ui.login.LoginScreen
import com.example.esfocarapp.ui.principal.PrincipalScreen
import com.example.esfocarapp.ui.sesion.CerrarSesionScreen
import com.example.esfocarapp.ui.trivia.TriviaScreen
import com.example.esfocarapp.util.Sesion

// Mapa de pantallas de la app. Cada "composable(...)" es una pantalla con su ruta.
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Ir a la pantalla principal y quitar login/registro del historial
    val irAPrincipal: () -> Unit = {
        navController.navigate("principal") {
            popUpTo("login") { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = "login") {

        // Vista: iniciar sesión
        composable("login") {
            LoginScreen(
                registro = false,
                onIngreso = irAPrincipal,
                onCambiarVista = { navController.navigate("registro") }
            )
        }

        // Vista: registrar usuario
        composable("registro") {
            LoginScreen(
                registro = true,
                onIngreso = irAPrincipal,
                onCambiarVista = { navController.popBackStack() }
            )
        }

        composable("principal") {
            PrincipalScreen(
                onAsignatura = { id -> navController.navigate("trivia/$id") },
                onApuntes = { navController.navigate("apuntes") },
                onDocumentos = { navController.navigate("documentos") },
                onCerrarSesion = { navController.navigate("cerrar_sesion") }
            )
        }

        composable("trivia/{id}") { entrada ->
            val id = entrada.arguments?.getString("id")?.toIntOrNull()
            val nombre = FakeData.asignaturas.find { it.id == id }?.nombre ?: "Trivia"
            TriviaScreen(titulo = nombre, onBack = { navController.popBackStack() })
        }

        composable("apuntes") {
            ApuntesScreen(onBack = { navController.popBackStack() })
        }

        composable("documentos") {
            DocumentosScreen(onBack = { navController.popBackStack() })
        }

        // Vista: cierre de sesión (con confirmación)
        composable("cerrar_sesion") {
            CerrarSesionScreen(
                onCancelar = { navController.popBackStack() },
                onConfirmar = {
                    Sesion.alias = null
                    navController.navigate("login") {
                        popUpTo("principal") { inclusive = true }
                    }
                }
            )
        }
    }
}
