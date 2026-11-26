package com.example.esfocarapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.esfocarapp.AppContainer
import com.example.esfocarapp.BuildConfig
import com.example.esfocarapp.EsfocarApp

// Le dice a Android cómo construir cada ViewModel entregándole sus repositorios.
// Las pantallas lo usan así: viewModel(factory = Fabrica.Proveedor)
object Fabrica {
    val Proveedor: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AuthViewModel(contenedor().appRepository, contenedor().preferencias, BuildConfig.CODIGO_DOCENTE)
        }
        initializer { AjustesViewModel(contenedor().preferencias, contenedor().sincronizador) }
        initializer { EstudioViewModel(contenedor().contenidoRepository, contenedor().progresoRepository) }
        initializer {
            val c = contenedor()
            ProgresoViewModel(c.contenidoRepository, c.progresoRepository, c.monitorConexion.enLinea)
        }
        initializer {
            val argumentos = createSavedStateHandle()
            QuizViewModel(
                contenedor().contenidoRepository, contenedor().progresoRepository,
                ModoQuiz.valueOf(argumentos.texto("modo") ?: ModoQuiz.RAPIDA.name),
                argumentos.numero("id")
            )
        }
        initializer {
            CasoViewModel(
                contenedor().contenidoRepository, contenedor().progresoRepository,
                createSavedStateHandle().numero("id")
            )
        }
        initializer { ApuntesViewModel(contenedor().apuntesRepository) }
        initializer { DocenteViewModel(contenedor().contenidoRepository, contenedor().gestionContenidoRepository) }
        initializer {
            val argumentos = createSavedStateHandle()
            FormPreguntaViewModel(
                contenedor().contenidoRepository, contenedor().gestionContenidoRepository,
                argumentos.numero("unidadId"), argumentos.numero("id")
            )
        }
        initializer {
            val argumentos = createSavedStateHandle()
            FormCasoViewModel(
                contenedor().contenidoRepository, contenedor().gestionContenidoRepository,
                argumentos.numero("unidadId"), argumentos.numero("id")
            )
        }
        initializer { DocumentosViewModel(contenedor().appRepository) }
        initializer { ConceptosViewModel(contenedor().conceptosRepository) }
    }

    private fun CreationExtras.contenedor(): AppContainer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as EsfocarApp
        return app.container
    }

    // Los argumentos de navegación llegan como texto
    private fun SavedStateHandle.texto(clave: String): String? = get<Any>(clave)?.toString()
    private fun SavedStateHandle.numero(clave: String): Long = texto(clave)?.toLongOrNull() ?: 0L
}
