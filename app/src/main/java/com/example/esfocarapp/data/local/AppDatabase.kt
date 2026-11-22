package com.example.esfocarapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// version = 4: contenido académico local (offline-first), progreso detallado, favoritos y apuntes sincronizables
@Database(
    entities = [
        Usuario::class, AsignaturaEntity::class, UnidadEntity::class, FichaEntity::class,
        PreguntaEntity::class, CasoEntity::class, Intento::class, RespuestaEntity::class,
        Favorito::class, ApunteLocal::class, Documento::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun progresoDao(): ProgresoDao
    abstract fun apunteDao(): ApunteDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        // Crea la base de datos una sola vez y la reutiliza
        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esfocar.db"
                )
                    // Al subir de versión, recrea las tablas (suficiente para un MVP académico)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
