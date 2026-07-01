package br.gov.pm.gestaoefetivo.efetivo.dto;

public record PessoaResumoResponse(
        Long id,
        String re,
        String nome,
        Long postoId,
        String postoNome,
        String postoAbreviacao,
        Long unidadeId,
        String unidadeNome,
        Long situacaoId,
        String situacaoNome
) {
}
