package br.gov.pm.gestaoefetivo.exception;

/** Violação de uma regra de negócio (ex.: duplicidade, estado inválido) — mapeada para HTTP 409/422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
