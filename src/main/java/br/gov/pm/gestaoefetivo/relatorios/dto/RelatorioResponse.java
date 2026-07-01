package br.gov.pm.gestaoefetivo.relatorios.dto;

import java.util.List;

public record RelatorioResponse(
        long total,
        List<RelatorioAgrupamentoResponse> porLotacao,
        List<RelatorioAgrupamentoResponse> porSituacao,
        List<RelatorioAgrupamentoResponse> porPosto
) {
}
