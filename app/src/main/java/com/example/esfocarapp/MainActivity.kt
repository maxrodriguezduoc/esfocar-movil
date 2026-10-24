package com.example.esfocarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.esfocarapp.ui.navigation.AppNavigation
import com.example.esfocarapp.ui.theme.EsfocarTheme

// Única Activity de la app. Todas las pantallas se muestran dentro de AppNavigation.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EsfocarTheme {
                AppNavigation()
            }
        }
    }
}
