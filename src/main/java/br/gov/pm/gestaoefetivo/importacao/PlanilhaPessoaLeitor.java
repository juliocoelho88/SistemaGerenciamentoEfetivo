package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.exception.RegraNegocioException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lê a planilha (.xlsx ou .xls) e devolve as linhas como texto — sem interpretar regra de negócio. */
@Component
public class PlanilhaPessoaLeitor {

    /** O cabeçalho raramente está na primeira linha: planilhas trazem título, brasão e linhas em branco antes. */
    private static final int LINHAS_PROCURADAS_PARA_CABECALHO = 15;

    public PlanilhaPessoa ler(InputStream entrada) {
        try (Workbook workbook = WorkbookFactory.create(entrada)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new RegraNegocioException("A planilha não tem nenhuma aba.");
            }
            Sheet aba = workbook.getSheetAt(0);
            int linhaCabecalho = localizarCabecalho(aba);
            if (linhaCabecalho < 0) {
                throw new RegraNegocioException("Não encontrei a linha de cabeçalho. "
                        + "A planilha precisa ter uma linha com os títulos das colunas (RE, Nome, CPF, Posto...). "
                        + "Baixe o modelo em GET /api/pessoas/importacao/modelo.");
            }
            Map<ColunaPessoa, Integer> colunas = mapearColunas(aba.getRow(linhaCabecalho));
            validarObrigatorias(colunas);
            return new PlanilhaPessoa(aba.getSheetName(), colunas, colunasIgnoradas(aba.getRow(linhaCabecalho)),
                    lerLinhas(aba, linhaCabecalho, colunas));
        } catch (IOException e) {
            throw new RegraNegocioException("Não consegui abrir o arquivo — ele não parece ser uma planilha Excel válida.");
        }
    }

    /** Vale a linha que reconhecer mais colunas; empate fica com a primeira encontrada. */
    private int localizarCabecalho(Sheet aba) {
        int melhorLinha = -1;
        int melhorPontuacao = 0;
        int limite = Math.min(aba.getLastRowNum(), aba.getFirstRowNum() + LINHAS_PROCURADAS_PARA_CABECALHO);
        for (int i = aba.getFirstRowNum(); i <= limite; i++) {
            int pontuacao = mapearColunas(aba.getRow(i)).size();
            if (pontuacao > melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhorLinha = i;
            }
        }
        return melhorLinha;
    }

    private Map<ColunaPessoa, Integer> mapearColunas(Row linha) {
        Map<ColunaPessoa, Integer> colunas = new EnumMap<>(ColunaPessoa.class);
        if (linha == null) {
            return colunas;
        }
        for (int i = linha.getFirstCellNum(); i < linha.getLastCellNum(); i++) {
            int indice = i;
            // putIfAbsent: se a planilha repetir o título, a primeira ocorrência manda.
            ColunaPessoa.porCabecalho(texto(linha.getCell(i))).ifPresent(c -> colunas.putIfAbsent(c, indice));
        }
        return colunas;
    }

    private List<String> colunasIgnoradas(Row cabecalho) {
        List<String> ignoradas = new ArrayList<>();
        if (cabecalho == null) {
            return ignoradas;
        }
        for (int i = cabecalho.getFirstCellNum(); i < cabecalho.getLastCellNum(); i++) {
            String titulo = TextoPlanilha.limpar(texto(cabecalho.getCell(i)));
            if (titulo != null && ColunaPessoa.porCabecalho(titulo).isEmpty()) {
                ignoradas.add(titulo);
            }
        }
        return ignoradas;
    }

    private void validarObrigatorias(Map<ColunaPessoa, Integer> colunas) {
        List<String> faltando = java.util.Arrays.stream(ColunaPessoa.values())
                .filter(ColunaPessoa::obrigatoria)
                .filter(coluna -> !colunas.containsKey(coluna))
                .map(ColunaPessoa::rotulo)
                .toList();
        if (!faltando.isEmpty()) {
            throw new RegraNegocioException("A planilha não tem as colunas obrigatórias: " + String.join(", ", faltando) + ".");
        }
    }

    private List<LinhaPlanilha> lerLinhas(Sheet aba, int linhaCabecalho, Map<ColunaPessoa, Integer> colunas) {
        List<LinhaPlanilha> linhas = new ArrayList<>();
        for (int i = linhaCabecalho + 1; i <= aba.getLastRowNum(); i++) {
            Row linha = aba.getRow(i);
            if (linha == null) {
                continue;
            }
            Map<ColunaPessoa, String> valores = new LinkedHashMap<>();
            colunas.forEach((coluna, indice) -> valores.put(coluna, texto(linha.getCell(indice))));
            LinhaPlanilha lida = new LinhaPlanilha(i + 1, valores);
            if (!lida.vazia()) {
                linhas.add(lida);
            }
        }
        return linhas;
    }

    /**
     * Converte a célula em texto preservando o que importa: número inteiro não vira "123456.0" (o RE
     * digitado como número continua legível) e data vira ISO, independentemente do formato de exibição.
     */
    private String texto(Cell celula) {
        if (celula == null) {
            return null;
        }
        CellType tipo = celula.getCellType() == CellType.FORMULA
                ? celula.getCachedFormulaResultType()
                : celula.getCellType();
        return switch (tipo) {
            case STRING -> celula.getStringCellValue();
            case BOOLEAN -> String.valueOf(celula.getBooleanCellValue());
            case NUMERIC -> numerico(celula);
            default -> null;
        };
    }

    private String numerico(Cell celula) {
        if (DateUtil.isCellDateFormatted(celula)) {
            LocalDateTime data = celula.getLocalDateTimeCellValue();
            return data == null ? null : data.toLocalDate().toString();
        }
        double valor = celula.getNumericCellValue();
        if (valor == Math.rint(valor) && Math.abs(valor) < 1e15) {
            return String.valueOf((long) valor);
        }
        return BigDecimal.valueOf(valor).stripTrailingZeros().toPlainString();
    }
}
