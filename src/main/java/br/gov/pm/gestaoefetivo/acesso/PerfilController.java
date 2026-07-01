package br.gov.pm.gestaoefetivo.acesso;

import br.gov.pm.gestaoefetivo.acesso.dto.ModuloResponse;
import br.gov.pm.gestaoefetivo.acesso.dto.PerfilMatrizResponse;
import br.gov.pm.gestaoefetivo.acesso.dto.PerfilResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/acessos")
@PreAuthorize("@autorizacaoService.pode('ACESSOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/perfis")
    public List<PerfilResponse> perfis() {
        return perfilService.listarPerfis();
    }

    @GetMapping("/modulos")
    public List<ModuloResponse> modulos() {
        return perfilService.listarModulos();
    }

    @GetMapping("/matriz")
    public List<PerfilMatrizResponse> matriz() {
        return perfilService.matriz();
    }
}
