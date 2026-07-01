package br.gov.pm.gestaoefetivo.acesso;

import br.gov.pm.gestaoefetivo.acesso.dto.ModuloResponse;
import br.gov.pm.gestaoefetivo.acesso.dto.PerfilMatrizResponse;
import br.gov.pm.gestaoefetivo.acesso.dto.PerfilResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final ModuloRepository moduloRepository;
    private final PerfilPermissaoRepository perfilPermissaoRepository;

    public PerfilService(PerfilRepository perfilRepository, ModuloRepository moduloRepository,
                          PerfilPermissaoRepository perfilPermissaoRepository) {
        this.perfilRepository = perfilRepository;
        this.moduloRepository = moduloRepository;
        this.perfilPermissaoRepository = perfilPermissaoRepository;
    }

    public List<PerfilResponse> listarPerfis() {
        return perfilRepository.findAll().stream()
                .map(p -> new PerfilResponse(p.getId(), p.getNome(), p.getDescricao()))
                .toList();
    }

    public List<ModuloResponse> listarModulos() {
        return moduloRepository.findAll().stream()
                .map(m -> new ModuloResponse(m.getId(), m.getNome(), m.getChave()))
                .toList();
    }

    /** Matriz perfil x módulo — uma linha por perfil, uma célula por módulo (nível NENHUM quando não configurado). */
    public List<PerfilMatrizResponse> matriz() {
        List<Modulo> modulos = moduloRepository.findAll();
        List<PerfilPermissao> todas = perfilPermissaoRepository.findAllComPerfilEModulo();

        return perfilRepository.findAll().stream()
                .sorted(Comparator.comparing(Perfil::getId))
                .map(perfil -> {
                    var permissoesDoPerfil = todas.stream()
                            .filter(pp -> pp.getPerfil().getId().equals(perfil.getId()))
                            .collect(Collectors.toMap(pp -> pp.getModulo().getId(), PerfilPermissao::getNivel));

                    List<PerfilMatrizResponse.Celula> celulas = modulos.stream()
                            .map(modulo -> new PerfilMatrizResponse.Celula(
                                    modulo.getChave(),
                                    modulo.getNome(),
                                    permissoesDoPerfil.getOrDefault(modulo.getId(), NivelPermissao.NENHUM)))
                            .toList();

                    return new PerfilMatrizResponse(perfil.getId(), perfil.getNome(), celulas);
                })
                .toList();
    }
}
