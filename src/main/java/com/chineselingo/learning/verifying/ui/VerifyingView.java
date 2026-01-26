package com.chineselingo.learning.verifying.ui;

import com.chineselingo.app.ApplicationService;
import com.chineselingo.learning.common.dto.CharacterCandidate;
import com.chineselingo.learning.ui.MainView;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class VerifyingView  extends BorderPane {
    private final ApplicationService service;
    private final MainView mainView;

    private Label charLabel = new Label();
    private TextArea answerField = new TextArea();
    private Label resultLabel = new Label();
    private Label correctAnswerLabel = new Label();

    private Button verifyButton = new Button("Verify");
    private Button nextButton = new Button("Next sign");

    private Button repeatButton = new Button("Repeat test");
    private Button nextTestButton = new Button("Next test");
    private Button backToTrainingButton = new Button("Go to training");

    public VerifyingView(ApplicationService service, MainView mainView) {
        this.service = service;
        this.mainView = mainView;

        service.verifying().startNewTest();
        createCenterContent();
        setupButtons();
        show(service.verifying().getCurrent());
    }

    private void setupButtons() {
        verifyButton.setOnAction(e -> {
            boolean correct = service.verifying().verify(answerField.getText());
            service.saveUserState();

            resultLabel.setVisible(true);

            if (correct) {
                resultLabel.setText("Correct");
                resultLabel.setStyle("-fx-text-fill: green;-fx-font-size: 12px;");
                correctAnswerLabel.setText(service.verifying().getCorrectAnswer());
                correctAnswerLabel.setVisible(true);
            } else {
                resultLabel.setText("Wrong");
                resultLabel.setStyle("-fx-text-fill: red;-fx-font-size: 12px;");
                correctAnswerLabel.setText(service.verifying().getCorrectAnswer());
                correctAnswerLabel.setVisible(true);
            }

            if (service.verifying().isLast()) {
                nextButton.setDisable(true);
                repeatButton.setDisable(false);
                nextTestButton.setDisable(false);
                backToTrainingButton.setDisable(false);
            } else {
                nextButton.setDisable(false);
            }

            verifyButton.setDisable(true);
        });

        nextButton.setOnAction(e -> {
            service.verifying().next();
            show(service.verifying().getCurrent());
        });

        repeatButton.setOnAction(e -> {
            service.verifying().restartTest();
            show(service.verifying().getCurrent());
        });

        nextTestButton.setOnAction(e -> {
            service.verifying().startNewTest();
            show(service.verifying().getCurrent());
        });

        backToTrainingButton.setOnAction(e -> mainView.showTraining());
    }

    private void show(CharacterCandidate candidate) {
        if (candidate == null) {
            charLabel.setText("No characters to test");
            answerField.setDisable(true);
            verifyButton.setDisable(true);
            return;
        }
        charLabel.setText(candidate.getCharacter());
        answerField.clear();
        answerField.setDisable(false);
        answerField.requestFocus();
        resultLabel.setVisible(false);
        correctAnswerLabel.setVisible(false);
        verifyButton.setDisable(false);
        nextButton.setDisable(true);
        repeatButton.setDisable(true);
        nextTestButton.setDisable(true);
        backToTrainingButton.setDisable(true);
    }

    private void createCenterContent() {
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);

        charLabel.setStyle("-fx-font-size: 48px;");

        resultLabel.setVisible(false);

        correctAnswerLabel.setVisible(false);
        correctAnswerLabel.setStyle("-fx-font-size: 18px;");
        correctAnswerLabel.setAlignment(Pos.CENTER);
        correctAnswerLabel.setWrapText(true);
        correctAnswerLabel.setTextAlignment(TextAlignment.CENTER);
        correctAnswerLabel.maxWidthProperty().bind(widthProperty().multiply(0.8));

        answerField.setWrapText(true);              // zawijanie linii
        answerField.setPrefRowCount(3);             // sensowna wysokość
        answerField.setPromptText("Type your answer here...");
        answerField.setStyle("-fx-font-size: 16px;");
        answerField.maxWidthProperty().bind(widthProperty().multiply(0.8)
        );

        nextButton.setDisable(true);

        HBox primaryActions = new HBox(15, verifyButton, nextButton);
        primaryActions.setAlignment(Pos.CENTER);

        HBox endButtons = new HBox(10, repeatButton, nextTestButton, backToTrainingButton);
        endButtons.setAlignment(Pos.CENTER);
        repeatButton.setDisable(true);
        nextTestButton.setDisable(true);
        backToTrainingButton.setDisable(true);

        content.getChildren().addAll(
                charLabel,
                answerField,
                primaryActions,
                resultLabel,
                correctAnswerLabel,
                endButtons
        );

        setCenter(content);
    }
}
