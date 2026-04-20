package com.teatro.desktop.navigation;

import com.teatro.desktop.service.AuthService;
import com.teatro.desktop.view.EventsView;
import com.teatro.desktop.view.RoomsView;
import com.teatro.desktop.view.SessionsView;
import com.teatro.desktop.view.DashboardView;
import com.teatro.desktop.view.LoginView;
import com.teatro.desktop.view.ReportsView;
import com.teatro.desktop.view.ZonesView;
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
        stage.setScene(new Scene(loginView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void showDashboard(String userEmail) {
        DashboardView dashboardView = new DashboardView(this, authService, userEmail);
        stage.setScene(new Scene(dashboardView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void showEvents(String userEmail) {
        EventsView eventsView = new EventsView(this, authService, userEmail);
        stage.setScene(new Scene(eventsView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void showSessions(String userEmail) {
        SessionsView sessionsView = new SessionsView(this, authService, userEmail);
        stage.setScene(new Scene(sessionsView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void showRooms(String userEmail) {
        RoomsView roomsView = new RoomsView(this, authService, userEmail);
        stage.setScene(new Scene(roomsView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void showZones(String userEmail) {
        ZonesView zonesView = new ZonesView(this, authService, userEmail);
        stage.setScene(new Scene(zonesView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }

    public void showReports(String userEmail) {
        ReportsView reportsView = new ReportsView(this, authService, userEmail);
        stage.setScene(new Scene(reportsView.getRoot(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    }
}
