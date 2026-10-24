package com.example.esfocarapp.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esfocarapp.ui.components.CampoTexto
import com.example.esfocarapp.ui.components.LogoEsfocar
import com.example.esfocarapp.viewmodel.AuthViewModel

// Se usa para DOS vistas:
//  registro = false -> vista "Iniciar sesión"
//  registro = true  -> vista "Registrar usuario"
@Composable
fun LoginScreen(
    registro: Boolean,
    onIngreso: () -> Unit,
    onCambiarVista: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            LogoEsfocar(alto = 140.dp)

            Text(
                text = if (registro) "Registrar usuario" else "Iniciar sesión",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Usa un alias ficticio (no tu nombre real)",
                style = MaterialTheme.typography.bodySmall
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

            // Animación: el mensaje general aparece y desaparece suavemente
            AnimatedVisibility(visible = vm.mensaje != null) {
                Text(
                    text = vm.mensaje ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { vm.enviar(registro, onIngreso) },
                enabled = !vm.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (registro) "Registrarme" else "Ingresar")
            }

            TextButton(onClick = onCambiarVista) {
                Text(
                    if (registro) "¿Ya tienes cuenta? Inicia sesión"
                    else "¿No tienes cuenta? Regístrate"
                )
            }
        }
    }
}
