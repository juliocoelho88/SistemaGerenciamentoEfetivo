package br.gov.pm.gestaoefetivo.importacao.dto;

import java.util.List;

/** Resultado da importação de um arquivo. */
public record ImportacaoArquivoResponse(String arquivo,
                                        String aba,
                                        List<String> colunasReconhecidas,
                                        List<String> colunasIgnoradas,
                                        int linhasLidas,
                                        int criadas,
                                        int atualizadas,
                                        int ignoradas,
                                        int rejeitadas,
                                        String erro,
                                        List<LinhaRejeitadaResponse> linhasRejeitadas) {

    /** Arquivo que nem chegou a ser lido (formato inválido, cabeçalho ausente, sem coluna obrigatória). */
    public static ImportacaoArquivoResponse falha(String arquivo, String erro) {
        return new ImportacaoArquivoResponse(arquivo, null, List.of(), List.of(), 0, 0, 0, 0, 0, erro, List.of());
    }
}
