package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaDetalheResponse;
import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaRequest;
import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaResumoResponse;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import br.gov.pm.gestaoefetivo.exception.RegraNegocioException;
import br.gov.pm.gestaoefetivo.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final PostoGraduacaoRepository postoGraduacaoRepository;
    private final UnidadeRepository unidadeRepository;
    private final SituacaoFuncionalRepository situacaoFuncionalRepository;
    private final PessoaMapper pessoaMapper;

    public PessoaService(PessoaRepository pessoaRepository, PostoGraduacaoRepository postoGraduacaoRepository,
                          UnidadeRepository unidadeRepository, SituacaoFuncionalRepository situacaoFuncionalRepository,
                          PessoaMapper pessoaMapper) {
        this.pessoaRepository = pessoaRepository;
        this.postoGraduacaoRepository = postoGraduacaoRepository;
        this.unidadeRepository = unidadeRepository;
        this.situacaoFuncionalRepository = situacaoFuncionalRepository;
        this.pessoaMapper = pessoaMapper;
    }

    @Transactional(readOnly = true)
    public Page<PessoaResumoResponse> buscar(String q, Long unidadeId, Long situacaoId, Pageable pageable) {
        AuthenticatedUser usuario = AuthenticatedUser.atual();

        Specification<Pessoa> spec = Specification.where(null);
        spec = comFiltro(spec, PessoaSpecifications.textoLivre(q));
        spec = comFiltro(spec, PessoaSpecifications.daSituacao(situacaoId));

        if (usuario != null && usuario.isVinculadoAPessoa()) {
            spec = comFiltro(spec, PessoaSpecifications.comId(usuario.pessoaId()));
        } else if (usuario != null && usuario.isGestorDeUnidade()) {
            spec = comFiltro(spec, PessoaSpecifications.daUnidade(usuario.escopoUnidadeId()));
        } else {
            spec = comFiltro(spec, PessoaSpecifications.daUnidade(unidadeId));
        }

        return pessoaRepository.findAll(spec, pageable).map(pessoaMapper::toResumo);
    }

    @Transactional(readOnly = true)
    public PessoaDetalheResponse buscarPorId(Long id) {
        Pessoa pessoa = buscarEntidadeOuFalhar(id);
        verificarAcesso(pessoa);
        return pessoaMapper.toDetalhe(pessoa);
    }

    @Transactional
    public PessoaDetalheResponse criar(PessoaRequest request) {
        if (pessoaRepository.existsByRe(request.re())) {
            throw new RegraNegocioException("Já existe uma pessoa cadastrada com o RE " + request.re() + ".");
        }
        if (pessoaRepository.existsByCpf(request.cpf())) {
            throw new RegraNegocioException("Já existe uma pessoa cadastrada com este CPF.");
        }
        Pessoa pessoa = new Pessoa();
        aplicar(pessoa, request);
        verificarAcesso(pessoa);
        return pessoaMapper.toDetalhe(pessoaRepository.save(pessoa));
    }

    @Transactional
    public PessoaDetalheResponse atualizar(Long id, PessoaRequest request) {
        Pessoa pessoa = buscarEntidadeOuFalhar(id);
        verificarAcesso(pessoa);
        if (!pessoa.getRe().equals(request.re()) && pessoaRepository.existsByRe(request.re())) {
            throw new RegraNegocioException("Já existe uma pessoa cadastrada com o RE " + request.re() + ".");
        }
        if (!pessoa.getCpf().equals(request.cpf()) && pessoaRepository.existsByCpf(request.cpf())) {
            throw new RegraNegocioException("Já existe uma pessoa cadastrada com este CPF.");
        }
        aplicar(pessoa, request);
        verificarAcesso(pessoa);
        return pessoaMapper.toDetalhe(pessoaRepository.save(pessoa));
    }

    private void aplicar(Pessoa pessoa, PessoaRequest request) {
        pessoa.setRe(request.re());
        pessoa.setNome(request.nome());
        pessoa.setCpf(request.cpf());
        pessoa.setDataNascimento(request.dataNascimento());
        pessoa.setSexo(request.sexo());
        pessoa.setFotoUrl(request.fotoUrl());
        pessoa.setDataPraca(request.dataPraca());
        pessoa.setTelefone(request.telefone());
        pessoa.setEmail(request.email());
        pessoa.setPosto(postoGraduacaoRepository.findById(request.postoId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Posto/Graduação", request.postoId())));
        pessoa.setUnidade(unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Unidade", request.unidadeId())));
        pessoa.setSituacao(situacaoFuncionalRepository.findById(request.situacaoId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Situação funcional", request.situacaoId())));
    }

    /** Gestor só enxerga/mantém a própria unidade; Policial só o próprio registro. */
    private void verificarAcesso(Pessoa pessoa) {
        AuthenticatedUser usuario = AuthenticatedUser.atual();
        if (usuario == null) {
            return;
        }
        if (usuario.isVinculadoAPessoa() && !usuario.pessoaId().equals(pessoa.getId())) {
            throw new AccessDeniedException("Este perfil só pode acessar o próprio registro.");
        }
        if (usuario.isGestorDeUnidade() && !usuario.escopoUnidadeId().equals(pessoa.getUnidade().getId())) {
            throw new AccessDeniedException("Este perfil só pode acessar registros da própria unidade.");
        }
    }

    @Transactional
    public void excluir(Long id) {
        Pessoa pessoa = buscarEntidadeOuFalhar(id);
        verificarAcesso(pessoa);
        pessoaRepository.delete(pessoa);
    }

    Pessoa buscarEntidadeOuFalhar(Long id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Pessoa", id));
    }

    /** Usado pelos módulos de formação (cursos/habilitações/estágios) para validar acesso à pessoa "dona" do vínculo. */
    public Pessoa obterComVerificacaoDeAcesso(Long id) {
        Pessoa pessoa = buscarEntidadeOuFalhar(id);
        verificarAcesso(pessoa);
        return pessoa;
    }

    private Specification<Pessoa> comFiltro(Specification<Pessoa> base, Specification<Pessoa> filtro) {
        return filtro == null ? base : base.and(filtro);
    }
}
