package br.gov.pm.gestaoefetivo.acesso;

import br.gov.pm.gestaoefetivo.acesso.dto.LoginRequest;
import br.gov.pm.gestaoefetivo.acesso.dto.LoginResponse;
import br.gov.pm.gestaoefetivo.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByLoginAndAtivoTrue(request.login())
                .orElseThrow(() -> new BadCredentialsException("Login ou senha inválidos."));
        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("Login ou senha inválidos.");
        }
        usuario.setUltimoAcesso(LocalDateTime.now());
        String token = jwtService.gerarToken(usuario);
        return LoginResponse.de(token, usuarioMapper.toResponse(usuario));
    }
}
