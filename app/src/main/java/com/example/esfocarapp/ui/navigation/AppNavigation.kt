package com.example.esfocarapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.esfocarapp.ui.apuntes.ApuntesScreen
import com.example.esfocarapp.ui.conceptos.ConceptosScreen
import com.example.esfocarapp.ui.docente.FormCasoScreen
import com.example.esfocarapp.ui.docente.FormPreguntaScreen
import com.example.esfocarapp.ui.docente.PanelDocenteScreen
import com.example.esfocarapp.ui.documentos.DocumentosScreen
import com.example.esfocarapp.ui.estudio.AsignaturaScreen
import com.example.esfocarapp.ui.estudio.FavoritosScreen
import com.example.esfocarapp.ui.estudio.FichaScreen
import com.example.esfocarapp.ui.estudio.UnidadScreen
import com.example.esfocarapp.ui.login.LoginScreen
import com.example.esfocarapp.ui.onboarding.OnboardingScreen
import com.example.esfocarapp.ui.practica.CasoScreen
import com.example.esfocarapp.ui.practica.CasosScreen
import com.example.esfocarapp.ui.practica.QuizScreen
import com.example.esfocarapp.ui.practica.SimulacroScreen
import com.example.esfocarapp.ui.principal.PrincipalScreen
import com.example.esfocarapp.ui.sesion.CerrarSesionScreen
import com.example.esfocarapp.viewmodel.AjustesViewModel
import com.example.esfocarapp.viewmodel.Fabrica
import com.example.esfocarapp.viewmodel.ModoQuiz
import com.example.esfocarapp.viewmodel.Rutas

// Acciones de navegación que usan las pantallas (así no dependen directamente del NavController)
class Navegacion(private val nav: NavHostController) {
    fun volver() { nav.popBackStack() }
    fun asignatura(id: Long) = nav.navigate("asignatura/$id")
    fun unidad(id: Long) = nav.navigate("unidad/$id")
    fun ficha(id: Long) = nav.navigate("ficha/$id")
    fun quiz(modo: ModoQuiz, id: Long) = nav.navigate("quiz/${modo.name}/$id")
    fun simulacro() = nav.navigate("simulacro")
    fun casos() = nav.navigate("casos")
    fun caso(id: Long) = nav.navigate("caso/$id")
    fun favoritos() = nav.navigate("favoritos")
    fun apuntes() = nav.navigate("apuntes")
    fun documentos() = nav.navigate("documentos")
    fun conceptos() = nav.navigate("conceptos")
    fun cerrarSesion() = nav.navigate("cerrar_sesion")
    fun panelDocente() = nav.navigate("docente")
    fun formPregunta(unidadId: Long, id: Long) = nav.navigate("docente/pregunta/$unidadId/$id")
    fun formCaso(unidadId: Long, id: Long) = nav.navigate("docente/caso/$unidadId/$id")
}

// Mapa de pantallas de la app. Cada "composable(...)" es una pantalla con su ruta.
@Composable
fun AppNavigation(ajustes: AjustesViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val navController = rememberNavController()
    val nav = remember(navController) { Navegacion(navController) }
    val inicio = remember { ajustes.destinoInicial() }

    val irAPrincipal: () -> Unit = {
        navController.navigate(Rutas.PRINCIPAL) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = inicio) {

        composable(Rutas.ONBOARDING) {
            OnboardingScreen(onTerminar = {
                ajustes.completarOnboarding()
                navController.navigate(Rutas.LOGIN) { popUpTo(Rutas.ONBOARDING) { inclusive = true } }
            })
        }

        // Vista: iniciar sesión
        composable(Rutas.LOGIN) {
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

        composable(Rutas.PRINCIPAL) { PrincipalScreen(nav) }

        composable("asignatura/{id}") { e -> AsignaturaScreen(e.arguments.id(), nav) }
        composable("unidad/{id}") { e -> UnidadScreen(e.arguments.id(), nav) }
        composable("ficha/{id}") { e -> FichaScreen(e.arguments.id(), nav) }
        composable("favoritos") { FavoritosScreen(nav) }

        composable("quiz/{modo}/{id}") { QuizScreen(nav) }
        composable("simulacro") { SimulacroScreen(nav) }
        composable("casos") { CasosScreen(nav) }
        composable("caso/{id}") { CasoScreen(nav) }

        // Modo docente
        composable("docente") { PanelDocenteScreen(nav) }
        composable("docente/pregunta/{unidadId}/{id}") { FormPreguntaScreen(nav) }
        composable("docente/caso/{unidadId}/{id}") { FormCasoScreen(nav) }

        composable("apuntes") { ApuntesScreen(onBack = nav::volver) }
        composable("documentos") { DocumentosScreen(onBack = nav::volver) }
        composable("conceptos") { ConceptosScreen(onBack = nav::volver) }

        // Vista: cierre de sesión (con confirmación)
        composable("cerrar_sesion") {
            CerrarSesionScreen(
                onCancelar = nav::volver,
                onConfirmar = {
                    ajustes.cerrarSesion()
                    navController.navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}

private fun android.os.Bundle?.id(): Long = this?.getString("id")?.toLongOrNull() ?: 0L
