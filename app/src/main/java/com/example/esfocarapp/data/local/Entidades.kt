package com.example.esfocarapp.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Cada clase con @Entity es una TABLA de la base de datos local (Room / SQLite).
// Todo lo necesario para estudiar y autoevaluarse vive aquí, así la app funciona sin internet.

@Entity(tableName = "usuarios", indices = [Index(value = ["alias"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alias: String,
    val claveHash: String,  // la contraseña nunca se guarda en texto plano
    val rol: String = "ESTUDIANTE"
)

// ---------- Contenido académico (copia local del microservicio ms-contenidos) ----------

@Entity(tableName = "asignaturas")
data class AsignaturaEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val color: String,
    val orden: Int
)

@Entity(tableName = "unidades", indices = [Index("asignaturaId")])
data class UnidadEntity(
    @PrimaryKey val id: Long,
    val asignaturaId: Long,
    val numero: Int,
    val titulo: String,
    val resumen: String
)

@Entity(tableName = "fichas", indices = [Index("unidadId")])
data class FichaEntity(
    @PrimaryKey val id: Long,
    val unidadId: Long,
    val titulo: String,
    val contenido: String,
    val puntosClave: String          // un punto por línea
)

@Entity(tableName = "preguntas", indices = [Index("unidadId")])
data class PreguntaEntity(
    @PrimaryKey val id: Long,
    val unidadId: Long,
    val enunciado: String,
    val explicacion: String,
    val alternativas: String         // JSON
)

@Entity(tableName = "casos", indices = [Index("unidadId")])
data class CasoEntity(
    @PrimaryKey val id: Long,
    val unidadId: Long,
    val titulo: String,
    val situacion: String,
    val pregunta: String,
    val dificultad: String,
    val fundamento: String,
    val opciones: String             // JSON
)

data class ContenidoEntidades(
    val asignaturas: List<AsignaturaEntity>,
    val unidades: List<UnidadEntity>,
    val fichas: List<FichaEntity>,
    val preguntas: List<PreguntaEntity>,
    val casos: List<CasoEntity>
)

// ---------- Progreso del estudiante ----------

// Una actividad terminada (trivia, simulacro, caso o repaso). Se sube luego a ms-resultados.
@Entity(tableName = "intentos", indices = [Index("alias")])
data class Intento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val tipo: String,
    val titulo: String,
    val aciertos: Int,
    val total: Int,
    val asignaturaId: Long? = null,
    val refId: Long? = null,         // id del caso, si la actividad fue un caso simulado
    val timestamp: Long,
    val fecha: String,
    val sincronizado: Boolean = false
)

// Cada respuesta individual: permite calcular dominio por unidad y detectar preguntas falladas
@Entity(tableName = "respuestas", indices = [Index(value = ["alias", "preguntaId"])])
data class RespuestaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alias: String,
    val preguntaId: Long,
    val unidadId: Long,
    val correcta: Boolean,
    val timestamp: Long
)

data class RendimientoUnidad(val unidadId: Long, val aciertos: Int, val total: Int)

@Entity(tableName = "favoritos", primaryKeys = ["alias", "fichaId"])
data class Favorito(val alias: String, val fichaId: Long)

// ---------- Apuntes (offline-first: se guardan aquí y luego se sincronizan con ms-apuntes) ----------

object EstadoSync {
    const val SINCRONIZADO = "SINCRONIZADO"
    const val CREAR = "CREAR"
    const val ACTUALIZAR = "ACTUALIZAR"
    const val ELIMINAR = "ELIMINAR"
}

@Entity(tableName = "apuntes", indices = [Index("alias")])
data class ApunteLocal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val idRemoto: Long? = null,
    val alias: String,
    val titulo: String,
    val contenido: String,
    val fecha: String,
    val estado: String = EstadoSync.CREAR
) {
    val pendiente: Boolean get() = estado != EstadoSync.SINCRONIZADO
}

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
