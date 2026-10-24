package com.example.esfocarapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Un DAO define las operaciones (guardar, buscar, borrar) sobre una tabla

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE alias = :alias LIMIT 1")
    suspend fun buscarPorAlias(alias: String): Usuario?
}

@Dao
interface ApunteDao {
    @Insert
    suspend fun insertar(apunte: Apunte)

    @Delete
    suspend fun eliminar(apunte: Apunte)

    // Flow = lista que se actualiza sola cuando cambia la base de datos
    @Query("SELECT * FROM apuntes ORDER BY id DESC")
    fun todos(): Flow<List<Apunte>>
}

@Dao
interface IntentoDao {
    @Insert
    suspend fun insertar(intento: Intento)

    @Query("SELECT * FROM intentos ORDER BY id DESC")
    fun todos(): Flow<List<Intento>>
}

@Dao
interface DocumentoDao {
    @Insert
    suspend fun insertar(documento: Documento)

    @Delete
    suspend fun eliminar(documento: Documento)

    @Query("SELECT * FROM documentos ORDER BY id DESC")
    fun todos(): Flow<List<Documento>>
}
