package com.example.esfocarapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// version = 3: los apuntes ahora viven en el microservicio, ya no en la base local
@Database(
    entities = [Usuario::class, Intento::class, Documento::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun intentoDao(): IntentoDao
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
                    // Al subir de versión, recrea las tablas (suficiente para un MVP)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
