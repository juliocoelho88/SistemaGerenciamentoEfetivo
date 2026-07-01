package br.gov.pm.gestaoefetivo.efetivo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PessoaRepository extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {

    boolean existsByRe(String re);

    boolean existsByCpf(String cpf);
}
