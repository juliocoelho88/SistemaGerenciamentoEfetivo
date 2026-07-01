package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.common.PageResponse;
import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaDetalheResponse;
import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaResumoResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pessoas")
@PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).VER)")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @GetMapping
    public PageResponse<PessoaResumoResponse> buscar(@RequestParam(required = false) String q,
                                                       @RequestParam(required = false) Long unidadeId,
                                                       @RequestParam(required = false) Long situacaoId,
                                                       @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return PageResponse.de(pessoaService.buscar(q, unidadeId, situacaoId, pageable));
    }

    @GetMapping("/{id}")
    public PessoaDetalheResponse buscarPorId(@PathVariable Long id) {
        return pessoaService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaDetalheResponse criar(@Valid @RequestBody PessoaRequest request) {
        return pessoaService.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).EDITAR)")
    public PessoaDetalheResponse atualizar(@PathVariable Long id, @Valid @RequestBody PessoaRequest request) {
        return pessoaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@autorizacaoService.pode('EFETIVO', T(br.gov.pm.gestaoefetivo.acesso.NivelPermissao).TOTAL)")
    public void excluir(@PathVariable Long id) {
        pessoaService.excluir(id);
    }
}
