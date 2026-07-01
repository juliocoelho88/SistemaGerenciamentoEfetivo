package br.gov.pm.gestaoefetivo.vencimentos;

import br.gov.pm.gestaoefetivo.vencimentos.dto.VencimentoResumoResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vencimentos")
@PreAuthorize("@autorizacaoService.pode('VENCIMENTOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class VencimentoController {

    private final VencimentoService vencimentoService;

    public VencimentoController(VencimentoService vencimentoService) {
        this.vencimentoService = vencimentoService;
    }

    @GetMapping
    public VencimentoResumoResponse listar() {
        return vencimentoService.listar();
    }
}
