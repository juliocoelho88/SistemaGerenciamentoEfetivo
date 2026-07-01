package br.gov.pm.gestaoefetivo.acesso;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilPermissaoRepository extends JpaRepository<PerfilPermissao, Long> {

    @org.springframework.data.jpa.repository.Query("""
            select pp from PerfilPermissao pp
            join fetch pp.modulo
            where pp.perfil.id = :perfilId
            """)
    List<PerfilPermissao> findByPerfilId(Long perfilId);

    @org.springframework.data.jpa.repository.Query("""
            select pp from PerfilPermissao pp
            where pp.perfil.id = :perfilId and pp.modulo.chave = :moduloChave
            """)
    Optional<PerfilPermissao> findByPerfilIdEModuloChave(Long perfilId, String moduloChave);

    @org.springframework.data.jpa.repository.Query("""
            select pp from PerfilPermissao pp
            join fetch pp.perfil
            join fetch pp.modulo
            """)
    List<PerfilPermissao> findAllComPerfilEModulo();
}
