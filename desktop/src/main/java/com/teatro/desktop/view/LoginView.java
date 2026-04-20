package com.teatro.desktop.view;

import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginView {

    private final BorderPane root;

    public LoginView(SceneManager sceneManager, AuthService authService) {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #161616, #232323);");

        Label brandLabel = new Label("Teatro Central");
        brandLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #67a9f4;");

        Label titleLabel = new Label("Acesso Administrativo");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Entrar com uma conta administrativa pre-configurada");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9f9f9f;");

        Label hintLabel = new Label("Conta principal: admin@teatro.pt");
        hintLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f7f7f;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(320);
        emailField.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #7f7f7f; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 12 14 12 14;"
        );

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(320);
        passwordField.setStyle(
                "-fx-background-color: #2a2a2a; " +
                        "-fx-text-fill: white; " +
                        "-fx-prompt-text-fill: #7f7f7f; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 12 14 12 14;"
        );

        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #ff8a8a; -fx-font-size: 12px;");

        Button loginButton = new Button("Entrar");
        loginButton.setDefaultButton(true);
        loginButton.setStyle(
                "-fx-background-color: #67a9f4; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 10; " +
                        "-fx-padding: 12 24 12 24;"
        );

        loginButton.setOnAction(event -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (authService.authenticate(email, password)) {
                feedbackLabel.setText("");
                sceneManager.showDashboard(email);
            } else {
                feedbackLabel.setText(authService.getLastErrorMessage());
            }
        });

        HBox actions = new HBox(12, loginButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label sideTitle = new Label("Painel de Administracao");
        sideTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label sideText = new Label("Gerir eventos, sessoes, salas, zonas, lugares e relatorios a partir de um unico back-office.");
        sideText.setWrapText(true);
        sideText.setStyle("-fx-font-size: 14px; -fx-text-fill: #a7a7a7;");

        VBox infoPanel = new VBox(16, brandLabel, sideTitle, sideText);
        infoPanel.setPadding(new Insets(40));
        infoPanel.setPrefWidth(420);
        infoPanel.setStyle(
                "-fx-background-color: #1d1d1d; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: #2e2e2e; " +
                        "-fx-border-radius: 20;"
        );

        VBox loginCard = new VBox(14, titleLabel, subtitle, hintLabel, emailField, passwordField, actions, feedbackLabel);
        loginCard.setAlignment(Pos.CENTER_LEFT);
        loginCard.setMaxWidth(420);
        loginCard.setPadding(new Insets(40));
        loginCard.setStyle(
                "-fx-background-color: #242424; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-radius: 20; " +
                        "-fx-border-color: #313131; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 18, 0.2, 0, 6);"
        );

        HBox layout = new HBox(28, infoPanel, loginCard);
        layout.setAlignment(Pos.CENTER);

        BorderPane.setAlignment(layout, Pos.CENTER);
        root.setCenter(layout);
        root.setPadding(new Insets(40));
    }

    public Parent getRoot() {
        return root;
    }
}
