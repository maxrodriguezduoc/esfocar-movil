package com.example.esfocarapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

// Campo de formulario reutilizable. Muestra:
//  - ícono a la izquierda
//  - ícono de error (rojo) o de correcto (verde) a la derecha
//  - mensaje de error debajo
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    esClave: Boolean = false,
    unaLinea: Boolean = true
) {
    val esValido = error == null && valor.isNotEmpty()

    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            } else if (esValido) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Correcto",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = unaLinea,
        modifier = modifier.fillMaxWidth()
    )
}
