package com.example.esfocarapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// version 4: contenido académico local (offline-first), progreso detallado, favoritos y apuntes sincronizables
// version 5: rol de usuario (estudiante / docente)
@Database(
    entities = [
        Usuario::class, AsignaturaEntity::class, UnidadEntity::class, FichaEntity::class,
        PreguntaEntity::class, CasoEntity::class, Intento::class, RespuestaEntity::class,
        Favorito::class, ApunteLocal::class, Documento::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun contenidoDao(): ContenidoDao
    abstract fun progresoDao(): ProgresoDao
    abstract fun apunteDao(): ApunteDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        // Agrega la columna "rol" conservando usuarios, progreso y apuntes existentes
        val MIGRACION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN rol TEXT NOT NULL DEFAULT 'ESTUDIANTE'")
            }
        }

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
                    // 4 → 5 se migra sin perder datos; versiones más antiguas se recrean (MVP académico)
                    .addMigrations(MIGRACION_4_5)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
