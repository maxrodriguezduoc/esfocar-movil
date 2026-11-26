package com.example.esfocarapp.ui.estudio

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.model.Dificultad
import com.example.esfocarapp.model.Ficha
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.BarraProgreso
import com.example.esfocarapp.ui.components.EstadoVacio
import com.example.esfocarapp.ui.components.Etiqueta
import com.example.esfocarapp.ui.components.InsigniaIcono
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.components.colorAsignatura
import com.example.esfocarapp.ui.components.colorDominio
import com.example.esfocarapp.ui.components.iconoAsignatura
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.viewmodel.EstudioViewModel
import com.example.esfocarapp.viewmodel.Fabrica
import com.example.esfocarapp.viewmodel.ModoQuiz

// ------------------------------- Pestaña "Estudiar" -------------------------------

@Composable
fun EstudiarContent(vm: EstudioViewModel, nav: Navegacion) {
    val asignaturas by vm.asignaturas.collectAsState()
    val consulta by vm.consulta.collectAsState()
    val resultados by vm.resultadosBusqueda.collectAsState()
    val favoritas by vm.fichasFavoritas.collectAsState()
    val buscando = consulta.trim().length >= 2

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = consulta,
                onValueChange = vm::buscar,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar en fichas de estudio") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (consulta.isNotEmpty()) {
                        IconButton(onClick = { vm.buscar("") }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )
        }

        if (buscando) {
            item {
                Text(
                    "${resultados.size} ${if (resultados.size == 1) "resultado" else "resultados"}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (resultados.isEmpty()) {
                item {
                    EstadoVacio(
                        Icons.Rounded.SearchOff, "Sin resultados",
                        "Prueba con otra palabra, por ejemplo \"perímetro\" o \"hemorragia\"."
                    )
                }
            }
            items(resultados, key = { "r-${it.id}" }) { ficha -> FilaFicha(ficha) { nav.ficha(ficha.id) } }
        } else {
            if (favoritas.isNotEmpty()) {
                item {
                    Tarjeta(onClick = nav::favoritos, color = MaterialTheme.colorScheme.secondaryContainer) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InsigniaIcono(Icons.Rounded.Bookmark, MaterialTheme.colorScheme.onSecondaryContainer, 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Mis fichas guardadas", style = MaterialTheme.typography.titleSmall)
                                Text("${favoritas.size} para repasar", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }

            items(asignaturas, key = { it.asignatura.id }) { avance ->
                val a = avance.asignatura
                val color = colorAsignatura(a.color)
                Tarjeta(onClick = { nav.asignatura(a.id) }) {
                    Row(verticalAlignment = Alignment.Top) {
                        InsigniaIcono(iconoAsignatura(a.icono), color, 52.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.nombre, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                a.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    BarraProgreso((avance.porcentaje ?: 0) / 100f, color)
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "${avance.unidades} unidades",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            avance.porcentaje?.let { "Dominio $it%" } ?: "Sin práctica aún",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorDominio(avance.porcentaje)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaFicha(ficha: Ficha, onClick: () -> Unit) {
    Tarjeta(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InsigniaIcono(Icons.AutoMirrored.Rounded.Article, MaterialTheme.colorScheme.primary, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ficha.titulo, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${ficha.puntosClave.size} puntos clave",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

// ------------------------------- Asignatura -------------------------------

@Composable
fun AsignaturaScreen(id: Long, nav: Navegacion, vm: EstudioViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val asignatura by remember(id) { vm.asignatura(id) }.collectAsState(initial = null)
    val unidades by remember(id) { vm.unidadesDe(id) }.collectAsState(initial = emptyList())

    Scaffold(topBar = { AppTopBar(asignatura?.nombre ?: "Asignatura", onBack = nav::volver) }) { padding ->
        val a = asignatura
        if (a == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(32.dp))
            return@Scaffold
        }
        val color = colorAsignatura(a.color)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Tarjeta(color = color.copy(alpha = 0.10f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(iconoAsignatura(a.icono), color, 56.dp)
                        Spacer(Modifier.width(14.dp))
                        Text(a.nombre, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(a.descripcion, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { nav.quiz(ModoQuiz.ASIGNATURA, a.id) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Trivia")
                        }
                        OutlinedButton(onClick = { nav.quiz(ModoQuiz.SIMULACRO, a.id) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Simulacro")
                        }
                    }
                }
            }
            item { TituloSeccion("Unidades") }
            items(unidades, key = { it.unidad.id }) { avance ->
                val u = avance.unidad
                Tarjeta(onClick = { nav.unidad(u.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("UNIDAD ${u.numero}", style = MaterialTheme.typography.labelMedium, color = color)
                            Text(u.titulo, style = MaterialTheme.typography.titleMedium)
                        }
                        Etiqueta(
                            avance.porcentaje?.let { "$it%" } ?: "Nueva",
                            colorDominio(avance.porcentaje)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        u.resumen,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ------------------------------- Unidad -------------------------------

@Composable
fun UnidadScreen(id: Long, nav: Navegacion, vm: EstudioViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val unidad by remember(id) { vm.unidad(id) }.collectAsState(initial = null)
    val fichas by remember(id) { vm.fichas(id) }.collectAsState(initial = emptyList())
    val casos by vm.casos.collectAsState()
    val casosUnidad = casos.filter { it.caso.unidadId == id }

    Scaffold(topBar = { AppTopBar(unidad?.let { "Unidad ${it.numero}" } ?: "Unidad", onBack = nav::volver) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(unidad?.titulo ?: "", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    unidad?.resumen ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { TituloSeccion("Fichas de estudio") }
            items(fichas, key = { it.id }) { ficha -> FilaFicha(ficha) { nav.ficha(ficha.id) } }

            item { TituloSeccion("Practica esta unidad") }
            item {
                Tarjeta(onClick = { nav.quiz(ModoQuiz.UNIDAD, id) }, color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(Icons.Rounded.Quiz, MaterialTheme.colorScheme.onPrimaryContainer, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Trivia de la unidad", style = MaterialTheme.typography.titleSmall)
                            Text("Retroalimentación inmediata en cada pregunta", style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                    }
                }
            }
            items(casosUnidad, key = { "caso-${it.caso.id}" }) { item ->
                Tarjeta(onClick = { nav.caso(item.caso.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(Icons.Rounded.Psychology, MaterialTheme.colorScheme.tertiary, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.caso.titulo, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Caso simulado · ${Dificultad.de(item.caso.dificultad).etiqueta}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.mejorPuntaje == 2) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = "Resuelto", tint = ColoresEstado.exito)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------- Ficha de lectura -------------------------------

@Composable
fun FichaScreen(id: Long, nav: Navegacion, vm: EstudioViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val ficha by produceState<Ficha?>(initialValue = null, id) { value = vm.ficha(id) }
    val favoritos by vm.favoritos.collectAsState()
    val guardada = id in favoritos

    Scaffold(
        topBar = {
            AppTopBar("Ficha de estudio", onBack = nav::volver, acciones = {
                IconButton(onClick = { vm.alternarFavorito(id) }) {
                    Icon(
                        if (guardada) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                        contentDescription = if (guardada) "Quitar de guardadas" else "Guardar ficha"
                    )
                }
            })
        }
    ) { padding ->
        val f = ficha
        if (f == null) {
            CircularProgressIndicator(Modifier.padding(padding).padding(32.dp))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(f.titulo, style = MaterialTheme.typography.headlineSmall)
            HorizontalDivider()
            f.contenido.split("\n\n").forEach { parrafo ->
                Text(parrafo.trim(), style = MaterialTheme.typography.bodyLarge)
            }
            Tarjeta(color = MaterialTheme.colorScheme.secondaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Puntos clave",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                f.puntosClave.forEach { punto ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).padding(top = 2.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(punto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
            Button(onClick = { nav.quiz(ModoQuiz.UNIDAD, f.unidadId) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Rounded.Quiz, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ponerme a prueba con esta unidad")
            }
        }
    }
}

// ------------------------------- Fichas guardadas -------------------------------

@Composable
fun FavoritosScreen(nav: Navegacion, vm: EstudioViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val fichas by vm.fichasFavoritas.collectAsState()
    Scaffold(topBar = { AppTopBar("Fichas guardadas", onBack = nav::volver) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (fichas.isEmpty()) {
                item {
                    EstadoVacio(
                        Icons.Rounded.BookmarkBorder, "Aún no guardas fichas",
                        "Toca el marcador en cualquier ficha de estudio para tenerla siempre a mano."
                    )
                }
            }
            items(fichas, key = { it.id }) { ficha -> FilaFicha(ficha) { nav.ficha(ficha.id) } }
        }
    }
}
