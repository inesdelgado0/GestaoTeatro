package com.teatro.desktop.view;

import com.teatro.desktop.model.SalaModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.SalaApiService;
import com.teatro.desktop.view.layout.AdminLayout;
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

import java.util.List;

public class RoomsView {

    private final Parent root;
    private final TableView<SalaModel> tableView;
    private final SalaApiService salaApiService;
    private final Label feedbackLabel;
    private final String userEmail;

    public RoomsView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.tableView = new TableView<>();
        this.salaApiService = new SalaApiService(authService);
        this.feedbackLabel = new Label();
        this.userEmail = userEmail;

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_ROOMS,
                "Gest\u00e3o de Salas",
                "Configura\u00e7\u00e3o da infraestrutura e capacidade das salas",
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
    }

    private VBox buildRoomForm() {
        Label title = new Label("Criar Sala");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome da sala");

        TextField capacidadeField = new TextField();
        capacidadeField.setPromptText("Capacidade total");

        feedbackLabel.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 12px;");

        Button createButton = new Button("Guardar sala");
        createButton.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        createButton.setOnAction(event -> {
            try {
                SalaModel novaSala = new SalaModel(
                        null,
                        nomeField.getText(),
                        Integer.parseInt(capacidadeField.getText())
                );

                salaApiService.criarSala(novaSala);
                feedbackLabel.setText("Sala criada com sucesso.");
                nomeField.clear();
                capacidadeField.clear();
                carregarSalas();
            } catch (NumberFormatException e) {
                feedbackLabel.setText("A capacidade tem de ser numérica.");
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.add(createFormLabel("Nome"), 0, 0);
        form.add(nomeField, 0, 1);
        form.add(createFormLabel("Capacidade total"), 0, 2);
        form.add(capacidadeField, 0, 3);

        VBox formCard = new VBox(14, title, form, createButton, feedbackLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return formCard;
    }

    private void carregarSalas() {
        try {
            List<SalaModel> salas = salaApiService.listarSalas();
            tableView.getItems().setAll(salas);
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
