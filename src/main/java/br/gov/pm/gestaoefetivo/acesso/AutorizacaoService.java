package br.gov.pm.gestaoefetivo.acesso;

import br.gov.pm.gestaoefetivo.security.AuthenticatedUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Fonte única de verdade para "o que o perfil logado pode fazer no módulo X" — lê {@link PerfilPermissao}
 * em vez de checar roles fixas, para que a matriz de permissões continue editável sem redeploy.
 * Usado em @PreAuthorize("@autorizacaoService.pode('EFETIVO','VER')") nos controllers.
 */
@Service
public class AutorizacaoService {

    private final PerfilPermissaoRepository perfilPermissaoRepository;

    public AutorizacaoService(PerfilPermissaoRepository perfilPermissaoRepository) {
        this.perfilPermissaoRepository = perfilPermissaoRepository;
    }

    public boolean pode(String moduloChave, NivelPermissao nivelMinimo) {
        AuthenticatedUser usuario = AuthenticatedUser.atual();
        if (usuario == null) {
            return false;
        }
        NivelPermissao nivelDoPerfil = nivelDoPerfil(usuario.perfilId(), moduloChave);
        return nivelDoPerfil.atende(nivelMinimo);
    }

    @Cacheable(cacheNames = "perfilPermissao", key = "#perfilId + ':' + #moduloChave")
    public NivelPermissao nivelDoPerfil(Long perfilId, String moduloChave) {
        return perfilPermissaoRepository.findByPerfilIdEModuloChave(perfilId, moduloChave)
                .map(PerfilPermissao::getNivel)
                .orElse(NivelPermissao.NENHUM);
    }
}
