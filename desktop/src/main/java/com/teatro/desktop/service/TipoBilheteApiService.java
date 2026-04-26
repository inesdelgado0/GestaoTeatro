package com.teatro.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.TipoBilheteModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TipoBilheteApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public TipoBilheteApiService(AuthService authService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.authService = authService;
    }

    public List<TipoBilheteModel> listarTiposBilhete() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/tipos-bilhete"))
                .GET();
        authService.applyAuthentication(requestBuilder);
        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {
                });
            }
            throw ApiErrorHandler.buildException("Não foi possível obter as categorias de desconto.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de tipos de bilhete.", e);
        }
    }

    public TipoBilheteModel criarTipoBilhete(TipoBilheteModel tipoBilhete) {
        try {
            String json = objectMapper.writeValueAsString(tipoBilhete);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/tipos-bilhete"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), TipoBilheteModel.class);
            }
            throw ApiErrorHandler.buildException("Não foi possível criar a categoria de desconto.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de tipos de bilhete.", e);
        }
    }

    public TipoBilheteModel atualizarTipoBilhete(TipoBilheteModel tipoBilhete) {
        if (tipoBilhete.id() == null) {
            throw new RuntimeException("A categoria tem de ter identificador para ser atualizada.");
        }

        try {
            String json = objectMapper.writeValueAsString(tipoBilhete);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/tipos-bilhete/" + tipoBilhete.id()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), TipoBilheteModel.class);
            }
            throw ApiErrorHandler.buildException("Não foi possível atualizar a categoria de desconto.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de tipos de bilhete.", e);
        }
    }

    public void eliminarTipoBilhete(Integer tipoBilheteId) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/tipos-bilhete/" + tipoBilheteId))
                    .DELETE();
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }
            throw ApiErrorHandler.buildException("Não foi possível eliminar a categoria de desconto.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de tipos de bilhete.", e);
        }
    }
}
