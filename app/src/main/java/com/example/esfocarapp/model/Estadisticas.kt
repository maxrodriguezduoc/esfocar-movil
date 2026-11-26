package com.example.esfocarapp.model

import com.example.esfocarapp.data.local.Intento
import com.example.esfocarapp.data.local.RendimientoUnidad
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

// Reglas de cálculo puras (sin Android): fáciles de probar con pruebas unitarias.

object Calificacion {

    const val EXIGENCIA = 0.6

    fun porcentaje(aciertos: Int, total: Int): Int =
        if (total <= 0) 0 else (aciertos * 100.0 / total).roundToInt()

    // Escala chilena de 1.0 a 7.0 con 60 % de exigencia (nota 4.0 = 60 % de logro)
    fun nota(aciertos: Int, total: Int, exigencia: Double = EXIGENCIA): Double {
        if (total <= 0) return 1.0
        val logro = aciertos.toDouble() / total
        val nota = if (logro < exigencia) {
            1 + 3 * logro / exigencia
        } else {
            4 + 3 * (logro - exigencia) / (1 - exigencia)
        }
        return (nota * 10).roundToInt() / 10.0
    }

    fun aprobado(nota: Double): Boolean = nota >= 4.0
}

enum class NivelActuacion(val etiqueta: String) {
    OPTIMA("Actuación óptima"),
    ACEPTABLE("Actuación aceptable"),
    INCORRECTA("Actuación incorrecta");

    companion object {
        fun de(puntaje: Int): NivelActuacion = when {
            puntaje >= 2 -> OPTIMA
            puntaje == 1 -> ACEPTABLE
            else -> INCORRECTA
        }
    }
}

object Racha {
    // Días seguidos con al menos una actividad. Cuenta hasta hoy o, si hoy aún no estudia, hasta ayer.
    fun calcular(marcas: List<Long>, hoy: LocalDate, zona: ZoneId): Int {
        val dias = marcas.map { Instant.ofEpochMilli(it).atZone(zona).toLocalDate() }.toSet()
        var dia = if (hoy in dias) hoy else hoy.minusDays(1)
        var cantidad = 0
        while (dia in dias) {
            cantidad++
            dia = dia.minusDays(1)
        }
        return cantidad
    }

    // Actividades por día de los últimos 7 días (de más antiguo a hoy)
    fun ultimaSemana(marcas: List<Long>, hoy: LocalDate, zona: ZoneId): List<Int> {
        val dias = marcas.map { Instant.ofEpochMilli(it).atZone(zona).toLocalDate() }
        return (6 downTo 0).map { atras -> dias.count { it == hoy.minusDays(atras.toLong()) } }
    }
}

// El "caso del día" cambia cada día pero es el mismo para todo el día
fun casoDelDia(casos: List<Caso>, fecha: LocalDate): Caso? {
    if (casos.isEmpty()) return null
    val indice = Math.floorMod(fecha.toEpochDay(), casos.size.toLong()).toInt()
    return casos[indice]
}

data class AvanceAsignatura(
    val asignatura: Asignatura,
    val unidades: Int,
    val porcentaje: Int?,      // null = aún no responde preguntas de esta asignatura
    val respondidas: Int
)

data class AvanceUnidad(
    val unidad: Unidad,
    val porcentaje: Int?,
    val respondidas: Int
)

data class TemaDebil(val unidad: Unidad, val asignatura: String, val porcentaje: Int)

data class Panel(
    val actividades: Int = 0,
    val promedio: Int? = null,
    val racha: Int = 0,
    val mejorNota: Double? = null,
    val avance: List<AvanceAsignatura> = emptyList(),
    val debiles: List<TemaDebil> = emptyList(),
    val recientes: List<Intento> = emptyList(),
    val errores: Int = 0,
    val casosResueltos: Int = 0,
    val totalCasos: Int = 0,
    val semana: List<Int> = List(7) { 0 }
)

object ConstructorPanel {

    const val UMBRAL_DEBIL = 60
    private const val MINIMO_RESPUESTAS = 3

    fun avanceUnidades(unidades: List<Unidad>, rendimiento: List<RendimientoUnidad>): List<AvanceUnidad> {
        val porUnidad = rendimiento.associateBy { it.unidadId }
        return unidades.map { u ->
            val r = porUnidad[u.id]
            AvanceUnidad(u, r?.let { Calificacion.porcentaje(it.aciertos, it.total) }, r?.total ?: 0)
        }
    }

    fun avanceAsignaturas(
        asignaturas: List<Asignatura>,
        unidades: List<Unidad>,
        rendimiento: List<RendimientoUnidad>
    ): List<AvanceAsignatura> {
        val asignaturaDeUnidad = unidades.associate { it.id to it.asignaturaId }
        return asignaturas.map { a ->
            val propios = rendimiento.filter { asignaturaDeUnidad[it.unidadId] == a.id }
            val aciertos = propios.sumOf { it.aciertos }
            val total = propios.sumOf { it.total }
            AvanceAsignatura(
                asignatura = a,
                unidades = unidades.count { it.asignaturaId == a.id },
                porcentaje = if (total == 0) null else Calificacion.porcentaje(aciertos, total),
                respondidas = total
            )
        }
    }

    fun construir(
        asignaturas: List<Asignatura>,
        unidades: List<Unidad>,
        intentos: List<Intento>,
        rendimiento: List<RendimientoUnidad>,
        errores: Int,
        totalCasos: Int,
        hoy: LocalDate,
        zona: ZoneId
    ): Panel {
        val nombreAsignatura = asignaturas.associate { it.id to it.nombre }
        val debiles = avanceUnidades(unidades, rendimiento)
            .filter { it.respondidas >= MINIMO_RESPUESTAS && (it.porcentaje ?: 100) < UMBRAL_DEBIL }
            .sortedBy { it.porcentaje }
            .take(3)
            .map { TemaDebil(it.unidad, nombreAsignatura[it.unidad.asignaturaId] ?: "", it.porcentaje ?: 0) }

        val marcas = intentos.map { it.timestamp }
        return Panel(
            actividades = intentos.size,
            promedio = if (intentos.isEmpty()) null
            else intentos.map { Calificacion.porcentaje(it.aciertos, it.total) }.average().roundToInt(),
            racha = Racha.calcular(marcas, hoy, zona),
            mejorNota = intentos.filter { it.tipo == TipoActividad.SIMULACRO.name }
                .maxOfOrNull { Calificacion.nota(it.aciertos, it.total) },
            avance = avanceAsignaturas(asignaturas, unidades, rendimiento),
            debiles = debiles,
            recientes = intentos.sortedByDescending { it.timestamp }.take(10),
            errores = errores,
            casosResueltos = intentos.filter { it.tipo == TipoActividad.CASO.name }.mapNotNull { it.refId }.toSet().size,
            totalCasos = totalCasos,
            semana = Racha.ultimaSemana(marcas, hoy, zona)
        )
    }
}
