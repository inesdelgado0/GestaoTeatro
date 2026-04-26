package com.teatro.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Locale;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException exception) {
        HttpStatus status = mapStatus(exception);
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                safeMessage(exception),
                Instant.now()
        ));
    }

    private HttpStatus mapStatus(RuntimeException exception) {
        if (exception instanceof UsernameNotFoundException) {
            return HttpStatus.UNAUTHORIZED;
        }

        String message = safeMessage(exception).toLowerCase(Locale.ROOT);

        if (message.contains("credenciais inválidas")
                || message.contains("credenciais invalidas")
                || message.contains("iniciar sessão")) {
            return HttpStatus.UNAUTHORIZED;
        }

        if (message.contains("permissão administrativa")
                || message.contains("permissao administrativa")) {
            return HttpStatus.FORBIDDEN;
        }

        if (message.contains("não encontrada")
                || message.contains("não encontrado")
                || message.contains("não existe")
                || message.contains("nao encontrada")
                || message.contains("nao encontrado")
                || message.contains("nao existe")) {
            return HttpStatus.NOT_FOUND;
        }

        if (message.contains("já existe")
                || message.contains("ja existe")
                || message.contains("ocupado")
                || message.contains("associadas")
                || message.contains("associados")) {
            return HttpStatus.CONFLICT;
        }

        return HttpStatus.BAD_REQUEST;
    }

    private String safeMessage(RuntimeException exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "O pedido não pôde ser processado.";
        }
        return exception.getMessage();
    }

    private record ApiErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {
    }
}
