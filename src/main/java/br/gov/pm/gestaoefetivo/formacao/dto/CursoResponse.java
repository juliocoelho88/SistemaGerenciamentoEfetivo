package br.gov.pm.gestaoefetivo.formacao.dto;

public record CursoResponse(
        Long id,
        String nome,
        String instituicao,
        Integer cargaHoraria,
        boolean exigeValidade,
        Integer validadeMeses
) {
}
