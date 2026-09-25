package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.efetivo.SexoPessoa;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Converte o texto cru da planilha nos tipos e formatos que o banco exige, recusando a linha com uma
 * mensagem específica quando não dá. A recusa é deliberada: o CHECK do RE e os UNIQUE de RE/CPF são
 * constraints do PostgreSQL e, num erro, abortariam a transação inteira — é mais barato validar antes
 * e reportar a linha do que perder o lote.
 */
@Component
public class NormalizadorPessoa {

    /**
     * ResolverStyle.STRICT é essencial: no modo padrão (SMART) o Java "conserta" 31/02/1990 para
     * 28/02/1990 em silêncio, e um erro de digitação da planilha viraria data errada no cadastro.
     */
    private static final List<DateTimeFormatter> FORMATOS_DATA = Stream.concat(
            Stream.of(DateTimeFormatter.ISO_LOCAL_DATE),
            Stream.of("dd/MM/uuuu", "d/M/uuuu", "dd-MM-uuuu", "dd.MM.uuuu")
                    .map(padrao -> DateTimeFormatter.ofPattern(padrao, Locale.ROOT)
                            .withResolverStyle(ResolverStyle.STRICT))).toList();

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    public PessoaNormalizada normalizar(LinhaPlanilha linha, CatalogoEfetivo catalogo, Padroes padroes) {
        return new PessoaNormalizada(
                linha.numero(),
                re(linha.valor(ColunaPessoa.RE)),
                nome(linha.valor(ColunaPessoa.NOME)),
                cpf(linha.valor(ColunaPessoa.CPF)),
                data(linha.valor(ColunaPessoa.DATA_NASCIMENTO), ColunaPessoa.DATA_NASCIMENTO),
                sexo(linha.valor(ColunaPessoa.SEXO)),
                data(linha.valor(ColunaPessoa.DATA_PRACA), ColunaPessoa.DATA_PRACA),
                posto(linha.valor(ColunaPessoa.POSTO), catalogo),
                unidade(linha.valor(ColunaPessoa.UNIDADE), catalogo, padroes),
                situacao(linha.valor(ColunaPessoa.SITUACAO), catalogo, padroes),
                telefone(linha.valor(ColunaPessoa.TELEFONE)),
                email(linha.valor(ColunaPessoa.EMAIL)));
    }

    /**
     * Devolve o RE no formato XXX.XXX-D exigido pelo CHECK da tabela. Aceita "123.456-7", "1234567" e
     * "123456" — este último porque o Excel corta o zero à esquerda de células numéricas, transformando
     * "012.345-6" em 123456; o zero é reposto à esquerda.
     */
    String re(String bruto) {
        if (bruto == null) {
            throw new LinhaInvalidaException("RE em branco.");
        }
        String limpo = bruto.toUpperCase(Locale.ROOT).replaceAll("[^0-9A]", "");
        if (limpo.isEmpty() || limpo.length() > 7) {
            throw new LinhaInvalidaException("RE \"" + bruto + "\" fora do formato esperado (XXX.XXX-D).");
        }
        String completo = "0".repeat(7 - limpo.length()) + limpo;
        if (!completo.substring(0, 6).matches("\\d{6}")) {
            throw new LinhaInvalidaException("RE \"" + bruto + "\" tem letra fora do dígito verificador.");
        }
        char digito = completo.charAt(6);
        if (!Character.isDigit(digito) && digito != 'A') {
            throw new LinhaInvalidaException("RE \"" + bruto + "\": o dígito verificador deve ser 0-9 ou A.");
        }
        return completo.substring(0, 3) + "." + completo.substring(3, 6) + "-" + digito;
    }

    String nome(String bruto) {
        if (bruto == null) {
            throw new LinhaInvalidaException("Nome em branco.");
        }
        String limpo = bruto.replaceAll("\\s+", " ").trim();
        if (limpo.length() > 150) {
            throw new LinhaInvalidaException("Nome tem " + limpo.length() + " caracteres (máximo 150).");
        }
        return limpo;
    }

    /** Devolve o CPF mascarado (xxx.xxx.xxx-xx), que é o formato do campo VARCHAR(14) da tabela. */
    String cpf(String bruto) {
        if (bruto == null) {
            throw new LinhaInvalidaException("CPF em branco.");
        }
        String digitos = TextoPlanilha.somenteDigitos(bruto);
        if (digitos.isEmpty() || digitos.length() > 11) {
            throw new LinhaInvalidaException("CPF \"" + bruto + "\" não tem 11 dígitos.");
        }
        String completo = "0".repeat(11 - digitos.length()) + digitos;
        if (!digitosVerificadoresConferem(completo)) {
            throw new LinhaInvalidaException("CPF \"" + bruto + "\" é inválido (dígitos verificadores não conferem).");
        }
        return completo.substring(0, 3) + "." + completo.substring(3, 6) + "." + completo.substring(6, 9)
                + "-" + completo.substring(9);
    }

