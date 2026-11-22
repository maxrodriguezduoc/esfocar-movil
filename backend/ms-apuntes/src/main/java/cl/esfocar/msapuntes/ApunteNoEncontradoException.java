package cl.esfocar.msapuntes;

public class ApunteNoEncontradoException extends RuntimeException {
    public ApunteNoEncontradoException(Long id) {
        super("No existe el apunte con id " + id);
    }
}
