package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.efetivo.PessoaService;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaCursoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaCursoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PessoaCursoService {

    private final PessoaCursoRepository pessoaCursoRepository;
    private final CursoRepository cursoRepository;
    private final PessoaService pessoaService;
    private final FormacaoMapper mapper;

    public PessoaCursoService(PessoaCursoRepository pessoaCursoRepository, CursoRepository cursoRepository,
                               PessoaService pessoaService, FormacaoMapper mapper) {
        this.pessoaCursoRepository = pessoaCursoRepository;
        this.cursoRepository = cursoRepository;
        this.pessoaService = pessoaService;
        this.mapper = mapper;
    }

    public List<PessoaCursoResponse> listar(Long pessoaId) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        return pessoaCursoRepository.findByPessoaId(pessoaId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PessoaCursoResponse criar(Long pessoaId, PessoaCursoRequest request) {
        var pessoa = pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        PessoaCurso vinculo = new PessoaCurso();
        vinculo.setPessoa(pessoa);
        aplicar(vinculo, request);
        return mapper.toResponse(pessoaCursoRepository.save(vinculo));
    }

    @Transactional
    public PessoaCursoResponse atualizar(Long pessoaId, Long vinculoId, PessoaCursoRequest request) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        PessoaCurso vinculo = buscarDaPessoa(pessoaId, vinculoId);
        aplicar(vinculo, request);
        return mapper.toResponse(pessoaCursoRepository.save(vinculo));
    }

    @Transactional
    public void excluir(Long pessoaId, Long vinculoId) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        pessoaCursoRepository.delete(buscarDaPessoa(pessoaId, vinculoId));
    }

    private void aplicar(PessoaCurso vinculo, PessoaCursoRequest request) {
        vinculo.setCurso(cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Curso", request.cursoId())));
        vinculo.setDataConclusao(request.dataConclusao());
        vinculo.setDataValidade(request.dataValidade());
        vinculo.setCertificadoUrl(request.certificadoUrl());
        vinculo.setStatus(request.status());
    }

    private PessoaCurso buscarDaPessoa(Long pessoaId, Long vinculoId) {
        PessoaCurso vinculo = pessoaCursoRepository.findById(vinculoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Vínculo de curso", vinculoId));
        if (!vinculo.getPessoa().getId().equals(pessoaId)) {
            throw RecursoNaoEncontradoException.de("Vínculo de curso", vinculoId);
        }
        return vinculo;
    }
}
