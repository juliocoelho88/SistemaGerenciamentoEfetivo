package br.gov.pm.gestaoefetivo.security;

import br.gov.pm.gestaoefetivo.acesso.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService {

    private static final String CLAIM_LOGIN = "login";
    private static final String CLAIM_PERFIL_ID = "perfilId";
    private static final String CLAIM_PERFIL_NOME = "perfilNome";
    private static final String CLAIM_ESCOPO_UNIDADE_ID = "escopoUnidadeId";
    private static final String CLAIM_PESSOA_ID = "pessoaId";

    private final SecretKey chave;
    private final Duration expiracao;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                       @Value("${app.jwt.expiracao-minutos}") long expiracaoMinutos) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracao = Duration.ofMinutes(expiracaoMinutos);
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        var builder = Jwts.builder()
                .subject(usuario.getId().toString())
                .claim(CLAIM_LOGIN, usuario.getLogin())
                .claim(CLAIM_PERFIL_ID, usuario.getPerfil().getId())
                .claim(CLAIM_PERFIL_NOME, usuario.getPerfil().getNome())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracao)))
                .signWith(chave);
        if (usuario.getEscopoUnidade() != null) {
            builder.claim(CLAIM_ESCOPO_UNIDADE_ID, usuario.getEscopoUnidade().getId());
        }
        if (usuario.getPessoa() != null) {
            builder.claim(CLAIM_PESSOA_ID, usuario.getPessoa().getId());
        }
        return builder.compact();
    }

    /** @return o usuário autenticado extraído do token, ou vazio se o token for inválido/expirado. */
    public Optional<AuthenticatedUser> validarEExtrair(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            AuthenticatedUser user = new AuthenticatedUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get(CLAIM_LOGIN, String.class),
                    claims.get(CLAIM_PERFIL_ID, Long.class),
                    claims.get(CLAIM_PERFIL_NOME, String.class),
                    claims.get(CLAIM_ESCOPO_UNIDADE_ID, Long.class),
                    claims.get(CLAIM_PESSOA_ID, Long.class));
            return Optional.of(user);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
