package com.example.esfocarapp.data

import com.example.esfocarapp.model.Alternativa
import com.example.esfocarapp.model.Asignatura
import com.example.esfocarapp.model.Pregunta

// Contenido de ejemplo (ficticio). En la versión final vendrá de una API REST.
object FakeData {

    val asignaturas = listOf(
        Asignatura(1, "Fundamentos de Procedimientos", "Conceptos base y protocolos de actuación (ficticio)", 4),
        Asignatura(2, "Normativa Institucional", "Reglamentos y deberes del estudiante (ficticio)", 3),
        Asignatura(3, "Primeros Auxilios", "Atención inicial en situaciones de emergencia (ficticio)", 5),
        Asignatura(4, "Comunicación y Trato", "Comunicación efectiva con la comunidad (ficticio)", 3)
    )

    val preguntas = listOf(
        Pregunta(
            "Ante una situación de emergencia, ¿cuál es el primer paso?",
            listOf(
                Alternativa("Evaluar la seguridad del lugar", true, "Correcto: primero se asegura el entorno."),
                Alternativa("Actuar de inmediato sin evaluar", false, "Incorrecto: actuar sin evaluar aumenta el riesgo."),
                Alternativa("Esperar instrucciones sin informar", false, "Incorrecto: se debe informar y coordinar.")
            )
        ),
        Pregunta(
            "¿Qué se debe hacer antes de iniciar un procedimiento?",
            listOf(
                Alternativa("Revisar el protocolo aplicable", true, "Correcto: el protocolo guía la actuación."),
                Alternativa("Improvisar según la experiencia", false, "Incorrecto: se debe seguir el protocolo.")
            )
        ),
        Pregunta(
            "La retroalimentación inmediata ayuda principalmente a...",
            listOf(
                Alternativa("Corregir errores mientras se aprende", true, "Correcto: permite ajustar de inmediato."),
                Alternativa("Reemplazar el estudio", false, "Incorrecto: complementa el estudio."),
                Alternativa("Evitar las evaluaciones", false, "Incorrecto: no reemplaza las evaluaciones.")
            )
        )
    )
}
