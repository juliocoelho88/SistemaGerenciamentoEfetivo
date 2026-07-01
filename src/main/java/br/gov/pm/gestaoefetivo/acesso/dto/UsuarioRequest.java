package br.gov.pm.gestaoefetivo.acesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** {@code senha} é opcional na atualização (nulo = mantém a senha atual); obrigatória na criação — validado no service. */
public record UsuarioRequest(
        @NotBlank String login,
        String senha,
        @NotNull Long perfilId,
        Long escopoUnidadeId,
        Long pessoaId,
        Boolean ativo
) {
}
