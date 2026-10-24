package com.example.esfocarapp.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.esfocarapp.data.local.Documento
import java.util.Locale

// Funciones de ayuda para trabajar con archivos
object Documentos {

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
