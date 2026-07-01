package br.gov.pm.gestaoefetivo.vencimentos.dto;

import br.gov.pm.gestaoefetivo.formacao.StatusHabilitacao;

import java.time.LocalDate;

public record VencimentoItemResponse(
        Long pessoaId,
        String pessoaNome,
        String postoAbreviacao,
        String unidadeNome,
        String tipo,
        String nome,
        LocalDate dataValidade,
        StatusHabilitacao urgencia
) {
}
