package com.example.esfocarapp.data

import com.example.esfocarapp.data.local.AppDatabase
import com.example.esfocarapp.data.local.Apunte
import com.example.esfocarapp.data.local.Documento
import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.data.local.Usuario
import com.example.esfocarapp.util.Seguridad
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// El repositorio es el "intermediario": los ViewModels le piden datos a él
// y él decide de dónde salen (hoy: Room; mañana: Room + API).
class AppRepository(db: AppDatabase) {

    private val usuarioDao = db.usuarioDao()
    private val apunteDao = db.apunteDao()
    private val intentoDao = db.intentoDao()
    private val documentoDao = db.documentoDao()

    private fun fechaActual(): String {
        return SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    // ---------- Usuarios ----------
    suspend fun registrarUsuario(alias: String, clave: String): Boolean {
        if (usuarioDao.buscarPorAlias(alias) != null) return false   // ya existe
        usuarioDao.insertar(Usuario(alias = alias, claveHash = Seguridad.hash(clave)))
        return true
    }

    suspend fun iniciarSesion(alias: String, clave: String): Boolean {
        val usuario = usuarioDao.buscarPorAlias(alias)
        return usuario != null && usuario.claveHash == Seguridad.hash(clave)
    }

    // ---------- Apuntes ----------
    val apuntes: Flow<List<Apunte>> = apunteDao.todos()

    suspend fun guardarApunte(titulo: String, contenido: String) {
        apunteDao.insertar(Apunte(titulo = titulo, contenido = contenido, fecha = fechaActual()))
    }

    suspend fun borrarApunte(apunte: Apunte) {
        apunteDao.eliminar(apunte)
    }

    // ---------- Resultados de trivias ----------
    val intentos: Flow<List<Intento>> = intentoDao.todos()

    suspend fun guardarIntento(actividad: String, aciertos: Int, total: Int) {
        intentoDao.insertar(
            Intento(actividad = actividad, aciertos = aciertos, total = total, fecha = fechaActual())
        )
    }

    // ---------- Documentos (PDF / Word) ----------
    val documentos: Flow<List<Documento>> = documentoDao.todos()

    suspend fun guardarDocumento(nombre: String, uri: String, tipo: String, tamano: Long) {
        documentoDao.insertar(
            Documento(nombre = nombre, uri = uri, tipo = tipo, tamano = tamano, fecha = fechaActual())
        )
    }

    suspend fun borrarDocumento(documento: Documento) {
        documentoDao.eliminar(documento)
    }
}
