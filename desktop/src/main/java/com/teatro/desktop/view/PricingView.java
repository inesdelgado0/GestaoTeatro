package com.teatro.desktop.view;

import com.teatro.desktop.model.TipoBilheteModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.TipoBilheteApiService;
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

import java.math.BigDecimal;
import java.util.List;

public class PricingView {

    private static final String ERROR_STYLE = "-fx-text-fill: #ff8a8a; -fx-font-size: 12px;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #7CFC98; -fx-font-size: 12px;";

    private final Parent root;
    private final TableView<TipoBilheteModel> tableView;
    private final TipoBilheteApiService tipoBilheteApiService;
    private final Label feedbackLabel;
    private final PauseTransition feedbackReset;
    private TipoBilheteModel selectedTipoBilhete;

    private TextField nomeField;
    private TextField descontoField;
    private Label formTitleLabel;
    private Button saveButton;
    private Button deleteButton;

    public PricingView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.tableView = new TableView<>();
        this.tipoBilheteApiService = new TipoBilheteApiService(authService);
        this.feedbackLabel = new Label();
        this.feedbackReset = new PauseTransition(Duration.seconds(4));
        this.selectedTipoBilhete = null;

        feedbackReset.setOnFinished(event -> clearFeedback());

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_PRICING,
                "Preços e Descontos",
                "Gestão de categorias de bilhete e percentagens de desconto",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarTiposBilhete();
    }

    private Parent buildContent() {
        setupTable();
        Label tableTitleLabel = new Label("Categorias de desconto");
        tableTitleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        VBox tableCard = new VBox(12, tableTitleLabel, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );

        VBox formCard = buildForm();
        formCard.setPrefWidth(400);

        HBox main = new HBox(20, tableCard, formCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        main.setPadding(new Insets(18, 28, 28, 28));
        return main;
    }

    private void setupTable() {
        TableColumn<TipoBilheteModel, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().id()));
        idColumn.setPrefWidth(70);

        TableColumn<TipoBilheteModel, String> nomeColumn = new TableColumn<>("Categoria");
        nomeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nome()));
        nomeColumn.setPrefWidth(220);

        TableColumn<TipoBilheteModel, String> descontoColumn = new TableColumn<>("Desconto");
        descontoColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().percentagemDesconto() != null ? data.getValue().percentagemDesconto() + "%" : ""
        ));
        descontoColumn.setPrefWidth(140);

        tableView.getColumns().setAll(idColumn, nomeColumn, descontoColumn);
        tableView.setPlaceholder(new Label("Sem dados disponíveis."));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> preencherFormulario(newValue));
    }

    private VBox buildForm() {
        formTitleLabel = new Label("Adicionar Categoria");
        formTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        nomeField = new TextField();
        nomeField.setPromptText("Nome da categoria");

        descontoField = new TextField();
        descontoField.setPromptText("Percentagem de desconto");

        clearFeedback();

        saveButton = new Button("Guardar categoria");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        deleteButton = new Button("Remover categoria");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setDisable(true);
        deleteButton.setStyle(
                "-fx-background-color: #383838; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        Button clearButton = new Button("Limpar seleção");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setStyle(
                "-fx-background-color: #1f1f1f; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        saveButton.setOnAction(event -> guardarCategoria());
        deleteButton.setOnAction(event -> eliminarCategoriaSelecionada());
        clearButton.setOnAction(event -> {
            limparFormulario();
            clearFeedback();
        });

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.add(createFormLabel("Nome"), 0, 0);
        form.add(nomeField, 0, 1);
        form.add(createFormLabel("Percentagem de desconto"), 0, 2);
        form.add(descontoField, 0, 3);

        HBox actions = new HBox(10, saveButton, deleteButton);
        actions.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        HBox.setHgrow(deleteButton, Priority.ALWAYS);
        VBox formCard = new VBox(14, formTitleLabel, form, actions, clearButton, feedbackLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return formCard;
    }

    private void guardarCategoria() {
        try {
            TipoBilheteModel tipoBilhete = new TipoBilheteModel(
                    selectedTipoBilhete != null ? selectedTipoBilhete.id() : null,
                    nomeField.getText(),
                    new BigDecimal(descontoField.getText())
            );

            String successMessage;
            if (selectedTipoBilhete == null) {
                tipoBilheteApiService.criarTipoBilhete(tipoBilhete);
                successMessage = "Categoria criada com sucesso.";
            } else {
                tipoBilheteApiService.atualizarTipoBilhete(tipoBilhete);
                successMessage = "Categoria atualizada com sucesso.";
            }
            limparFormulario();
            carregarTiposBilhete();
            showSuccess(successMessage);
        } catch (NumberFormatException e) {
            showError("A percentagem de desconto deve ser numérica.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void eliminarCategoriaSelecionada() {
        if (selectedTipoBilhete == null) {
            showError("Seleciona uma categoria para remover.");
            return;
        }

        try {
            tipoBilheteApiService.eliminarTipoBilhete(selectedTipoBilhete.id());
            limparFormulario();
            carregarTiposBilhete();
            showSuccess("Categoria removida com sucesso.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void carregarTiposBilhete() {
        try {
            List<TipoBilheteModel> tipos = tipoBilheteApiService.listarTiposBilhete();
            tableView.getItems().setAll(tipos);
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
    }

    private void preencherFormulario(TipoBilheteModel tipoBilhete) {
        selectedTipoBilhete = tipoBilhete;
        clearFeedback();

        if (tipoBilhete == null) {
            formTitleLabel.setText("Adicionar Categoria");
            saveButton.setText("Guardar categoria");
            deleteButton.setDisable(true);
            return;
        }

        formTitleLabel.setText("Editar Categoria");
        saveButton.setText("Atualizar categoria");
        deleteButton.setDisable(false);
        nomeField.setText(tipoBilhete.nome());
        descontoField.setText(tipoBilhete.percentagemDesconto() != null ? tipoBilhete.percentagemDesconto().toString() : "");
    }

    private void limparFormulario() {
        selectedTipoBilhete = null;
        tableView.getSelectionModel().clearSelection();
        nomeField.clear();
        descontoField.clear();
        formTitleLabel.setText("Adicionar Categoria");
        saveButton.setText("Guardar categoria");
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
