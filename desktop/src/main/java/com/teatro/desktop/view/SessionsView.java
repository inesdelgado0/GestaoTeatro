package com.teatro.desktop.view;

import com.teatro.desktop.model.EventoModel;
import com.teatro.desktop.model.SalaModel;
import com.teatro.desktop.model.SessaoModel;
import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.service.EventoApiService;
import com.teatro.desktop.service.SalaApiService;
import com.teatro.desktop.service.SessaoApiService;
import com.teatro.desktop.view.layout.AdminLayout;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SessionsView {

    private static final String ERROR_STYLE = "-fx-text-fill: #ff8a8a; -fx-font-size: 12px;";
    private static final String SUCCESS_STYLE = "-fx-text-fill: #7CFC98; -fx-font-size: 12px;";
    private static final DateTimeFormatter TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Parent root;
    private final TableView<SessaoModel> tableView;
    private final SessaoApiService sessaoApiService;
    private final EventoApiService eventoApiService;
    private final SalaApiService salaApiService;
    private final Label feedbackLabel;
    private final PauseTransition feedbackReset;
    private SessaoModel selectedSessao;

    private ComboBox<EventoModel> eventoComboBox;
    private ComboBox<SalaModel> salaComboBox;
    private DatePicker dataPicker;
    private TextField horaField;
    private TextField precoBaseField;
    private ComboBox<String> estadoComboBox;
    private Label formTitleLabel;
    private Button saveButton;
    private Button deleteButton;

    public SessionsView(SceneManager sceneManager, AuthService authService, String userEmail) {
        this.tableView = new TableView<>();
        this.sessaoApiService = new SessaoApiService(authService);
        this.eventoApiService = new EventoApiService(authService);
        this.salaApiService = new SalaApiService(authService);
        this.feedbackLabel = new Label();
        this.feedbackReset = new PauseTransition(Duration.seconds(4));
        this.selectedSessao = null;

        feedbackReset.setOnFinished(event -> clearFeedback());

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_SESSIONS,
                "Gestão de Sessões",
                "Agenda de sessões com evento, sala, horário e preço base",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarReferencias();
        carregarSessoes();
    }

    private Parent buildContent() {
        setupTable();
        Label tableTitleLabel = new Label("Sessões agendadas");
        tableTitleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        VBox tableCard = new VBox(12, tableTitleLabel, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );

        VBox formCard = buildSessionForm();
        formCard.setPrefWidth(460);

        HBox main = new HBox(20, tableCard, formCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        main.setPadding(new Insets(18, 28, 28, 28));
        return main;
    }

    private void setupTable() {
        TableColumn<SessaoModel, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().id()));
        idColumn.setPrefWidth(60);

        TableColumn<SessaoModel, String> eventoColumn = new TableColumn<>("Evento");
        eventoColumn.setCellValueFactory(data -> new SimpleStringProperty(encontrarTituloEvento(data.getValue().eventoId())));
        eventoColumn.setPrefWidth(220);

        TableColumn<SessaoModel, String> salaColumn = new TableColumn<>("Sala");
        salaColumn.setCellValueFactory(data -> new SimpleStringProperty(encontrarNomeSala(data.getValue().salaId())));
        salaColumn.setPrefWidth(160);

        TableColumn<SessaoModel, String> dataHoraColumn = new TableColumn<>("Data/Hora");
        dataHoraColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDateTime(data.getValue().dataHora())));
        dataHoraColumn.setPrefWidth(160);

        TableColumn<SessaoModel, String> precoColumn = new TableColumn<>("Preço Base");
        precoColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().precoBase() != null ? data.getValue().precoBase() + " EUR" : ""
        ));
        precoColumn.setPrefWidth(120);

        TableColumn<SessaoModel, String> estadoColumn = new TableColumn<>("Estado");
        estadoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().estado()));
        estadoColumn.setPrefWidth(110);

        tableView.getColumns().setAll(idColumn, eventoColumn, salaColumn, dataHoraColumn, precoColumn, estadoColumn);
        tableView.setPlaceholder(new Label("Sem dados disponíveis."));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> preencherFormulario(newValue));
    }

    private VBox buildSessionForm() {
        formTitleLabel = new Label("Criar Sessão");
        formTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        eventoComboBox = new ComboBox<>();
        eventoComboBox.setPromptText("Selecionar evento");
        eventoComboBox.setMaxWidth(Double.MAX_VALUE);
        configurarEventoComboBox(eventoComboBox);

        salaComboBox = new ComboBox<>();
        salaComboBox.setPromptText("Selecionar sala");
        salaComboBox.setMaxWidth(Double.MAX_VALUE);
        configurarSalaComboBox(salaComboBox);

        dataPicker = new DatePicker();
        dataPicker.setPromptText("Selecionar data");
        dataPicker.setMaxWidth(Double.MAX_VALUE);

        horaField = new TextField();
        horaField.setPromptText("Hora (HH:mm)");

        precoBaseField = new TextField();
        precoBaseField.setPromptText("Preço base");

        estadoComboBox = new ComboBox<>();
        estadoComboBox.getItems().setAll("Aberta", "Esgotada", "Finalizada", "Cancelada");
        estadoComboBox.setPromptText("Estado");
        estadoComboBox.setMaxWidth(Double.MAX_VALUE);
        estadoComboBox.setValue("Aberta");

        clearFeedback();

        saveButton = new Button("Guardar sessão");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        deleteButton = new Button("Remover sessão");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
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

        saveButton.setOnAction(event -> guardarSessao());
        deleteButton.setOnAction(event -> eliminarSessaoSelecionada());
        clearButton.setOnAction(event -> {
            limparFormulario();
            clearFeedback();
        });

        Label helper = new Label("Escolhe evento e sala pelos nomes. A tabela da esquerda mostra a sessão em formato legível.");
        helper.setWrapText(true);
        helper.setStyle("-fx-text-fill: #9d9d9d; -fx-font-size: 12px;");

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.add(createFormLabel("Evento"), 0, 0);
        form.add(eventoComboBox, 0, 1);
        form.add(createFormLabel("Sala"), 0, 2);
        form.add(salaComboBox, 0, 3);
        form.add(createFormLabel("Data"), 0, 4);
        form.add(dataPicker, 0, 5);
        form.add(createFormLabel("Hora"), 0, 6);
        form.add(horaField, 0, 7);
        form.add(createFormLabel("Preço base"), 0, 8);
        form.add(precoBaseField, 0, 9);
        form.add(createFormLabel("Estado"), 0, 10);
        form.add(estadoComboBox, 0, 11);

        HBox actions = new HBox(10, saveButton, deleteButton);
        actions.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveButton, Priority.ALWAYS);
        HBox.setHgrow(deleteButton, Priority.ALWAYS);

        VBox formCard = new VBox(14, formTitleLabel, helper, form, actions, clearButton, feedbackLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return formCard;
    }

    private void guardarSessao() {
        try {
            EventoModel eventoSelecionado = eventoComboBox.getValue();
            SalaModel salaSelecionada = salaComboBox.getValue();
            LocalDate dataSelecionada = dataPicker.getValue();

            if (eventoSelecionado == null || salaSelecionada == null || dataSelecionada == null) {
                showError("Seleciona evento, sala e data.");
                return;
            }

            LocalTime horaSelecionada = LocalTime.parse(horaField.getText());
            LocalDateTime dataHoraLocal = LocalDateTime.of(dataSelecionada, horaSelecionada);
            Instant dataHora = dataHoraLocal.atZone(ZoneId.systemDefault()).toInstant();

            SessaoModel novaSessao = new SessaoModel(
                    selectedSessao != null ? selectedSessao.id() : null,
                    dataHora,
                    new BigDecimal(precoBaseField.getText()),
                    estadoComboBox.getValue(),
                    eventoSelecionado.id(),
                    salaSelecionada.id()
            );

            String successMessage;
            if (selectedSessao == null) {
                sessaoApiService.criarSessao(novaSessao);
                successMessage = "Sessão criada com sucesso.";
            } else {
                sessaoApiService.atualizarSessao(novaSessao);
                successMessage = "Sessão atualizada com sucesso.";
            }
            limparFormulario();
            carregarSessoes();
            showSuccess(successMessage);
        } catch (NumberFormatException e) {
            showError("O preço base deve ser numérico.");
        } catch (DateTimeParseException e) {
            showError("A hora deve estar no formato HH:mm.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void eliminarSessaoSelecionada() {
        if (selectedSessao == null) {
            showError("Seleciona uma sessão para remover.");
            return;
        }

        try {
            sessaoApiService.eliminarSessao(selectedSessao.id());
            limparFormulario();
            carregarSessoes();
            showSuccess("Sessão removida com sucesso.");
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void carregarReferencias() {
        try {
            List<EventoModel> eventos = eventoApiService.listarEventos();
            List<SalaModel> salas = salaApiService.listarSalas();
            if (eventoComboBox != null) {
                eventoComboBox.getItems().setAll(eventos);
            }
            if (salaComboBox != null) {
                salaComboBox.getItems().setAll(salas);
            }
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void carregarSessoes() {
        try {
            List<SessaoModel> sessoes = sessaoApiService.listarSessoes();
            tableView.getItems().setAll(sessoes);
            tableView.refresh();
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    private void configurarEventoComboBox(ComboBox<EventoModel> comboBox) {
        comboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(EventoModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.titulo());
            }
        });
        comboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(EventoModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.titulo());
            }
        });
    }

    private void configurarSalaComboBox(ComboBox<SalaModel> comboBox) {
        comboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(SalaModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nome());
            }
        });
        comboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(SalaModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.nome());
            }
        });
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
    }

    private void preencherFormulario(SessaoModel sessao) {
        selectedSessao = sessao;
        clearFeedback();

        if (sessao == null) {
            formTitleLabel.setText("Criar Sessão");
            saveButton.setText("Guardar sessão");
            deleteButton.setDisable(true);
            return;
        }

        formTitleLabel.setText("Editar Sessão");
        saveButton.setText("Atualizar sessão");
        deleteButton.setDisable(false);
        selecionarEvento(sessao.eventoId());
        selecionarSala(sessao.salaId());

        if (sessao.dataHora() != null) {
            LocalDateTime dataHoraLocal = LocalDateTime.ofInstant(sessao.dataHora(), ZoneId.systemDefault());
            dataPicker.setValue(dataHoraLocal.toLocalDate());
            horaField.setText(dataHoraLocal.toLocalTime().withSecond(0).withNano(0).toString());
        } else {
            dataPicker.setValue(null);
            horaField.clear();
        }

        precoBaseField.setText(sessao.precoBase() != null ? sessao.precoBase().toString() : "");
        estadoComboBox.setValue(sessao.estado() != null ? sessao.estado() : "Aberta");
    }

    private void limparFormulario() {
        selectedSessao = null;
        tableView.getSelectionModel().clearSelection();
        eventoComboBox.getSelectionModel().clearSelection();
        salaComboBox.getSelectionModel().clearSelection();
        dataPicker.setValue(null);
        horaField.clear();
        precoBaseField.clear();
        estadoComboBox.setValue("Aberta");
        formTitleLabel.setText("Criar Sessão");
        saveButton.setText("Guardar sessão");
        deleteButton.setDisable(true);
    }

    private void selecionarEvento(Integer eventoId) {
        if (eventoId == null) {
            eventoComboBox.getSelectionModel().clearSelection();
            return;
        }

        eventoComboBox.getItems().stream()
                .filter(evento -> eventoId.equals(evento.id()))
                .findFirst()
                .ifPresentOrElse(
                        evento -> eventoComboBox.getSelectionModel().select(evento),
                        () -> eventoComboBox.getSelectionModel().clearSelection()
                );
    }

    private void selecionarSala(Integer salaId) {
        if (salaId == null) {
            salaComboBox.getSelectionModel().clearSelection();
            return;
        }

        salaComboBox.getItems().stream()
                .filter(sala -> salaId.equals(sala.id()))
                .findFirst()
                .ifPresentOrElse(
                        sala -> salaComboBox.getSelectionModel().select(sala),
                        () -> salaComboBox.getSelectionModel().clearSelection()
                );
    }

    private String encontrarTituloEvento(Integer eventoId) {
        return eventoComboBox != null
                ? eventoComboBox.getItems().stream()
                .filter(evento -> eventoId != null && eventoId.equals(evento.id()))
                .map(EventoModel::titulo)
                .findFirst()
                .orElse(eventoId != null ? "Evento #" + eventoId : "-")
                : "-";
    }

    private String encontrarNomeSala(Integer salaId) {
        return salaComboBox != null
                ? salaComboBox.getItems().stream()
                .filter(sala -> salaId != null && salaId.equals(sala.id()))
                .map(SalaModel::nome)
                .findFirst()
                .orElse(salaId != null ? "Sala #" + salaId : "-")
                : "-";
    }

    private String formatDateTime(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return TABLE_DATE_FORMAT.withZone(ZoneId.systemDefault()).format(instant);
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
