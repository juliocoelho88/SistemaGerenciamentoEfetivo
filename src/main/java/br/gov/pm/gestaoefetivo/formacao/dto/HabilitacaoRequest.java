package br.gov.pm.gestaoefetivo.formacao.dto;

import jakarta.validation.constraints.NotBlank;

public record HabilitacaoRequest(
        @NotBlank String nome,
        String categoria,
        boolean exigeValidade,
        String orgaoEmissor,
        Integer validadeMeses
) {
}
