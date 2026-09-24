package br.gov.pm.gestaoefetivo.efetivo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface PessoaRepository extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {

    boolean existsByRe(String re);

    boolean existsByCpf(String cpf);

    /**
     * Usados pela importação em massa: resolvem em uma consulta o que, linha a linha, custaria um
     * existsBy por registro da planilha.
     */
    List<Pessoa> findAllByReIn(Collection<String> res);

    List<Pessoa> findAllByCpfIn(Collection<String> cpfs);
}
