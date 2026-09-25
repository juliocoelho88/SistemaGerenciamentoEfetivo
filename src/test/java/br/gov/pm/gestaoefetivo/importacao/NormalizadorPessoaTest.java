package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.efetivo.SexoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NormalizadorPessoaTest {

    private final NormalizadorPessoa normalizador = new NormalizadorPessoa();

    private final CatalogoEfetivo catalogo = new CatalogoEfetivo(
            Map.of("3 SGT", 3L, "SOLDADO", 1L),
            Map.of("1 BPM", 10L, "5 BPM", 11L),
            Map.of("ATIVO", 1L, "FERIAS", 2L));

    // ---------- RE ----------

    @Test
    @DisplayName("aplica a máscara XXX.XXX-D que o CHECK da tabela exige")
    void formataRe() {
        assertThat(normalizador.re("123.456-7")).isEqualTo("123.456-7");
        assertThat(normalizador.re("1234567")).isEqualTo("123.456-7");
        assertThat(normalizador.re("123456A")).isEqualTo("123.456-A");
        assertThat(normalizador.re(" 123456-7 ")).isEqualTo("123.456-7");
    }

    @Test
    @DisplayName("repõe o zero à esquerda que o Excel corta de célula numérica")
    void reconstroiZeroPerdidoPeloExcel() {
        assertThat(normalizador.re("123456")).isEqualTo("012.345-6");
    }

    @Test
    void recusaReForaDoFormato() {
        assertThatThrownBy(() -> normalizador.re("12345678"))
                .isInstanceOf(LinhaInvalidaException.class)
                .hasMessageContaining("XXX.XXX-D");
        assertThatThrownBy(() -> normalizador.re(null))
                .isInstanceOf(LinhaInvalidaException.class);
    }

    // ---------- CPF ----------

    @Test
    void formataCpfValidoERecusaInvalido() {
        assertThat(normalizador.cpf("11144477735")).isEqualTo("111.444.777-35");
        assertThat(normalizador.cpf("111.444.777-35")).isEqualTo("111.444.777-35");
        assertThatThrownBy(() -> normalizador.cpf("111.444.777-36"))
                .isInstanceOf(LinhaInvalidaException.class)
                .hasMessageContaining("dígitos verificadores");
        assertThatThrownBy(() -> normalizador.cpf("11111111111"))
                .isInstanceOf(LinhaInvalidaException.class);
    }

    // ---------- datas e sexo ----------

    @Test
    void aceitaOsFormatosDeDataUsadosNasPlanilhas() {
        assertThat(normalizador.data("1990-02-01", ColunaPessoa.DATA_NASCIMENTO)).isEqualTo(LocalDate.of(1990, 2, 1));
        assertThat(normalizador.data("01/02/1990", ColunaPessoa.DATA_NASCIMENTO)).isEqualTo(LocalDate.of(1990, 2, 1));
        assertThat(normalizador.data("1/2/1990", ColunaPessoa.DATA_NASCIMENTO)).isEqualTo(LocalDate.of(1990, 2, 1));
        assertThat(normalizador.data(null, ColunaPessoa.DATA_NASCIMENTO)).isNull();
        assertThatThrownBy(() -> normalizador.data("31/02/1990", ColunaPessoa.DATA_NASCIMENTO))
                .isInstanceOf(LinhaInvalidaException.class);
    }

    @Test
    void interpretaSexoPorExtensoOuAbreviado() {
        assertThat(normalizador.sexo("Masculino")).isEqualTo(SexoPessoa.M);
        assertThat(normalizador.sexo("f")).isEqualTo(SexoPessoa.F);
        assertThat(normalizador.sexo(null)).isNull();
        assertThatThrownBy(() -> normalizador.sexo("X")).isInstanceOf(LinhaInvalidaException.class);
    }

    // ---------- linha completa ----------

    @Test
    @DisplayName("resolve as chaves estrangeiras pelo texto da planilha")
    void normalizaLinhaCompleta() {
        LinhaPlanilha linha = new LinhaPlanilha(2, Map.of(
                ColunaPessoa.RE, "123.456-7",
                ColunaPessoa.NOME, "  Fulano  de   Tal ",
                ColunaPessoa.CPF, "11144477735",
                ColunaPessoa.POSTO, "3º Sgt",
                ColunaPessoa.UNIDADE, "1º BPM",
                ColunaPessoa.SITUACAO, "Ativo"));

        PessoaNormalizada pessoa = normalizador.normalizar(linha, catalogo, new NormalizadorPessoa.Padroes(null, null));

        assertThat(pessoa.re()).isEqualTo("123.456-7");
        assertThat(pessoa.nome()).isEqualTo("Fulano de Tal");
        assertThat(pessoa.postoId()).isEqualTo(3L);
        assertThat(pessoa.unidadeId()).isEqualTo(10L);
        assertThat(pessoa.situacaoId()).isEqualTo(1L);
        assertThat(pessoa.linha()).isEqualTo(2);
    }

    @Test
    @DisplayName("cai no padrão quando a planilha não traz unidade nem situação")
    void aplicaPadroes() {
        LinhaPlanilha linha = new LinhaPlanilha(2, Map.of(
                ColunaPessoa.RE, "123.456-7",
                ColunaPessoa.NOME, "Fulano",
                ColunaPessoa.CPF, "11144477735",
                ColunaPessoa.POSTO, "Soldado"));

        PessoaNormalizada pessoa = normalizador.normalizar(linha, catalogo, new NormalizadorPessoa.Padroes(11L, 2L));

        assertThat(pessoa.unidadeId()).isEqualTo(11L);
        assertThat(pessoa.situacaoId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("a mensagem de erro lista os valores aceitos, para o usuário corrigir sem perguntar")
    void erroDeCatalogoListaOpcoes() {
        LinhaPlanilha linha = new LinhaPlanilha(7, Map.of(
                ColunaPessoa.RE, "123.456-7",
                ColunaPessoa.NOME, "Fulano",
                ColunaPessoa.CPF, "11144477735",
                ColunaPessoa.POSTO, "Comandante Supremo",
                ColunaPessoa.UNIDADE, "1º BPM",
                ColunaPessoa.SITUACAO, "Ativo"));

        assertThatThrownBy(() -> normalizador.normalizar(linha, catalogo, new NormalizadorPessoa.Padroes(null, null)))
                .isInstanceOf(LinhaInvalidaException.class)
                .hasMessageContaining("Comandante Supremo")
                .hasMessageContaining("SOLDADO");
    }

    @Test
    void validaEmailETamanhoDosCampos() {
        assertThat(normalizador.email(" Fulano@PM.SP.GOV.BR ")).isEqualTo("fulano@pm.sp.gov.br");
        assertThat(normalizador.email(null)).isNull();
        assertThatThrownBy(() -> normalizador.email("fulano@@pm")).isInstanceOf(LinhaInvalidaException.class);
        assertThatThrownBy(() -> normalizador.nome("x".repeat(151))).isInstanceOf(LinhaInvalidaException.class);
        assertThatThrownBy(() -> normalizador.telefone("9".repeat(21))).isInstanceOf(LinhaInvalidaException.class);
    }
}