    private boolean digitosVerificadoresConferem(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        for (int digito = 0; digito < 2; digito++) {
            int soma = 0;
            int peso = 10 + digito;
            for (int i = 0; i < 9 + digito; i++) {
                soma += (cpf.charAt(i) - '0') * peso--;
            }
            int resto = soma % 11;
            int esperado = resto < 2 ? 0 : 11 - resto;
            if (cpf.charAt(9 + digito) - '0' != esperado) {
                return false;
            }
        }
        return true;
    }

    LocalDate data(String bruto, ColunaPessoa coluna) {
        if (bruto == null) {
            return null;
        }
        for (DateTimeFormatter formato : FORMATOS_DATA) {
            try {
                return LocalDate.parse(bruto, formato);
            } catch (DateTimeParseException ignorado) {
                // tenta o próximo formato
            }
        }
        throw new LinhaInvalidaException(coluna.rotulo() + " \"" + bruto + "\" não é uma data válida (use dd/mm/aaaa).");
    }

    SexoPessoa sexo(String bruto) {
        if (bruto == null) {
            return null;
        }
        String chave = TextoPlanilha.chave(bruto);
        return switch (chave) {
            case "M", "MASCULINO", "MASC", "H", "HOMEM" -> SexoPessoa.M;
            case "F", "FEMININO", "FEM", "MULHER" -> SexoPessoa.F;
            default -> throw new LinhaInvalidaException("Sexo \"" + bruto + "\" não reconhecido (use M ou F).");
        };
    }

    private Long posto(String bruto, CatalogoEfetivo catalogo) {
        if (bruto == null) {
            throw new LinhaInvalidaException("Posto/Graduação em branco.");
        }
        Long id = catalogo.posto(bruto);
        if (id == null) {
            throw new LinhaInvalidaException("Posto/Graduação \"" + bruto + "\" não existe no sistema. "
                    + "Valores aceitos: " + CatalogoEfetivo.amostra(catalogo.postos()) + ".");
        }
        return id;
    }

    private Long unidade(String bruto, CatalogoEfetivo catalogo, Padroes padroes) {
        if (bruto == null) {
            if (padroes.unidadeId() == null) {
                throw new LinhaInvalidaException("Unidade em branco e nenhuma unidade padrão informada "
                        + "(use o parâmetro unidadePadrao).");
            }
            return padroes.unidadeId();
        }
        Long id = catalogo.unidade(bruto);
        if (id == null) {
            throw new LinhaInvalidaException("Unidade \"" + bruto + "\" não existe no sistema. "
                    + "Valores aceitos: " + CatalogoEfetivo.amostra(catalogo.unidades()) + ".");
        }
        return id;
    }

    private Long situacao(String bruto, CatalogoEfetivo catalogo, Padroes padroes) {
        if (bruto == null) {
            if (padroes.situacaoId() == null) {
                throw new LinhaInvalidaException("Situação em branco e nenhuma situação padrão informada "
                        + "(use o parâmetro situacaoPadrao).");
            }
            return padroes.situacaoId();
        }
        Long id = catalogo.situacao(bruto);
        if (id == null) {
            throw new LinhaInvalidaException("Situação \"" + bruto + "\" não existe no sistema. "
                    + "Valores aceitos: " + CatalogoEfetivo.amostra(catalogo.situacoes()) + ".");
        }
        return id;
    }

    String telefone(String bruto) {
        if (bruto == null) {
            return null;
        }
        String limpo = bruto.replaceAll("\\s+", " ").trim();
        if (limpo.length() > 20) {
            throw new LinhaInvalidaException("Telefone \"" + bruto + "\" tem mais de 20 caracteres.");
        }
        return limpo;
    }

    String email(String bruto) {
        if (bruto == null) {
            return null;
        }
        String limpo = bruto.trim().toLowerCase(Locale.ROOT);
        if (limpo.length() > 150) {
            throw new LinhaInvalidaException("E-mail tem mais de 150 caracteres.");
        }
        if (!EMAIL.matcher(limpo).matches()) {
            throw new LinhaInvalidaException("E-mail \"" + bruto + "\" é inválido.");
        }
        return limpo;
    }

    /** Valores aplicados quando a planilha não traz a coluna — típico de arquivo separado por unidade. */
    public record Padroes(Long unidadeId, Long situacaoId) {
    }
}
