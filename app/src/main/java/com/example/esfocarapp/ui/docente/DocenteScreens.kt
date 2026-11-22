package com.example.esfocarapp.ui.docente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.model.Dificultad
import com.example.esfocarapp.model.NivelActuacion
import com.example.esfocarapp.ui.components.AppTopBar
import com.example.esfocarapp.ui.components.EstadoVacio
import com.example.esfocarapp.ui.components.Etiqueta
import com.example.esfocarapp.ui.components.InsigniaIcono
import com.example.esfocarapp.ui.components.Tarjeta
import com.example.esfocarapp.ui.components.TituloSeccion
import com.example.esfocarapp.ui.navigation.Navegacion
import com.example.esfocarapp.ui.practica.colorDificultad
import com.example.esfocarapp.ui.practica.colorNivel
import com.example.esfocarapp.ui.theme.ColoresEstado
import com.example.esfocarapp.util.ValidadorContenido
import com.example.esfocarapp.viewmodel.DocenteViewModel
import com.example.esfocarapp.viewmodel.Fabrica
import com.example.esfocarapp.viewmodel.FormCasoViewModel
import com.example.esfocarapp.viewmodel.FormPreguntaViewModel

// ------------------------------- Panel docente -------------------------------

@Composable
fun PanelDocenteScreen(nav: Navegacion, vm: DocenteViewModel = viewModel(factory = Fabrica.Proveedor)) {
    val asignaturas by vm.asignaturas.collectAsState()
    val asignaturaId by vm.asignaturaId.collectAsState()
    val unidades by vm.unidades.collectAsState()
    val unidadId by vm.unidadId.collectAsState()
    val preguntas by vm.preguntas.collectAsState()
    val casos by vm.casos.collectAsState()
    var porEliminar by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    // Al entrar se selecciona la primera asignatura y unidad
    LaunchedEffect(asignaturas) { if (asignaturaId == null) asignaturas.firstOrNull()?.let { vm.elegirAsignatura(it.id) } }
    LaunchedEffect(unidades) { if (unidadId == null) unidades.firstOrNull()?.let { vm.elegirUnidad(it.id) } }

    Scaffold(topBar = { AppTopBar("Panel docente", onBack = nav::volver) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Tarjeta(color = MaterialTheme.colorScheme.secondaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsigniaIcono(Icons.Rounded.CloudUpload, MaterialTheme.colorScheme.onSecondaryContainer, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Lo que publiques llega a tus estudiantes cuando su app se sincroniza. Publicar requiere conexión con el servidor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            if (vm.procesando) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            vm.mensaje?.let { texto ->
                item { Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
            }

            item { Text("Asignatura", style = MaterialTheme.typography.labelLarge) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(asignaturas, key = { it.id }) { a ->
                        FilterChip(selected = a.id == asignaturaId, onClick = { vm.elegirAsignatura(a.id) }, label = { Text(a.nombre) })
                    }
                }
            }
            item { Text("Unidad", style = MaterialTheme.typography.labelLarge) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(unidades, key = { it.id }) { u ->
                        FilterChip(
                            selected = u.id == unidadId,
                            onClick = { vm.elegirUnidad(u.id) },
                            label = { Text("U${u.numero} · ${u.titulo}") }
                        )
                    }
                }
            }

            val unidad = unidadId
            if (unidad != null) {
                // ---------- Preguntas ----------
                item {
                    EncabezadoLista("Preguntas de trivia (${preguntas.size})") { nav.formPregunta(unidad, 0) }
                }
                if (preguntas.isEmpty()) item { Text("Esta unidad aún no tiene preguntas.") }
                items(preguntas, key = { "p-${it.id}" }) { p ->
                    FilaContenido(
                        icono = Icons.Rounded.HelpOutline,
                        titulo = p.enunciado,
                        detalle = "${p.alternativas.size} alternativas · correcta: ${p.alternativas.firstOrNull { it.esCorrecta }?.texto ?: "—"}",
                        onEditar = { nav.formPregunta(unidad, p.id) },
                        onEliminar = { porEliminar = "la pregunta \"${p.enunciado.take(60)}\"" to { vm.eliminarPregunta(p.id) } }
                    )
                }

                // ---------- Casos ----------
                item {
                    EncabezadoLista("Casos simulados (${casos.size})") { nav.formCaso(unidad, 0) }
                }
                if (casos.isEmpty()) item { Text("Esta unidad aún no tiene casos simulados.") }
                items(casos, key = { "c-${it.id}" }) { c ->
                    FilaContenido(
                        icono = Icons.Rounded.Psychology,
                        titulo = c.titulo,
                        detalle = "${Dificultad.de(c.dificultad).etiqueta} · ${c.opciones.size} opciones",
                        onEditar = { nav.formCaso(unidad, c.id) },
                        onEliminar = { porEliminar = "el caso \"${c.titulo}\"" to { vm.eliminarCaso(c.id) } }
                    )
                }
            } else {
                item { EstadoVacio(Icons.Rounded.School, "Elige una unidad", "Selecciona la asignatura y la unidad que quieres gestionar.") }
            }
        }
    }

    porEliminar?.let { (descripcion, accion) ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar contenido?") },
            text = { Text("Se eliminará $descripcion del servidor y desaparecerá de las apps de los estudiantes al sincronizar.") },
            confirmButton = {
                TextButton(onClick = {
                    accion()
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun EncabezadoLista(titulo: String, onNuevo: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        FilledTonalButton(onClick = onNuevo) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Nueva")
        }
    }
}

@Composable
private fun FilaContenido(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    detalle: String,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Tarjeta(relleno = PaddingValues(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            InsigniaIcono(icono, MaterialTheme.colorScheme.primary, 36.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onEditar) { Icon(Icons.Rounded.Edit, contentDescription = "Editar") }
            IconButton(onClick = onEliminar) { Icon(Icons.Rounded.Delete, contentDescription = "Eliminar") }
        }
    }
}

// ------------------------------- Formulario de pregunta -------------------------------

@Composable
fun FormPreguntaScreen(nav: Navegacion, vm: FormPreguntaViewModel = viewModel(factory = Fabrica.Proveedor)) {
    LaunchedEffect(vm.guardado) { if (vm.guardado) nav.volver() }

    FormularioBase(
        titulo = if (vm.editando) "Editar pregunta" else "Nueva pregunta",
        cargando = vm.cargando,
        guardando = vm.guardando,
        mensaje = vm.mensaje,
        onVolver = nav::volver,
        onGuardar = vm::guardar
    ) {
        Campo("Enunciado", vm.enunciado, vm::cambiarEnunciado, vm.errorEnunciado, lineas = 3)
        Campo("Explicación (opcional)", vm.explicacion, vm::cambiarExplicacion, vm.errorExplicacion, lineas = 2,
            ayuda = "Se muestra al estudiante después de responder")

        TituloSeccion("Alternativas")
        Text(
            "Marca con el círculo la alternativa correcta. Cada una necesita su retroalimentación.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        vm.alternativas.forEachIndexed { i, a ->
            Tarjeta(
                color = if (a.correcta) ColoresEstado.exitoContenedor else MaterialTheme.colorScheme.surfaceContainerLow,
                relleno = PaddingValues(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = a.correcta, onClick = { vm.marcarCorrecta(i) })
                    Text(
                        if (a.correcta) "Alternativa ${'A' + i} · correcta" else "Alternativa ${'A' + i}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (vm.alternativas.size > ValidadorContenido.MIN_OPCIONES) {
                        IconButton(onClick = { vm.quitarAlternativa(i) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Quitar alternativa")
                        }
                    }
                }
                Campo("Texto", a.texto, { vm.cambiarTexto(i, it) })
                Campo("Retroalimentación", a.retroalimentacion, { vm.cambiarRetroalimentacion(i, it) }, lineas = 2)
            }
        }
        if (vm.alternativas.size < ValidadorContenido.MAX_OPCIONES) {
            OutlinedButton(onClick = vm::agregarAlternativa, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Agregar alternativa")
            }
        }
        vm.errorAlternativas?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

// ------------------------------- Formulario de caso simulado -------------------------------

@Composable
fun FormCasoScreen(nav: Navegacion, vm: FormCasoViewModel = viewModel(factory = Fabrica.Proveedor)) {
    LaunchedEffect(vm.guardado) { if (vm.guardado) nav.volver() }

    FormularioBase(
        titulo = if (vm.editando) "Editar caso simulado" else "Nuevo caso simulado",
        cargando = vm.cargando,
        guardando = vm.guardando,
        mensaje = vm.mensaje,
        onVolver = nav::volver,
        onGuardar = vm::guardar
    ) {
        Campo("Título", vm.titulo, vm::cambiarTitulo, vm.errorTitulo)
        Campo("Situación", vm.situacion, vm::cambiarSituacion, vm.errorSituacion, lineas = 5,
            ayuda = "Describe el escenario con datos ficticios")
        Campo("Pregunta al estudiante", vm.pregunta, vm::cambiarPregunta, vm.errorPregunta)

        Text("Dificultad", style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            Dificultad.entries.forEachIndexed { i, d ->
                SegmentedButton(
                    selected = vm.dificultad == d,
                    onClick = { vm.cambiarDificultad(d) },
                    shape = SegmentedButtonDefaults.itemShape(i, Dificultad.entries.size)
                ) { Text(d.etiqueta) }
            }
        }
        Campo("Fundamento", vm.fundamento, vm::cambiarFundamento, vm.errorFundamento, lineas = 3,
            ayuda = "Explica por qué la actuación óptima es la correcta")

        TituloSeccion("Opciones de actuación")
        vm.opciones.forEachIndexed { i, o ->
            val nivel = NivelActuacion.de(o.puntaje)
            Tarjeta(color = colorNivel(nivel).copy(alpha = 0.08f), relleno = PaddingValues(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Opción ${i + 1}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Etiqueta(nivel.etiqueta, colorNivel(nivel))
                    if (vm.opciones.size > ValidadorContenido.MIN_OPCIONES) {
                        IconButton(onClick = { vm.quitarOpcion(i) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Quitar opción")
                        }
                    }
                }
                Campo("Actuación", o.texto, { vm.cambiarTexto(i, it) }, lineas = 2)
                Campo("Retroalimentación", o.retroalimentacion, { vm.cambiarRetroalimentacion(i, it) }, lineas = 2)
                Spacer(Modifier.height(4.dp))
                val niveles = listOf(2 to "Óptima", 1 to "Aceptable", 0 to "Incorrecta")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    niveles.forEachIndexed { j, (puntaje, etiqueta) ->
                        SegmentedButton(
                            selected = o.puntaje == puntaje,
                            onClick = { vm.cambiarPuntaje(i, puntaje) },
                            shape = SegmentedButtonDefaults.itemShape(j, niveles.size)
                        ) { Text(etiqueta, maxLines = 1) }
                    }
                }
            }
        }
        if (vm.opciones.size < ValidadorContenido.MAX_OPCIONES) {
            OutlinedButton(onClick = vm::agregarOpcion, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Agregar opción")
            }
        }
        vm.errorOpciones?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Etiqueta("Dificultad: ${vm.dificultad.etiqueta}", colorDificultad(vm.dificultad.name))
    }
}

// ------------------------------- Piezas comunes de los formularios -------------------------------

@Composable
private fun FormularioBase(
    titulo: String,
    cargando: Boolean,
    guardando: Boolean,
    mensaje: String?,
    onVolver: () -> Unit,
    onGuardar: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(titulo, onBack = onVolver) },
        bottomBar = {
            Column(Modifier.imePadding()) {
                mensaje?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Button(
                    onClick = onGuardar,
                    enabled = !guardando && !cargando,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Publicando...")
                    } else {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Publicar")
                    }
                }
            }
        }
    ) { padding ->
        if (cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = contenido
        )
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    error: String? = null,
    lineas: Int = 1,
    ayuda: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = (error ?: ayuda)?.let { texto -> { Text(texto) } },
        singleLine = lineas == 1,
        minLines = lineas,
        leadingIcon = if (lineas == 1) ({ Icon(Icons.Rounded.Title, contentDescription = null) }) else null,
        modifier = Modifier.fillMaxWidth()
    )
}
