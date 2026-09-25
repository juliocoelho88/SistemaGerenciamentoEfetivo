package br.gov.pm.gestaoefetivo.importacao.dto;

import java.util.List;

/**
 * Consolidado da importação.
 *
 * @param simulacao   quando true nada foi gravado — o relatório mostra o que aconteceria
 * @param criadas     pessoas novas inseridas
 * @param atualizadas pessoas que já existiam e foram atualizadas (só com atualizarExistentes=true)
 * @param ignoradas   pessoas que já existiam e foram deixadas como estavam
 * @param rejeitadas  linhas com erro, detalhadas em cada arquivo
 */
public record ImportacaoResultadoResponse(boolean simulacao,
                                          int arquivos,
                                          int linhasLidas,
                                          int criadas,
                                          int atualizadas,
                                          int ignoradas,
                                          int rejeitadas,
                                          List<ImportacaoArquivoResponse> detalhes) {

    public static ImportacaoResultadoResponse consolidar(boolean simulacao, List<ImportacaoArquivoResponse> detalhes) {
        return new ImportacaoResultadoResponse(
                simulacao,
                detalhes.size(),
                detalhes.stream().mapToInt(ImportacaoArquivoResponse::linhasLidas).sum(),
                detalhes.stream().mapToInt(ImportacaoArquivoResponse::criadas).sum(),
                detalhes.stream().mapToInt(ImportacaoArquivoResponse::atualizadas).sum(),
                detalhes.stream().mapToInt(ImportacaoArquivoResponse::ignoradas).sum(),
                detalhes.stream().mapToInt(ImportacaoArquivoResponse::rejeitadas).sum(),
                detalhes);
    }
}
