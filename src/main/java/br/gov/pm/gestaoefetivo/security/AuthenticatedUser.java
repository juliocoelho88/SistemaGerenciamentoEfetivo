package br.gov.pm.gestaoefetivo.security;

import org.springframework.security.core.context.SecurityContextHolder;

/** Principal customizado extraído do JWT — carrega os dados usados pelo row-level scoping (unidade/pessoa). */
public record AuthenticatedUser(
        Long usuarioId,
        String login,
        Long perfilId,
        String perfilNome,
        Long escopoUnidadeId,
        Long pessoaId
) {

    /** @return o usuário autenticado da requisição atual, ou {@code null} se não houver (endpoint público). */
    public static AuthenticatedUser atual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user;
    }

    public boolean isGestorDeUnidade() {
        return escopoUnidadeId != null;
    }

    public boolean isVinculadoAPessoa() {
        return pessoaId != null;
    }
}
