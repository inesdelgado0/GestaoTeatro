package com.teatro.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.ZonaModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ZonaApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public ZonaApiService(AuthService authService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.authService = authService;
    }

    public List<ZonaModel> listarPorSala(Integer salaId) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/zonas/sala/" + salaId))
                .GET();
        authService.applyAuthentication(requestBuilder);
        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {
                });
            }
            throw ApiErrorHandler.buildException("Nao foi possivel obter as zonas.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de zonas.", e);
        }
    }

    public ZonaModel criarZona(ZonaModel zona) {
        try {
            String json = objectMapper.writeValueAsString(zona);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/zonas"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), ZonaModel.class);
            }
            throw ApiErrorHandler.buildException("Nao foi possivel criar a zona.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de zonas.", e);
        }
    }

    public ZonaModel atualizarZona(ZonaModel zona) {
        if (zona.id() == null) {
            throw new RuntimeException("A zona tem de ter identificador para ser atualizada.");
        }

        try {
            String json = objectMapper.writeValueAsString(zona);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/zonas/" + zona.id()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), ZonaModel.class);
            }
            throw ApiErrorHandler.buildException("Nao foi possivel atualizar a zona.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de zonas.", e);
        }
    }

    public void eliminarZona(Integer zonaId) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/zonas/" + zonaId))
                    .DELETE();
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }
            throw ApiErrorHandler.buildException("Nao foi possivel eliminar a zona.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de zonas.", e);
        }
    }
}
