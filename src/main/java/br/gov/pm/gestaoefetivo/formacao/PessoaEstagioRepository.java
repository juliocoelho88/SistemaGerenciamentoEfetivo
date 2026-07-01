package br.gov.pm.gestaoefetivo.formacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PessoaEstagioRepository extends JpaRepository<PessoaEstagio, Long> {

    @Query("""
            select pe from PessoaEstagio pe
            left join fetch pe.unidade
            where pe.pessoa.id = :pessoaId
            order by pe.dataInicio desc
            """)
    List<PessoaEstagio> findByPessoaId(Long pessoaId);

    long countByPessoaId(Long pessoaId);
}
