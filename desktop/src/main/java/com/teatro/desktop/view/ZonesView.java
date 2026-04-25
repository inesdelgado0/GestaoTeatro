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
    private final Label zoneFormTitleLabel;
    private final Label seatFormTitleLabel;
    private final TextField zoneNameField;
    private final TextField zoneFeeField;
    private final TextField seatRowField;
    private final TextField seatNumberField;
    private final Button saveZoneButton;
    private final Button deleteZoneButton;
    private final Button clearZoneButton;
    private final Button saveSeatButton;
    private final Button deleteSeatButton;
    private final Button clearSeatButton;

    private SalaModel selectedSala;
    private ZonaModel selectedZona;
    private LugarModel selectedLugar;
    private List<ZonaModel> currentZonas;
    private List<LugarModel> currentLugares;

    public ZonesView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.salaApiService = new SalaApiService(authService);
        this.zonaApiService = new ZonaApiService(authService);
        this.lugarApiService = new LugarApiService(authService);
        this.salaComboBox = new ComboBox<>();
        this.zonasListBox = new VBox(12);
        this.seatsGrid = new GridPane();
        this.seatsTitleLabel = new Label("Mapa de Lugares");
        this.feedbackLabel = new Label();
        this.totalSeatsLabel = new Label("0");
        this.selectedZoneLabel = new Label("Nenhuma");
        this.zoneFormTitleLabel = new Label("Adicionar Zona");
        this.seatFormTitleLabel = new Label("Adicionar Lugar");
        this.zoneNameField = new TextField();
        this.zoneFeeField = new TextField();
        this.seatRowField = new TextField();
        this.seatNumberField = new TextField();
        this.saveZoneButton = createPrimaryButton("Guardar zona");
        this.deleteZoneButton = createSecondaryButton("Remover zona");
        this.clearZoneButton = createSecondaryButton("Limpar zona");
        this.saveSeatButton = createPrimaryButton("Guardar lugar");
        this.deleteSeatButton = createSecondaryButton("Remover lugar");
        this.clearSeatButton = createSecondaryButton("Limpar lugar");
        this.currentZonas = new ArrayList<>();
        this.currentLugares = new ArrayList<>();
        this.selectedLugar = null;

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_ZONES,
                "Gest\u00e3o de zonas e lugares",
                "Configurar zonas de assentos e disposi\u00e7\u00e3o individual de lugares",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarSalas();
    }

    private Parent buildContent() {
        VBox centerPanel = buildCenterPanel();
        HBox main = new HBox(20, buildLeftColumn(), centerPanel, buildRightColumn());
        HBox.setHgrow(centerPanel, Priority.ALWAYS);
        main.setPadding(new Insets(14, 20, 20, 20));

        ScrollPane scrollPane = new ScrollPane(main);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
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

        VBox salaPanel = createPanel(new VBox(10, salaLabel, salaComboBox));
        salaPanel.setPrefWidth(210);

        Label zonasLabel = createPanelTitle("Zonas");
        ScrollPane zonasScroll = new ScrollPane(zonasListBox);
        zonasScroll.setFitToWidth(true);
        zonasScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox zonasPanel = createPanel(new VBox(10, zonasLabel, zonasScroll));
        zonasPanel.setPrefWidth(210);
        VBox.setVgrow(zonasScroll, Priority.ALWAYS);

        return new VBox(14, salaPanel, zonasPanel);
    }

    private VBox buildCenterPanel() {
        seatsTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button addSeatButton = new Button("+ Adicionar lugar");
        addSeatButton.setStyle(
                "-fx-background-color: #1f1f1f; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 12 8 12;"
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

        VBox panel = createPanel(new VBox(12, headerRow, palcoLabel, seatsGrid));
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

        VBox right = new VBox(14, legendPanel, statsPanel, zoneForm, seatForm, feedbackLabel);
        right.setPrefWidth(245);
        return right;
    }

    private VBox buildLegendPanel() {
        VBox content = new VBox(
                14,
                createPanelTitle("Legenda"),
                createLegendRow("Zona selecionada", zoneColor(selectedZona)),
                createLegendRow("Lugar visivel", "#6ea8ff")
        );
        return createPanel(content);
    }

    private VBox buildStatsPanel() {
        totalSeatsLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #6ea8ff;");
        selectedZoneLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox content = new VBox(
                12,
                createPanelTitle("Estatísticas"),
                createStatItem("Zona ativa", selectedZoneLabel),
                createStatItem("Total de lugares", totalSeatsLabel),
                createMiniProgress("Taxa de desenho", "100%")
        );
        return createPanel(content);
    }

    private VBox buildZoneForm() {
        zoneFormTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        zoneNameField.setPromptText("Nome da zona");
        zoneFeeField.setPromptText("Taxa adicional");

        saveZoneButton.setOnAction(event -> {
            if (selectedSala == null) {
                feedbackLabel.setText("Selecione primeiro uma sala.");
                return;
            }

            try {
                BigDecimal taxa = zoneFeeField.getText() == null || zoneFeeField.getText().isBlank()
                        ? BigDecimal.ZERO
                        : new BigDecimal(zoneFeeField.getText());

                ZonaModel zona = new ZonaModel(
                        selectedZona != null ? selectedZona.id() : null,
                        zoneNameField.getText(),
                        taxa,
                        selectedSala.id()
                );

                if (selectedZona == null) {
                    zonaApiService.criarZona(zona);
                    feedbackLabel.setText("Zona criada com sucesso.");
                } else {
                    zonaApiService.atualizarZona(zona);
                    feedbackLabel.setText("Zona atualizada com sucesso.");
                }

                limparZonaSelecionada();
                carregarZonas();
            } catch (NumberFormatException e) {
                feedbackLabel.setText("A taxa adicional deve ser numérica.");
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        deleteZoneButton.setDisable(true);
        deleteZoneButton.setOnAction(event -> {
            if (selectedZona == null) {
                feedbackLabel.setText("Selecione primeiro uma zona.");
                return;
            }

            try {
                zonaApiService.eliminarZona(selectedZona.id());
                feedbackLabel.setText("Zona removida com sucesso.");
                limparZonaSelecionada();
                carregarZonas();
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        clearZoneButton.setOnAction(event -> {
            limparZonaSelecionada();
            feedbackLabel.setText("");
        });

        HBox actions = new HBox(10, saveZoneButton, deleteZoneButton);
        VBox content = new VBox(
                12,
                zoneFormTitleLabel,
                createFormLabel("Nome"),
                zoneNameField,
                createFormLabel("Taxa adicional"),
                zoneFeeField,
                actions,
                clearZoneButton
        );
        return createPanel(content);
    }

    private VBox buildSeatForm() {
        seatFormTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        seatRowField.setPromptText("Fila");
        seatNumberField.setPromptText("Número");

        saveSeatButton.setOnAction(event -> {
            if (selectedZona == null) {
                feedbackLabel.setText("Selecione primeiro uma zona.");
                return;
            }

            try {
                LugarModel lugar = new LugarModel(
                        selectedLugar != null ? selectedLugar.id() : null,
                        seatRowField.getText(),
                        Integer.parseInt(seatNumberField.getText()),
                        selectedZona.id()
                );

                if (selectedLugar == null) {
                    lugarApiService.criarLugar(lugar);
                    feedbackLabel.setText("Lugar criado com sucesso.");
                } else {
                    lugarApiService.atualizarLugar(lugar);
                    feedbackLabel.setText("Lugar atualizado com sucesso.");
                }

                limparLugarSelecionado();
                carregarLugares();
            } catch (NumberFormatException e) {
                feedbackLabel.setText("O número do lugar deve ser numérico.");
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        deleteSeatButton.setDisable(true);
        deleteSeatButton.setOnAction(event -> {
            if (selectedLugar == null) {
                feedbackLabel.setText("Selecione primeiro um lugar.");
                return;
            }

            try {
                lugarApiService.eliminarLugar(selectedLugar.id());
                feedbackLabel.setText("Lugar removido com sucesso.");
                limparLugarSelecionado();
                carregarLugares();
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        clearSeatButton.setOnAction(event -> {
            limparLugarSelecionado();
            feedbackLabel.setText("");
        });

        HBox actions = new HBox(10, saveSeatButton, deleteSeatButton);
        VBox content = new VBox(
                12,
                seatFormTitleLabel,
                createFormLabel("Fila"),
                seatRowField,
                createFormLabel("Número"),
                seatNumberField,
                actions,
                clearSeatButton
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
        limparZonaSelecionada();
        limparLugarSelecionado();
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
            limparLugarSelecionado();
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
                filaLabel.setStyle("-fx-text-fill: #bfbfbf; -fx-font-weight: bold; -fx-font-size: 12px;");
                seatsGrid.add(filaLabel, 0, rowIndex);

                int colIndex = 1;
                for (LugarModel lugar : entry.getValue()) {
                    Button seatButton = new Button(String.valueOf(lugar.numero()));
                    seatButton.setMinSize(36, 30);
                    seatButton.setStyle(
                            "-fx-background-color: " + (selectedLugar != null && lugar.id().equals(selectedLugar.id()) ? "#ff9f43" : zoneColor(selectedZona)) + "; " +
                                    "-fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;"
                    );
                    seatButton.setOnAction(event -> selecionarLugar(lugar));
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

        VBox card = new VBox(8, header, countLabel);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: " + (selectedZona != null && zona.id().equals(selectedZona.id()) ? "#335a8d" : "#242424") + "; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: " + (selectedZona != null && zona.id().equals(selectedZona.id()) ? "#6ea8ff" : "#2f2f2f") + "; " +
                        "-fx-border-radius: 12;"
        );
        card.setOnMouseClicked(event -> {
            selectedZona = zona;
            preencherZonaForm(zona);
            limparLugarSelecionado();
            carregarZonas();
            carregarLugares();
        });
        return card;
    }

    private String zoneColor(ZonaModel zona) {
        if (zona == null || zona.nome() == null) {
            return "#6ea8ff";
        }

        Map<String, String> palette = new HashMap<>();
        palette.put("Orquestra", "#4e8fe8");
        palette.put("Mezanino", "#8d5cf6");
        palette.put("Plateia", "#29c58c");
        palette.put("Balcao", "#ff9f43");

        return palette.getOrDefault(zona.nome(), "#6ea8ff");
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
        fill.setPrefWidth(92);
        fill.setStyle("-fx-background-color: #6ea8ff; -fx-background-radius: 8;");

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
        panel.setPadding(new Insets(14));
        panel.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return panel;
    }

    private Label createPanelTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
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
                        "-fx-padding: 8 14 8 14;"
        );
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setStyle(
                "-fx-background-color: #1f1f1f; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 14 8 14;"
        );
        return button;
    }

    private void preencherZonaForm(ZonaModel zona) {
        selectedZona = zona;

        if (zona == null) {
            zoneFormTitleLabel.setText("Adicionar Zona");
            saveZoneButton.setText("Guardar zona");
            deleteZoneButton.setDisable(true);
            zoneNameField.clear();
            zoneFeeField.clear();
            return;
        }

        zoneFormTitleLabel.setText("Editar Zona");
        saveZoneButton.setText("Atualizar zona");
        deleteZoneButton.setDisable(false);
        zoneNameField.setText(zona.nome());
        zoneFeeField.setText(zona.taxaAdicional() != null ? zona.taxaAdicional().toString() : "");
    }

    private void limparZonaSelecionada() {
        preencherZonaForm(null);
        selectedZona = null;
    }

    private void selecionarLugar(LugarModel lugar) {
        selectedLugar = lugar;
        seatFormTitleLabel.setText("Editar Lugar");
        saveSeatButton.setText("Atualizar lugar");
        deleteSeatButton.setDisable(false);
        seatRowField.setText(lugar.fila());
        seatNumberField.setText(lugar.numero() != null ? String.valueOf(lugar.numero()) : "");
        carregarLugares();
    }

    private void limparLugarSelecionado() {
        selectedLugar = null;
        seatFormTitleLabel.setText("Adicionar Lugar");
        saveSeatButton.setText("Guardar lugar");
        deleteSeatButton.setDisable(true);
        seatRowField.clear();
        seatNumberField.clear();
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
