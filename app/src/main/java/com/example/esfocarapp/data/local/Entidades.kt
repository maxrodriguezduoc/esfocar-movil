package com.example.esfocarapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Cada clase con @Entity es una TABLA de la base de datos local (Room / SQLite)

@Entity(tableName = "usuarios", indices = [Index(value = ["alias"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alias: String,
    val claveHash: String   // la contraseña nunca se guarda en texto plano
)

@Entity(tableName = "apuntes")
data class Apunte(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val contenido: String,
    val fecha: String
)

@Entity(tableName = "intentos")
data class Intento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actividad: String,
    val aciertos: Int,
    val total: Int,
    val fecha: String
)

// Documento cargado por el usuario (PDF o Word). Guardamos sus datos y la dirección (uri) del archivo.
@Entity(tableName = "documentos")
data class Documento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val uri: String,
    val tipo: String,
    val tamano: Long,
    val fecha: String
)
