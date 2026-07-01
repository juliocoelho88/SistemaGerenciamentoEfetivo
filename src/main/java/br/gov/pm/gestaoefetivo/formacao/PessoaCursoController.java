package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.formacao.dto.PessoaCursoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaCursoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/pessoas/{pessoaId}/cursos")
@PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class PessoaCursoController {

    private final PessoaCursoService service;

    public PessoaCursoController(PessoaCursoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PessoaCursoResponse> listar(@PathVariable Long pessoaId) {
        return service.listar(pessoaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaCursoResponse criar(@PathVariable Long pessoaId, @Valid @RequestBody PessoaCursoRequest request) {
        return service.criar(pessoaId, request);
    }

    @PutMapping("/{vinculoId}")
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaCursoResponse atualizar(@PathVariable Long pessoaId, @PathVariable Long vinculoId,
                                         @Valid @RequestBody PessoaCursoRequest request) {
        return service.atualizar(pessoaId, vinculoId, request);
    }

    @DeleteMapping("/{vinculoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public void excluir(@PathVariable Long pessoaId, @PathVariable Long vinculoId) {
        service.excluir(pessoaId, vinculoId);
    }
}
