package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.UnidadeRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.UnidadeResponse;
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
@RequestMapping("/api/unidades")
@PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class UnidadeController {

    private final UnidadeService service;

    public UnidadeController(UnidadeService service) {
        this.service = service;
    }

    @GetMapping
    public List<UnidadeResponse> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public UnidadeResponse criar(@Valid @RequestBody UnidadeRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public UnidadeResponse atualizar(@PathVariable Long id, @Valid @RequestBody UnidadeRequest request) {
        return service.atualizar(id, request);
    }
}
