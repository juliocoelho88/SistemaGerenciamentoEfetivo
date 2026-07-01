package br.gov.pm.gestaoefetivo.acesso.dto;

public record LoginResponse(
        String token,
        String tipo,
        UsuarioResponse usuario
) {
    public static LoginResponse de(String token, UsuarioResponse usuario) {
        return new LoginResponse(token, "Bearer", usuario);
    }
}
