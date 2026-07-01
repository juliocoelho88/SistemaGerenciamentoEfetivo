package br.gov.pm.gestaoefetivo.formacao.dto;

import br.gov.pm.gestaoefetivo.formacao.StatusEstagio;

import java.time.LocalDate;

public record PessoaEstagioResponse(
        Long id,
        String nome,
        Long unidadeId,
        String unidadeNome,
        String supervisor,
        LocalDate dataInicio,
        LocalDate dataFim,
        String avaliacao,
        StatusEstagio status
) {
}
