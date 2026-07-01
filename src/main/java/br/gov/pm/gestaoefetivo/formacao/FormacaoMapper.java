package br.gov.pm.gestaoefetivo.formacao;

import br.gov.pm.gestaoefetivo.formacao.dto.CursoResponse;
import br.gov.pm.gestaoefetivo.formacao.dto.HabilitacaoResponse;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaCursoResponse;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaEstagioResponse;
import br.gov.pm.gestaoefetivo.formacao.dto.PessoaHabilitacaoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FormacaoMapper {

    CursoResponse toResponse(Curso curso);

    HabilitacaoResponse toResponse(Habilitacao habilitacao);

    @Mapping(target = "cursoId", source = "curso.id")
    @Mapping(target = "cursoNome", source = "curso.nome")
    @Mapping(target = "instituicao", source = "curso.instituicao")
    @Mapping(target = "cargaHoraria", source = "curso.cargaHoraria")
    PessoaCursoResponse toResponse(PessoaCurso pessoaCurso);

    @Mapping(target = "habilitacaoId", source = "habilitacao.id")
    @Mapping(target = "habilitacaoNome", source = "habilitacao.nome")
    @Mapping(target = "categoria", source = "habilitacao.categoria")
    PessoaHabilitacaoResponse toResponse(PessoaHabilitacao pessoaHabilitacao);

    @Mapping(target = "unidadeId", source = "unidade.id")
    @Mapping(target = "unidadeNome", source = "unidade.nome")
    PessoaEstagioResponse toResponse(PessoaEstagio pessoaEstagio);
}
