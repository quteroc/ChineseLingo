package com.chineselingo.learning.ui;

import com.chineselingo.app.ApplicationService;
import com.chineselingo.learning.training.ui.TrainingView;
import com.chineselingo.learning.verifying.ui.VerifyingView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainView extends BorderPane {

    private final ApplicationService appService;
    private final Label modeLabel = new Label();

    public MainView(ApplicationService appService) {
        this.appService = appService;
        createTopBar();
        showTraining();
    }

    private void createTopBar() {
        modeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        HBox top = new HBox(modeLabel);
        top.setPadding(new Insets(10));
        top.setAlignment(Pos.CENTER_LEFT);

        setTop(top);
    }

    public void showTraining() {
        modeLabel.setText("ChineseLingo - training");
        setCenter(new TrainingView(appService, this));
    }

    public void showTesting() {
        modeLabel.setText("ChineseLingo - testing");
        setCenter(new VerifyingView(appService, this));
    }
}
