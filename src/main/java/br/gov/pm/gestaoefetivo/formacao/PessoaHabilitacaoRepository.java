package br.gov.pm.gestaoefetivo.formacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PessoaHabilitacaoRepository extends JpaRepository<PessoaHabilitacao, Long> {

    @Query("""
            select ph from PessoaHabilitacao ph
            join fetch ph.habilitacao
            where ph.pessoa.id = :pessoaId
            order by ph.dataValidade
            """)
    List<PessoaHabilitacao> findByPessoaId(Long pessoaId);

    @Query("""
            select ph from PessoaHabilitacao ph
            join fetch ph.habilitacao
            join fetch ph.pessoa pe
            join fetch pe.unidade
            join fetch pe.posto
            where ph.dataValidade is not null
            """)
    List<PessoaHabilitacao> findTodosComValidade();

    long countByPessoaId(Long pessoaId);
}
