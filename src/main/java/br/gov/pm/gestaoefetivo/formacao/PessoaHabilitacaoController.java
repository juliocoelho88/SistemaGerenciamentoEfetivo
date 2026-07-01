package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.formacao.dto.PessoaHabilitacaoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaHabilitacaoResponse;
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
@RequestMapping("/api/pessoas/{pessoaId}/habilitacoes")
@PreAuthorize("@autorizacaoService.pode('HABILITACOES', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class PessoaHabilitacaoController {

    private final PessoaHabilitacaoService service;

    public PessoaHabilitacaoController(PessoaHabilitacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PessoaHabilitacaoResponse> listar(@PathVariable Long pessoaId) {
        return service.listar(pessoaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('HABILITACOES', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaHabilitacaoResponse criar(@PathVariable Long pessoaId, @Valid @RequestBody PessoaHabilitacaoRequest request) {
        return service.criar(pessoaId, request);
    }

    @PutMapping("/{vinculoId}")
    @PreAuthorize("@autorizacaoService.pode('HABILITACOES', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaHabilitacaoResponse atualizar(@PathVariable Long pessoaId, @PathVariable Long vinculoId,
                                               @Valid @RequestBody PessoaHabilitacaoRequest request) {
        return service.atualizar(pessoaId, vinculoId, request);
    }

    @DeleteMapping("/{vinculoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@autorizacaoService.pode('HABILITACOES', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public void excluir(@PathVariable Long pessoaId, @PathVariable Long vinculoId) {
        service.excluir(pessoaId, vinculoId);
    }
}
