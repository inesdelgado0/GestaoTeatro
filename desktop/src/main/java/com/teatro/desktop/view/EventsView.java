package com.teatro.desktop.view;

import com.teatro.desktop.model.EventoModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.EventoApiService;
import com.teatro.desktop.view.layout.AdminLayout;
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

import java.util.List;

public class EventsView {

    private final Parent root;
    private final TableView<EventoModel> tableView;
    private final EventoApiService eventoApiService;
    private final Label feedbackLabel;
    private final String userEmail;

    public EventsView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.tableView = new TableView<>();
        this.eventoApiService = new EventoApiService();
        this.feedbackLabel = new Label();
        this.userEmail = userEmail;

        AdminLayout layout = new AdminLayout(
                sceneManager,
                userEmail,
                AdminLayout.SECTION_EVENTS,
                "Gestao de Eventos",
                "Criacao e consulta de eventos disponiveis no catalogo",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarEventos();
    }

    private Parent buildContent() {
        setupTable();
        VBox tableCard = new VBox(12, new Label("Catalogo de eventos"), tableView);
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

        TableColumn<EventoModel, String> tituloColumn = new TableColumn<>("Titulo");
        tituloColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().titulo()));
        tituloColumn.setPrefWidth(220);

        TableColumn<EventoModel, String> generoColumn = new TableColumn<>("Genero");
        generoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().genero()));
        generoColumn.setPrefWidth(140);

        TableColumn<EventoModel, Number> duracaoColumn = new TableColumn<>("Duracao");
        duracaoColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().duracaoMin() != null ? data.getValue().duracaoMin() : 0));
        duracaoColumn.setPrefWidth(100);

        TableColumn<EventoModel, String> classificacaoColumn = new TableColumn<>("Classificacao");
        classificacaoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().classificacaoEtaria()));
        classificacaoColumn.setPrefWidth(140);

        tableView.getColumns().setAll(idColumn, tituloColumn, generoColumn, duracaoColumn, classificacaoColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private VBox buildEventForm() {
        Label title = new Label("Criar Evento");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField tituloField = new TextField();
        tituloField.setPromptText("Titulo");

        TextField generoField = new TextField();
        generoField.setPromptText("Genero");

        TextField duracaoField = new TextField();
        duracaoField.setPromptText("Duracao (min)");

        TextField classificacaoField = new TextField();
        classificacaoField.setPromptText("Classificacao etaria");

        TextArea descricaoArea = new TextArea();
        descricaoArea.setPromptText("Descricao");
        descricaoArea.setPrefRowCount(5);

        feedbackLabel.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 12px;");

        Button createButton = new Button("Guardar evento");
        createButton.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        createButton.setOnAction(event -> {
            try {
                Integer duracao = duracaoField.getText() == null || duracaoField.getText().isBlank()
                        ? null
                        : Integer.parseInt(duracaoField.getText());

                EventoModel novoEvento = new EventoModel(
                        null,
                        tituloField.getText(),
                        descricaoArea.getText(),
                        duracao,
                        classificacaoField.getText(),
                        generoField.getText()
                );

                eventoApiService.criarEvento(novoEvento);
                feedbackLabel.setText("Evento criado com sucesso.");
                tituloField.clear();
                generoField.clear();
                duracaoField.clear();
                classificacaoField.clear();
                descricaoArea.clear();
                carregarEventos();
            } catch (NumberFormatException e) {
                feedbackLabel.setText("A duracao tem de ser numerica.");
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.add(createFormLabel("Titulo"), 0, 0);
        form.add(tituloField, 0, 1);
        form.add(createFormLabel("Genero"), 0, 2);
        form.add(generoField, 0, 3);
        form.add(createFormLabel("Duracao"), 0, 4);
        form.add(duracaoField, 0, 5);
        form.add(createFormLabel("Classificacao"), 0, 6);
        form.add(classificacaoField, 0, 7);
        form.add(createFormLabel("Descricao"), 0, 8);
        form.add(descricaoArea, 0, 9);

        VBox formCard = new VBox(14, title, form, createButton, feedbackLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return formCard;
    }

    private void carregarEventos() {
        try {
            List<EventoModel> eventos = eventoApiService.listarEventos();
            tableView.getItems().setAll(eventos);
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
        }
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
    }

    public Parent getRoot() {
        return root;
    }
}
