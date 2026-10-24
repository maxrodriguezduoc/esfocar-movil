package com.example.esfocarapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// version = 2 porque agregamos la tabla "documentos"
@Database(
    entities = [Usuario::class, Apunte::class, Intento::class, Documento::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun apunteDao(): ApunteDao
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
