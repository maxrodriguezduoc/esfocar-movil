package com.example.esfocarapp.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.ui.components.CampoTexto
import com.example.esfocarapp.ui.components.FondoInstitucional
import com.example.esfocarapp.ui.components.LogoEsfocar
import com.example.esfocarapp.viewmodel.AuthViewModel
import com.example.esfocarapp.viewmodel.Fabrica

// Se usa para DOS vistas:
//  registro = false -> vista "Iniciar sesión"
//  registro = true  -> vista "Registrar usuario"
@Composable
fun LoginScreen(
    registro: Boolean,
    onIngreso: () -> Unit,
    onCambiarVista: () -> Unit,
    vm: AuthViewModel = viewModel(factory = Fabrica.Proveedor)
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(FondoInstitucional())
            .safeDrawingPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            LogoEsfocar(alto = 120.dp)
            Text("ESFOCAR Estudio", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "Escuela de Formación de Carabineros",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (registro) "Crear cuenta" else "Iniciar sesión",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Usa un alias ficticio, no tu nombre real.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    CampoTexto(
                        valor = vm.alias,
                        alCambiar = vm::cambiarAlias,
                        etiqueta = "Usuario",
                        icono = Icons.Default.Person,
                        error = vm.errorAlias
                    )
                    CampoTexto(
                        valor = vm.clave,
                        alCambiar = vm::cambiarClave,
                        etiqueta = "Contraseña",
                        icono = Icons.Default.Lock,
                        error = vm.errorClave,
                        esClave = true
                    )
                    // Solo en la vista de registro se pide repetir la contraseña
                    if (registro) {
                        CampoTexto(
                            valor = vm.confirmacion,
                            alCambiar = vm::cambiarConfirmacion,
                            etiqueta = "Repetir contraseña",
                            icono = Icons.Default.Lock,
                            error = vm.errorConfirmacion,
                            esClave = true
                        )
                    }

                    AnimatedVisibility(visible = vm.mensaje != null) {
                        Text(vm.mensaje ?: "", color = MaterialTheme.colorScheme.error)
                    }

                    Button(
                        onClick = { vm.enviar(registro, onIngreso) },
                        enabled = !vm.cargando,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (vm.cargando) {
                            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (registro) "Registrarme" else "Ingresar")
                        }
                    }

                    TextButton(onClick = onCambiarVista) {
                        Text(if (registro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
                    }
                }
            }

            Text(
                "Contenido de ejemplo con fines académicos",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
