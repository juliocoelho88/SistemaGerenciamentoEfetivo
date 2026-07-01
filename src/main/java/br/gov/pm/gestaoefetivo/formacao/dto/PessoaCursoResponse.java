package br.gov.pm.gestaoefetivo.formacao.dto;

import br.gov.pm.gestaoefetivo.formacao.StatusPessoaCurso;

import java.time.LocalDate;

public record PessoaCursoResponse(
        Long id,
        Long cursoId,
        String cursoNome,
        String instituicao,
        Integer cargaHoraria,
        LocalDate dataConclusao,
        LocalDate dataValidade,
        String certificadoUrl,
        StatusPessoaCurso status
) {
}
