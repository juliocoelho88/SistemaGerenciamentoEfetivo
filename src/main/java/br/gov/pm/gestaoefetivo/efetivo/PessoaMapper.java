package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaDetalheResponse;
import br.gov.pm.gestaoefetivo.efetivo.dto.PessoaResumoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PessoaMapper {

    @Mapping(target = "postoId", source = "posto.id")
    @Mapping(target = "postoNome", source = "posto.nome")
    @Mapping(target = "postoAbreviacao", source = "posto.abreviacao")
    @Mapping(target = "unidadeId", source = "unidade.id")
    @Mapping(target = "unidadeNome", source = "unidade.nome")
    @Mapping(target = "situacaoId", source = "situacao.id")
    @Mapping(target = "situacaoNome", source = "situacao.nome")
    PessoaResumoResponse toResumo(Pessoa pessoa);

    @Mapping(target = "postoId", source = "posto.id")
    @Mapping(target = "postoNome", source = "posto.nome")
    @Mapping(target = "postoAbreviacao", source = "posto.abreviacao")
    @Mapping(target = "unidadeId", source = "unidade.id")
    @Mapping(target = "unidadeNome", source = "unidade.nome")
    @Mapping(target = "situacaoId", source = "situacao.id")
    @Mapping(target = "situacaoNome", source = "situacao.nome")
    PessoaDetalheResponse toDetalhe(Pessoa pessoa);
}
