package br.gov.pm.gestaoefetivo.formacao.dto;

import br.gov.pm.gestaoefetivo.formacao.StatusPessoaCurso;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaCursoRequest(
        @NotNull Long cursoId,
        LocalDate dataConclusao,
        LocalDate dataValidade,
        String certificadoUrl,
        @NotNull StatusPessoaCurso status
) {
}
