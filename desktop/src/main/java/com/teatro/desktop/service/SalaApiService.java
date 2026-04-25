package com.teatro.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.SalaModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class SalaApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public SalaApiService(AuthService authService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.authService = authService;
    }

    public List<SalaModel> listarSalas() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/salas"))
                .GET();
        authService.applyAuthentication(requestBuilder);
        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {
                });
            }
            throw ApiErrorHandler.buildException("Nao foi possivel obter as salas.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de salas.", e);
        }
    }

    public SalaModel criarSala(SalaModel sala) {
        try {
            String json = objectMapper.writeValueAsString(sala);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/salas"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), SalaModel.class);
            }
            throw ApiErrorHandler.buildException("Nao foi possivel criar a sala.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de salas.", e);
        }
    }
}
