package br.gov.pm.gestaoefetivo.formacao.dto;

import jakarta.validation.constraints.NotBlank;

public record CursoRequest(
        @NotBlank String nome,
        String instituicao,
        Integer cargaHoraria,
        boolean exigeValidade,
        Integer validadeMeses
) {
}
