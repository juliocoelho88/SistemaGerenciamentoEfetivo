package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.UnidadeRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.UnidadeResponse;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnidadeService {

    private final UnidadeRepository repository;
    private final UnidadeMapper mapper;

    public UnidadeService(UnidadeRepository repository, UnidadeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UnidadeResponse> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public UnidadeResponse criar(UnidadeRequest request) {
        Unidade unidade = new Unidade();
        aplicar(unidade, request);
        return mapper.toResponse(repository.save(unidade));
    }

    @Transactional
    public UnidadeResponse atualizar(Long id, UnidadeRequest request) {
        Unidade unidade = repository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Unidade", id));
        aplicar(unidade, request);
        return mapper.toResponse(repository.save(unidade));
    }

    private void aplicar(Unidade unidade, UnidadeRequest request) {
        unidade.setNome(request.nome());
        unidade.setSigla(request.sigla());
        unidade.setTipo(request.tipo());
        if (request.unidadePaiId() != null) {
            Unidade pai = repository.findById(request.unidadePaiId())
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Unidade (pai)", request.unidadePaiId()));
            unidade.setUnidadePai(pai);
        } else {
            unidade.setUnidadePai(null);
        }
    }
}
