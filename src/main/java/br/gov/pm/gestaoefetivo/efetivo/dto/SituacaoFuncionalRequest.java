package br.gov.pm.gestaoefetivo.efetivo.dto;

import jakarta.validation.constraints.NotBlank;

public record SituacaoFuncionalRequest(
        @NotBlank String nome,
        boolean disponivelOperacao
) {
}
