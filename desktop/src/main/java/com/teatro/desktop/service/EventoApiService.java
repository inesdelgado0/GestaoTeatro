package com.teatro.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.EventoModel;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class EventoApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public EventoApiService(AuthService authService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.authService = authService;
    }

    public List<EventoModel> listarEventos() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.BASE_URL + "/eventos"))
                .GET();
        authService.applyAuthentication(requestBuilder);
        HttpRequest request = requestBuilder.build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<>() {
                });
            }
            throw ApiErrorHandler.buildException("Nao foi possivel obter os eventos.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de eventos.", e);
        }
    }

    public EventoModel criarEvento(EventoModel evento) {
        try {
            String json = objectMapper.writeValueAsString(evento);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/eventos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), EventoModel.class);
            }
            throw ApiErrorHandler.buildException("Nao foi possivel criar o evento.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de eventos.", e);
        }
    }

    public EventoModel atualizarEvento(EventoModel evento) {
        if (evento.id() == null) {
            throw new RuntimeException("O evento tem de ter identificador para ser atualizado.");
        }

        try {
            String json = objectMapper.writeValueAsString(evento);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/eventos/" + evento.id()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json));
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), EventoModel.class);
            }
            throw ApiErrorHandler.buildException("Nao foi possivel atualizar o evento.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de eventos.", e);
        }
    }

    public void eliminarEvento(Integer eventoId) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + "/eventos/" + eventoId))
                    .DELETE();
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return;
            }
            throw ApiErrorHandler.buildException("Nao foi possivel eliminar o evento.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de eventos.", e);
        }
    }
}
