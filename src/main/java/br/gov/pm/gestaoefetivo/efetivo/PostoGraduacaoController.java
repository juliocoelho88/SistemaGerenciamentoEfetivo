package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.PostoGraduacaoRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.PostoGraduacaoResponse;
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
@RequestMapping("/api/postos")
@PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class PostoGraduacaoController {

    private final PostoGraduacaoService service;

    public PostoGraduacaoController(PostoGraduacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PostoGraduacaoResponse> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PostoGraduacaoResponse criar(@Valid @RequestBody PostoGraduacaoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PostoGraduacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody PostoGraduacaoRequest request) {
        return service.atualizar(id, request);
    }
}
