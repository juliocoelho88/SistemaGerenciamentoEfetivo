package br.gov.pm.gestaoefetivo.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException de(String entidade, Object id) {
        return new RecursoNaoEncontradoException(entidade + " não encontrado(a): " + id);
    }
}
