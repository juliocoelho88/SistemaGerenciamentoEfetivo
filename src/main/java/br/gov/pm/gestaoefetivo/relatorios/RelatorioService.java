package br.gov.pm.gestaoefetivo.relatorios;

import br.gov.pm.gestaoefetivo.efetivo.Pessoa;
import br.gov.pm.gestaoefetivo.efetivo.PessoaRepository;
import br.gov.pm.gestaoefetivo.efetivo.PessoaSpecifications;
import br.gov.pm.gestaoefetivo.relatorios.dto.RelatorioAgrupamentoResponse;
import br.gov.pm.gestaoefetivo.relatorios.dto.RelatorioResponse;
import br.gov.pm.gestaoefetivo.security.AuthenticatedUser;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private final PessoaRepository pessoaRepository;

    public RelatorioService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioResponse gerar() {
        AuthenticatedUser usuario = AuthenticatedUser.atual();
        Specification<Pessoa> spec = Specification.where(null);
        if (usuario != null && usuario.isVinculadoAPessoa()) {
            spec = spec.and(PessoaSpecifications.comId(usuario.pessoaId()));
        } else if (usuario != null && usuario.isGestorDeUnidade()) {
            spec = spec.and(PessoaSpecifications.daUnidade(usuario.escopoUnidadeId()));
        }

        List<Pessoa> pessoas = pessoaRepository.findAll(spec);

        return new RelatorioResponse(
                pessoas.size(),
                agrupar(pessoas, p -> p.getUnidade().getNome()),
                agrupar(pessoas, p -> p.getSituacao().getNome()),
                agrupar(pessoas, p -> p.getPosto().getNome()));
    }

    private List<RelatorioAgrupamentoResponse> agrupar(List<Pessoa> pessoas, Function<Pessoa, String> chave) {
        Map<String, Long> contagem = pessoas.stream()
                .collect(Collectors.groupingBy(chave, Collectors.counting()));
        return contagem.entrySet().stream()
                .map(e -> new RelatorioAgrupamentoResponse(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(RelatorioAgrupamentoResponse::quantidade, Comparator.reverseOrder()))
                .toList();
    }
}
