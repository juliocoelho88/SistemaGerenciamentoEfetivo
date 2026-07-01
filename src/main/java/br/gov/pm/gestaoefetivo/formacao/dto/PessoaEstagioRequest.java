package br.gov.pm.gestaoefetivo.formacao.dto;

import br.gov.pm.gestaoefetivo.formacao.StatusEstagio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaEstagioRequest(
        @NotBlank String nome,
        Long unidadeId,
        String supervisor,
        LocalDate dataInicio,
        LocalDate dataFim,
        String avaliacao,
        @NotNull StatusEstagio status
) {
}
