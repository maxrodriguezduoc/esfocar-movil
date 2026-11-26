package cl.esfocar.msresultados;

public class ResultadoNoEncontradoException extends RuntimeException {
    public ResultadoNoEncontradoException(Long id) {
        super("No existe el resultado con id " + id);
    }
}
