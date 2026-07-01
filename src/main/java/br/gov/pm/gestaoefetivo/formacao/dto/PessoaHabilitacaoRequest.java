package br.gov.pm.gestaoefetivo.formacao.dto;

import br.gov.pm.gestaoefetivo.formacao.StatusHabilitacao;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PessoaHabilitacaoRequest(
        @NotNull Long habilitacaoId,
        String numero,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        @NotNull StatusHabilitacao status
) {
}
