package com.example.esfocarapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.example.esfocarapp.data.local.Documento
import java.util.Locale

// Datos básicos de un archivo elegido por el usuario
data class DatosArchivo(val nombre: String, val tipo: String, val tamano: Long)

// Funciones de ayuda para trabajar con archivos
object Documentos {

    // Consulta al teléfono el nombre, tipo y tamaño del archivo elegido
    fun leerDatos(context: Context, uri: Uri): DatosArchivo {
        val tipo = context.contentResolver.getType(uri) ?: ""
        var nombre = "documento"
        var tamano = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnaNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val columnaTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (columnaNombre >= 0) nombre = cursor.getString(columnaNombre)
                if (columnaTamano >= 0) tamano = cursor.getLong(columnaTamano)
            }
        }
        return DatosArchivo(nombre, tipo, tamano)
    }

    // Pide conservar el permiso de lectura para poder abrir el archivo después
    fun conservarPermiso(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Si el proveedor no lo permite, igual guardamos el registro
        }
    }

    // RECURSO NATIVO 3: abrir el archivo con la aplicación instalada (lector de PDF, Word, etc.)
    // Devuelve false si el teléfono no tiene una app que pueda abrirlo.
    fun abrir(context: Context, documento: Documento): Boolean {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(documento.uri), documento.tipo)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun etiquetaTipo(tipo: String): String {
        return if (tipo == "application/pdf") "PDF" else "Word"
    }

    fun formatearTamano(bytes: Long): String {
        val kb = bytes / 1024.0
        return if (kb >= 1024) {
            String.format(Locale.getDefault(), "%.1f MB", kb / 1024)
        } else {
            String.format(Locale.getDefault(), "%.0f KB", kb)
        }
    }
}
