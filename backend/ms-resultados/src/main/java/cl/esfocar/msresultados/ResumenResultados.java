package cl.esfocar.msresultados;

/** Resumen estadístico de los intentos de un usuario. */
public record ResumenResultados(String alias, int intentos, double promedioPorcentaje, int mejorPorcentaje) {
}
