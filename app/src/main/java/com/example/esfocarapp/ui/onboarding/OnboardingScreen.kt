package com.example.esfocarapp.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.ui.components.FondoInstitucional
import com.example.esfocarapp.ui.components.LogoEsfocar
import com.example.esfocarapp.ui.theme.VerdeProfundo
import kotlinx.coroutines.launch

private data class Pagina(val icono: ImageVector?, val titulo: String, val texto: String)

private val paginas = listOf(
    Pagina(
        null,
        "Bienvenido a ESFOCAR Estudio",
        "Tu herramienta de estudio, repaso y autoevaluación como estudiante en formación."
    ),
    Pagina(
        Icons.Rounded.CloudOff,
        "Estudia donde estés",
        "Todo el contenido, las trivias y los casos funcionan sin internet. Cuando vuelvas a tener señal, tu avance se sincroniza solo."
    ),
    Pagina(
        Icons.Rounded.Psychology,
        "Practica con situaciones reales",
        "Resuelve trivias, simulacros con nota y casos simulados donde eliges cómo actuar y recibes retroalimentación inmediata."
    ),
    Pagina(
        Icons.Rounded.Insights,
        "Mide tu avance",
        "Revisa tu racha de estudio, tu dominio por asignatura y los temas que conviene reforzar."
    )
)

@Composable
fun OnboardingScreen(onTerminar: () -> Unit) {
    val estado = rememberPagerState { paginas.size }
    val alcance = rememberCoroutineScope()
    val ultima = estado.currentPage == paginas.lastIndex

    Box(
        Modifier
            .fillMaxSize()
            .background(FondoInstitucional())
            .safeDrawingPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
                if (!ultima) TextButton(onClick = onTerminar) { Text("Omitir", color = Color.White) }
            }

            HorizontalPager(state = estado, modifier = Modifier.weight(1f)) { indice ->
                val pagina = paginas[indice]
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (pagina.icono == null) {
                        LogoEsfocar(alto = 180.dp)
                    } else {
                        Box(
                            Modifier.size(150.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(pagina.icono, contentDescription = null, tint = Color(0xFFFFD86B), modifier = Modifier.size(76.dp))
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                    Text(
                        pagina.titulo,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        pagina.texto,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Indicador de páginas
            Row(
                Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(paginas.size) { i ->
                    val activo = i == estado.currentPage
                    val ancho by animateDpAsState(if (activo) 28.dp else 8.dp, label = "punto")
                    val color by animateColorAsState(
                        if (activo) Color(0xFFFFD86B) else Color.White.copy(alpha = 0.35f), label = "color"
                    )
                    Box(
                        Modifier.padding(horizontal = 4.dp).height(8.dp).width(ancho).clip(CircleShape)
                            .background(color)
                    )
                }
            }

            Button(
                onClick = {
                    if (ultima) onTerminar() else alcance.launch { estado.animateScrollToPage(estado.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerdeProfundo)
            ) {
                Text(if (ultima) "Comenzar" else "Siguiente", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
