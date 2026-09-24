package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.exception.RegraNegocioException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanilhaPessoaLeitorTest {

    private final PlanilhaPessoaLeitor leitor = new PlanilhaPessoaLeitor();

    @Test
    @DisplayName("acha o cabeçalho mesmo com título e linhas em branco antes dele")
    void ignoraLinhasAntesDoCabecalho() throws IOException {
        byte[] arquivo = planilha(aba -> {
            aba.createRow(0).createCell(0).setCellValue("RELAÇÃO NOMINAL DO EFETIVO — 1º BPM");
            aba.createRow(1);
            Row cabecalho = aba.createRow(2);
            cabecalho.createCell(0).setCellValue("RE");
            cabecalho.createCell(1).setCellValue("Nome completo");
            cabecalho.createCell(2).setCellValue("CPF");
            cabecalho.createCell(3).setCellValue("Posto/Graduação");
            Row dados = aba.createRow(3);
            dados.createCell(0).setCellValue("123.456-7");
            dados.createCell(1).setCellValue("Fulano de Tal");
            dados.createCell(2).setCellValue("111.444.777-35");
            dados.createCell(3).setCellValue("3º Sgt");
        });

        PlanilhaPessoa planilha = leitor.ler(new ByteArrayInputStream(arquivo));

        assertThat(planilha.colunas()).containsKeys(ColunaPessoa.RE, ColunaPessoa.NOME, ColunaPessoa.CPF,
                ColunaPessoa.POSTO);
        assertThat(planilha.linhas()).hasSize(1);
        assertThat(planilha.linhas().get(0).valor(ColunaPessoa.NOME)).isEqualTo("Fulano de Tal");
        // numeração como o Excel mostra: o cabeçalho está na linha 3, os dados na 4
        assertThat(planilha.linhas().get(0).numero()).isEqualTo(4);
    }

    @Test
    @DisplayName("RE digitado como número não vira 123456.0 e data formatada sai em ISO")
    void preservaNumerosEDatas() throws IOException {
        byte[] arquivo = planilha(aba -> {
            Workbook workbook = aba.getWorkbook();
            CellStyle estiloData = workbook.createCellStyle();
            estiloData.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));

            Row cabecalho = aba.createRow(0);
            cabecalho.createCell(0).setCellValue("RE");
            cabecalho.createCell(1).setCellValue("Nome");
            cabecalho.createCell(2).setCellValue("CPF");
            cabecalho.createCell(3).setCellValue("Posto");
            cabecalho.createCell(4).setCellValue("Data de nascimento");

            Row dados = aba.createRow(1);
            dados.createCell(0).setCellValue(1234567);
            dados.createCell(1).setCellValue("Fulano");
            dados.createCell(2).setCellValue("111.444.777-35");
            dados.createCell(3).setCellValue("Soldado");
            var celulaData = dados.createCell(4);
            celulaData.setCellValue(LocalDate.of(1990, 2, 1));
            celulaData.setCellStyle(estiloData);
        });

        PlanilhaPessoa planilha = leitor.ler(new ByteArrayInputStream(arquivo));
        LinhaPlanilha linha = planilha.linhas().get(0);

        assertThat(linha.valor(ColunaPessoa.RE)).isEqualTo("1234567");
        assertThat(linha.valor(ColunaPessoa.DATA_NASCIMENTO)).isEqualTo("1990-02-01");
    }

    @Test
    @DisplayName("pula linhas totalmente vazias no meio da planilha")
    void ignoraLinhasVazias() throws IOException {
        byte[] arquivo = planilha(aba -> {
            Row cabecalho = aba.createRow(0);
            cabecalho.createCell(0).setCellValue("RE");
            cabecalho.createCell(1).setCellValue("Nome");
            cabecalho.createCell(2).setCellValue("CPF");
            cabecalho.createCell(3).setCellValue("Posto");
            aba.createRow(1).createCell(0).setCellValue("123.456-7");
            aba.createRow(2);
            Row terceira = aba.createRow(3);
            terceira.createCell(0).setCellValue("765.432-1");
        });

        assertThat(leitor.ler(new ByteArrayInputStream(arquivo)).linhas()).hasSize(2);
    }

    @Test
    void reclamaDeColunaObrigatoriaAusente() throws IOException {
        byte[] arquivo = planilha(aba -> {
            Row cabecalho = aba.createRow(0);
            cabecalho.createCell(0).setCellValue("RE");
            cabecalho.createCell(1).setCellValue("Nome");
            aba.createRow(1).createCell(0).setCellValue("123.456-7");
        });

        assertThatThrownBy(() -> leitor.ler(new ByteArrayInputStream(arquivo)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CPF")
                .hasMessageContaining("Posto");
    }

    @Test
    void reportaColunasQueNaoSeraoAproveitadas() throws IOException {
        byte[] arquivo = planilha(aba -> {
            Row cabecalho = aba.createRow(0);
            cabecalho.createCell(0).setCellValue("RE");
            cabecalho.createCell(1).setCellValue("Nome");
            cabecalho.createCell(2).setCellValue("CPF");
            cabecalho.createCell(3).setCellValue("Posto");
            cabecalho.createCell(4).setCellValue("Observações do comandante");
            aba.createRow(1).createCell(0).setCellValue("123.456-7");
        });

        assertThat(leitor.ler(new ByteArrayInputStream(arquivo)).colunasIgnoradas())
                .containsExactly("Observações do comandante");
    }

    private byte[] planilha(Consumer<Sheet> conteudo) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            conteudo.accept(workbook.createSheet("Efetivo"));
            workbook.write(saida);
            return saida.toByteArray();
        }
    }
}
