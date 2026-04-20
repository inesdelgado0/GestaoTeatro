package com.teatro.desktop.view;

import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.view.layout.AdminLayout;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DashboardView {

    private final Parent root;

    public DashboardView(SceneManager sceneManager, AuthService authService, String userEmail) {
        GridPane content = buildContent();
        AdminLayout layout = new AdminLayout(
                sceneManager,
                userEmail,
                AdminLayout.SECTION_DASHBOARD,
                "Painel Administrativo",
                "Visao geral da operacao, eventos ativos e estado do sistema",
                content
        );
        root = layout.getRoot();
    }

    private GridPane buildContent() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(18, 28, 28, 28));
        grid.setHgap(18);
        grid.setVgap(18);

        grid.add(createMetricCard("Eventos ativos", "24", "Espetaculos atualmente disponiveis", "#3f8cff"), 0, 0);
        grid.add(createMetricCard("Sessoes futuras", "18", "Agendadas para os proximos dias", "#8e5cff"), 1, 0);
        grid.add(createMetricCard("Taxa media", "72%", "Ocupacao media das sessoes", "#29c58c"), 2, 0);
        grid.add(createWidePanel(), 0, 1, 2, 1);
        grid.add(createStatsPanel(), 2, 1);

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

    private VBox createWidePanel() {
        Label titleLabel = new Label("Atividade recente");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox recentEvents = new VBox(
                12,
                createTimelineItem("Evento atualizado", "Hamlet recebeu nova classificacao etaria"),
                createTimelineItem("Sessao criada", "Nova sessao para A Tempestade na Sala Principal"),
                createTimelineItem("Sala revista", "Capacidade da Sala Estudio confirmada")
        );
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

    private VBox createStatsPanel() {
        Label titleLabel = new Label("Indicadores");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox panel = new VBox(
                16,
                titleLabel,
                createStatRow("Receita estimada", "12 450 EUR", "#3f8cff"),
                createStatRow("Espacos ativos", "4 salas", "#8e5cff"),
                createStatRow("Alertas", "2 por rever", "#ff8b3d")
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

    public Parent getRoot() {
        return root;
    }
}
