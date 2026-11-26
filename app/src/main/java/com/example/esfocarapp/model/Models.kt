package com.example.esfocarapp.model

// Modelos del dominio que usan los ViewModels y las pantallas.
// El contenido académico es ficticio y proviene del microservicio ms-contenidos (guardado en Room).

data class Asignatura(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val color: String
)

data class Unidad(
    val id: Long,
    val asignaturaId: Long,
    val numero: Int,
    val titulo: String,
    val resumen: String
)

// Ficha de estudio: texto de lectura y sus puntos clave
data class Ficha(
    val id: Long,
    val unidadId: Long,
    val titulo: String,
    val contenido: String,
    val puntosClave: List<String>
)

data class Alternativa(
    val texto: String,
    val esCorrecta: Boolean,
    val retroalimentacion: String
)

data class Pregunta(
    val id: Long,
    val unidadId: Long,
    val enunciado: String,
    val alternativas: List<Alternativa>,
    val explicacion: String = ""
)

// Opción de actuación en un caso simulado: 2 = óptima, 1 = aceptable, 0 = incorrecta
data class OpcionCaso(
    val texto: String,
    val puntaje: Int,
    val retroalimentacion: String
)

data class Caso(
    val id: Long,
    val unidadId: Long,
    val titulo: String,
    val situacion: String,
    val pregunta: String,
    val dificultad: String,
    val fundamento: String,
    val opciones: List<OpcionCaso>
)

enum class TipoActividad(val etiqueta: String) {
    TRIVIA("Trivia"),
    SIMULACRO("Simulacro"),
    CASO("Caso simulado"),
    REPASO("Repaso de errores")
}

enum class Dificultad(val etiqueta: String) {
    BASICA("Básica"), MEDIA("Media"), AVANZADA("Avanzada");

    companion object {
        fun de(valor: String): Dificultad = entries.find { it.name == valor } ?: MEDIA
    }
}
