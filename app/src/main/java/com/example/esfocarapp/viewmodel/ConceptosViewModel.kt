package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.ConceptosRepository
import com.example.esfocarapp.data.remote.ConceptoRemoto
import com.example.esfocarapp.data.remote.Respuesta
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.launch

// ViewModel de "Explorar conceptos": consulta la API externa (Wikipedia)
class ConceptosViewModel(private val repo: ConceptosRepository) : ViewModel() {

    var termino by mutableStateOf("")
        private set
    var errorTermino by mutableStateOf<String?>(null)
        private set
    var concepto by mutableStateOf<ConceptoRemoto?>(null)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set

    fun cambiarTermino(valor: String) {
        termino = valor
        errorTermino = Validador.termino(valor)
        mensaje = null
    }

    fun buscar() {
        errorTermino = Validador.termino(termino)
        if (errorTermino != null) return

        viewModelScope.launch {
            cargando = true
            mensaje = null
            when (val respuesta = repo.buscar(termino)) {
                is Respuesta.Exito -> concepto = respuesta.datos
                is Respuesta.Fallo -> {
                    concepto = null
                    mensaje = respuesta.mensaje
                }
            }
            cargando = false
        }
    }
}
