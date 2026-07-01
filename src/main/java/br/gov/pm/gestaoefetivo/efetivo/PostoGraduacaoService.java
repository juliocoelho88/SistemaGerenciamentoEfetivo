package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.PostoGraduacaoRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.PostoGraduacaoResponse;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PostoGraduacaoService {

    private final PostoGraduacaoRepository repository;

    public PostoGraduacaoService(PostoGraduacaoRepository repository) {
        this.repository = repository;
    }

    public List<PostoGraduacaoResponse> listar() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(PostoGraduacao::getOrdemHierarquica))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PostoGraduacaoResponse criar(PostoGraduacaoRequest request) {
        PostoGraduacao posto = new PostoGraduacao();
        aplicar(posto, request);
        return toResponse(repository.save(posto));
    }

    @Transactional
    public PostoGraduacaoResponse atualizar(Long id, PostoGraduacaoRequest request) {
        PostoGraduacao posto = repository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Posto/Graduação", id));
        aplicar(posto, request);
        return toResponse(repository.save(posto));
    }

    private void aplicar(PostoGraduacao posto, PostoGraduacaoRequest request) {
        posto.setNome(request.nome());
        posto.setAbreviacao(request.abreviacao());
        posto.setOrdemHierarquica(request.ordemHierarquica());
        posto.setCirculo(request.circulo());
    }

    private PostoGraduacaoResponse toResponse(PostoGraduacao posto) {
        return new PostoGraduacaoResponse(posto.getId(), posto.getNome(), posto.getAbreviacao(),
                posto.getOrdemHierarquica(), posto.getCirculo());
    }
}
