package com.example.esfocarapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.esfocarapp.R

@Composable
fun LogoEsfocar(modifier: Modifier = Modifier, alto: Dp = 120.dp) {
    Image(
        painter = painterResource(R.drawable.logo_esfocar),
        contentDescription = "Logo Escuela de Formación de Carabineros",
        modifier = modifier.height(alto),
        contentScale = ContentScale.Fit
    )
}
