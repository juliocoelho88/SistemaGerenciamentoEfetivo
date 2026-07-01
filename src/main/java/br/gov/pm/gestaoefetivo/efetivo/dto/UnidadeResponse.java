package br.gov.pm.gestaoefetivo.efetivo.dto;

import br.gov.pm.gestaoefetivo.efetivo.TipoUnidade;

public record UnidadeResponse(
        Long id,
        String nome,
        String sigla,
        TipoUnidade tipo,
        Long unidadePaiId,
        String unidadePaiNome
) {
}
