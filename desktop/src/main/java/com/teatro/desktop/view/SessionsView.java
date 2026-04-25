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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SessionsView {

    private final Parent root;
    private final TableView<SessaoModel> tableView;
    private final SessaoApiService sessaoApiService;
    private final EventoApiService eventoApiService;
    private final SalaApiService salaApiService;
    private final Label feedbackLabel;
    private final String userEmail;
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
        this.userEmail = userEmail;
        this.selectedSessao = null;

        AdminLayout layout = new AdminLayout(
                sceneManager,
                authService,
                userEmail,
                AdminLayout.SECTION_SESSIONS,
                "Gest\u00e3o de Sess\u00f5es",
                "Agendamento de sess\u00f5es e consulta do planeamento atual",
                buildContent()
        );
        this.root = layout.getRoot();

        carregarSessoes();
    }

    private Parent buildContent() {
        setupTable();
        Label tableTitleLabel = new Label("Sess\u00f5es agendadas");
        tableTitleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        VBox tableCard = new VBox(12, tableTitleLabel, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );

        VBox formCard = buildSessionForm();
        formCard.setPrefWidth(380);

        HBox main = new HBox(20, tableCard, formCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);
        main.setPadding(new Insets(18, 28, 28, 28));
        return main;
    }

    private void setupTable() {
        TableColumn<SessaoModel, Number> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().id()));
        idColumn.setPrefWidth(60);

        TableColumn<SessaoModel, String> dataHoraColumn = new TableColumn<>("Data/Hora");
        dataHoraColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().dataHora() != null ? data.getValue().dataHora().toString() : ""
        ));
        dataHoraColumn.setPrefWidth(220);

        TableColumn<SessaoModel, String> precoColumn = new TableColumn<>("Preço Base");
        precoColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().precoBase() != null ? data.getValue().precoBase().toString() : ""
        ));
        precoColumn.setPrefWidth(120);

        TableColumn<SessaoModel, Number> eventoColumn = new TableColumn<>("Evento ID");
        eventoColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().eventoId() != null ? data.getValue().eventoId() : 0
        ));
        eventoColumn.setPrefWidth(100);

        TableColumn<SessaoModel, Number> salaColumn = new TableColumn<>("Sala ID");
        salaColumn.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().salaId() != null ? data.getValue().salaId() : 0
        ));
        salaColumn.setPrefWidth(100);

        TableColumn<SessaoModel, String> estadoColumn = new TableColumn<>("Estado");
        estadoColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().estado()));
        estadoColumn.setPrefWidth(120);

        tableView.getColumns().setAll(idColumn, dataHoraColumn, precoColumn, eventoColumn, salaColumn, estadoColumn);
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

        feedbackLabel.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 12px;");

        saveButton = new Button("Guardar sessão");
        saveButton.setStyle(
                "-fx-background-color: #6f2232; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 10 18 10 18;"
        );

        deleteButton = new Button("Remover sessão");
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

        saveButton.setOnAction(event -> {
            try {
                EventoModel eventoSelecionado = eventoComboBox.getValue();
                SalaModel salaSelecionada = salaComboBox.getValue();
                LocalDate dataSelecionada = dataPicker.getValue();

                if (eventoSelecionado == null || salaSelecionada == null || dataSelecionada == null) {
                    feedbackLabel.setText("Selecione evento, sala e data.");
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

                if (selectedSessao == null) {
                    sessaoApiService.criarSessao(novaSessao);
                    feedbackLabel.setText("Sessão criada com sucesso.");
                } else {
                    sessaoApiService.atualizarSessao(novaSessao);
                    feedbackLabel.setText("Sessão atualizada com sucesso.");
                }

                limparFormulario();
                carregarSessoes();
            } catch (NumberFormatException e) {
                feedbackLabel.setText("O preço base deve ser numérico.");
            } catch (DateTimeParseException e) {
                feedbackLabel.setText("A hora deve estar no formato HH:mm.");
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        deleteButton.setOnAction(event -> {
            if (selectedSessao == null) {
                feedbackLabel.setText("Selecione uma sessão para remover.");
                return;
            }

            try {
                sessaoApiService.eliminarSessao(selectedSessao.id());
                feedbackLabel.setText("Sessão removida com sucesso.");
                limparFormulario();
                carregarSessoes();
            } catch (RuntimeException e) {
                feedbackLabel.setText(e.getMessage());
            }
        });

        clearButton.setOnAction(event -> {
            limparFormulario();
            feedbackLabel.setText("");
        });

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
        VBox formCard = new VBox(14, formTitleLabel, form, actions, clearButton, feedbackLabel);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(
                "-fx-background-color: #2c2c2c; -fx-background-radius: 16; " +
                        "-fx-border-color: #333333; -fx-border-radius: 16;"
        );
        return formCard;
    }

    private void carregarSessoes() {
        try {
            List<SessaoModel> sessoes = sessaoApiService.listarSessoes();
            tableView.getItems().setAll(sessoes);
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
        }
    }

    private void configurarEventoComboBox(ComboBox<EventoModel> eventoComboBox) {
        try {
            List<EventoModel> eventos = eventoApiService.listarEventos();
            eventoComboBox.getItems().setAll(eventos);
            eventoComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(EventoModel item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.titulo());
                }
            });
            eventoComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(EventoModel item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.titulo());
                }
            });
        } catch (RuntimeException e) {
            feedbackLabel.setText(e.getMessage());
        }
    }

    private void configurarSalaComboBox(ComboBox<SalaModel> salaComboBox) {
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

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cfcfcf;");
        return label;
    }

    private void preencherFormulario(SessaoModel sessao) {
        selectedSessao = sessao;

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

    public Parent getRoot() {
        return root;
    }
}
