package br.gov.pm.gestaoefetivo.acesso.dto;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String login,
        Long perfilId,
        String perfilNome,
        Long escopoUnidadeId,
        String escopoUnidadeNome,
        Long pessoaId,
        String pessoaNome,
        boolean ativo,
        LocalDateTime ultimoAcesso
) {
}
