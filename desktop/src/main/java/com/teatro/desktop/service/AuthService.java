package com.teatro.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.AuthLoginResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String lastErrorMessage;
    private String authorizationHeader;
    private AuthLoginResponse authenticatedUser;

    public AuthService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public boolean authenticate(String email, String password) {
        try {
            String json = objectMapper.writeValueAsString(new LoginPayload(email, password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                authenticatedUser = objectMapper.readValue(response.body(), AuthLoginResponse.class);
                authorizationHeader = buildBasicAuthorizationHeader(email, password);
                lastErrorMessage = null;
                return true;
            }

            lastErrorMessage = "Credenciais inválidas ou sem permissão administrativa.";
            return false;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            lastErrorMessage = "Não foi possível comunicar com o backend de autenticação.";
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void applyAuthentication(HttpRequest.Builder requestBuilder) {
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            requestBuilder.header("Authorization", authorizationHeader);
        }
    }

    public void logout() {
        authorizationHeader = null;
        authenticatedUser = null;
        lastErrorMessage = null;
    }

    public AuthLoginResponse getAuthenticatedUser() {
        return authenticatedUser;
    }

    private String buildBasicAuthorizationHeader(String email, String password) {
        String credentials = email + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private record LoginPayload(String email, String password) {
    }
}
