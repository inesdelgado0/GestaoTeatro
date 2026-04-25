package com.teatro.desktop;

import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DesktopApplication extends Application {

    @Override
    public void start(Stage stage) {
        AuthService authService = new AuthService();
        SceneManager sceneManager = new SceneManager(stage, authService);

        stage.setTitle("Teatro Desktop");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/app-symbol.png")));

        sceneManager.showLogin();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
