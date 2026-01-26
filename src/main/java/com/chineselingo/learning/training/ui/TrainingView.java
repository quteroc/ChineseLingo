package com.chineselingo.learning.training.ui;

import com.chineselingo.app.ApplicationService;
import com.chineselingo.learning.common.dto.CharacterCandidate;
import com.chineselingo.learning.ui.MainView;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class TrainingView extends BorderPane {
    private final ApplicationService service;
    private final MainView mainView;

    private final Label charLabel = new Label();
    private final Label meaningLabel = new Label();

    private final Button gotItButton = new Button("Got it");
    private final Button nextButton = new Button("Next sign");
    private final Button goToTestButton = new Button("Go to test");

    public TrainingView(ApplicationService service, MainView mainView) {
        this.service = service;
        this.mainView = mainView;

        createCenterContent();
        setupButtons();
        loadNextCharacter();
    }

    private void setupButtons() {

        gotItButton.setOnAction(e -> {
            service.training().markCurrentAsKnown();
            service.saveUserState();
            updateButtonsAfterGotIt();
        });

        nextButton.setOnAction(e -> {
            loadNextCharacter();
            updateButtonsForNewCharacter();
        });

        goToTestButton.setOnAction(e -> mainView.showTesting());
    }

    private void loadNextCharacter() {
        CharacterCandidate c = service.training().nextSign();
        charLabel.setText(c.getCharacter());
        meaningLabel.setText(c.getMeaning());
    }

    private void updateButtonsForNewCharacter() {
        gotItButton.setDisable(false);
        nextButton.setDisable(true);
        goToTestButton.setDisable(true);
    }

    private void updateButtonsAfterGotIt() {
        gotItButton.setDisable(true);
        nextButton.setDisable(false);
        goToTestButton.setDisable(false);
    }

    private VBox createButtonsLayout() {

        // --- górny rząd: Got it | Next sign ---
        HBox topButtons = new HBox(10);
        topButtons.setAlignment(Pos.CENTER);
        topButtons.getChildren().addAll(gotItButton, nextButton);

        // --- dolny przycisk: Go to test ---
        goToTestButton.setPrefWidth(200);
        goToTestButton.setPrefHeight(40);
        nextButton.setDisable(true);
        goToTestButton.setDisable(true);

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(topButtons, goToTestButton);

        return buttonsBox;
    }

    private void createCenterContent() {
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        charLabel.setStyle("-fx-font-size: 48px;");
        charLabel.setAlignment(Pos.CENTER);
        meaningLabel.setStyle("-fx-font-size: 18px;");
        meaningLabel.setAlignment(Pos.CENTER);
        meaningLabel.setWrapText(true);
        meaningLabel.setTextAlignment(TextAlignment.CENTER);
        meaningLabel.maxWidthProperty().bind(widthProperty().multiply(0.8));

        VBox buttonsLayout = createButtonsLayout();

        content.getChildren().addAll(
                charLabel,
                meaningLabel,
                buttonsLayout
        );

        setCenter(content);
    }
}