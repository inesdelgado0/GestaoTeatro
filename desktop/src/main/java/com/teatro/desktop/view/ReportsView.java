package com.teatro.desktop.view;

import com.teatro.desktop.model.LugarModel;
import com.teatro.desktop.model.SalaModel;
import com.teatro.desktop.model.SessaoModel;
import com.teatro.desktop.model.EventoModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.EventoApiService;
import com.teatro.desktop.service.LugarApiService;
import com.teatro.desktop.service.SalaApiService;
import com.teatro.desktop.service.SessaoApiService;
import com.teatro.desktop.view.layout.AdminLayout;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

public class ReportsView {

    private final Parent root;
    private final EventoApiService eventoApiService;
    private final SessaoApiService sessaoApiService;
    private final SalaApiService salaApiService;
    private final LugarApiService lugarApiService;
    private final Label feedbackLabel;

    public ReportsView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.eventoApiService = new EventoApiService();
        this.sessaoApiService = new SessaoApiService();
        this.salaApiService = new SalaApiService();
        this.lugarApiService = new LugarApiService();
        this.feedbackLabel = new Label();

        AdminLayout layout = new AdminLayout(
                sceneManager,
                userEmail,
                AdminLayout.SECTION_REPORTS,
                "Relatorios",
                "Indicadores gerais da operacao administrativa",
                buildContent()
        );
        this.root = layout.getRoot();
    }

    private Parent buildContent() {
        VBox wrapper = new VBox(18);
        wrapper.setPadding(new Insets(18, 28, 28, 28));

        try {
            List<EventoModel> eventos = eventoApiService.listarEventos();
            List<SessaoModel> sessoes = sessaoApiService.listarSessoes();
            List<SalaModel> salas = salaApiService.listarSalas();
            List<LugarModel> lugares = lugarApiService.listarTodos();

            GridPane metrics = new GridPane();
            metrics.setHgap(18);
            metrics.setVgap(18);
            metrics.add(createMetricCard("Eventos", String.valueOf(eventos.size()), "Catalogo atualmente registado", "#3f8cff"), 0, 0);
            metrics.add(createMetricCard("Sessoes", String.valueOf(sessoes.size()), "Agendamentos registados", "#8e5cf6"), 1, 0);
            metrics.add(createMetricCard("Salas", String.valueOf(salas.size()), "Infraestrutura ativa", "#29c58c"), 2, 0);
            metrics.add(createMetricCard("Lugares", String.valueOf(lugares.size()), "Mapa total de lugares", "#ff9f43"), 3, 0);

            HBox lowerPanels = new HBox(18, createTopEventsPanel(eventos, sessoes), createRoomsTablePanel(salas, sessoes));
            HBox.setHgrow(lowerPanels.getChildren().get(0), Priority.ALWAYS);
            HBox.setHgrow(lowerPanels.getChildren().get(1), Priority.ALWAYS);

            wrapper.getChildren().addAll(metrics, lowerPanels);
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
            feedbackLabel.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 13px;");
            wrapper.getChildren().add(feedbackLabel);
        }

        return wrapper;
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
        accentLabel.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 8;");

        VBox card = new VBox(12, accentLabel, titleLabel, valueLabel, descriptionLabel);
        card.setPadding(new Insets(22));
        card.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return card;
    }

    private VBox createTopEventsPanel(List<EventoModel> eventos, List<SessaoModel> sessoes) {
        VBox content = new VBox(12);
        eventos.stream()
                .sorted(Comparator.comparingLong((EventoModel evento) -> contarSessoesDoEvento(evento, sessoes)).reversed())
                .limit(5)
                .forEach(evento -> {
                    long totalSessoes = contarSessoesDoEvento(evento, sessoes);
                    content.getChildren().add(createInfoRow(
                            evento.titulo(),
                            totalSessoes + " sessoes"
                    ));
                });

        if (content.getChildren().isEmpty()) {
            content.getChildren().add(createInfoRow("Sem dados", "Nao existem eventos registados"));
        }

        VBox panel = new VBox(16, createSectionTitle("Eventos com mais sessoes"), content);
        panel.setPadding(new Insets(22));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return panel;
    }

    private VBox createRoomsTablePanel(List<SalaModel> salas, List<SessaoModel> sessoes) {
        TableView<SalaModel> tableView = new TableView<>();

        TableColumn<SalaModel, String> nomeColumn = new TableColumn<>("Sala");
        nomeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nome()));
        nomeColumn.setPrefWidth(220);

        TableColumn<SalaModel, Number> capacidadeColumn = new TableColumn<>("Capacidade");
        capacidadeColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().capacidadeTotal() != null ? data.getValue().capacidadeTotal() : 0
        ));
        capacidadeColumn.setPrefWidth(120);

        TableColumn<SalaModel, Number> sessoesColumn = new TableColumn<>("Sessoes");
        sessoesColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                (int) sessoes.stream()
                        .filter(sessao -> data.getValue().id() != null && data.getValue().id().equals(sessao.salaId()))
                        .count()
        ));
        sessoesColumn.setPrefWidth(100);

        tableView.getColumns().setAll(nomeColumn, capacidadeColumn, sessoesColumn);
        tableView.getItems().setAll(salas);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        VBox panel = new VBox(16, createSectionTitle("Capacidade por sala"), tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        panel.setPadding(new Insets(22));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return panel;
    }

    private VBox createInfoRow(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #8c8c8c; -fx-font-size: 12px;");

        VBox row = new VBox(4, titleLabel, subtitleLabel);
        row.setPadding(new Insets(12));
        row.setStyle(
                "-fx-background-color: #242424; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #303030; " +
                        "-fx-border-radius: 12;"
        );
        return row;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        return label;
    }

    private long contarSessoesDoEvento(EventoModel evento, List<SessaoModel> sessoes) {
        return sessoes.stream()
                .filter(sessao -> evento.id() != null && evento.id().equals(sessao.eventoId()))
                .count();
    }

    public Parent getRoot() {
        return root;
    }
}
