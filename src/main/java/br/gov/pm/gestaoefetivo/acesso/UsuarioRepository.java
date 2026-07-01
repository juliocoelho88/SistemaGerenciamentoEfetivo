package br.gov.pm.gestaoefetivo.acesso;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLoginAndAtivoTrue(String login);

    boolean existsByLogin(String login);
}
