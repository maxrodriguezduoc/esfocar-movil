package com.example.esfocarapp.model

// Modelos simples del contenido de estudio (datos ficticios)

data class Asignatura(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val unidades: Int
)

data class Alternativa(
    val texto: String,
    val esCorrecta: Boolean,
    val retroalimentacion: String
)

data class Pregunta(
    val enunciado: String,
    val alternativas: List<Alternativa>
)
