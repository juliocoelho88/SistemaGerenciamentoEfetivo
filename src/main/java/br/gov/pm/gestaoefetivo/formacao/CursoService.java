package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import br.gov.pm.gestaoefetivo.formacao.dto.CursoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.CursoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final FormacaoMapper mapper;

    public CursoService(CursoRepository cursoRepository, FormacaoMapper mapper) {
        this.cursoRepository = cursoRepository;
        this.mapper = mapper;
    }

    public List<CursoResponse> listar() {
        return cursoRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CursoResponse criar(CursoRequest request) {
        Curso curso = new Curso();
        aplicar(curso, request);
        return mapper.toResponse(cursoRepository.save(curso));
    }

    @Transactional
    public CursoResponse atualizar(Long id, CursoRequest request) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Curso", id));
        aplicar(curso, request);
        return mapper.toResponse(cursoRepository.save(curso));
    }

    private void aplicar(Curso curso, CursoRequest request) {
        curso.setNome(request.nome());
        curso.setInstituicao(request.instituicao());
        curso.setCargaHoraria(request.cargaHoraria());
        curso.setExigeValidade(request.exigeValidade());
        curso.setValidadeMeses(request.validadeMeses());
    }
}
