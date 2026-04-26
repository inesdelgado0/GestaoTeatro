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
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ZonesView {

    private static final String ERROR_STYLE = "-fx-text-fill: #ff8a8a; -fx-font-size: 12px;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #7CFC98; -fx-font-size: 12px;";

    private final Parent root;
    private final SalaApiService salaApiService;
    private final ZonaApiService zonaApiService;
    private final LugarApiService lugarApiService;

    private final ComboBox<SalaModel> salaComboBox;
    private final VBox zonasListBox;
    private final GridPane seatsGrid;
    private final Label seatsTitleLabel;
    private final Label feedbackLabel;
    private final PauseTransition feedbackReset;
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
    private final Button saveSeatButton;
    private final Button deleteSeatButton;

    private SalaModel selectedSala;
    private ZonaModel selectedZona;
    private LugarModel selectedLugar;
    private List<ZonaModel> currentZonas;
    private List<LugarModel> currentLugares;
    private Map<Integer, List<LugarModel>> lugaresPorZona;

    public ZonesView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.salaApiService = new SalaApiService(authService);
        this.zonaApiService = new ZonaApiService(authService);
        this.lugarApiService = new LugarApiService(authService);
        this.salaComboBox = new ComboBox<>();
        this.zonasListBox = new VBox(12);
        this.seatsGrid = new GridPane();
        this.seatsTitleLabel = new Label("Mapa de Lugares");
        this.feedbackLabel = new Label();
        this.feedbackReset = new PauseTransition(Duration.seconds(4));
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
        this.saveSeatButton = createPrimaryButton("Guardar lugar");
        this.deleteSeatButton = createSecondaryButton("Remover lugar");
        this.currentZonas = new ArrayList<>();
        this.currentLugares = new ArrayList<>();
        this.lugaresPorZona = new LinkedHashMap<>();

        feedbackReset.setOnFinished(event -> clearFeedback());

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_ZONES,
                "Gestão de zonas e lugares",
                "Configurar zonas primeiro e depois criar lugares por zona",
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
        Label salaLabel = createPanelTitle("1. Escolher Sala");
        salaComboBox.setPromptText("Escolher sala");
        salaComboBox.setMaxWidth(Double.MAX_VALUE);
        salaComboBox.setOnAction(event -> {
            selectedSala = salaComboBox.getValue();
            selectedZona = null;
            selectedLugar = null;
            clearFeedback();
            carregarZonas();
        });

        VBox salaPanel = createPanel(new VBox(10, salaLabel, salaComboBox));
        salaPanel.setPrefWidth(220);

        Label zonasLabel = createPanelTitle("2. Selecionar Zona");
        Label helpLabel = new Label("Clica numa zona da lista para criares ou editares lugares.");
        helpLabel.setWrapText(true);
        helpLabel.setStyle("-fx-text-fill: #9d9d9d; -fx-font-size: 12px;");

        ScrollPane zonasScroll = new ScrollPane(zonasListBox);
        zonasScroll.setFitToWidth(true);
        zonasScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox zonasPanel = createPanel(new VBox(10, zonasLabel, helpLabel, zonasScroll));
        zonasPanel.setPrefWidth(220);
        VBox.setVgrow(zonasScroll, Priority.ALWAYS);

        return new VBox(14, salaPanel, zonasPanel);
    }

    private VBox buildCenterPanel() {
        seatsTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label palcoLabel = new Label("PALCO");
        palcoLabel.setStyle(
                "-fx-background-color: #1f1f1f; -fx-text-fill: white; -fx-font-size: 20px; " +
                        "-fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10 0 10 0;"
        );
        palcoLabel.setMaxWidth(Double.MAX_VALUE);

        Label helper = new Label("O mapa mostra todas as zonas da sala. A zona selecionada continua a ser a ativa para criar ou editar lugares.");
        helper.setWrapText(true);
        helper.setStyle("-fx-text-fill: #9d9d9d; -fx-font-size: 12px;");

        seatsGrid.setHgap(8);
        seatsGrid.setVgap(8);
        seatsGrid.setPadding(new Insets(12));

        VBox panel = createPanel(new VBox(12, seatsTitleLabel, helper, palcoLabel, seatsGrid));
        VBox.setVgrow(seatsGrid, Priority.ALWAYS);
        HBox.setHgrow(panel, Priority.ALWAYS);
        return panel;
    }

    private VBox buildRightColumn() {
        VBox zoneForm = buildZoneForm();
        VBox seatForm = buildSeatForm();
        VBox statsPanel = buildStatsPanel();

        clearFeedback();

        VBox right = new VBox(14, feedbackLabel, zoneForm, seatForm, statsPanel);
        right.setPrefWidth(290);
        return right;
    }

    private VBox buildStatsPanel() {
        totalSeatsLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #6ea8ff;");
        selectedZoneLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox content = new VBox(
                12,
                createPanelTitle("Resumo"),
                createStatItem("Zona ativa", selectedZoneLabel),
                createStatItem("Total de lugares", totalSeatsLabel)
        );
        return createPanel(content);
    }

    private VBox buildZoneForm() {
        zoneFormTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        zoneNameField.setPromptText("Nome da zona");
        zoneFeeField.setPromptText("Taxa adicional");

        saveZoneButton.setOnAction(event -> guardarZona());

        deleteZoneButton.setDisable(true);
        deleteZoneButton.setOnAction(event -> eliminarZonaSelecionada());

        Button clearZoneButton = createSecondaryButton("Limpar zona");
        clearZoneButton.setOnAction(event -> {
            limparZonaSelecionada();
            clearFeedback();
        });

        HBox actions = new HBox(10, saveZoneButton, deleteZoneButton);
        VBox content = new VBox(
                12,
                zoneFormTitleLabel,
                createFormLabel("3. Criar ou editar zona"),
                zoneNameField,
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

        saveSeatButton.setOnAction(event -> guardarLugar());

        deleteSeatButton.setDisable(true);
        deleteSeatButton.setOnAction(event -> eliminarLugarSelecionado());

        Button clearSeatButton = createSecondaryButton("Limpar lugar");
        clearSeatButton.setOnAction(event -> {
            limparLugarSelecionado();
            clearFeedback();
        });

        Label helper = new Label("Só podes criar lugares depois de selecionar uma zona na lista da esquerda.");
        helper.setWrapText(true);
        helper.setStyle("-fx-text-fill: #9d9d9d; -fx-font-size: 12px;");

        HBox actions = new HBox(10, saveSeatButton, deleteSeatButton);
        VBox content = new VBox(
                12,
                seatFormTitleLabel,
                helper,
                createFormLabel("Fila"),
                seatRowField,
                createFormLabel("Número"),
                seatNumberField,
                actions,
                clearSeatButton
        );
        return createPanel(content);
    }

    private void guardarZona() {
        if (selectedSala == null) {
            showError("Seleciona primeiro uma sala.");
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
                ZonaModel criada = zonaApiService.criarZona(zona);
                showSuccess("Zona criada com sucesso.");
                carregarZonas();
                selecionarZonaPorId(criada.id());
            } else {
                zonaApiService.atualizarZona(zona);
                showSuccess("Zona atualizada com sucesso.");
                carregarZonas();
                selecionarZonaPorId(zona.id());
            }
        } catch (NumberFormatException e) {
            showError("A taxa adicional deve ser numérica.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void eliminarZonaSelecionada() {
        if (selectedZona == null) {
            showError("Seleciona primeiro uma zona na lista da esquerda.");
            return;
        }

        try {
            Integer zonaId = selectedZona.id();
            zonaApiService.eliminarZona(zonaId);
            showSuccess("Zona removida com sucesso.");
            selectedZona = null;
            selectedLugar = null;
            carregarZonas();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void guardarLugar() {
        if (selectedZona == null) {
            showError("Seleciona primeiro uma zona na lista da esquerda.");
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
                showSuccess("Lugar criado com sucesso.");
            } else {
                lugarApiService.atualizarLugar(lugar);
                showSuccess("Lugar atualizado com sucesso.");
            }

            limparLugarSelecionado();
            carregarZonas();
        } catch (NumberFormatException e) {
            showError("O número do lugar deve ser numérico.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void eliminarLugarSelecionado() {
        if (selectedLugar == null) {
            showError("Seleciona primeiro um lugar no mapa.");
            return;
        }

        try {
            lugarApiService.eliminarLugar(selectedLugar.id());
            showSuccess("Lugar removido com sucesso.");
            limparLugarSelecionado();
            carregarZonas();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
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
            showError(e.getMessage());
        }
    }

    private void carregarZonas() {
        zonasListBox.getChildren().clear();
        seatsGrid.getChildren().clear();
        seatsTitleLabel.setText("Mapa de Lugares");
        totalSeatsLabel.setText("0");
        currentLugares = new ArrayList<>();
        lugaresPorZona = new LinkedHashMap<>();

        if (selectedSala == null) {
            currentZonas = new ArrayList<>();
            selectedZoneLabel.setText("Nenhuma");
            preencherZonaForm(null);
            limparLugarSelecionado();
            return;
        }

        try {
            currentZonas = zonaApiService.listarPorSala(selectedSala.id());
            carregarMapaDaSala();
            renderZonasList();

            if (selectedZona == null) {
                selectedZoneLabel.setText("Nenhuma");
                preencherZonaForm(null);
                limparLugarSelecionado();
            } else {
                currentZonas.stream()
                        .filter(zona -> selectedZona.id().equals(zona.id()))
                        .findFirst()
                        .ifPresentOrElse(
                                zona -> {
                                    selectedZona = zona;
                                    currentLugares = new ArrayList<>(lugaresPorZona.getOrDefault(zona.id(), List.of()));
                                    preencherZonaForm(zona);
                                },
                                () -> {
                                    selectedZona = null;
                                    selectedLugar = null;
                                    currentLugares = new ArrayList<>();
                                    preencherZonaForm(null);
                                    limparLugarSelecionado();
                                    selectedZoneLabel.setText("Nenhuma");
                                }
                        );
            }
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void carregarMapaDaSala() {
        lugaresPorZona = new LinkedHashMap<>();
        for (ZonaModel zona : currentZonas) {
            List<LugarModel> lugares = new ArrayList<>(lugarApiService.listarPorZona(zona.id()));
            lugares.sort(Comparator
                    .comparing(LugarModel::fila, Comparator.nullsLast(String::compareTo))
                    .thenComparing(LugarModel::numero, Comparator.nullsLast(Integer::compareTo)));
            lugaresPorZona.put(zona.id(), lugares);
        }
        renderMapaDaSala();
    }

    private void renderZonasList() {
        zonasListBox.getChildren().clear();
        for (ZonaModel zona : currentZonas) {
            zonasListBox.getChildren().add(createZoneCard(zona));
        }
    }

    private void renderMapaDaSala() {
        seatsGrid.getChildren().clear();

        if (selectedSala == null) {
            seatsTitleLabel.setText("Mapa de Lugares");
            totalSeatsLabel.setText("0");
            return;
        }

        seatsTitleLabel.setText("Mapa da Sala - " + selectedSala.nome());
        selectedZoneLabel.setText(selectedZona != null ? selectedZona.nome() : "Nenhuma");

        int totalLugares = lugaresPorZona.values().stream()
                .mapToInt(List::size)
                .sum();
        totalSeatsLabel.setText(String.valueOf(totalLugares));

        int rowIndex = 0;
        for (ZonaModel zona : currentZonas) {
            boolean zonaAtiva = selectedZona != null && zona.id().equals(selectedZona.id());
            Label zoneLabel = new Label(zona.nome() + (zonaAtiva ? " (ativa)" : ""));
            zoneLabel.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                            "-fx-background-color: " + (zonaAtiva ? "#335a8d" : "#1f1f1f") + "; " +
                            "-fx-background-radius: 10; -fx-padding: 6 10 6 10;"
            );
            seatsGrid.add(zoneLabel, 0, rowIndex, 12, 1);
            rowIndex++;

            List<LugarModel> lugaresDaZona = new ArrayList<>(lugaresPorZona.getOrDefault(zona.id(), List.of()));
            if (zonaAtiva) {
                currentLugares = lugaresDaZona;
            }

            if (lugaresDaZona.isEmpty()) {
                Label emptyLabel = new Label("Sem lugares nesta zona.");
                emptyLabel.setStyle("-fx-text-fill: #8a8a8a; -fx-font-size: 12px;");
                seatsGrid.add(emptyLabel, 0, rowIndex, 12, 1);
                rowIndex += 2;
                continue;
            }

            Map<String, List<LugarModel>> porFila = new LinkedHashMap<>();
            for (LugarModel lugar : lugaresDaZona) {
                porFila.computeIfAbsent(lugar.fila(), key -> new ArrayList<>()).add(lugar);
            }

            for (Map.Entry<String, List<LugarModel>> entry : porFila.entrySet()) {
                Label filaLabel = new Label(entry.getKey());
                filaLabel.setStyle("-fx-text-fill: #bfbfbf; -fx-font-weight: bold; -fx-font-size: 12px;");
                seatsGrid.add(filaLabel, 0, rowIndex);

                int colIndex = 1;
                for (LugarModel lugar : entry.getValue()) {
                    Button seatButton = new Button(String.valueOf(lugar.numero()));
                    seatButton.setMinSize(36, 30);
                    seatButton.setStyle(
                            "-fx-background-color: " + (selectedLugar != null && lugar.id().equals(selectedLugar.id()) ? "#ff9f43" : zoneColor(zona)) + "; " +
                                    "-fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;"
                    );
                    seatButton.setOnAction(event -> selecionarLugarNaZona(zona, lugar));
                    seatsGrid.add(seatButton, colIndex++, rowIndex);
                }
                rowIndex++;
            }

            rowIndex++;
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

        boolean isSelected = selectedZona != null && zona.id().equals(selectedZona.id());
        int totalLugares = lugaresPorZona.getOrDefault(zona.id(), List.of()).size();
        Label countLabel = new Label(totalLugares + (totalLugares == 1 ? " lugar" : " lugares"));
        countLabel.setStyle("-fx-text-fill: #8a8a8a; -fx-font-size: 12px;");

        VBox textBox = new VBox(6, nameLabel, feeLabel);
        HBox header = new HBox(10, swatch, textBox);

        VBox card = new VBox(8, header, countLabel);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: " + (isSelected ? "#335a8d" : "#242424") + "; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: " + (isSelected ? "#6ea8ff" : "#2f2f2f") + "; " +
                        "-fx-border-radius: 12;"
        );
        card.setOnMouseClicked(event -> selecionarZona(zona));
        return card;
    }

    private String zoneColor(ZonaModel zona) {
        if (zona == null || zona.nome() == null) {
            return "#6ea8ff";
        }

        return switch (zona.nome().trim().toLowerCase()) {
            case "orquestra" -> "#4e8fe8";
            case "mezanino" -> "#8d5cf6";
            case "plateia" -> "#29c58c";
            case "balcão", "balcao" -> "#ff9f43";
            case "vip" -> "#d97706";
            default -> "#6ea8ff";
        };
    }

    private VBox createStatItem(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #9d9d9d; -fx-font-size: 12px;");
        return new VBox(4, titleLabel, valueLabel);
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

    private void selecionarZona(ZonaModel zona) {
        selectedZona = zona;
        selectedLugar = null;
        preencherZonaForm(zona);
        limparLugarSelecionado();
        currentLugares = new ArrayList<>(lugaresPorZona.getOrDefault(zona.id(), List.of()));
        renderZonasList();
        renderMapaDaSala();
    }

    private void selecionarZonaPorId(Integer zonaId) {
        if (zonaId == null) {
            return;
        }

        currentZonas.stream()
                .filter(zona -> zonaId.equals(zona.id()))
                .findFirst()
                .ifPresent(this::selecionarZona);
    }

    private void preencherZonaForm(ZonaModel zona) {
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
        selectedZona = null;
        preencherZonaForm(null);
    }

    private void selecionarLugar(LugarModel lugar) {
        selectedLugar = lugar;
        seatFormTitleLabel.setText("Editar Lugar");
        saveSeatButton.setText("Atualizar lugar");
        deleteSeatButton.setDisable(false);
        seatRowField.setText(lugar.fila());
        seatNumberField.setText(lugar.numero() != null ? String.valueOf(lugar.numero()) : "");
        renderMapaDaSala();
    }

    private void selecionarLugarNaZona(ZonaModel zona, LugarModel lugar) {
        if (selectedZona == null || !selectedZona.id().equals(zona.id())) {
            selectedZona = zona;
            preencherZonaForm(zona);
            currentLugares = new ArrayList<>(lugaresPorZona.getOrDefault(zona.id(), List.of()));
            renderZonasList();
        }
        selecionarLugar(lugar);
    }

    private void limparLugarSelecionado() {
        selectedLugar = null;
        seatFormTitleLabel.setText("Adicionar Lugar");
        saveSeatButton.setText("Guardar lugar");
        deleteSeatButton.setDisable(true);
        seatRowField.clear();
        seatNumberField.clear();
    }

    private void showSuccess(String message) {
        feedbackReset.stop();
        feedbackLabel.setStyle(SUCCESS_STYLE);
        feedbackLabel.setText(message);
        feedbackReset.playFromStart();
    }

    private void showError(String message) {
        feedbackReset.stop();
        feedbackLabel.setStyle(ERROR_STYLE);
        feedbackLabel.setText(message != null ? message : "Ocorreu um erro.");
    }

    private void clearFeedback() {
        feedbackReset.stop();
        feedbackLabel.setStyle(ERROR_STYLE);
        feedbackLabel.setText("");
    }

    public Parent getRoot() {
        return root;
    }
}
