package br.gov.pm.gestaoefetivo.importacao;

import br.gov.pm.gestaoefetivo.efetivo.SexoPessoa;

import java.time.LocalDate;

/** Uma linha da planilha já validada e com as chaves estrangeiras resolvidas, pronta para virar entidade. */
public record PessoaNormalizada(int linha,
                                String re,
                                String nome,
                                String cpf,
                                LocalDate dataNascimento,
                                SexoPessoa sexo,
                                LocalDate dataPraca,
                                Long postoId,
                                Long unidadeId,
                                Long situacaoId,
                                String telefone,
                                String email) {
}
