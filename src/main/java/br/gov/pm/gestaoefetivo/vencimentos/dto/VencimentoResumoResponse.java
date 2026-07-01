package br.gov.pm.gestaoefetivo.vencimentos.dto;

import java.util.List;

public record VencimentoResumoResponse(
        List<VencimentoItemResponse> itens,
        long vencidos,
        long vencendo,
        long pessoasAfetadas
) {
}
