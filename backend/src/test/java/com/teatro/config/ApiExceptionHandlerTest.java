package com.teatro.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void deveMapearDuplicadosParaConflict() {
        ResponseEntity<?> response = handler.handleRuntimeException(new RuntimeException("Já existe um evento com esse título."));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void deveMapearReferenciasEmFaltaParaNotFound() {
        ResponseEntity<?> response = handler.handleRuntimeException(new RuntimeException("A sala associada à sessão não existe."));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deveMapearPermissaoAdministrativaParaForbidden() {
        ResponseEntity<?> response = handler.handleRuntimeException(new RuntimeException("O utilizador não tem permissão administrativa."));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void deveMapearCredenciaisInvalidasParaUnauthorized() {
        ResponseEntity<?> response = handler.handleRuntimeException(new RuntimeException("Credenciais inválidas."));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deveMapearUsernameNotFoundParaUnauthorized() {
        ResponseEntity<?> response = handler.handleRuntimeException(new UsernameNotFoundException("Utilizador não encontrado."));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void deveMapearErrosDeValidacaoParaBadRequest() {
        ResponseEntity<?> response = handler.handleRuntimeException(new RuntimeException("O período do relatório é obrigatório."));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
