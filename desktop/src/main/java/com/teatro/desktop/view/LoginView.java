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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginView {

    private final BorderPane root;

    public LoginView(SceneManager sceneManager, AuthService authService) {
        root = new BorderPane();
        root.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #141414, #1d1d1d 55%, #111111);"
        );

        ImageView symbolView = new ImageView(new Image(getClass().getResourceAsStream("/assets/app-symbol.png")));
        symbolView.setFitWidth(82);
        symbolView.setFitHeight(82);
        symbolView.setPreserveRatio(true);

        Label brandLabel = new Label("Teatro Central");
        brandLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #6ea8ff;");

        Label subtitleLabel = new Label("Back-office");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #b0b0b0; -fx-font-weight: bold;");

        VBox brandBox = new VBox(14, symbolView, brandLabel, subtitleLabel);
        brandBox.setAlignment(Pos.CENTER);

        TextField emailField = createInputField("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle(inputStyle());

        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #ff9a9a; -fx-font-size: 12px;");

        Button loginButton = new Button("Entrar");
        loginButton.setDefaultButton(true);
        loginButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #6ea8ff, #4e8fe8); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 13 28 13 28;"
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

        VBox fieldsBox = new VBox(14, createFieldBlock("Email", emailField), createFieldBlock("Password", passwordField));

        HBox actionRow = new HBox(loginButton);
        actionRow.setAlignment(Pos.CENTER);

        Region separator = createSeparator();

        VBox loginCard = new VBox(
                20,
                brandBox,
                separator,
                fieldsBox,
                actionRow,
                feedbackLabel
        );
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(460);
        loginCard.setPadding(new Insets(30, 34, 30, 34));
        loginCard.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(36,36,36,0.98), rgba(29,29,29,0.98)); " +
                        "-fx-background-radius: 26; " +
                        "-fx-border-radius: 26; " +
                        "-fx-border-color: rgba(255,255,255,0.08); " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.34), 28, 0.25, 0, 10);"
        );

        BorderPane.setAlignment(loginCard, Pos.CENTER);
        root.setCenter(loginCard);
        root.setPadding(new Insets(32));
    }

    private TextField createInputField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(inputStyle());
        return field;
    }

    private VBox createFieldBlock(String labelText, javafx.scene.control.Control input) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #d0d0d0; -fx-font-size: 13px; -fx-font-weight: bold;");
        return new VBox(8, label, input);
    }

    private String inputStyle() {
        return "-fx-background-color: #2b2b2b; " +
                "-fx-text-fill: white; " +
                "-fx-prompt-text-fill: #727272; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 14 16 14 16; " +
                "-fx-font-size: 14px;";
    }

    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setStyle("-fx-background-color: rgba(255,255,255,0.08);");
        return separator;
    }

    public Parent getRoot() {
        return root;
    }
}
