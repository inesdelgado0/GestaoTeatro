package com.teatro.desktop.view;

import com.teatro.desktop.model.RelatorioOcupacaoItemModel;
import com.teatro.desktop.model.RelatorioOcupacaoModel;
import com.teatro.desktop.model.RelatorioVendasItemModel;
import com.teatro.desktop.model.RelatorioVendasModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.RelatorioApiService;
import com.teatro.desktop.view.layout.AdminLayout;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

public class ReportsView {

    private final Parent root;
    private final RelatorioApiService relatorioApiService;
    private final Label feedbackLabel;
    private final Label totalFaturadoLabel;
    private final Label totalBilhetesLabel;
    private final Label taxaMediaLabel;
    private final DatePicker inicioPicker;
    private final DatePicker fimPicker;
    private final TableView<RelatorioVendasItemModel> vendasTable;
    private final TableView<RelatorioOcupacaoItemModel> ocupacaoTable;
    private Button aplicarButton;

    public ReportsView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.relatorioApiService = new RelatorioApiService(authService);
        this.feedbackLabel = new Label();
        this.totalFaturadoLabel = createMetricValue();
        this.totalBilhetesLabel = createMetricValue();
        this.taxaMediaLabel = createMetricValue();
        this.inicioPicker = new DatePicker(LocalDate.now().minusMonths(1));
        this.fimPicker = new DatePicker(LocalDate.now());
        this.vendasTable = new TableView<>();
        this.ocupacaoTable = new TableView<>();

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_REPORTS,
                "Relatórios",
                "Vendas por período e taxa de ocupação das sessões",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarRelatorios();
    }

    private Parent buildContent() {
        VBox wrapper = new VBox(18);
        wrapper.setPadding(new Insets(18, 28, 28, 28));

        feedbackLabel.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 13px;");

        HBox filtros = buildFiltros();
        GridPane metricas = buildMetricas();
        HBox tabelas = new HBox(18, buildVendasPanel(), buildOcupacaoPanel());
        HBox.setHgrow(tabelas.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(tabelas.getChildren().get(1), Priority.ALWAYS);

        wrapper.getChildren().addAll(filtros, metricas, tabelas, feedbackLabel);
        return wrapper;
    }

    private HBox buildFiltros() {
        inicioPicker.setPromptText("Data inicial");
        fimPicker.setPromptText("Data final");
        inicioPicker.setPrefWidth(150);
        fimPicker.setPrefWidth(150);

        aplicarButton = new Button("Aplicar período");
        aplicarButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6ea8ff, #4e8fe8); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 11 18 11 18;"
        );
        aplicarButton.setOnAction(event -> carregarRelatorios());

        VBox inicioBox = new VBox(6, createFieldLabel("Início"), inicioPicker);
        VBox fimBox = new VBox(6, createFieldLabel("Fim"), fimPicker);
        HBox filtros = new HBox(12, inicioBox, fimBox, aplicarButton);
        filtros.setAlignment(Pos.BOTTOM_LEFT);
        filtros.setPadding(new Insets(20));
        filtros.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return filtros;
    }

    private GridPane buildMetricas() {
        GridPane metrics = new GridPane();
        metrics.setHgap(18);
        metrics.setVgap(18);
        metrics.add(createMetricCard("Faturação", totalFaturadoLabel, "Total pago no período", "#6ea8ff"), 0, 0);
        metrics.add(createMetricCard("Bilhetes Pagos", totalBilhetesLabel, "Bilhetes vendidos no periodo", "#29c58c"), 1, 0);
        metrics.add(createMetricCard("Taxa Média", taxaMediaLabel, "Ocupação média das sessões", "#ff9f43"), 2, 0);
        return metrics;
    }

    private VBox buildVendasPanel() {
        TableColumn<RelatorioVendasItemModel, String> eventoColumn = new TableColumn<>("Evento");
        eventoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().eventoTitulo()));

        TableColumn<RelatorioVendasItemModel, String> salaColumn = new TableColumn<>("Sala");
        salaColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().salaNome()));

        TableColumn<RelatorioVendasItemModel, Number> bilhetesColumn = new TableColumn<>("Bilhetes");
        bilhetesColumn.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().totalBilhetes()));

        TableColumn<RelatorioVendasItemModel, String> totalColumn = new TableColumn<>("Faturado");
        totalColumn.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().totalFaturado())));

        vendasTable.getColumns().setAll(eventoColumn, salaColumn, bilhetesColumn, totalColumn);
        vendasTable.setPlaceholder(new Label("Sem dados disponíveis."));
        vendasTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        VBox panel = new VBox(16, createSectionTitle("Vendas por Sessão"), vendasTable);
        VBox.setVgrow(vendasTable, Priority.ALWAYS);
        panel.setPadding(new Insets(22));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return panel;
    }

    private VBox buildOcupacaoPanel() {
        TableColumn<RelatorioOcupacaoItemModel, String> eventoColumn = new TableColumn<>("Evento");
        eventoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().eventoTitulo()));

        TableColumn<RelatorioOcupacaoItemModel, Number> capacidadeColumn = new TableColumn<>("Capacidade");
        capacidadeColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().capacidadeSala()));

        TableColumn<RelatorioOcupacaoItemModel, Number> ocupadosColumn = new TableColumn<>("Ocupados");
        ocupadosColumn.setCellValueFactory(data -> new SimpleLongProperty(data.getValue().lugaresOcupados()));

        TableColumn<RelatorioOcupacaoItemModel, String> taxaColumn = new TableColumn<>("Taxa");
        taxaColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().taxaOcupacao() != null ? data.getValue().taxaOcupacao() + "%" : "0%"
        ));

        ocupacaoTable.getColumns().setAll(eventoColumn, capacidadeColumn, ocupadosColumn, taxaColumn);
        ocupacaoTable.setPlaceholder(new Label("Sem dados disponíveis."));
        ocupacaoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        VBox panel = new VBox(16, createSectionTitle("Ocupação por Sessão"), ocupacaoTable);
        VBox.setVgrow(ocupacaoTable, Priority.ALWAYS);
        panel.setPadding(new Insets(22));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; " +
                        "-fx-background-radius: 16; " +
                        "-fx-border-color: #333333; " +
                        "-fx-border-radius: 16;"
        );
        return panel;
    }

    private void carregarRelatorios() {
        Instant inicio;
        Instant fim;

        try {
            feedbackLabel.setText("");
            inicio = toStartOfDay(inicioPicker.getValue());
            fim = toEndOfDay(fimPicker.getValue());
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
            return;
        }

        aplicarButton.setDisable(true);
        inicioPicker.setDisable(true);
        fimPicker.setDisable(true);
        feedbackLabel.setText("A carregar relatórios...");

        CompletableFuture.supplyAsync(() -> new LoadedReports(
                relatorioApiService.obterRelatorioVendas(inicio, fim),
                relatorioApiService.obterRelatorioOcupacao(inicio, fim)
        )).whenComplete((loadedReports, throwable) -> Platform.runLater(() -> {
            aplicarButton.setDisable(false);
            inicioPicker.setDisable(false);
            fimPicker.setDisable(false);

            if (throwable != null) {
                String message = throwable.getCause() != null && throwable.getCause().getMessage() != null
                        ? throwable.getCause().getMessage()
                        : throwable.getMessage();
                feedbackLabel.setText(message != null ? message : "Não foi possível obter o relatório.");
                return;
            }

            feedbackLabel.setText("");
            totalFaturadoLabel.setText(formatCurrency(loadedReports.relatorioVendas().totalFaturado()));
            totalBilhetesLabel.setText(String.valueOf(loadedReports.relatorioVendas().totalBilhetes()));
            taxaMediaLabel.setText((loadedReports.relatorioOcupacao().taxaMediaOcupacao() != null
                    ? loadedReports.relatorioOcupacao().taxaMediaOcupacao()
                    : BigDecimal.ZERO) + "%");
            vendasTable.getItems().setAll(loadedReports.relatorioVendas().itens());
            ocupacaoTable.getItems().setAll(loadedReports.relatorioOcupacao().itens());
        }));
    }

    private VBox createMetricCard(String title, Label valueLabel, String description, String accentColor) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #adadad;");

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

    private Label createMetricValue() {
        Label label = new Label("0");
        label.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
        return label;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        return label;
    }

    private Instant toStartOfDay(LocalDate date) {
        if (date == null) {
            throw new RuntimeException("A data inicial é obrigatória.");
        }
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private Instant toEndOfDay(LocalDate date) {
        if (date == null) {
            throw new RuntimeException("A data final é obrigatória.");
        }
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant();
    }

    private String formatCurrency(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO) + " EUR";
    }

    public Parent getRoot() {
        return root;
    }

    private record LoadedReports(
            RelatorioVendasModel relatorioVendas,
            RelatorioOcupacaoModel relatorioOcupacao
    ) {
    }
}
