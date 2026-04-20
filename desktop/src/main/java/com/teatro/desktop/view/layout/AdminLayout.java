package com.teatro.desktop.view.layout;

import com.teatro.desktop.navigation.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AdminLayout {

    public static final String SECTION_DASHBOARD = "Dashboard";
    public static final String SECTION_EVENTS = "Eventos";
    public static final String SECTION_SESSIONS = "Sessoes";
    public static final String SECTION_ROOMS = "Salas";
    public static final String SECTION_ZONES = "Zonas";
    public static final String SECTION_REPORTS = "Relatorios";

    private final BorderPane root;

    public AdminLayout(
            SceneManager sceneManager,
            String userEmail,
            String activeSection,
            String pageTitle,
            String pageSubtitle,
            Parent content
    ) {
        this.root = new BorderPane();
        root.setStyle("-fx-background-color: #1f1f1f;");
        root.setLeft(buildSidebar(sceneManager, userEmail, activeSection));
        root.setTop(buildTopbar(pageTitle, pageSubtitle, userEmail));
        root.setCenter(content);
    }

    private VBox buildSidebar(SceneManager sceneManager, String userEmail, String activeSection) {
        Label brandIcon = new Label("\uD83C\uDFAD");
        brandIcon.setStyle("-fx-font-size: 26px;");

        Label brandTitle = new Label("Teatro Central");
        brandTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #63a6ff;");

        VBox brandBox = new VBox(8, brandIcon, brandTitle);

        Button dashboardButton = createNavButton("Pagina Inicial", SECTION_DASHBOARD, activeSection);
        dashboardButton.setOnAction(event -> sceneManager.showDashboard(userEmail));

        Button eventsButton = createNavButton("Eventos", SECTION_EVENTS, activeSection);
        eventsButton.setOnAction(event -> sceneManager.showEvents(userEmail));

        Button sessionsButton = createNavButton("Sessoes", SECTION_SESSIONS, activeSection);
        sessionsButton.setOnAction(event -> sceneManager.showSessions(userEmail));

        Button roomsButton = createNavButton("Salas", SECTION_ROOMS, activeSection);
        roomsButton.setOnAction(event -> sceneManager.showRooms(userEmail));

        Button zonesButton = createNavButton("Zonas", SECTION_ZONES, activeSection);
        zonesButton.setOnAction(event -> sceneManager.showZones(userEmail));

        Button reportsButton = createNavButton("Relatorios", SECTION_REPORTS, activeSection);
        reportsButton.setOnAction(event -> sceneManager.showReports(userEmail));

        VBox navigation = new VBox(
                14,
                dashboardButton,
                eventsButton,
                sessionsButton,
                roomsButton,
                zonesButton,
                reportsButton
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Terminar sessao");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setStyle(
                "-fx-background-color: #ff4d4f; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 12 16 12 16;"
        );
        logoutButton.setOnAction(event -> sceneManager.showLogin());

        VBox sidebar = new VBox(28, brandBox, navigation, spacer, logoutButton);
        sidebar.setPadding(new Insets(28, 18, 24, 18));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #252525;");
        return sidebar;
    }

    private HBox buildTopbar(String pageTitle, String pageSubtitle, String userEmail) {
        Label titleLabel = new Label(pageTitle);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label(pageSubtitle);
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8a8a8a;");

        VBox titleBox = new VBox(6, titleLabel, subtitleLabel);

        Label roleLabel = new Label("Admin");
        roleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label userLabel = new Label(userEmail);
        userLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #d2d2d2;");

        Label avatarLabel = new Label("\u25EF");
        avatarLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #c8b4ff;");

        VBox userBox = new VBox(4, roleLabel, userLabel);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        HBox accountBox = new HBox(10, userBox, avatarLabel);
        accountBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topbar = new HBox(16, titleBox, spacer, accountBox);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(18, 28, 16, 28));
        topbar.setStyle("-fx-background-color: #1f1f1f;");
        return topbar;
    }

    private Button createNavButton(String text, String section, String activeSection) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);

        String baseStyle =
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #7ab4ff; " +
                        "-fx-alignment: center-left; " +
                        "-fx-background-radius: 14; " +
                        "-fx-padding: 14 16 14 16; " +
                        "-fx-font-size: 15px;";

        if (section.equals(activeSection)) {
            button.setStyle(
                    "-fx-background-color: #67a9f4; " +
                            "-fx-text-fill: white; " +
                            "-fx-alignment: center-left; " +
                            "-fx-background-radius: 14; " +
                            "-fx-padding: 14 16 14 16; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: bold;"
            );
        } else {
            button.setStyle(baseStyle);
        }

        button.disableProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                button.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #616161; " +
                                "-fx-alignment: center-left; " +
                                "-fx-background-radius: 14; " +
                                "-fx-padding: 14 16 14 16; " +
                                "-fx-font-size: 15px;"
                );
            }
        });

        return button;
    }

    public Parent getRoot() {
        return root;
    }
}
