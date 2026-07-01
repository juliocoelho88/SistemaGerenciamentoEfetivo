package br.gov.pm.gestaoefetivo.acesso;

import br.gov.pm.gestaoefetivo.acesso.dto.UsuarioRequest;
import br.gov.pm.gestaoefetivo.acesso.dto.UsuarioResponse;
import br.gov.pm.gestaoefetivo.efetivo.Pessoa;
import br.gov.pm.gestaoefetivo.efetivo.PessoaRepository;
import br.gov.pm.gestaoefetivo.efetivo.Unidade;
import br.gov.pm.gestaoefetivo.efetivo.UnidadeRepository;
import br.gov.pm.gestaoefetivo.exception.RecursoNaoEncontradoException;
import br.gov.pm.gestaoefetivo.exception.RegraNegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PessoaRepository pessoaRepository;
    private final UnidadeRepository unidadeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository,
                           PessoaRepository pessoaRepository, UnidadeRepository unidadeRepository,
                           PasswordEncoder passwordEncoder, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.pessoaRepository = pessoaRepository;
        this.unidadeRepository = unidadeRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResponse).toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (usuarioRepository.existsByLogin(request.login())) {
            throw new RegraNegocioException("Já existe um usuário com o login '" + request.login() + "'.");
        }
        if (request.senha() == null || request.senha().isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória ao criar um usuário.");
        }
        Usuario usuario = new Usuario();
        usuario.setLogin(request.login());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        aplicarVinculos(usuario, request);
        usuario.setAtivo(request.ativo() == null || request.ativo());
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscarOuFalhar(id);
        if (!usuario.getLogin().equals(request.login()) && usuarioRepository.existsByLogin(request.login())) {
            throw new RegraNegocioException("Já existe um usuário com o login '" + request.login() + "'.");
        }
        usuario.setLogin(request.login());
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        }
        aplicarVinculos(usuario, request);
        if (request.ativo() != null) {
            usuario.setAtivo(request.ativo());
        }
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    private void aplicarVinculos(Usuario usuario, UsuarioRequest request) {
        Perfil perfil = perfilRepository.findById(request.perfilId())
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Perfil", request.perfilId()));
        usuario.setPerfil(perfil);

        if (request.escopoUnidadeId() != null) {
            Unidade unidade = unidadeRepository.findById(request.escopoUnidadeId())
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Unidade", request.escopoUnidadeId()));
            usuario.setEscopoUnidade(unidade);
        } else {
            usuario.setEscopoUnidade(null);
        }

        if (request.pessoaId() != null) {
            Pessoa pessoa = pessoaRepository.findById(request.pessoaId())
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Pessoa", request.pessoaId()));
            usuario.setPessoa(pessoa);
        } else {
            usuario.setPessoa(null);
        }
    }

    private Usuario buscarOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuário", id));
    }
}
