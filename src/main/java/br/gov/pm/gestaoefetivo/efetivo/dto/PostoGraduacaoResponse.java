package br.gov.pm.gestaoefetivo.efetivo.dto;

import br.gov.pm.gestaoefetivo.efetivo.CirculoPosto;

public record PostoGraduacaoResponse(
        Long id,
        String nome,
        String abreviacao,
        Integer ordemHierarquica,
        CirculoPosto circulo
) {
}
