package com.example.esfocarapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.data.local.Intento
import kotlinx.coroutines.flow.Flow

// ViewModel del progreso: entrega la lista de resultados guardados
class ProgresoViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.obtener(app))

    val intentos: Flow<List<Intento>> = repo.intentos
}
