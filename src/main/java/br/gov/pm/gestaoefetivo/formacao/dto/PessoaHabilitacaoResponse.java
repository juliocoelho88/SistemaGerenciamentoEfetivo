package br.gov.pm.gestaoefetivo.formacao.dto;

import br.gov.pm.gestaoefetivo.formacao.StatusHabilitacao;

import java.time.LocalDate;

public record PessoaHabilitacaoResponse(
        Long id,
        Long habilitacaoId,
        String habilitacaoNome,
        String categoria,
        String numero,
        LocalDate dataEmissao,
        LocalDate dataValidade,
        StatusHabilitacao status
) {
}
