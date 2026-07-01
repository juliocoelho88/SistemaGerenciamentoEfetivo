package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import br.gov.pm.gestaoefetivo.formacao.dto.HabilitacaoRequest;
import br.gov.pm.gestaoefetivo.formacao.dto.HabilitacaoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HabilitacaoService {

    private final HabilitacaoRepository habilitacaoRepository;
    private final FormacaoMapper mapper;

    public HabilitacaoService(HabilitacaoRepository habilitacaoRepository, FormacaoMapper mapper) {
        this.habilitacaoRepository = habilitacaoRepository;
        this.mapper = mapper;
    }

    public List<HabilitacaoResponse> listar() {
        return habilitacaoRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public HabilitacaoResponse criar(HabilitacaoRequest request) {
        Habilitacao habilitacao = new Habilitacao();
        aplicar(habilitacao, request);
        return mapper.toResponse(habilitacaoRepository.save(habilitacao));
    }

    @Transactional
    public HabilitacaoResponse atualizar(Long id, HabilitacaoRequest request) {
        Habilitacao habilitacao = habilitacaoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Habilitação", id));
        aplicar(habilitacao, request);
        return mapper.toResponse(habilitacaoRepository.save(habilitacao));
    }

    private void aplicar(Habilitacao habilitacao, HabilitacaoRequest request) {
        habilitacao.setNome(request.nome());
        habilitacao.setCategoria(request.categoria());
        habilitacao.setExigeValidade(request.exigeValidade());
        habilitacao.setOrgaoEmissor(request.orgaoEmissor());
        habilitacao.setValidadeMeses(request.validadeMeses());
    }
}
