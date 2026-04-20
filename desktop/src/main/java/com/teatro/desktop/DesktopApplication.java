package com.teatro.desktop;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class DesktopApplication extends Application {

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(24));
        root.setCenter(new Label("Teatro Desktop - Estrutura inicial JavaFX"));

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Teatro Desktop");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
