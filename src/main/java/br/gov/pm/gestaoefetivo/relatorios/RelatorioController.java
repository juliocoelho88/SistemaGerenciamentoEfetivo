package br.gov.pm.gestaoefetivo.relatorios;

import br.gov.pm.gestaoefetivo.relatorios.dto.RelatorioResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
@PreAuthorize("@autorizacaoService.pode('RELATORIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public RelatorioResponse gerar() {
        return relatorioService.gerar();
    }
}
