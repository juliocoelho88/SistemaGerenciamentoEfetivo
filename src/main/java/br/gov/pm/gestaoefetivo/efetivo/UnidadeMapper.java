package br.gov.pm.gestaoefetivo.efetivo;

import br.gov.pm.gestaoefetivo.efetivo.dto.UnidadeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UnidadeMapper {

    @Mapping(target = "unidadePaiId", source = "unidadePai.id")
    @Mapping(target = "unidadePaiNome", source = "unidadePai.nome")
    UnidadeResponse toResponse(Unidade unidade);
}
