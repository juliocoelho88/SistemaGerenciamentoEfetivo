package br.gov.pm.gestaoefetivo.importacao;

/**
 * Erro de uma linha da planilha. Não aborta a importação: a linha entra no relatório com o motivo e o
 * processamento segue nas demais — uma planilha de 2.000 linhas não pode ser recusada inteira porque
 * três células estão erradas.
 */
public class LinhaInvalidaException extends RuntimeException {

    public LinhaInvalidaException(String message) {
        super(message);
    }
}
