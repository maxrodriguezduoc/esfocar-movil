package com.example.esfocarapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.ui.theme.VerdeInstitucional
import com.example.esfocarapp.ui.theme.VerdeProfundo
import com.example.esfocarapp.ui.theme.esOscuro

// ----------------------------------------------------------------------------------------------
// Sistema de componentes de ESFOCAR Estudio. Todas las pantallas se arman con estas piezas.
// ----------------------------------------------------------------------------------------------

fun iconoAsignatura(icono: String): ImageVector = when (icono) {
    "procedimientos" -> Icons.Rounded.Shield
    "normativa" -> Icons.Rounded.Gavel
    "salud" -> Icons.Rounded.MedicalServices
    "comunicacion" -> Icons.Rounded.Forum
    else -> Icons.AutoMirrored.Rounded.MenuBook
}

// Convierte "#1E5631" en un color. En modo oscuro se aclara para mantener buen contraste.
@Composable
fun colorAsignatura(hex: String): Color {
    val base = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(VerdeInstitucional)
    return if (esOscuro()) lerp(base, Color.White, 0.45f) else base
}

// Ícono dentro de un recuadro redondeado con el color de fondo suave
@Composable
fun InsigniaIcono(icono: ImageVector, color: Color, tamano: Dp = 48.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano / 3.2f))
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(tamano * 0.55f))
    }
}

// Anillo de progreso animado (0f..1f) con contenido al centro
@Composable
fun AnilloProgreso(
    progreso: Float,
    modifier: Modifier = Modifier,
    tamano: Dp = 120.dp,
    grosor: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    fondo: Color = MaterialTheme.colorScheme.surfaceVariant,
    contenido: @Composable BoxScope.() -> Unit = {}
) {
    val animado by animateFloatAsState(progreso.coerceIn(0f, 1f), tween(900), label = "anillo")
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(tamano)) {
            val trazo = Stroke(width = grosor.toPx(), cap = StrokeCap.Round)
            val margen = grosor.toPx() / 2
            val area = androidx.compose.ui.geometry.Size(size.width - grosor.toPx(), size.height - grosor.toPx())
            val origen = androidx.compose.ui.geometry.Offset(margen, margen)
            drawArc(fondo, -90f, 360f, false, origen, area, style = trazo)
            drawArc(color, -90f, 360f * animado, false, origen, area, style = trazo)
        }
        contenido()
    }
}

// Barra de progreso redondeada y animada
@Composable
fun BarraProgreso(progreso: Float, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    val animado by animateFloatAsState(progreso.coerceIn(0f, 1f), tween(800), label = "barra")
    LinearProgressIndicator(
        progress = { animado },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape),
        color = color,
        trackColor = color.copy(alpha = 0.15f),
        drawStopIndicator = {}
    )
}

// Pastilla pequeña de texto (dificultad, estado, etc.)
@Composable
fun Etiqueta(texto: String, color: Color, icono: ImageVector? = null) {
    Surface(color = color.copy(alpha = 0.14f), contentColor = color, shape = CircleShape) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icono != null) Icon(icono, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Estado de conexión visible para el usuario (la app funciona igual sin internet)
@Composable
fun ChipConexion(enLinea: Boolean, sobreFondoOscuro: Boolean = false) {
    val color = when {
        sobreFondoOscuro -> Color.White
        enLinea -> ColoresEstado.exito
        else -> ColoresEstado.advertencia
    }
    Etiqueta(
        texto = if (enLinea) "En línea" else "Sin conexión",
        color = color,
        icono = if (enLinea) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
    )
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier, accion: String? = null, onAccion: () -> Unit = {}) {
    Row(
        modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(texto, style = MaterialTheme.typography.titleLarge)
        if (accion != null) TextButton(onClick = onAccion) { Text(accion) }
    }
}

// Tarjeta base con superficie elevada suave
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    relleno: PaddingValues = PaddingValues(16.dp),
    contenido: @Composable ColumnScope.() -> Unit
) {
    val colores = CardDefaults.cardColors(containerColor = color)
    val forma = MaterialTheme.shapes.large
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), colors = colores, shape = forma) {
            Column(Modifier.padding(relleno), content = contenido)
        }
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = colores, shape = forma) {
            Column(Modifier.padding(relleno), content = contenido)
        }
    }
}

// Acceso rápido con ícono, título, descripción y contador opcional
@Composable
fun TarjetaAccion(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contador: Int? = null
) {
    Tarjeta(modifier = modifier, onClick = onClick) {
        BadgedBox(badge = { if (contador != null && contador > 0) Badge { Text("$contador") } }) {
            InsigniaIcono(icono, color, 44.dp)
        }
        Spacer(Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            descripcion,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Mensaje amigable cuando no hay datos
@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    textoAccion: String? = null,
    onAccion: () -> Unit = {}
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InsigniaIcono(icono, MaterialTheme.colorScheme.primary, 72.dp)
        Spacer(Modifier.height(4.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (textoAccion != null) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = onAccion) { Text(textoAccion) }
        }
    }
}

// Fondo degradado institucional (encabezados de inicio, login y bienvenida)
@Composable
fun FondoInstitucional(): Brush {
    val oscuro = esOscuro()
    return Brush.verticalGradient(
        if (oscuro) listOf(Color(0xFF173D24), Color(0xFF0F1510)) else listOf(VerdeInstitucional, VerdeProfundo)
    )
}

// Pequeño indicador numérico para encabezados (sobre fondo verde)
@Composable
fun DatoEncabezado(valor: String, etiqueta: String, icono: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.12f),
        contentColor = Color.White,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFFFD86B))
            Spacer(Modifier.height(4.dp))
            Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

// Fila de dato (etiqueta a la izquierda, valor a la derecha)
@Composable
fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// Color según porcentaje de dominio: verde (bien), dorado (regular), rojo (reforzar)
@Composable
fun colorDominio(porcentaje: Int?): Color = when {
    porcentaje == null -> MaterialTheme.colorScheme.outline
    porcentaje >= 80 -> ColoresEstado.exito
    porcentaje >= 60 -> ColoresEstado.advertencia
    else -> MaterialTheme.colorScheme.error
}
