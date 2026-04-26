package com.teatro.desktop.view;

import com.teatro.desktop.model.SalaModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.SalaApiService;
import com.teatro.desktop.view.layout.AdminLayout;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class RoomsView {

    private static final String ERROR_STYLE = "-fx-text-fill: #ff8a8a; -fx-font-size: 12px;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #7CFC98; -fx-font-size: 12px;";

    private final Parent root;
    private final TableView<SalaModel> tableView;
    private final SalaApiService salaApiService;
    private final Label feedbackLabel;
    private final PauseTransition feedbackReset;

    private SalaModel selectedSala;
    private Label formTitleLabel;
    private TextField nomeField;
    private TextField capacidadeField;
    private Button saveButton;
    private Button deleteButton;

    public RoomsView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.tableView = new TableView<>();
        this.salaApiService = new SalaApiService(authService);
        this.feedbackLabel = new Label();
        this.feedbackReset = new PauseTransition(Duration.seconds(4));
        this.selectedSala = null;

        feedbackReset.setOnFinished(event -> clearFeedback());

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_ROOMS,
                "Gestão de Salas",
                "Configuração da infraestrutura e capacidade das salas",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarSalas();
    }

    private Parent buildContent() {
        setupTable();
        Label tableTitleLabel = new Label("Salas registadas");
        tableTitleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        VBox tableCard = new VBox(12, tableTitleLabel, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );

        VBox formCard = buildRoomForm();
        formCard.setPrefWidth(360);

        HBox main = new HBox(20, tableCard, formCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        main.setPadding(new Insets(18, 28, 28, 28));
        return main;
    }

    private void setupTable() {
        TableColumn<SalaModel, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().id()));
        idColumn.setPrefWidth(80);

        TableColumn<SalaModel, String> nomeColumn = new TableColumn<>("Nome");
        nomeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nome()));
        nomeColumn.setPrefWidth(260);

        TableColumn<SalaModel, Number> capacidadeColumn = new TableColumn<>("Capacidade");
        capacidadeColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().capacidadeTotal() != null ? data.getValue().capacidadeTotal() : 0
        ));
        capacidadeColumn.setPrefWidth(140);

        tableView.getColumns().setAll(idColumn, nomeColumn, capacidadeColumn);
        tableView.setPlaceholder(new Label("Sem dados disponíveis."));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> preencherFormulario(newValue));
    }

    private VBox buildRoomForm() {
        formTitleLabel = new Label("Criar Sala");
        formTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        nomeField = new TextField();
        nomeField.setPromptText("Nome da sala");

        capacidadeField = new TextField();
        capacidadeField.setPromptText("Capacidade total");

        clearFeedback();

        saveButton = new Button("Guardar sala");
        saveButton.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        deleteButton = new Button("Remover sala");
        deleteButton.setDisable(true);
        deleteButton.setStyle(
                "-fx-background-color: #383838; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        Button clearButton = new Button("Limpar seleção");
        clearButton.setStyle(
                "-fx-background-color: #1f1f1f; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        saveButton.setOnAction(event -> guardarSala());
        deleteButton.setOnAction(event -> eliminarSalaSelecionada());
        clearButton.setOnAction(event -> {
            limparFormulario();
            clearFeedback();
        });

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.add(createFormLabel("Nome"), 0, 0);
        form.add(nomeField, 0, 1);
        form.add(createFormLabel("Capacidade total"), 0, 2);
        form.add(capacidadeField, 0, 3);

        HBox actions = new HBox(10, saveButton, deleteButton);
        VBox formCard = new VBox(14, formTitleLabel, form, actions, clearButton, feedbackLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return formCard;
    }

    private void guardarSala() {
        try {
            SalaModel sala = new SalaModel(
                    selectedSala != null ? selectedSala.id() : null,
                    nomeField.getText(),
                    Integer.parseInt(capacidadeField.getText())
            );

            if (selectedSala == null) {
                salaApiService.criarSala(sala);
                showSuccess("Sala criada com sucesso.");
            } else {
                salaApiService.atualizarSala(sala);
                showSuccess("Sala atualizada com sucesso.");
            }

            limparFormulario();
            carregarSalas();
        } catch (NumberFormatException e) {
            showError("A capacidade tem de ser numérica.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void eliminarSalaSelecionada() {
        if (selectedSala == null) {
            showError("Selecione uma sala para remover.");
            return;
        }

        try {
            salaApiService.eliminarSala(selectedSala.id());
            showSuccess("Sala removida com sucesso.");
            limparFormulario();
            carregarSalas();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void carregarSalas() {
        try {
            List<SalaModel> salas = salaApiService.listarSalas();
            tableView.getItems().setAll(salas);
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void preencherFormulario(SalaModel sala) {
        selectedSala = sala;
        clearFeedback();

        if (sala == null) {
            formTitleLabel.setText("Criar Sala");
            saveButton.setText("Guardar sala");
            deleteButton.setDisable(true);
            nomeField.clear();
            capacidadeField.clear();
            return;
        }

        formTitleLabel.setText("Editar Sala");
        saveButton.setText("Atualizar sala");
        deleteButton.setDisable(false);
        nomeField.setText(sala.nome());
        capacidadeField.setText(sala.capacidadeTotal() != null ? String.valueOf(sala.capacidadeTotal()) : "");
    }

    private void limparFormulario() {
        selectedSala = null;
        tableView.getSelectionModel().clearSelection();
        formTitleLabel.setText("Criar Sala");
        saveButton.setText("Guardar sala");
        deleteButton.setDisable(true);
        nomeField.clear();
        capacidadeField.clear();
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
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
