package com.example.esfocarapp.data

import com.example.esfocarapp.data.local.AsignaturaEntity
import com.example.esfocarapp.data.local.CasoEntity
import com.example.esfocarapp.data.local.ContenidoEntidades
import com.example.esfocarapp.data.local.FichaEntity
import com.example.esfocarapp.data.local.PreguntaEntity
import com.example.esfocarapp.data.local.UnidadEntity
import com.example.esfocarapp.data.remote.PaqueteDto
import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.Asignatura
import com.example.esfocarapp.model.Caso
import com.example.esfocarapp.model.Ficha
import com.example.esfocarapp.model.OpcionCaso
import com.example.esfocarapp.model.Pregunta
import com.example.esfocarapp.model.Unidad
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Convierte entre los tres "idiomas" de los datos: JSON del servidor (DTO), tablas Room (Entity) y modelo de la app.
object MapeoContenido {

    val gson = Gson()
    // getParameterized no depende de firmas genéricas, por lo que sigue funcionando tras la optimización de R8
    private val tipoAlternativas = TypeToken.getParameterized(List::class.java, Alternativa::class.java).type
    private val tipoOpciones = TypeToken.getParameterized(List::class.java, OpcionCaso::class.java).type

    fun leerPaquete(json: String): PaqueteDto = gson.fromJson(json, PaqueteDto::class.java)

    fun aEntidades(paquete: PaqueteDto): ContenidoEntidades {
        val asignaturas = mutableListOf<AsignaturaEntity>()
        val unidades = mutableListOf<UnidadEntity>()
        val fichas = mutableListOf<FichaEntity>()
        val preguntas = mutableListOf<PreguntaEntity>()
        val casos = mutableListOf<CasoEntity>()

        paquete.asignaturas.forEachIndexed { orden, a ->
            asignaturas += AsignaturaEntity(a.id, a.nombre, a.descripcion, a.icono, a.color, orden)
            a.unidades.forEach { u ->
                unidades += UnidadEntity(u.id, a.id, u.numero, u.titulo, u.resumen)
                u.fichas.forEach { f ->
                    fichas += FichaEntity(f.id, u.id, f.titulo, f.contenido, f.puntosClave.joinToString("\n"))
                }
                u.preguntas.forEach { p ->
                    val alternativas = p.alternativas.map { Alternativa(it.texto, it.correcta, it.retroalimentacion) }
                    preguntas += PreguntaEntity(p.id, u.id, p.enunciado, p.explicacion ?: "", gson.toJson(alternativas))
                }
                u.casos.forEach { c ->
                    val opciones = c.opciones.map { OpcionCaso(it.texto, it.puntaje, it.retroalimentacion) }
                    casos += CasoEntity(
                        c.id, u.id, c.titulo, c.situacion, c.pregunta, c.dificultad, c.fundamento ?: "",
                        gson.toJson(opciones)
                    )
                }
            }
        }
        return ContenidoEntidades(asignaturas, unidades, fichas, preguntas, casos)
    }

    fun asignatura(e: AsignaturaEntity) = Asignatura(e.id, e.nombre, e.descripcion, e.icono, e.color)

    fun unidad(e: UnidadEntity) = Unidad(e.id, e.asignaturaId, e.numero, e.titulo, e.resumen)

    fun ficha(e: FichaEntity) = Ficha(
        e.id, e.unidadId, e.titulo, e.contenido,
        e.puntosClave.split("\n").filter { it.isNotBlank() }
    )

    fun pregunta(e: PreguntaEntity) = Pregunta(
        e.id, e.unidadId, e.enunciado, gson.fromJson(e.alternativas, tipoAlternativas), e.explicacion
    )

    fun caso(e: CasoEntity) = Caso(
        e.id, e.unidadId, e.titulo, e.situacion, e.pregunta, e.dificultad, e.fundamento,
        gson.fromJson(e.opciones, tipoOpciones)
    )
}
