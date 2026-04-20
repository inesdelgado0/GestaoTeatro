package com.teatro.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.AuthLoginResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String lastErrorMessage;

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
                objectMapper.readValue(response.body(), AuthLoginResponse.class);
                lastErrorMessage = null;
                return true;
            }

            lastErrorMessage = "Credenciais invalidas ou sem permissao administrativa.";
            return false;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            lastErrorMessage = "Nao foi possivel comunicar com o backend de autenticacao.";
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private record LoginPayload(String email, String password) {
    }
}
