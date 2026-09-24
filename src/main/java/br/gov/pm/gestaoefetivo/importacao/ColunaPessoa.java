package br.gov.pm.gestaoefetivo.importacao;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Colunas que a importação entende, com os apelidos aceitos no cabeçalho da planilha. A comparação é
 * feita pela {@link TextoPlanilha#chave(String)}, então maiúsculas, acentos e pontuação não importam:
 * "E-mail", "e mail" e "EMAIL" chegam todos como "E MAIL".
 */
public enum ColunaPessoa {

    RE("RE", true, "re", "r e", "matricula", "re do policial", "re policial"),
    NOME("Nome", true, "nome", "nome completo", "nome do policial", "policial"),
    CPF("CPF", true, "cpf"),
    DATA_NASCIMENTO("Data de nascimento", false, "data de nascimento", "data nascimento", "nascimento", "dt nascimento"),
    SEXO("Sexo", false, "sexo", "genero"),
    DATA_PRACA("Data de praça", false, "data de praca", "data praca", "praca", "data de inclusao", "inclusao",
            "data de ingresso", "ingresso"),
    POSTO("Posto/Graduação", true, "posto", "graduacao", "posto graduacao", "posto grad", "pg", "posto e graduacao"),
    UNIDADE("Unidade", false, "unidade", "lotacao", "opm", "unidade de lotacao", "subunidade"),
    SITUACAO("Situação", false, "situacao", "situacao funcional", "status"),
    TELEFONE("Telefone", false, "telefone", "celular", "fone", "contato"),
    EMAIL("E-mail", false, "email", "e mail");

    private final String rotulo;
    private final boolean obrigatoria;
    private final List<String> chaves;

    ColunaPessoa(String rotulo, boolean obrigatoria, String... apelidos) {
        this.rotulo = rotulo;
        this.obrigatoria = obrigatoria;
        this.chaves = Arrays.stream(apelidos).map(TextoPlanilha::chave).toList();
    }

    public String rotulo() {
        return rotulo;
    }

    /** Colunas sem as quais a planilha não pode ser importada (as demais têm padrão ou são opcionais). */
    public boolean obrigatoria() {
        return obrigatoria;
    }

    public static Optional<ColunaPessoa> porCabecalho(String cabecalho) {
        String chave = TextoPlanilha.chave(cabecalho);
        if (chave.isEmpty()) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(coluna -> coluna.chaves.contains(chave)).findFirst();
    }
}
