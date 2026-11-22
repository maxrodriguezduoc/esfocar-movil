package com.example.esfocarapp.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.esfocarapp.AppContainer
import com.example.esfocarapp.EsfocarApp

// Le dice a Android cómo construir cada ViewModel entregándole sus repositorios.
// Las pantallas lo usan así: viewModel(factory = Fabrica.Proveedor)
object Fabrica {
    val Proveedor: ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel(contenedor().appRepository) }
        initializer { ApuntesViewModel(contenedor().apuntesRepository) }
        initializer { TriviaViewModel(contenedor().appRepository, contenedor().resultadosRepository) }
        initializer { ProgresoViewModel(contenedor().appRepository, contenedor().resultadosRepository) }
        initializer { DocumentosViewModel(contenedor().appRepository) }
        initializer { ConceptosViewModel(contenedor().conceptosRepository) }
    }

    private fun CreationExtras.contenedor(): AppContainer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EsfocarApp
        return app.container
    }
}
