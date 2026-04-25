package com.teatro.desktop.view;

import com.teatro.desktop.model.EventoModel;
import com.teatro.desktop.model.RelatorioOcupacaoItemModel;
import com.teatro.desktop.model.RelatorioOcupacaoModel;
import com.teatro.desktop.model.RelatorioVendasModel;
import com.teatro.desktop.model.SalaModel;
import com.teatro.desktop.model.SessaoModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.EventoApiService;
import com.teatro.desktop.service.RelatorioApiService;
import com.teatro.desktop.service.SalaApiService;
import com.teatro.desktop.service.SessaoApiService;
import com.teatro.desktop.view.layout.AdminLayout;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class DashboardView {

    private final Parent root;
    private final EventoApiService eventoApiService;
    private final SessaoApiService sessaoApiService;
    private final SalaApiService salaApiService;
    private final RelatorioApiService relatorioApiService;

    public DashboardView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.eventoApiService = new EventoApiService(authService);
        this.sessaoApiService = new SessaoApiService(authService);
        this.salaApiService = new SalaApiService(authService);
        this.relatorioApiService = new RelatorioApiService(authService);

        GridPane content = buildContent();
        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_DASHBOARD,
                "Painel Administrativo",
                "Vis\u00e3o geral da opera\u00e7\u00e3o, eventos ativos e estado do sistema",
                content
        );
        root = layout.getRoot();
    }

    private GridPane buildContent() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(18, 28, 28, 28));
        grid.setHgap(18);
        grid.setVgap(18);

        try {
            List<EventoModel> eventos = eventoApiService.listarEventos();
            List<SessaoModel> sessoes = sessaoApiService.listarSessoes();
            List<SalaModel> salas = salaApiService.listarSalas();

            Instant agora = Instant.now();
            Instant inicioPeriodo = LocalDate.now().minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant fimPeriodo = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();

            List<SessaoModel> sessoesFuturas = sessoes.stream()
                    .filter(sessao -> sessao.dataHora() != null && sessao.dataHora().isAfter(agora))
                    .sorted(Comparator.comparing(SessaoModel::dataHora))
                    .toList();

            RelatorioVendasModel relatorioVendas = relatorioApiService.obterRelatorioVendas(inicioPeriodo, fimPeriodo);
            RelatorioOcupacaoModel relatorioOcupacao = relatorioApiService.obterRelatorioOcupacao(inicioPeriodo, fimPeriodo);

            grid.add(createMetricCard("Eventos ativos", String.valueOf(eventos.size()), "Espet\u00e1culos atualmente dispon\u00edveis", "#6ea8ff"), 0, 0);
            grid.add(createMetricCard("Sess\u00f5es futuras", String.valueOf(sessoesFuturas.size()), "Agendadas a partir de agora", "#8e5cff"), 1, 0);
            grid.add(createMetricCard("Taxa m\u00e9dia", formatPercent(relatorioOcupacao.taxaMediaOcupacao()), "Ocupa\u00e7\u00e3o m\u00e9dia nos \u00faltimos 30 dias", "#29c58c"), 2, 0);
            grid.add(createWidePanel(sessoesFuturas, eventos), 0, 1, 2, 1);
            grid.add(createStatsPanel(relatorioVendas, salas, relatorioOcupacao), 2, 1);
        } catch (RuntimeException e) {
            grid.add(createErrorPanel(e.getMessage()), 0, 0, 3, 1);
        }

        return grid;
    }

    private VBox createMetricCard(String title, String value, String description, String accentColor) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #adadad;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8c8c8c;");

        Label accentLabel = new Label(" ");
        accentLabel.setMinHeight(4);
        accentLabel.setMaxWidth(Double.MAX_VALUE);
        accentLabel.setStyle(
                "-fx-background-color: " + accentColor + "; " +
                        "-fx-background-radius: 8;"
        );

        VBox card = new VBox(12, accentLabel, titleLabel, valueLabel, descriptionLabel);
        card.setPadding(new Insets(22));
        card.setMinHeight(170);
        card.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return card;
    }

    private VBox createWidePanel(List<SessaoModel> sessoesFuturas, List<EventoModel> eventos) {
        Label titleLabel = new Label("Pr\u00f3ximas Sess\u00f5es");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox recentEvents = new VBox(12);
        sessoesFuturas.stream()
                .limit(3)
                .forEach(sessao -> recentEvents.getChildren().add(createTimelineItem(
                        encontrarTituloEvento(sessao.eventoId(), eventos),
                        formatDateTime(sessao.dataHora()) + " | Sala ID " + (sessao.salaId() != null ? sessao.salaId() : "-")
                )));

        if (recentEvents.getChildren().isEmpty()) {
            recentEvents.getChildren().add(createTimelineItem("Sem sess\u00f5es futuras", "N\u00e3o existem sess\u00f5es agendadas a partir de agora."));
        }

        recentEvents.setPadding(new Insets(6, 0, 0, 0));

        VBox panel = new VBox(16, titleLabel, recentEvents);
        panel.setPadding(new Insets(22));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return panel;
    }

    private VBox createStatsPanel(RelatorioVendasModel relatorioVendas, List<SalaModel> salas, RelatorioOcupacaoModel relatorioOcupacao) {
        Label titleLabel = new Label("Indicadores");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        RelatorioOcupacaoItemModel sessaoMaisCheia = relatorioOcupacao.itens().stream()
                .max(Comparator.comparing(RelatorioOcupacaoItemModel::taxaOcupacao, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        VBox panel = new VBox(
                16,
                titleLabel,
                createStatRow("Receita 30 dias", formatCurrency(relatorioVendas.totalFaturado()), "#6ea8ff"),
                createStatRow("Espa\u00e7os ativos", salas.size() + " salas", "#8e5cff"),
                createStatRow(
                        "Sess\u00e3o com maior ocupa\u00e7\u00e3o",
                        sessaoMaisCheia != null ? formatPercent(sessaoMaisCheia.taxaOcupacao()) : "0%",
                        "#ff8b3d"
                )
        );
        panel.setPadding(new Insets(22));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return panel;
    }

    private VBox createTimelineItem(String title, String description) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8c8c8c;");

        VBox item = new VBox(4, titleLabel, descriptionLabel);
        item.setPadding(new Insets(14));
        item.setStyle(
                "-fx-background-color: #242424; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #303030; " +
                        "-fx-border-radius: 12;"
        );
        return item;
    }

    private VBox createStatRow(String title, String value, String valueColor) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #a7a7a7;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + valueColor + ";");

        VBox row = new VBox(6, titleLabel, valueLabel);
        row.setPadding(new Insets(12));
        row.setStyle(
                "-fx-background-color: #242424; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #303030; " +
                        "-fx-border-radius: 12;"
        );
        return row;
    }

    private VBox createErrorPanel(String message) {
        Label titleLabel = new Label("N\u00e3o foi poss\u00edvel carregar o dashboard");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label descriptionLabel = new Label(message != null ? message : "Erro inesperado.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ff8a8a;");

        VBox panel = new VBox(12, titleLabel, descriptionLabel);
        panel.setPadding(new Insets(24));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return panel;
    }

    private String encontrarTituloEvento(Integer eventoId, List<EventoModel> eventos) {
        return eventos.stream()
                .filter(evento -> eventoId != null && eventoId.equals(evento.id()))
                .map(EventoModel::titulo)
                .findFirst()
                .orElse("Evento #" + (eventoId != null ? eventoId : "-"));
    }

    private String formatCurrency(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO) + " EUR";
    }

    private String formatPercent(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO) + "%";
    }

    private String formatDateTime(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }

    public Parent getRoot() {
        return root;
    }
}
