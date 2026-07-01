package br.gov.pm.gestaoefetivo.vencimentos;

import br.gov.pm.gestaoefetivo.formacao.PessoaCurso;
import br.gov.pm.gestaoefetivo.formacao.PessoaCursoRepository;
import br.gov.pm.gestaoefetivo.formacao.PessoaHabilitacao;
import br.gov.pm.gestaoefetivo.formacao.PessoaHabilitacaoRepository;
import br.gov.pm.gestaoefetivo.formacao.StatusHabilitacao;
import br.gov.pm.gestaoefetivo.formacao.StatusPessoaCurso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Recalcula diariamente o status de validade de cursos/habilitações comparando {@code dataValidade} com a data atual
 * (janela de 90 dias para "vencendo") — mantém o painel de Vencimentos e as fichas coerentes sem intervenção manual.
 */
@Component
public class VencimentoStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(VencimentoStatusScheduler.class);
    private static final int JANELA_DIAS = 90;

    private final PessoaCursoRepository pessoaCursoRepository;
    private final PessoaHabilitacaoRepository pessoaHabilitacaoRepository;

    public VencimentoStatusScheduler(PessoaCursoRepository pessoaCursoRepository,
                                      PessoaHabilitacaoRepository pessoaHabilitacaoRepository) {
        this.pessoaCursoRepository = pessoaCursoRepository;
        this.pessoaHabilitacaoRepository = pessoaHabilitacaoRepository;
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void recalcular() {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(JANELA_DIAS);

        List<PessoaHabilitacao> habilitacoes = pessoaHabilitacaoRepository.findTodosComValidade();
        int alteradosHabilitacao = 0;
        for (PessoaHabilitacao vinculo : habilitacoes) {
            StatusHabilitacao novoStatus = classificarHabilitacao(vinculo.getDataValidade(), hoje, limite);
            if (vinculo.getStatus() != novoStatus) {
                vinculo.setStatus(novoStatus);
                alteradosHabilitacao++;
            }
        }
        pessoaHabilitacaoRepository.saveAll(habilitacoes);

        List<PessoaCurso> cursos = pessoaCursoRepository.findTodosComValidade();
        int alteradosCurso = 0;
        for (PessoaCurso vinculo : cursos) {
            if (vinculo.getStatus() == StatusPessoaCurso.EM_ANDAMENTO) {
                continue;
            }
            StatusPessoaCurso novoStatus = vinculo.getDataValidade().isBefore(hoje)
                    ? StatusPessoaCurso.VENCIDO
                    : StatusPessoaCurso.CONCLUIDO;
            if (vinculo.getStatus() != novoStatus) {
                vinculo.setStatus(novoStatus);
                alteradosCurso++;
            }
        }
        pessoaCursoRepository.saveAll(cursos);

        log.info("Recalculo de vencimentos: {} habilitações e {} cursos com status atualizado.",
                alteradosHabilitacao, alteradosCurso);
    }

    private StatusHabilitacao classificarHabilitacao(LocalDate dataValidade, LocalDate hoje, LocalDate limite) {
        if (dataValidade.isBefore(hoje)) {
            return StatusHabilitacao.VENCIDO;
        }
        if (!dataValidade.isAfter(limite)) {
            return StatusHabilitacao.VENCENDO;
        }
        return StatusHabilitacao.VALIDO;
    }
}
