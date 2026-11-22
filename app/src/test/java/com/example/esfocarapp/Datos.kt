package com.example.esfocarapp

import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.Asignatura
import com.example.esfocarapp.model.Caso
import com.example.esfocarapp.model.Ficha
import com.example.esfocarapp.model.OpcionCaso
import com.example.esfocarapp.model.Pregunta
import com.example.esfocarapp.model.Unidad
import com.example.esfocarapp.util.Preferencias
import com.example.esfocarapp.util.Tema
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset

// Datos de prueba compartidos por las pruebas unitarias
object Datos {
    val ZONA: ZoneOffset = ZoneOffset.UTC
    val HOY: LocalDate = LocalDate.of(2026, 11, 22)

    fun ms(dia: LocalDate, hora: Int = 10): Long = dia.atTime(hora, 0).toInstant(ZONA).toEpochMilli()

    val asignatura = Asignatura(1, "Procedimientos", "Desc", "procedimientos", "#1E5631")
    val asignatura2 = Asignatura(2, "Normativa", "Desc", "normativa", "#7A5900")
    val unidad = Unidad(11, 1, 1, "Principios", "Resumen")
    val unidad2 = Unidad(21, 2, 1, "Deberes", "Resumen")
    val ficha = Ficha(111, 11, "Secuencia", "Texto\n\nOtro", listOf("Uno", "Dos"))

    fun pregunta(id: Long, unidadId: Long = 11) = Pregunta(
        id, unidadId, "¿Pregunta $id?",
        listOf(
            Alternativa("Correcta", true, "Bien"),
            Alternativa("Incorrecta", false, "Mal")
        ),
        "Explicación $id"
    )

    val caso = Caso(
        1151, 11, "Discusión", "Situación", "¿Qué haces?", "BASICA", "Fundamento",
        listOf(OpcionCaso("Óptima", 2, "Bien"), OpcionCaso("Aceptable", 1, "Regular"), OpcionCaso("Mala", 0, "Mal"))
    )

    fun intento(
        id: Long, tipo: String = "TRIVIA", aciertos: Int = 1, total: Int = 2,
        dia: LocalDate = HOY, refId: Long? = null
    ) = Intento(
        id = id, alias = "cadete_01", tipo = tipo, titulo = "Actividad $id", aciertos = aciertos, total = total,
        refId = refId, timestamp = ms(dia), fecha = "x"
    )

    // El mismo archivo que la app trae en assets y que el microservicio usa como semilla
    fun contenidoIncluido(): String = File("src/main/assets/contenido_inicial.json").readText()
}

class PreferenciasFalsas(
    override var onboardingVisto: Boolean = false,
    override var aliasSesion: String? = null,
    override var versionContenido: Int = 0,
    override var tema: Tema = Tema.SISTEMA
) : Preferencias
