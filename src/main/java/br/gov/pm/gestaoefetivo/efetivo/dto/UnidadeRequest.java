package br.gov.pm.gestaoefetivo.efetivo.dto;

import br.gov.pm.gestaoefetivo.efetivo.TipoUnidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UnidadeRequest(
        @NotBlank String nome,
        @NotBlank String sigla,
        @NotNull TipoUnidade tipo,
        Long unidadePaiId
) {
}
