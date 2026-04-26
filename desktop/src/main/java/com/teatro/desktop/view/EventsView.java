package com.teatro.desktop.view;

import com.teatro.desktop.model.EventoModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.EventoApiService;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class EventsView {

    private static final String ERROR_STYLE = "-fx-text-fill: #ff8a8a; -fx-font-size: 12px;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #7CFC98; -fx-font-size: 12px;";

    private final Parent root;
    private final TableView<EventoModel> tableView;
    private final EventoApiService eventoApiService;
    private final Label feedbackLabel;
    private final PauseTransition feedbackReset;
    private EventoModel selectedEvento;

    private TextField tituloField;
    private TextField generoField;
    private TextField duracaoField;
    private TextField classificacaoField;
    private TextArea descricaoArea;
    private Label formTitleLabel;
    private Button saveButton;
    private Button deleteButton;

    public EventsView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.tableView = new TableView<>();
        this.eventoApiService = new EventoApiService(authService);
        this.feedbackLabel = new Label();
        this.feedbackReset = new PauseTransition(Duration.seconds(4));
        this.selectedEvento = null;

        feedbackReset.setOnFinished(event -> clearFeedback());

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_EVENTS,
                "Gestão de Eventos",
                "Criação e consulta de eventos disponíveis no catálogo",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarEventos();
    }

    private Parent buildContent() {
        setupTable();
        Label tableTitleLabel = new Label("Catálogo de eventos");
        tableTitleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        VBox tableCard = new VBox(12, tableTitleLabel, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );

        VBox formCard = buildEventForm();
        formCard.setPrefWidth(360);

        HBox main = new HBox(20, tableCard, formCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        main.setPadding(new Insets(18, 28, 28, 28));
        return main;
    }

    private void setupTable() {
        TableColumn<EventoModel, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().id()));
        idColumn.setPrefWidth(60);

        TableColumn<EventoModel, String> tituloColumn = new TableColumn<>("Título");
        tituloColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().titulo()));
        tituloColumn.setPrefWidth(220);

        TableColumn<EventoModel, String> generoColumn = new TableColumn<>("Género");
        generoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().genero()));
        generoColumn.setPrefWidth(140);

        TableColumn<EventoModel, Number> duracaoColumn = new TableColumn<>("Duração");
        duracaoColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().duracaoMin() != null ? data.getValue().duracaoMin() : 0
        ));
        duracaoColumn.setPrefWidth(100);

        TableColumn<EventoModel, String> classificacaoColumn = new TableColumn<>("Classificação");
        classificacaoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().classificacaoEtaria()));
        classificacaoColumn.setPrefWidth(140);

        tableView.getColumns().setAll(idColumn, tituloColumn, generoColumn, duracaoColumn, classificacaoColumn);
        tableView.setPlaceholder(new Label("Sem dados disponíveis."));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> preencherFormulario(newValue));
    }

    private VBox buildEventForm() {
        formTitleLabel = new Label("Criar Evento");
        formTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        tituloField = new TextField();
        tituloField.setPromptText("Título");

        generoField = new TextField();
        generoField.setPromptText("Género");

        duracaoField = new TextField();
        duracaoField.setPromptText("Duração (min)");

        classificacaoField = new TextField();
        classificacaoField.setPromptText("Classificação etária");

        descricaoArea = new TextArea();
        descricaoArea.setPromptText("Descrição");
        descricaoArea.setPrefRowCount(5);

        clearFeedback();

        saveButton = new Button("Guardar evento");
        saveButton.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        deleteButton = new Button("Remover evento");
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

        saveButton.setOnAction(event -> guardarEvento());
        deleteButton.setOnAction(event -> eliminarEventoSelecionado());
        clearButton.setOnAction(event -> {
            limparFormulario();
            clearFeedback();
        });

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.add(createFormLabel("Título"), 0, 0);
        form.add(tituloField, 0, 1);
        form.add(createFormLabel("Género"), 0, 2);
        form.add(generoField, 0, 3);
        form.add(createFormLabel("Duração"), 0, 4);
        form.add(duracaoField, 0, 5);
        form.add(createFormLabel("Classificação"), 0, 6);
        form.add(classificacaoField, 0, 7);
        form.add(createFormLabel("Descrição"), 0, 8);
        form.add(descricaoArea, 0, 9);

        HBox actions = new HBox(10, saveButton, deleteButton);
        VBox formCard = new VBox(14, formTitleLabel, form, actions, clearButton, feedbackLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return formCard;
    }

    private void guardarEvento() {
        try {
            Integer duracao = duracaoField.getText() == null || duracaoField.getText().isBlank()
                    ? null
                    : Integer.parseInt(duracaoField.getText());

            EventoModel eventoFormulario = new EventoModel(
                    selectedEvento != null ? selectedEvento.id() : null,
                    tituloField.getText(),
                    descricaoArea.getText(),
                    duracao,
                    classificacaoField.getText(),
                    generoField.getText()
            );

            if (selectedEvento == null) {
                eventoApiService.criarEvento(eventoFormulario);
                showSuccess("Evento criado com sucesso.");
            } else {
                eventoApiService.atualizarEvento(eventoFormulario);
                showSuccess("Evento atualizado com sucesso.");
            }

            limparFormulario();
            carregarEventos();
        } catch (NumberFormatException e) {
            showError("A duração tem de ser numérica.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void eliminarEventoSelecionado() {
        if (selectedEvento == null) {
            showError("Selecione um evento para remover.");
            return;
        }

        try {
            eventoApiService.eliminarEvento(selectedEvento.id());
            showSuccess("Evento removido com sucesso.");
            limparFormulario();
            carregarEventos();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void carregarEventos() {
        try {
            List<EventoModel> eventos = eventoApiService.listarEventos();
            tableView.getItems().setAll(eventos);
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
    }

    private void preencherFormulario(EventoModel evento) {
        selectedEvento = evento;
        clearFeedback();

        if (evento == null) {
            formTitleLabel.setText("Criar Evento");
            saveButton.setText("Guardar evento");
            deleteButton.setDisable(true);
            return;
        }

        formTitleLabel.setText("Editar Evento");
        saveButton.setText("Atualizar evento");
        deleteButton.setDisable(false);
        tituloField.setText(evento.titulo());
        generoField.setText(evento.genero());
        duracaoField.setText(evento.duracaoMin() != null ? String.valueOf(evento.duracaoMin()) : "");
        classificacaoField.setText(evento.classificacaoEtaria());
        descricaoArea.setText(evento.descricao());
    }

    private void limparFormulario() {
        selectedEvento = null;
        tableView.getSelectionModel().clearSelection();
        tituloField.clear();
        generoField.clear();
        duracaoField.clear();
        classificacaoField.clear();
        descricaoArea.clear();
        formTitleLabel.setText("Criar Evento");
        saveButton.setText("Guardar evento");
        deleteButton.setDisable(true);
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
