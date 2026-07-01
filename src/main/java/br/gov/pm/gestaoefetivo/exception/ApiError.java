package br.gov.pm.gestaoefetivo.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> detalhes
) {
    public ApiError(int status, String error, String message, String path, List<String> detalhes) {
        this(Instant.now(), status, error, message, path, detalhes);
    }
}
