package br.gov.pm.gestaoefetivo.importacao;

import java.util.List;
import java.util.Map;

/**
 * Conteúdo útil extraído de uma planilha: quais colunas foram reconhecidas no cabeçalho, quais foram
 * ignoradas (ficam no relatório para o usuário perceber que uma coluna não foi aproveitada) e as linhas.
 */
public record PlanilhaPessoa(String aba,
                             Map<ColunaPessoa, Integer> colunas,
                             List<String> colunasIgnoradas,
                             List<LinhaPlanilha> linhas) {
}
