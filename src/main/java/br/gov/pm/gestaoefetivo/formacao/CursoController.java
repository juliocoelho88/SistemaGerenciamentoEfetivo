package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.formacao.dto.CursoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.CursoResponse;
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
@RequestMapping("/api/cursos")
@PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public List<CursoResponse> listar() {
        return cursoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public CursoResponse criar(@Valid @RequestBody CursoRequest request) {
        return cursoService.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public CursoResponse atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest request) {
        return cursoService.atualizar(id, request);
    }
}
