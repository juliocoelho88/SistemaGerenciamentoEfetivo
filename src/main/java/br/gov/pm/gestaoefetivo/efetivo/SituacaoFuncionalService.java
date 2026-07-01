package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.SituacaoFuncionalRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.SituacaoFuncionalResponse;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SituacaoFuncionalService {

    private final SituacaoFuncionalRepository repository;

    public SituacaoFuncionalService(SituacaoFuncionalRepository repository) {
        this.repository = repository;
    }

    public List<SituacaoFuncionalResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public SituacaoFuncionalResponse criar(SituacaoFuncionalRequest request) {
        SituacaoFuncional situacao = new SituacaoFuncional();
        aplicar(situacao, request);
        return toResponse(repository.save(situacao));
    }

    @Transactional
    public SituacaoFuncionalResponse atualizar(Long id, SituacaoFuncionalRequest request) {
        SituacaoFuncional situacao = repository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Situação funcional", id));
        aplicar(situacao, request);
        return toResponse(repository.save(situacao));
    }

    private void aplicar(SituacaoFuncional situacao, SituacaoFuncionalRequest request) {
        situacao.setNome(request.nome());
        situacao.setDisponivelOperacao(request.disponivelOperacao());
    }

    private SituacaoFuncionalResponse toResponse(SituacaoFuncional situacao) {
        return new SituacaoFuncionalResponse(situacao.getId(), situacao.getNome(), situacao.isDisponivelOperacao());
    }
}
