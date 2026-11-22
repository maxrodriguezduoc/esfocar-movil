package com.example.esfocarapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.local.Documento
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ViewModel de los documentos: cargar, listar y eliminar archivos PDF / Word.
// Leer los datos del archivo (nombre, tamaño) es trabajo de Android y se hace en la pantalla.
class DocumentosViewModel(private val repo: AppRepository) : ViewModel() {

    val documentos: Flow<List<Documento>> = repo.documentos

    var mensaje by mutableStateOf<String?>(null)
        private set

    fun avisar(texto: String) {
        mensaje = texto
    }

    // Devuelve true si el documento se aceptó y se guardó
    fun agregar(nombre: String, uri: String, tipo: String, tamano: Long): Boolean {
        val error = Validador.documento(tipo, tamano)
        if (error != null) {
            mensaje = error
            return false
        }
        viewModelScope.launch {
            repo.guardarDocumento(nombre, uri, tipo, tamano)
            mensaje = "Documento cargado: $nombre"
        }
        return true
    }

    fun borrar(documento: Documento) {
        viewModelScope.launch {
            repo.borrarDocumento(documento)
            mensaje = "Documento eliminado"
        }
    }
}
