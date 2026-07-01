package br.gov.pm.gestaoefetivo.vencimentos;

import br.gov.pm.gestaoefetivo.formacao.PessoaCursoRepository;
import br.gov.pm.gestaoefetivo.formacao.PessoaHabilitacaoRepository;
import br.gov.pm.gestaoefetivo.formacao.StatusHabilitacao;
import br.gov.pm.gestaoefetivo.security.AuthenticatedUser;
import br.gov.pm.gestaoefetivo.vencimentos.dto.VencimentoItemResponse;
import br.gov.pm.gestaoefetivo.vencimentos.dto.VencimentoResumoResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class VencimentoService {

    private static final int JANELA_DIAS = 90;

    private final PessoaCursoRepository pessoaCursoRepository;
    private final PessoaHabilitacaoRepository pessoaHabilitacaoRepository;

    public VencimentoService(PessoaCursoRepository pessoaCursoRepository,
                              PessoaHabilitacaoRepository pessoaHabilitacaoRepository) {
        this.pessoaCursoRepository = pessoaCursoRepository;
        this.pessoaHabilitacaoRepository = pessoaHabilitacaoRepository;
    }

    public VencimentoResumoResponse listar() {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(JANELA_DIAS);
        AuthenticatedUser usuario = AuthenticatedUser.atual();

        Stream<VencimentoItemResponse> deCursos = pessoaCursoRepository.findTodosComValidade().stream()
                .filter(pc -> visivelParaUsuario(usuario, pc.getPessoa().getUnidade().getId(), pc.getPessoa().getId()))
                .map(pc -> classificar(pc.getDataValidade(), hoje, limite)
                        .map(urgencia -> new VencimentoItemResponse(
                                pc.getPessoa().getId(), pc.getPessoa().getNome(), pc.getPessoa().getPosto().getAbreviacao(),
                                pc.getPessoa().getUnidade().getNome(), "Curso", pc.getCurso().getNome(),
                                pc.getDataValidade(), urgencia))
                        .orElse(null))
                .filter(java.util.Objects::nonNull);

        Stream<VencimentoItemResponse> deHabilitacoes = pessoaHabilitacaoRepository.findTodosComValidade().stream()
                .filter(ph -> visivelParaUsuario(usuario, ph.getPessoa().getUnidade().getId(), ph.getPessoa().getId()))
                .map(ph -> classificar(ph.getDataValidade(), hoje, limite)
                        .map(urgencia -> new VencimentoItemResponse(
                                ph.getPessoa().getId(), ph.getPessoa().getNome(), ph.getPessoa().getPosto().getAbreviacao(),
                                ph.getPessoa().getUnidade().getNome(), "Habilitação", ph.getHabilitacao().getNome(),
                                ph.getDataValidade(), urgencia))
                        .orElse(null))
                .filter(java.util.Objects::nonNull);

        List<VencimentoItemResponse> itens = Stream.concat(deCursos, deHabilitacoes)
                .sorted(Comparator.comparing(VencimentoItemResponse::dataValidade))
                .toList();

        long vencidos = itens.stream().filter(i -> i.urgencia() == StatusHabilitacao.VENCIDO).count();
        long vencendo = itens.stream().filter(i -> i.urgencia() == StatusHabilitacao.VENCENDO).count();
        long pessoasAfetadas = itens.stream().map(VencimentoItemResponse::pessoaId).collect(java.util.stream.Collectors.toSet()).size();

        return new VencimentoResumoResponse(itens, vencidos, vencendo, pessoasAfetadas);
    }

    private java.util.Optional<StatusHabilitacao> classificar(LocalDate dataValidade, LocalDate hoje, LocalDate limite) {
        if (dataValidade.isBefore(hoje)) {
            return java.util.Optional.of(StatusHabilitacao.VENCIDO);
        }
        if (!dataValidade.isAfter(limite)) {
            return java.util.Optional.of(StatusHabilitacao.VENCENDO);
        }
        return java.util.Optional.empty();
    }

    /** Mesma regra de row-level do módulo Efetivo: Gestor só a própria unidade, Policial só o próprio registro. */
    private boolean visivelParaUsuario(AuthenticatedUser usuario, Long unidadeId, Long pessoaId) {
        if (usuario == null) {
            return true;
        }
        if (usuario.isVinculadoAPessoa()) {
            return usuario.pessoaId().equals(pessoaId);
        }
        if (usuario.isGestorDeUnidade()) {
            return usuario.escopoUnidadeId().equals(unidadeId);
        }
        return true;
    }
}
