package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.efetivo.PessoaService;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaHabilitacaoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaHabilitacaoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PessoaHabilitacaoService {

    private final PessoaHabilitacaoRepository pessoaHabilitacaoRepository;
    private final HabilitacaoRepository habilitacaoRepository;
    private final PessoaService pessoaService;
    private final FormacaoMapper mapper;

    public PessoaHabilitacaoService(PessoaHabilitacaoRepository pessoaHabilitacaoRepository,
                                     HabilitacaoRepository habilitacaoRepository,
                                     PessoaService pessoaService, FormacaoMapper mapper) {
        this.pessoaHabilitacaoRepository = pessoaHabilitacaoRepository;
        this.habilitacaoRepository = habilitacaoRepository;
        this.pessoaService = pessoaService;
        this.mapper = mapper;
    }

    public List<PessoaHabilitacaoResponse> listar(Long pessoaId) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        return pessoaHabilitacaoRepository.findByPessoaId(pessoaId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PessoaHabilitacaoResponse criar(Long pessoaId, PessoaHabilitacaoRequest request) {
        var pessoa = pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        PessoaHabilitacao vinculo = new PessoaHabilitacao();
        vinculo.setPessoa(pessoa);
        aplicar(vinculo, request);
        return mapper.toResponse(pessoaHabilitacaoRepository.save(vinculo));
    }

    @Transactional
    public PessoaHabilitacaoResponse atualizar(Long pessoaId, Long vinculoId, PessoaHabilitacaoRequest request) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        PessoaHabilitacao vinculo = buscarDaPessoa(pessoaId, vinculoId);
        aplicar(vinculo, request);
        return mapper.toResponse(pessoaHabilitacaoRepository.save(vinculo));
    }

    @Transactional
    public void excluir(Long pessoaId, Long vinculoId) {
        pessoaService.obterComVerificacaoDeAcesso(pessoaId);
        pessoaHabilitacaoRepository.delete(buscarDaPessoa(pessoaId, vinculoId));
    }

    private void aplicar(PessoaHabilitacao vinculo, PessoaHabilitacaoRequest request) {
        vinculo.setHabilitacao(habilitacaoRepository.findById(request.habilitacaoId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Habilitação", request.habilitacaoId())));
        vinculo.setNumero(request.numero());
        vinculo.setDataEmissao(request.dataEmissao());
        vinculo.setDataValidade(request.dataValidade());
        vinculo.setStatus(request.status());
    }

    private PessoaHabilitacao buscarDaPessoa(Long pessoaId, Long vinculoId) {
        PessoaHabilitacao vinculo = pessoaHabilitacaoRepository.findById(vinculoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Vínculo de habilitação", vinculoId));
        if (!vinculo.getPessoa().getId().equals(pessoaId)) {
            throw RecursoNaoEncontradoException.de("Vínculo de habilitação", vinculoId);
        }
        return vinculo;
    }
}
