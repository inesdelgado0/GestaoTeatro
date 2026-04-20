package com.teatro.desktop.view;

import com.teatro.desktop.model.LugarModel;
import com.teatro.desktop.model.SalaModel;
import com.teatro.desktop.model.ZonaModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.LugarApiService;
import com.teatro.desktop.service.SalaApiService;
import com.teatro.desktop.service.ZonaApiService;
import com.teatro.desktop.view.layout.AdminLayout;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZonesView {

    private final Parent root;
    private final SalaApiService salaApiService;
    private final ZonaApiService zonaApiService;
    private final LugarApiService lugarApiService;

    private final ComboBox<SalaModel> salaComboBox;
    private final VBox zonasListBox;
    private final GridPane seatsGrid;
    private final Label seatsTitleLabel;
    private final Label feedbackLabel;
    private final Label totalSeatsLabel;
    private final Label selectedZoneLabel;

    private SalaModel selectedSala;
    private ZonaModel selectedZona;
    private List<ZonaModel> currentZonas;
    private List<LugarModel> currentLugares;

    public ZonesView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.salaApiService = new SalaApiService();
        this.zonaApiService = new ZonaApiService();
        this.lugarApiService = new LugarApiService();
        this.salaComboBox = new ComboBox<>();
        this.zonasListBox = new VBox(12);
        this.seatsGrid = new GridPane();
        this.seatsTitleLabel = new Label("Mapa de Lugares");
        this.feedbackLabel = new Label();
        this.totalSeatsLabel = new Label("0");
        this.selectedZoneLabel = new Label("Nenhuma");
        this.currentZonas = new ArrayList<>();
        this.currentLugares = new ArrayList<>();

        AdminLayout layout = new AdminLayout(
                sceneManager,
                userEmail,
                AdminLayout.SECTION_ZONES,
                "Gestao de zonas & lugares",
                "Configurar zonas de assentos e disposicao individual de lugares",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarSalas();
    }

    private Parent buildContent() {
        VBox centerPanel = buildCenterPanel();
        HBox main = new HBox(20, buildLeftColumn(), centerPanel, buildRightColumn());
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        main.setPadding(new Insets(18, 28, 28, 28));
        return main;
    }

    private VBox buildLeftColumn() {
        Label salaLabel = createPanelTitle("Selecionar Sala");
        salaComboBox.setPromptText("Escolher sala");
        salaComboBox.setMaxWidth(Double.MAX_VALUE);
        salaComboBox.setOnAction(event -> {
            selectedSala = salaComboBox.getValue();
            selectedZona = null;
            carregarZonas();
        });

        VBox salaPanel = createPanel(new VBox(12, salaLabel, salaComboBox));
        salaPanel.setPrefWidth(240);

        Label zonasLabel = createPanelTitle("Zonas");
        ScrollPane zonasScroll = new ScrollPane(zonasListBox);
        zonasScroll.setFitToWidth(true);
        zonasScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox zonasPanel = createPanel(new VBox(12, zonasLabel, zonasScroll));
        zonasPanel.setPrefWidth(240);
        VBox.setVgrow(zonasScroll, Priority.ALWAYS);

        return new VBox(20, salaPanel, zonasPanel);
    }

    private VBox buildCenterPanel() {
        seatsTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button addSeatButton = new Button("+ Adicionar Lugar");
        addSeatButton.setStyle(
                "-fx-background-color: #1f1f1f; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 14 10 14;"
        );

        HBox headerRow = new HBox(16, seatsTitleLabel, spacer(), addSeatButton);

        Label palcoLabel = new Label("PALCO");
        palcoLabel.setStyle(
                "-fx-background-color: #1f1f1f; -fx-text-fill: white; -fx-font-size: 20px; " +
                        "-fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10 0 10 0;"
        );
        palcoLabel.setMaxWidth(Double.MAX_VALUE);

        seatsGrid.setHgap(8);
        seatsGrid.setVgap(8);
        seatsGrid.setPadding(new Insets(12));

        VBox panel = createPanel(new VBox(16, headerRow, palcoLabel, seatsGrid));
        VBox.setVgrow(seatsGrid, Priority.ALWAYS);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildRightColumn() {
        VBox legendPanel = buildLegendPanel();
        VBox statsPanel = buildStatsPanel();
        VBox zoneForm = buildZoneForm();
        VBox seatForm = buildSeatForm();

        feedbackLabel.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 12px;");

        VBox right = new VBox(20, legendPanel, statsPanel, zoneForm, seatForm, feedbackLabel);
        right.setPrefWidth(280);
        return right;
    }

    private VBox buildLegendPanel() {
        VBox content = new VBox(
                14,
                createPanelTitle("Legenda"),
                createLegendRow("Zona selecionada", zoneColor(selectedZona)),
                createLegendRow("Lugar visivel", "#67a9f4")
        );
        return createPanel(content);
    }

    private VBox buildStatsPanel() {
        totalSeatsLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #67a9f4;");
        selectedZoneLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox content = new VBox(
                12,
                createPanelTitle("Estatisticas"),
                createStatItem("Zona ativa", selectedZoneLabel),
                createStatItem("Total de lugares", totalSeatsLabel),
                createMiniProgress("Taxa de desenho", "100%")
        );
        return createPanel(content);
    }

    private VBox buildZoneForm() {
        Label title = createPanelTitle("Adicionar Zona");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome da zona");

        TextField taxaField = new TextField();
        taxaField.setPromptText("Taxa adicional");

        Button saveButton = createPrimaryButton("Guardar zona");
        saveButton.setOnAction(event -> {
            if (selectedSala == null) {
                feedbackLabel.setText("Selecione primeiro uma sala.");
                return;
            }

            try {
                BigDecimal taxa = taxaField.getText() == null || taxaField.getText().isBlank()
                        ? BigDecimal.ZERO
                        : new BigDecimal(taxaField.getText());

                ZonaModel zona = new ZonaModel(null, nomeField.getText(), taxa, selectedSala.id());
                zonaApiService.criarZona(zona);
                feedbackLabel.setText("Zona criada com sucesso.");
                nomeField.clear();
                taxaField.clear();
                carregarZonas();
            } catch (NumberFormatException e) {
                feedbackLabel.setText("A taxa adicional deve ser numerica.");
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        VBox content = new VBox(
                12,
                title,
                createFormLabel("Nome"),
                nomeField,
                createFormLabel("Taxa adicional"),
                taxaField,
                saveButton
        );
        return createPanel(content);
    }

    private VBox buildSeatForm() {
        Label title = createPanelTitle("Adicionar Lugar");

        TextField filaField = new TextField();
        filaField.setPromptText("Fila");

        TextField numeroField = new TextField();
        numeroField.setPromptText("Numero");

        Button saveButton = createPrimaryButton("Guardar lugar");
        saveButton.setOnAction(event -> {
            if (selectedZona == null) {
                feedbackLabel.setText("Selecione primeiro uma zona.");
                return;
            }

            try {
                LugarModel lugar = new LugarModel(
                        null,
                        filaField.getText(),
                        Integer.parseInt(numeroField.getText()),
                        selectedZona.id()
                );
                lugarApiService.criarLugar(lugar);
                feedbackLabel.setText("Lugar criado com sucesso.");
                filaField.clear();
                numeroField.clear();
                carregarLugares();
            } catch (NumberFormatException e) {
                feedbackLabel.setText("O numero do lugar deve ser numerico.");
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        VBox content = new VBox(
                12,
                title,
                createFormLabel("Fila"),
                filaField,
                createFormLabel("Numero"),
                numeroField,
                saveButton
        );
        return createPanel(content);
    }

    private void carregarSalas() {
        try {
            List<SalaModel> salas = salaApiService.listarSalas();
            salaComboBox.getItems().setAll(salas);
            salaComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(SalaModel item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.nome());
                }
            });
            salaComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(SalaModel item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.nome());
                }
            });
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
        }
    }

    private void carregarZonas() {
        zonasListBox.getChildren().clear();
        seatsGrid.getChildren().clear();
        seatsTitleLabel.setText("Mapa de Lugares");
        totalSeatsLabel.setText("0");
        selectedZoneLabel.setText("Nenhuma");
        currentLugares = new ArrayList<>();

        if (selectedSala == null) {
            currentZonas = new ArrayList<>();
            return;
        }

        try {
            currentZonas = zonaApiService.listarPorSala(selectedSala.id());
            for (ZonaModel zona : currentZonas) {
                zonasListBox.getChildren().add(createZoneCard(zona));
            }
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
        }
    }

    private void carregarLugares() {
        seatsGrid.getChildren().clear();

        if (selectedZona == null) {
            seatsTitleLabel.setText("Mapa de Lugares");
            selectedZoneLabel.setText("Nenhuma");
            totalSeatsLabel.setText("0");
            currentLugares = new ArrayList<>();
            return;
        }

        seatsTitleLabel.setText("Mapa de Lugares - " + selectedZona.nome());
        selectedZoneLabel.setText(selectedZona.nome());

        try {
            currentLugares = new ArrayList<>(lugarApiService.listarPorZona(selectedZona.id()));
            currentLugares.sort(Comparator
                    .comparing(LugarModel::fila, Comparator.nullsLast(String::compareTo))
                    .thenComparing(LugarModel::numero, Comparator.nullsLast(Integer::compareTo)));

            totalSeatsLabel.setText(String.valueOf(currentLugares.size()));

            Map<String, List<LugarModel>> porFila = new LinkedHashMap<>();
            for (LugarModel lugar : currentLugares) {
                porFila.computeIfAbsent(lugar.fila(), key -> new ArrayList<>()).add(lugar);
            }

            int rowIndex = 0;
            for (Map.Entry<String, List<LugarModel>> entry : porFila.entrySet()) {
                Label filaLabel = new Label(entry.getKey());
                filaLabel.setStyle("-fx-text-fill: #bfbfbf; -fx-font-weight: bold;");
                seatsGrid.add(filaLabel, 0, rowIndex);

                int colIndex = 1;
                for (LugarModel lugar : entry.getValue()) {
                    Button seatButton = new Button(String.valueOf(lugar.numero()));
                    seatButton.setMinSize(42, 36);
                    seatButton.setStyle(
                            "-fx-background-color: " + zoneColor(selectedZona) + "; " +
                                    "-fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;"
                    );
                    seatsGrid.add(seatButton, colIndex++, rowIndex);
                }
                rowIndex++;
            }
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
        }
    }

    private VBox createZoneCard(ZonaModel zona) {
        Label swatch = new Label(" ");
        swatch.setMinSize(12, 12);
        swatch.setMaxSize(12, 12);
        swatch.setStyle(
                "-fx-background-color: " + zoneColor(zona) + "; " +
                        "-fx-background-radius: 4;"
        );

        Label nameLabel = new Label(zona.nome());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label feeLabel = new Label("Taxa: +" + zona.taxaAdicional());
        feeLabel.setStyle("-fx-text-fill: #b0b0b0; -fx-font-size: 12px;");

        Label countLabel = new Label(
                selectedZona != null && zona.id().equals(selectedZona.id())
                        ? currentLugares.size() + " lugares"
                        : "Selecionar para ver lugares"
        );
        countLabel.setStyle("-fx-text-fill: #8a8a8a; -fx-font-size: 12px;");

        VBox textBox = new VBox(6, nameLabel, feeLabel);
        HBox header = new HBox(10, swatch, textBox);

        VBox card = new VBox(10, header, countLabel);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color: " + (selectedZona != null && zona.id().equals(selectedZona.id()) ? "#335a8d" : "#242424") + "; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: " + (selectedZona != null && zona.id().equals(selectedZona.id()) ? "#67a9f4" : "#2f2f2f") + "; " +
                        "-fx-border-radius: 12;"
        );
        card.setOnMouseClicked(event -> {
            selectedZona = zona;
            carregarZonas();
            carregarLugares();
        });
        return card;
    }

    private String zoneColor(ZonaModel zona) {
        if (zona == null || zona.nome() == null) {
            return "#67a9f4";
        }

        Map<String, String> palette = new HashMap<>();
        palette.put("Orquestra", "#4a8cff");
        palette.put("Mezanino", "#8d5cf6");
        palette.put("Plateia", "#29c58c");
        palette.put("Balcao", "#ff9f43");

        return palette.getOrDefault(zona.nome(), "#67a9f4");
    }

    private HBox createLegendRow(String labelText, String color) {
        Label swatch = new Label(" ");
        swatch.setMinSize(18, 18);
        swatch.setMaxSize(18, 18);
        swatch.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white;");

        return new HBox(10, swatch, label);
    }

    private VBox createStatItem(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #9d9d9d; -fx-font-size: 12px;");
        return new VBox(4, titleLabel, valueLabel);
    }

    private VBox createMiniProgress(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #9d9d9d; -fx-font-size: 12px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Region fill = new Region();
        fill.setPrefHeight(8);
        fill.setPrefWidth(120);
        fill.setStyle("-fx-background-color: #67a9f4; -fx-background-radius: 8;");

        Region track = new Region();
        track.setPrefHeight(8);
        track.setStyle("-fx-background-color: #1f1f1f; -fx-background-radius: 8;");
        track.setMaxWidth(Double.MAX_VALUE);

        BorderPane progress = new BorderPane();
        progress.setCenter(track);
        progress.setLeft(fill);

        return new VBox(6, titleLabel, valueLabel, progress);
    }

    private VBox createPanel(Parent content) {
        VBox panel = new VBox(content);
        panel.setPadding(new Insets(18));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return panel;
    }

    private Label createPanelTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        return label;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );
        return button;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public Parent getRoot() {
        return root;
    }
}
