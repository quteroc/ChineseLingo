package com.chineselingo;

import com.chineselingo.app.AppContext;
import com.chineselingo.app.ApplicationService;
import com.chineselingo.learning.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene;
        try {
            AppContext context = new AppContext();
            ApplicationService appService = new ApplicationService(context);
            MainView mainView = new MainView(appService);
            scene = new Scene(mainView, 1200, 500);
        } catch (Exception e) {
            Label errorLabel = new Label( "ChineseLingo Error. Could not load necessary files." + e.getMessage());
            errorLabel.setStyle("-fx-font-size: 20px");
            VBox root = new VBox(errorLabel);
            root.setSpacing(20);
            scene = new Scene(root, 800, 400);
        }

        stage.setTitle("ChineseLingo");
        stage.setScene(scene);
        stage.show();
    }
}