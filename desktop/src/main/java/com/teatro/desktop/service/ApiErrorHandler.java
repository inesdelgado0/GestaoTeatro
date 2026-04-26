package com.teatro.desktop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;

final class ApiErrorHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ApiErrorHandler() {
    }

    static RuntimeException buildException(String fallbackMessage, HttpResponse<String> response) {
        if (response.statusCode() == 401) {
            return new RuntimeException("Sessão expirada ou credenciais inválidas.");
        }
        if (response.statusCode() == 403) {
            return new RuntimeException("Não tens permissões para aceder a esta funcionalidade.");
        }

        String detailedMessage = extractMessage(response.body());
        if (detailedMessage == null || detailedMessage.isBlank()) {
            return new RuntimeException(fallbackMessage + " HTTP " + response.statusCode());
        }
        return new RuntimeException(detailedMessage);
    }

    private static String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        try {
            JsonNode json = OBJECT_MAPPER.readTree(body);
            if (json.hasNonNull("message") && !json.get("message").asText().isBlank()) {
                return json.get("message").asText();
            }
            if (json.hasNonNull("error") && !json.get("error").asText().isBlank()) {
                return json.get("error").asText();
            }
        } catch (Exception ignored) {
        }

        String normalized = body.strip();
        if (normalized.startsWith("{") || normalized.startsWith("<!DOCTYPE") || normalized.startsWith("<html")) {
            return null;
        }

        return normalized;
    }
}
