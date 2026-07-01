package br.gov.pm.gestaoefetivo.efetivo.dto;

import br.gov.pm.gestaoefetivo.efetivo.SexoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PessoaRequest(
        @NotBlank @Pattern(regexp = "\\d{3}\\.\\d{3}-[0-9A]", message = "RE deve seguir o formato XXX.XXX-D") String re,
        @NotBlank String nome,
        @NotBlank String cpf,
        LocalDate dataNascimento,
        SexoPessoa sexo,
        String fotoUrl,
        LocalDate dataPraca,
        @NotNull Long postoId,
        @NotNull Long unidadeId,
        @NotNull Long situacaoId,
        String telefone,
        @Email String email
) {
}
