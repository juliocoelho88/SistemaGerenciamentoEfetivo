package br.gov.pm.gestaoefetivo.acesso.dto;

import br.gov.pm.gestaoefetivo.acesso.NivelPermissao;

import java.util.List;

public record PerfilMatrizResponse(
        Long perfilId,
        String perfilNome,
        List<Celula> permissoes
) {
    public record Celula(String moduloChave, String moduloNome, NivelPermissao nivel) {
    }
}
