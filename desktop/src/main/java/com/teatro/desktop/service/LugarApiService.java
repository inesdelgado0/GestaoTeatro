package com.teatro.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.LugarModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class LugarApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public LugarApiService(AuthService authService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.authService = authService;
    }

    public List<LugarModel> listarTodos() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/lugares"))
                .GET();
        authService.applyAuthentication(requestBuilder);
        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {
                });
            }
            throw ApiErrorHandler.buildException("Não foi possível obter os lugares.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de lugares.", e);
        }
    }

    public List<LugarModel> listarPorZona(Integer zonaId) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/lugares/zona/" + zonaId))
                .GET();
        authService.applyAuthentication(requestBuilder);
        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {
                });
            }
            throw ApiErrorHandler.buildException("Não foi possível obter os lugares.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de lugares.", e);
        }
    }

    public LugarModel criarLugar(LugarModel lugar) {
        try {
            String json = objectMapper.writeValueAsString(lugar);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/lugares"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), LugarModel.class);
            }
            throw ApiErrorHandler.buildException("Não foi possível criar o lugar.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de lugares.", e);
        }
    }

    public LugarModel atualizarLugar(LugarModel lugar) {
        if (lugar.id() == null) {
            throw new RuntimeException("O lugar tem de ter identificador para ser atualizado.");
        }

        try {
            String json = objectMapper.writeValueAsString(lugar);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/lugares/" + lugar.id()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), LugarModel.class);
            }
            throw ApiErrorHandler.buildException("Não foi possível atualizar o lugar.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de lugares.", e);
        }
    }

    public void eliminarLugar(Integer lugarId) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/lugares/" + lugarId))
                    .DELETE();
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }
            throw ApiErrorHandler.buildException("Não foi possível eliminar o lugar.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de lugares.", e);
        }
    }
}
