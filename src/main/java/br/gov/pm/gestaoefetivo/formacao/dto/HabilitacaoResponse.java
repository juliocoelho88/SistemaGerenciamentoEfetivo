package br.gov.pm.gestaoefetivo.formacao.dto;

public record HabilitacaoResponse(
        Long id,
        String nome,
        String categoria,
        boolean exigeValidade,
        String orgaoEmissor,
        Integer validadeMeses
) {
}
