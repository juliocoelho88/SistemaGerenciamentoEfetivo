package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.efetivo.PessoaService;
import br.gov.pm.gestaoefetivo.efetivo.UnidadeRepository;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaEstagioRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaEstagioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PessoaEstagioService {

    private final PessoaEstagioRepository pessoaEstagioRepository;
    private final UnidadeRepository unidadeRepository;
    private final PessoaService pessoaService;
    private final FormacaoMapper mapper;

    public PessoaEstagioService(PessoaEstagioRepository pessoaEstagioRepository, UnidadeRepository unidadeRepository,
                                 PessoaService pessoaService, FormacaoMapper mapper) {
        this.pessoaEstagioRepository = pessoaEstagioRepository;
        this.unidadeRepository = unidadeRepository;
        this.pessoaService = pessoaService;
        this.mapper = mapper;
    }

    public List<PessoaEstagioResponse> listar(Long pessoaId) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        return pessoaEstagioRepository.findByPessoaId(pessoaId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PessoaEstagioResponse criar(Long pessoaId, PessoaEstagioRequest request) {
        var pessoa = pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        PessoaEstagio estagio = new PessoaEstagio();
        estagio.setPessoa(pessoa);
        aplicar(estagio, request);
        return mapper.toResponse(pessoaEstagioRepository.save(estagio));
    }

    @Transactional
    public PessoaEstagioResponse atualizar(Long pessoaId, Long estagioId, PessoaEstagioRequest request) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        PessoaEstagio estagio = buscarDaPessoa(pessoaId, estagioId);
        aplicar(estagio, request);
        return mapper.toResponse(pessoaEstagioRepository.save(estagio));
    }

    @Transactional
    public void excluir(Long pessoaId, Long estagioId) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        pessoaEstagioRepository.delete(buscarDaPessoa(pessoaId, estagioId));
    }

    private void aplicar(PessoaEstagio estagio, PessoaEstagioRequest request) {
        estagio.setNome(request.nome());
        estagio.setSupervisor(request.supervisor());
        estagio.setDataInicio(request.dataInicio());
        estagio.setDataFim(request.dataFim());
        estagio.setAvaliacao(request.avaliacao());
        estagio.setStatus(request.status());
        if (request.unidadeId() != null) {
            estagio.setUnidade(unidadeRepository.findById(request.unidadeId())
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Unidade", request.unidadeId())));
        } else {
            estagio.setUnidade(null);
        }
    }

    private PessoaEstagio buscarDaPessoa(Long pessoaId, Long estagioId) {
        PessoaEstagio estagio = pessoaEstagioRepository.findById(estagioId)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Estágio", estagioId));
        if (!estagio.getPessoa().getId().equals(pessoaId)) {
            throw RecursoNaoEncontradoException.de("Estágio", estagioId);
        }
        return estagio;
    }
}
