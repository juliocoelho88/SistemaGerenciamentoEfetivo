package br.gov.pm.gestaoefetivo.efetivo.dto;

import br.gov.pm.gestaoefetivo.efetivo.SexoPessoa;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PessoaDetalheResponse(
        Long id,
        String re,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        SexoPessoa sexo,
        String fotoUrl,
        LocalDate dataPraca,
        Long postoId,
        String postoNome,
        String postoAbreviacao,
        Long unidadeId,
        String unidadeNome,
        Long situacaoId,
        String situacaoNome,
        String telefone,
        String email,
        LocalDateTime criadoEm
) {
}
