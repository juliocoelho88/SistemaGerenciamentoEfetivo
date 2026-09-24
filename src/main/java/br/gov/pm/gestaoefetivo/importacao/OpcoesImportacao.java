package br.gov.pm.gestaoefetivo.importacao;

/**
 * Como a importação deve se comportar.
 *
 * @param atualizarExistentes true = quem já está cadastrado (mesmo RE) é atualizado com os dados da
 *                            planilha; false = é deixado como está e contado em "ignoradas"
 * @param simulacao           true = valida tudo e devolve o relatório sem gravar nada
 * @param unidadePadrao       aplicada às linhas sem coluna/valor de unidade (planilha por OPM)
 * @param situacaoPadrao      aplicada às linhas sem coluna/valor de situação
 */
public record OpcoesImportacao(boolean atualizarExistentes,
                               boolean simulacao,
                               String unidadePadrao,
                               String situacaoPadrao) {
}
