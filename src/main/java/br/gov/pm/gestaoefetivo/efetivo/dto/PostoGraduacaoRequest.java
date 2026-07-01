package br.gov.pm.gestaoefetivo.efetivo.dto;

import br.gov.pm.gestaoefetivo.efetivo.CirculoPosto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostoGraduacaoRequest(
        @NotBlank String nome,
        @NotBlank String abreviacao,
        @NotNull Integer ordemHierarquica,
        @NotNull CirculoPosto circulo
) {
}
