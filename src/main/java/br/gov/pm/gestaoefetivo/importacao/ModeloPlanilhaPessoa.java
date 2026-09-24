package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.efetivo.PostoGraduacao;
import br.gov.pm.gestaoefetivo.efetivo.PostoGraduacaoRepository;
import br.gov.pm.gestaoefetivo.efetivo.SituacaoFuncional;
import br.gov.pm.gestaoefetivo.efetivo.SituacaoFuncionalRepository;
import br.gov.pm.gestaoefetivo.efetivo.Unidade;
import br.gov.pm.gestaoefetivo.efetivo.UnidadeRepository;
import br.gov.pm.gestaoefetivo.exception.RegraNegocioException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Gera a planilha modelo. Serve de contrato visível: o usuário baixa, vê os cabeçalhos que a
 * importação entende e, na segunda aba, exatamente quais postos, unidades e situações o sistema aceita.
 */
@Component
public class ModeloPlanilhaPessoa {

    private final PostoGraduacaoRepository postoGraduacaoRepository;
    private final UnidadeRepository unidadeRepository;
    private final SituacaoFuncionalRepository situacaoFuncionalRepository;

    public ModeloPlanilhaPessoa(PostoGraduacaoRepository postoGraduacaoRepository,
                                 UnidadeRepository unidadeRepository,
                                 SituacaoFuncionalRepository situacaoFuncionalRepository) {
        this.postoGraduacaoRepository = postoGraduacaoRepository;
        this.unidadeRepository = unidadeRepository;
        this.situacaoFuncionalRepository = situacaoFuncionalRepository;
    }

    public byte[] gerar() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            escreverAbaEfetivo(workbook);
            escreverAbaValoresAceitos(workbook);
            workbook.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new RegraNegocioException("Não consegui gerar a planilha modelo.");
        }
    }

    private void escreverAbaEfetivo(Workbook workbook) {
        Sheet aba = workbook.createSheet("Efetivo");
        CellStyle negrito = estiloNegrito(workbook);

        Row cabecalho = aba.createRow(0);
        ColunaPessoa[] colunas = ColunaPessoa.values();
        for (int i = 0; i < colunas.length; i++) {
            Cell celula = cabecalho.createCell(i);
            celula.setCellValue(colunas[i].rotulo());
            celula.setCellStyle(negrito);
        }

        Row exemplo = aba.createRow(1);
        String[] valores = {"123.456-7", "Fulano de Tal", "111.444.777-35", "01/02/1990", "M", "15/03/2010",
                "3º Sgt", "1º BPM", "Ativo", "(11) 90000-0000", "fulano@pm.sp.gov.br"};
        for (int i = 0; i < valores.length; i++) {
            exemplo.createCell(i).setCellValue(valores[i]);
        }

        for (int i = 0; i < colunas.length; i++) {
            aba.autoSizeColumn(i);
        }
    }

    private void escreverAbaValoresAceitos(Workbook workbook) {
        Sheet aba = workbook.createSheet("Valores aceitos");
        CellStyle negrito = estiloNegrito(workbook);

        Row cabecalho = aba.createRow(0);
        String[] titulos = {"Posto/Graduação", "Unidade", "Situação"};
        for (int i = 0; i < titulos.length; i++) {
            Cell celula = cabecalho.createCell(i);
            celula.setCellValue(titulos[i]);
            celula.setCellStyle(negrito);
        }

        List<String> postos = postoGraduacaoRepository.findAll().stream().map(PostoGraduacao::getNome).sorted().toList();
        List<String> unidades = unidadeRepository.findAll().stream().map(Unidade::getSigla).sorted().toList();
        List<String> situacoes = situacaoFuncionalRepository.findAll().stream().map(SituacaoFuncional::getNome).sorted().toList();

        int linhas = Math.max(postos.size(), Math.max(unidades.size(), situacoes.size()));
        for (int i = 0; i < linhas; i++) {
            Row linha = aba.createRow(i + 1);
            if (i < postos.size()) {
                linha.createCell(0).setCellValue(postos.get(i));
            }
            if (i < unidades.size()) {
                linha.createCell(1).setCellValue(unidades.get(i));
            }
            if (i < situacoes.size()) {
                linha.createCell(2).setCellValue(situacoes.get(i));
            }
        }
        for (int i = 0; i < titulos.length; i++) {
            aba.autoSizeColumn(i);
        }
    }

    private CellStyle estiloNegrito(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fonte = workbook.createFont();
        fonte.setBold(true);
        estilo.setFont(fonte);
        return estilo;
    }
}
