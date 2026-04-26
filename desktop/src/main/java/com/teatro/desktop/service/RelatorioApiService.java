package com.teatro.desktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.teatro.desktop.config.ApiConfig;
import com.teatro.desktop.model.RelatorioOcupacaoModel;
import com.teatro.desktop.model.RelatorioVendasModel;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class RelatorioApiService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public RelatorioApiService(AuthService authService) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.authService = authService;
    }

    public RelatorioVendasModel obterRelatorioVendas(Instant inicio, Instant fim) {
        return execute("/relatorios/vendas", inicio, fim, RelatorioVendasModel.class);
    }

    public RelatorioOcupacaoModel obterRelatorioOcupacao(Instant inicio, Instant fim) {
        return execute("/relatorios/ocupacao", inicio, fim, RelatorioOcupacaoModel.class);
    }

    private <T> T execute(String path, Instant inicio, Instant fim, Class<T> responseType) {
        try {
            String query = "?inicio=" + encode(inicio.toString()) + "&fim=" + encode(fim.toString());
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(ApiConfig.BASE_URL + path + query))
                    .GET();
            authService.applyAuthentication(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), responseType);
            }
            throw ApiErrorHandler.buildException("Não foi possível obter o relatório.", response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o backend de relatórios.", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
