package com.teatro.desktop.navigation;

import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.view.EventsView;
import com.teatro.desktop.view.RoomsView;
import com.teatro.desktop.view.SessionsView;
import com.teatro.desktop.view.DashboardView;
import com.teatro.desktop.view.LoginView;
import com.teatro.desktop.view.PricingView;
import com.teatro.desktop.view.ReportsView;
import com.teatro.desktop.view.ZonesView;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    private final Stage stage;
    private final AuthService authService;

    public SceneManager(Stage stage, AuthService authService) {
        this.stage = stage;
        this.authService = authService;
    }

    public void showLogin() {
        LoginView loginView = new LoginView(this, authService);
        applyScene(loginView.getRoot());
    }

    public void showDashboard(String userEmail) {
        DashboardView dashboardView = new DashboardView(this, authService, userEmail);
        applyScene(dashboardView.getRoot());
    }

    public void showEvents(String userEmail) {
        EventsView eventsView = new EventsView(this, authService, userEmail);
        applyScene(eventsView.getRoot());
    }

    public void showSessions(String userEmail) {
        SessionsView sessionsView = new SessionsView(this, authService, userEmail);
        applyScene(sessionsView.getRoot());
    }

    public void showRooms(String userEmail) {
        RoomsView roomsView = new RoomsView(this, authService, userEmail);
        applyScene(roomsView.getRoot());
    }

    public void showZones(String userEmail) {
        ZonesView zonesView = new ZonesView(this, authService, userEmail);
        applyScene(zonesView.getRoot());
    }

    public void showPricing(String userEmail) {
        PricingView pricingView = new PricingView(this, authService, userEmail);
        applyScene(pricingView.getRoot());
    }

    public void showReports(String userEmail) {
        ReportsView reportsView = new ReportsView(this, authService, userEmail);
        applyScene(reportsView.getRoot());
    }

    private void applyScene(Parent root) {
        boolean wasFullScreen = stage.isFullScreen();
        boolean wasMaximized = stage.isMaximized();

        Scene currentScene = stage.getScene();
        Scene nextScene;

        if (currentScene == null) {
            nextScene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        } else {
            nextScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
        }

        stage.setScene(nextScene);

        if (wasMaximized) {
            stage.setMaximized(true);
        }
        if (wasFullScreen) {
            Platform.runLater(() -> stage.setFullScreen(true));
        }
    }
}
