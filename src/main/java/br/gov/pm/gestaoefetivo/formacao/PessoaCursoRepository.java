package br.gov.pm.gestaoefetivo.formacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PessoaCursoRepository extends JpaRepository<PessoaCurso, Long> {

    @Query("""
            select pc from PessoaCurso pc
            join fetch pc.curso
            where pc.pessoa.id = :pessoaId
            order by pc.dataConclusao desc
            """)
    List<PessoaCurso> findByPessoaId(Long pessoaId);

    @Query("""
            select pc from PessoaCurso pc
            join fetch pc.curso
            join fetch pc.pessoa pe
            join fetch pe.unidade
            join fetch pe.posto
            where pc.dataValidade is not null
            """)
    List<PessoaCurso> findTodosComValidade();

    long countByPessoaId(Long pessoaId);
}
