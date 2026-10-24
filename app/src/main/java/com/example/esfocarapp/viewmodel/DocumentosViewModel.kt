package com.example.esfocarapp.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.esfocarapp.data.AppRepository
import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.data.local.Documento
import com.example.esfocarapp.util.Validador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ViewModel de los documentos: cargar, listar y eliminar archivos PDF / Word
class DocumentosViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppRepository(AppDatabase.obtener(app))

    val documentos: Flow<List<Documento>> = repo.documentos

    var mensaje by mutableStateOf<String?>(null)
        private set

    fun avisar(texto: String) {
        mensaje = texto
    }

    // Se llama cuando el usuario elige un archivo en el selector del teléfono
    fun agregar(uri: Uri) {
        val contexto = getApplication<Application>()
        val tipo = contexto.contentResolver.getType(uri) ?: ""
        val (nombre, tamano) = leerNombreYTamano(uri)

        // Validación (la regla está en Validador, no aquí en la pantalla)
        val error = Validador.documento(tipo, tamano)
        if (error != null) {
            mensaje = error
            return
        }

        // Pedimos conservar el permiso de lectura para poder abrirlo después
        try {
            contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Si el proveedor no permite conservar el permiso, igual guardamos el registro
        }

        viewModelScope.launch {
            repo.guardarDocumento(nombre, uri.toString(), tipo, tamano)
            mensaje = "Documento cargado: $nombre"
        }
    }

    fun borrar(documento: Documento) {
        viewModelScope.launch {
            repo.borrarDocumento(documento)
            mensaje = "Documento eliminado"
        }
    }

    // Consulta al teléfono el nombre y el tamaño del archivo elegido
    private fun leerNombreYTamano(uri: Uri): Pair<String, Long> {
        var nombre = "documento"
        var tamano = 0L
        val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnaNombre = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val columnaTamano = it.getColumnIndex(OpenableColumns.SIZE)
                if (columnaNombre >= 0) nombre = it.getString(columnaNombre)
                if (columnaTamano >= 0) tamano = it.getLong(columnaTamano)
            }
        }
        return Pair(nombre, tamano)
    }
}
