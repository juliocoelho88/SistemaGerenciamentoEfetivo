package br.gov.pm.gestaoefetivo.efetivo.dto;

public record SituacaoFuncionalResponse(
        Long id,
        String nome,
        boolean disponivelOperacao
) {
}
