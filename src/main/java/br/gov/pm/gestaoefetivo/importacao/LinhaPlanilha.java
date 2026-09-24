package br.gov.pm.gestaoefetivo.importacao;

import java.util.Map;

/**
 * Uma linha de dados da planilha, já com os valores das células como texto e associados à coluna que
 * o cabeçalho identificou.
 *
 * @param numero número da linha como aparece no Excel (1-based), para o relatório de erros apontar
 *               exatamente a linha que o usuário precisa corrigir
 */
public record LinhaPlanilha(int numero, Map<ColunaPessoa, String> valores) {

    public String valor(ColunaPessoa coluna) {
        return TextoPlanilha.limpar(valores.get(coluna));
    }

    public boolean vazia() {
        return valores.values().stream().allMatch(v -> TextoPlanilha.limpar(v) == null);
    }
}
