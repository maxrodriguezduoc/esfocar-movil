package com.example.esfocarapp.data

import com.example.esfocarapp.data.local.Documento
import com.example.esfocarapp.data.local.DocumentoDao
import com.example.esfocarapp.data.local.Usuario
import com.example.esfocarapp.data.local.UsuarioDao
import com.example.esfocarapp.util.Rol
import com.example.esfocarapp.util.Seguridad
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Repositorio de datos LOCALES (Room): usuarios y documentos.
class AppRepository(
    private val usuarioDao: UsuarioDao,
    private val documentoDao: DocumentoDao,
    private val fechaActual: () -> String = ::fechaPorDefecto
) {

    // ---------- Usuarios ----------
    suspend fun registrarUsuario(alias: String, clave: String, rol: String = Rol.ESTUDIANTE): Boolean {
        if (usuarioDao.buscarPorAlias(alias) != null) return false   // ya existe
        usuarioDao.insertar(Usuario(alias = alias, claveHash = Seguridad.hash(clave), rol = rol))
        return true
    }

    // Devuelve el usuario si la clave es correcta (así se conoce su rol), o null si no
    suspend fun autenticar(alias: String, clave: String): Usuario? {
        val usuario = usuarioDao.buscarPorAlias(alias)
        return usuario?.takeIf { it.claveHash == Seguridad.hash(clave) }
    }

    suspend fun iniciarSesion(alias: String, clave: String): Boolean = autenticar(alias, clave) != null

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

    companion object {
        fun fechaPorDefecto(): String =
            SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
    }
}
