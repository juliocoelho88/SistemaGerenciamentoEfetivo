package br.gov.pm.gestaoefetivo.acesso;

import br.gov.pm.gestaoefetivo.acesso.dto.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "perfilId", source = "perfil.id")
    @Mapping(target = "perfilNome", source = "perfil.nome")
    @Mapping(target = "escopoUnidadeId", source = "escopoUnidade.id")
    @Mapping(target = "escopoUnidadeNome", source = "escopoUnidade.nome")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nome")
    UsuarioResponse toResponse(Usuario usuario);
}
