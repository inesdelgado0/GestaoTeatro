package com.teatro.desktop.view.layout;

import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AdminLayout {

    public static final String SECTION_DASHBOARD = "Dashboard";
    public static final String SECTION_EVENTS = "Eventos";
    public static final String SECTION_SESSIONS = "Sess\u00f5es";
    public static final String SECTION_ROOMS = "Salas";
    public static final String SECTION_ZONES = "Zonas";
    public static final String SECTION_PRICING = "Pre\u00e7\u00e1rio";
    public static final String SECTION_REPORTS = "Relat\u00f3rios";

    private final BorderPane root;

    public AdminLayout(
            SceneManager sceneManager,
            AuthService authService,
            String userEmail,
            String activeSection,
            String pageTitle,
            String pageSubtitle,
            Parent content
    ) {
        this.root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #141414, #1d1d1d 58%, #111111);");
        root.setLeft(buildSidebar(sceneManager, authService, userEmail, activeSection));
        root.setTop(buildTopbar(pageTitle, pageSubtitle, userEmail));
        root.setCenter(content);
    }

    private VBox buildSidebar(SceneManager sceneManager, AuthService authService, String userEmail, String activeSection) {
        ImageView brandImage = new ImageView(new Image(getClass().getResourceAsStream("/assets/app-symbol.png")));
        brandImage.setFitWidth(58);
        brandImage.setFitHeight(58);
        brandImage.setPreserveRatio(true);

        Label brandTitle = new Label("Teatro Central");
        brandTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label brandSubtitle = new Label("Back-office");
        brandSubtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        StackPane brandIconContainer = new StackPane(brandImage);
        brandIconContainer.setAlignment(Pos.CENTER);
        brandIconContainer.setMaxWidth(Double.MAX_VALUE);
        brandIconContainer.setPadding(new Insets(12));
        brandIconContainer.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); " +
                        "-fx-background-radius: 18; " +
                        "-fx-border-color: rgba(255,255,255,0.08); " +
                        "-fx-border-radius: 18;"
        );

        VBox brandBox = new VBox(10, brandIconContainer, brandTitle, brandSubtitle);
        brandBox.setAlignment(Pos.CENTER);

        Button dashboardButton = createNavButton("\u2302", "P\u00e1gina Inicial", SECTION_DASHBOARD, activeSection);
        dashboardButton.setOnAction(event -> sceneManager.showDashboard(userEmail));

        Button eventsButton = createNavButton("\uD83C\uDFAD", "Eventos", SECTION_EVENTS, activeSection);
        eventsButton.setOnAction(event -> sceneManager.showEvents(userEmail));

        Button sessionsButton = createNavButton("\uD83D\uDDD3", "Sess\u00f5es", SECTION_SESSIONS, activeSection);
        sessionsButton.setOnAction(event -> sceneManager.showSessions(userEmail));

        Button roomsButton = createNavButton("\uD83C\uDFDB", "Salas", SECTION_ROOMS, activeSection);
        roomsButton.setOnAction(event -> sceneManager.showRooms(userEmail));

        Button zonesButton = createNavButton("\uD83D\uDCBA", "Zonas", SECTION_ZONES, activeSection);
        zonesButton.setOnAction(event -> sceneManager.showZones(userEmail));

        Button pricingButton = createNavButton("\uD83D\uDCB6", "Pre\u00e7\u00e1rio", SECTION_PRICING, activeSection);
        pricingButton.setOnAction(event -> sceneManager.showPricing(userEmail));

        Button reportsButton = createNavButton("\uD83D\uDCCA", "Relat\u00f3rios", SECTION_REPORTS, activeSection);
        reportsButton.setOnAction(event -> sceneManager.showReports(userEmail));

        VBox navigation = new VBox(
                10,
                dashboardButton,
                eventsButton,
                sessionsButton,
                roomsButton,
                zonesButton,
                pricingButton,
                reportsButton
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Terminar sess\u00e3o");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setStyle(
                "-fx-background-color: #dc2626; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 12 14 12 14; " +
                        "-fx-font-size: 13px; " +
                        "-fx-font-weight: bold;"
        );
        logoutButton.setOnAction(event -> {
            authService.logout();
            sceneManager.showLogin();
        });

        VBox sidebar = new VBox(22, brandBox, navigation, spacer, logoutButton);
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(20, 16, 20, 16));
        sidebar.setPrefWidth(220);
        sidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1e1e1e, #171717); " +
                        "-fx-border-color: rgba(255,255,255,0.06); " +
                        "-fx-border-width: 0 1 0 0;"
        );
        return sidebar;
    }

    private HBox buildTopbar(String pageTitle, String pageSubtitle, String userEmail) {
        Label titleLabel = new Label(pageTitle);
        titleLabel.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label subtitleLabel = new Label(pageSubtitle);
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");

        VBox titleBox = new VBox(4, titleLabel, subtitleLabel);

        Label roleLabel = new Label("Administrador");
        roleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #f8fafc;");

        Label userLabel = new Label(userEmail);
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cbd5e1;");

        Label avatarLabel = new Label("\uD83D\uDC64");
        avatarLabel.setMinSize(38, 38);
        avatarLabel.setAlignment(Pos.CENTER);
        avatarLabel.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-text-fill: #eff6ff; " +
                        "-fx-background-color: #4e8fe8; " +
                        "-fx-background-radius: 999;"
        );

        VBox userBox = new VBox(3, roleLabel, userLabel);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        HBox accountBox = new HBox(10, userBox, avatarLabel);
        accountBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topbar = new HBox(16, titleBox, spacer, accountBox);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(14, 24, 14, 24));
        topbar.setStyle(
                "-fx-background-color: rgba(24,24,24,0.94); " +
                        "-fx-border-color: rgba(255,255,255,0.06); " +
                        "-fx-border-width: 0 0 1 0;"
        );
        return topbar;
    }

    private Button createNavButton(String icon, String text, String section, String activeSection) {
        Label iconLabel = new Label(icon);
        iconLabel.setMinWidth(28);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #6ea8ff;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d9e8ff;");

        HBox graphic = new HBox(12, iconLabel, textLabel);
        graphic.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setMaxWidth(Double.MAX_VALUE);
        button.setGraphic(graphic);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        String baseStyle =
                "-fx-background-color: rgba(255,255,255,0.025); " +
                        "-fx-alignment: center-left; " +
                        "-fx-background-radius: 14; " +
                        "-fx-padding: 12 14 12 14; " +
                        "-fx-cursor: hand;";

        if (section.equals(activeSection)) {
            button.setStyle(
                    "-fx-background-color: linear-gradient(to right, #6ea8ff, #4e8fe8); " +
                            "-fx-alignment: center-left; " +
                            "-fx-background-radius: 14; " +
                            "-fx-padding: 12 14 12 14; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(110,168,255,0.30), 12, 0.15, 0, 5);"
            );
            iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            textLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        } else {
            button.setStyle(baseStyle);
        }

        button.disableProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue) {
                button.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-alignment: center-left; " +
                                "-fx-background-radius: 14; " +
                                "-fx-padding: 12 14 12 14; " +
                                "-fx-cursor: default;"
                );
                iconLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #475569;");
                textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
            }
        });

        return button;
    }

    public Parent getRoot() {
        return root;
    }
}
