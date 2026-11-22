package com.example.esfocarapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Un DAO define las operaciones (guardar, buscar, borrar) sobre las tablas

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario)

    @Query("SELECT * FROM usuarios WHERE alias = :alias LIMIT 1")
    suspend fun buscarPorAlias(alias: String): Usuario?
}

@Dao
abstract class ContenidoDao {
    @Query("SELECT * FROM asignaturas ORDER BY orden")
    abstract fun asignaturas(): Flow<List<AsignaturaEntity>>

    @Query("SELECT * FROM unidades ORDER BY asignaturaId, numero")
    abstract fun unidades(): Flow<List<UnidadEntity>>

    @Query("SELECT * FROM unidades WHERE id = :id")
    abstract suspend fun unidad(id: Long): UnidadEntity?

    @Query("SELECT * FROM fichas WHERE unidadId = :unidadId ORDER BY id")
    abstract fun fichas(unidadId: Long): Flow<List<FichaEntity>>

    @Query("SELECT * FROM fichas WHERE id = :id")
    abstract suspend fun ficha(id: Long): FichaEntity?

    @Query("SELECT * FROM fichas WHERE id IN (:ids) ORDER BY titulo")
    abstract fun fichasPorIds(ids: List<Long>): Flow<List<FichaEntity>>

    @Query(
        "SELECT * FROM fichas WHERE titulo LIKE '%' || :texto || '%' " +
            "OR contenido LIKE '%' || :texto || '%' OR puntosClave LIKE '%' || :texto || '%' ORDER BY titulo"
    )
    abstract fun buscarFichas(texto: String): Flow<List<FichaEntity>>

    @Query("SELECT * FROM preguntas WHERE unidadId = :unidadId ORDER BY id")
    abstract suspend fun preguntasDeUnidad(unidadId: Long): List<PreguntaEntity>

    @Query("SELECT p.* FROM preguntas p JOIN unidades u ON p.unidadId = u.id WHERE u.asignaturaId = :asignaturaId")
    abstract suspend fun preguntasDeAsignatura(asignaturaId: Long): List<PreguntaEntity>

    @Query("SELECT * FROM preguntas")
    abstract suspend fun todasLasPreguntas(): List<PreguntaEntity>

    @Query("SELECT * FROM preguntas WHERE id IN (:ids)")
    abstract suspend fun preguntasPorIds(ids: List<Long>): List<PreguntaEntity>

    @Query("SELECT * FROM casos ORDER BY unidadId, id")
    abstract fun casos(): Flow<List<CasoEntity>>

    @Query("SELECT * FROM casos WHERE id = :id")
    abstract suspend fun caso(id: Long): CasoEntity?

    @Query("SELECT COUNT(*) FROM asignaturas")
    abstract suspend fun contarAsignaturas(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarAsignaturas(lista: List<AsignaturaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarUnidades(lista: List<UnidadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarFichas(lista: List<FichaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarPreguntas(lista: List<PreguntaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarCasos(lista: List<CasoEntity>)

    @Query("DELETE FROM asignaturas") abstract suspend fun borrarAsignaturas()
    @Query("DELETE FROM unidades") abstract suspend fun borrarUnidades()
    @Query("DELETE FROM fichas") abstract suspend fun borrarFichas()
    @Query("DELETE FROM preguntas") abstract suspend fun borrarPreguntas()
    @Query("DELETE FROM casos") abstract suspend fun borrarCasos()

    // Reemplaza todo el contenido en una sola transacción (o se aplica completo o no se aplica)
    @Transaction
    open suspend fun reemplazarTodo(contenido: ContenidoEntidades) {
        borrarCasos()
        borrarPreguntas()
        borrarFichas()
        borrarUnidades()
        borrarAsignaturas()
        insertarAsignaturas(contenido.asignaturas)
        insertarUnidades(contenido.unidades)
        insertarFichas(contenido.fichas)
        insertarPreguntas(contenido.preguntas)
        insertarCasos(contenido.casos)
    }
}

@Dao
interface ProgresoDao {
    @Insert
    suspend fun insertarIntento(intento: Intento): Long

    @Query("SELECT * FROM intentos WHERE alias = :alias ORDER BY timestamp DESC")
    fun intentos(alias: String): Flow<List<Intento>>

    @Query("SELECT * FROM intentos WHERE sincronizado = 0 ORDER BY id")
    suspend fun intentosPendientes(): List<Intento>

    @Query("UPDATE intentos SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: Long)

    @Insert
    suspend fun insertarRespuesta(respuesta: RespuestaEntity)

    @Query(
        "SELECT unidadId, SUM(correcta) AS aciertos, COUNT(*) AS total FROM respuestas " +
            "WHERE alias = :alias GROUP BY unidadId"
    )
    fun rendimientoPorUnidad(alias: String): Flow<List<RendimientoUnidad>>

    // Preguntas cuya ÚLTIMA respuesta fue incorrecta (se "limpian" al responderlas bien)
    @Query(
        "SELECT r.preguntaId FROM respuestas r WHERE r.alias = :alias AND r.correcta = 0 AND r.id = " +
            "(SELECT MAX(r2.id) FROM respuestas r2 WHERE r2.alias = r.alias AND r2.preguntaId = r.preguntaId)"
    )
    fun errores(alias: String): Flow<List<Long>>

    @Query(
        "SELECT r.preguntaId FROM respuestas r WHERE r.alias = :alias AND r.correcta = 0 AND r.id = " +
            "(SELECT MAX(r2.id) FROM respuestas r2 WHERE r2.alias = r.alias AND r2.preguntaId = r.preguntaId)"
    )
    suspend fun erroresActuales(alias: String): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarFavorito(favorito: Favorito)

    @Query("DELETE FROM favoritos WHERE alias = :alias AND fichaId = :fichaId")
    suspend fun quitarFavorito(alias: String, fichaId: Long)

    @Query("SELECT fichaId FROM favoritos WHERE alias = :alias")
    fun favoritos(alias: String): Flow<List<Long>>
}

@Dao
interface ApunteDao {
    @Query("SELECT * FROM apuntes WHERE alias = :alias AND estado != 'ELIMINAR' ORDER BY id DESC")
    fun apuntes(alias: String): Flow<List<ApunteLocal>>

    @Query("SELECT * FROM apuntes WHERE estado != 'SINCRONIZADO' ORDER BY id")
    suspend fun pendientes(): List<ApunteLocal>

    @Query("SELECT * FROM apuntes WHERE alias = :alias")
    suspend fun todosDe(alias: String): List<ApunteLocal>

    @Insert
    suspend fun insertar(apunte: ApunteLocal): Long

    @Update
    suspend fun actualizar(apunte: ApunteLocal)

    @Delete
    suspend fun eliminar(apunte: ApunteLocal)
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
