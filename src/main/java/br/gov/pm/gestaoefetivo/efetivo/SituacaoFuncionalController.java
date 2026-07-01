package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.SituacaoFuncionalRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.SituacaoFuncionalResponse;
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
@RequestMapping("/api/situacoes")
@PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class SituacaoFuncionalController {

    private final SituacaoFuncionalService service;

    public SituacaoFuncionalController(SituacaoFuncionalService service) {
        this.service = service;
    }

    @GetMapping
    public List<SituacaoFuncionalResponse> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public SituacaoFuncionalResponse criar(@Valid @RequestBody SituacaoFuncionalRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public SituacaoFuncionalResponse atualizar(@PathVariable Long id, @Valid @RequestBody SituacaoFuncionalRequest request) {
        return service.atualizar(id, request);
    }
}
