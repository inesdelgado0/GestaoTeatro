package com.teatro.desktop;

import com.teatro.desktop.navigation.SceneManager;
import com.teatro.desktop.service.AuthService;
import javafx.application.Application;
import javafx.stage.Stage;

public class DesktopApplication extends Application {

    @Override
    public void start(Stage stage) {
        AuthService authService = new AuthService();
        SceneManager sceneManager = new SceneManager(stage, authService);

        stage.setTitle("Teatro Desktop");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        sceneManager.showLogin();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
