package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.formacao.dto.PessoaEstagioRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaEstagioResponse;
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
@RequestMapping("/api/pessoas/{pessoaId}/estagios")
@PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class PessoaEstagioController {

    private final PessoaEstagioService service;

    public PessoaEstagioController(PessoaEstagioService service) {
        this.service = service;
    }

    @GetMapping
    public List<PessoaEstagioResponse> listar(@PathVariable Long pessoaId) {
        return service.listar(pessoaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaEstagioResponse criar(@PathVariable Long pessoaId, @Valid @RequestBody PessoaEstagioRequest request) {
        return service.criar(pessoaId, request);
    }

    @PutMapping("/{estagioId}")
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaEstagioResponse atualizar(@PathVariable Long pessoaId, @PathVariable Long estagioId,
                                           @Valid @RequestBody PessoaEstagioRequest request) {
        return service.atualizar(pessoaId, estagioId, request);
    }

    @DeleteMapping("/{estagioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@autorizacaoService.pode('CURSOS_ESTAGIOS', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public void excluir(@PathVariable Long pessoaId, @PathVariable Long estagioId) {
        service.excluir(pessoaId, estagioId);
    }
}
