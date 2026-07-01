package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.formacao.dto.HabilitacaoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.HabilitacaoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/habilitacoes")
@PreAuthorize("@autorizacaoService.pode('HABILITACOES', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class HabilitacaoController {

    private final HabilitacaoService habilitacaoService;

    public HabilitacaoController(HabilitacaoService habilitacaoService) {
        this.habilitacaoService = habilitacaoService;
    }

    @GetMapping
    public List<HabilitacaoResponse> listar() {
        return habilitacaoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('HABILITACOES', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public HabilitacaoResponse criar(@Valid @RequestBody HabilitacaoRequest request) {
        return habilitacaoService.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacaoService.pode('HABILITACOES', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public HabilitacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody HabilitacaoRequest request) {
        return habilitacaoService.atualizar(id, request);
    }
}
