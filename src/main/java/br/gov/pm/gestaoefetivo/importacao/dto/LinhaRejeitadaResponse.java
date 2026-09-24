package br.gov.pm.gestaoefetivo.importacao.dto;

/**
 * Uma linha que não entrou, com o número da linha como aparece no Excel para o usuário ir direto ao
 * ponto na planilha.
 */
public record LinhaRejeitadaResponse(int linha, String re, String nome, String motivo) {
}
